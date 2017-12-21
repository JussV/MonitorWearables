package smartlife.monitorwearables;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.felkertech.settingsmanager.SettingsManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HeartbeatService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private int currentValue=0;
    private static final String TAG_HEART_BEAT = "HeartbeatService";
    private IBinder binder = new HeartbeatServiceBinder();
    private OnChangeListener onChangeListener;
    private GoogleApiClient mGoogleApiClient;
    private PowerManager.WakeLock wakeLock;
    Handler handler;
    String[] monitorIntervals;
    private Sensor mHeartRateSensor;
    private SettingsManager mSettingsManager;

    // interface to pass a heartbeat value to the implementing class
    public interface OnChangeListener {
        void onValueChanged(int newValue);
    }

    /**
     * Binder for this service. The binding activity passes a listener we send the heartbeat to.
     */
    public class HeartbeatServiceBinder extends Binder {
        public void setChangeListener(OnChangeListener listener) {
            onChangeListener = listener;
            listener.onValueChanged(currentValue);
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        mSettingsManager = new SettingsManager(this);
        monitorIntervals = getResources().getStringArray(R.array.hr_interval_array);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        //batching is not supported for Moto 360 Sport, because this fifoSize value is 0.
        int fifoSize = mHeartRateSensor.getFifoReservedEventCount();
        int fifoMax = mHeartRateSensor.getFifoMaxEventCount();
        // delay SENSOR_DELAY_UI is sufficient
        if(mSettingsManager.getBoolean(getString(R.string.key_enable_wear_continuous_monitoring), false)){
            boolean res = mSensorManager.registerListener(this, mHeartRateSensor,  600000000);
            Log.d(TAG_HEART_BEAT, " sensor registered: " + (res ? "yes" : "no"));
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
            mGoogleApiClient.connect();
          //  handler.post(processSensors);
        }
    }

    public void unregisterListener(){
        mSensorManager.unregisterListener(this, mHeartRateSensor);
    }

    public void registerListener(){
        mSensorManager.registerListener(this, mHeartRateSensor,  600000000);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
    }



    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
     //   handler.removeCallbacks(processSensors);
        mSensorManager.unregisterListener(this);
        Log.d(TAG_HEART_BEAT," sensor unregistered");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(!mSettingsManager.getBoolean(getString(R.string.key_enable_wear_continuous_monitoring), false)){
            unregisterListener();
        } else {
            // is this a heartbeat event and does it have data?
            if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE && sensorEvent.values.length > 0) {
                int newValue = Math.round(sensorEvent.values[0]);
                //            int newValue = 60;
                //Log.d(TAG_HEART_BEAT,sensorEvent.sensor.getName() + " changed to: " + newValue);
                // only do something if the value differs from the value before and the value is not 0.
                if (currentValue != newValue && newValue != 0) {
                    // save the new value
                    currentValue = newValue;
                    // send the value to the listener
                    if (onChangeListener != null) {
                        Log.d(TAG_HEART_BEAT, new Date().toString() + ": Sending new value to listener: " + newValue);
                        onChangeListener.onValueChanged(newValue);
                        sendMessageToHandheld(Integer.toString(newValue));
                    }
                }
            }
        }
    }

    private void sendMessageToHandheld(final String message) {
       if (mGoogleApiClient == null)
            return;
        // use the api client to send the heartbeat value to the handheld
        if(mGoogleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    final String path = "/wear/heartRate";
                    for (Node node : nodes.getNodes()) {
                        Log.d(TAG_HEART_BEAT, "Send message to handheld: " + message);
                        ArrayList<DataMap> messagesToHandheld = new ArrayList<>();
                        DataMap hrMessage = new DataMap();
                        hrMessage.putString("heartRate", message);
                        messagesToHandheld.add(hrMessage);
                        DataMap wearModel = new DataMap();
                        wearModel.putString("wearModel", Build.MODEL);
                        messagesToHandheld.add(wearModel);
                        DataMap dm = new DataMap();
                        dm.putDataMapArrayList("key", messagesToHandheld);
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, dm.toByteArray());
                    }
                }
            }).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}