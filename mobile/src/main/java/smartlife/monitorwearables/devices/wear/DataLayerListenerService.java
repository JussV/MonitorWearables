package smartlife.monitorwearables.devices.wear;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Locale;

import smartlife.monitorwearables.db.HRMonitorContract;
import smartlife.monitorwearables.db.HRMonitorDbHelper;
import smartlife.monitorwearables.model.DeviceType;

public class DataLayerListenerService extends WearableListenerService {

    private static final String LOG_TAG = "WearableListener";

    private static Handler handler;
    private HRMonitorDbHelper mDbHelper = new HRMonitorDbHelper(this);;

    public static Handler getHandler() {
        return handler;
    }

    public static void setHandler(Handler handler) {
        int currentValue=0;
        DataLayerListenerService.handler = handler;
        // send current value as initial value.
        if(handler!=null)
            handler.sendEmptyMessage(currentValue);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        String id = peer.getId();
        String name = peer.getDisplayName();

        Log.d(LOG_TAG, "Connected peer name & ID: " + name + "|" + id);
    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/wear/heartRate")) {
            /*final String message = new String(messageEvent.getData());*/
            DataMap resultMap = DataMap.fromByteArray(messageEvent.getData());
            ArrayList<DataMap> resultArr = resultMap.get("key");
            Integer heartRate = Integer.valueOf(resultArr.get(0).get("heartRate").toString());
            String androidWearModel = resultArr.get(1).get("wearModel");
            int deviceKey = DeviceType.getKeyByWearDeviceName(androidWearModel);
            Log.v(LOG_TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.v(LOG_TAG, "Message received on watch is: " + heartRate);
            //store value to db
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(HRMonitorContract.HeartRate.COLUMN_VALUE, heartRate);
            //Gives the number of milliseconds since January 1, 1970 00:00:00 UTC
            values.put(HRMonitorContract.HeartRate.COLUMN_CREATED_AT, System.currentTimeMillis());
            values.put(HRMonitorContract.HeartRate.COLUMN_DEVICE_TYPE_KEY, deviceKey);
            long newRowId = db.insert(HRMonitorContract.HeartRate.TABLE_NAME, null, values);
            db.close();
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }


}
