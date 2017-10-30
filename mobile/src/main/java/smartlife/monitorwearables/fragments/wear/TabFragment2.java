package smartlife.monitorwearables.fragments.wear;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

import pl.droidsonroids.gif.GifTextView;
import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.R;
import smartlife.monitorwearables.db.HRMonitorDbHelper;
import smartlife.monitorwearables.service.HeartRateService;

/**
 * Created by Joana on 8/15/2017.
 */

public class TabFragment2 extends Fragment implements SensorEventListener {

    private Button measureHR;
    private TextView tvLiveHR;
    private long liveHR;
    private GifTextView gifHR;
    private HRMonitorDbHelper mDbHelper;
    private static SharedPreferences sharedPrefs;
    public static final String IS_HR_LIVE_TAB_ACTIVE = "isHRLiveTabActive";
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private GoogleApiClient mGoogleApiClient;
    private SensorEventListener sensorEventListener;

    public TabFragment2(){}

    public static Fragment newInstance() {
        TabFragment2 myFragment = new TabFragment2();
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_BEAT);
        sensorEventListener = this;
        View rootView = inflater.inflate(R.layout.tab_2, container, false);
        mDbHelper = new HRMonitorDbHelper(getContext());
        tvLiveHR = (TextView) rootView.findViewById(R.id.tv_live_hr);
        measureHR = (Button) rootView.findViewById(R.id.btn_measure_hr);
        gifHR = (GifTextView) rootView.findViewById(R.id.gif_hr);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        measureHR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                gifHR.setVisibility(View.VISIBLE);
                tvLiveHR.setVisibility(View.GONE);
                params.addRule(RelativeLayout.BELOW, gifHR.getId());
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                measureHR.setLayoutParams(params);
                sharedPrefs.edit().putBoolean(IS_HR_LIVE_TAB_ACTIVE, true).apply();
                mSensorManager.registerListener(sensorEventListener, mHeartRateSensor,  SensorManager.SENSOR_DELAY_UI);
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                        .addApi(Wearable.API)
                        .build();
                mGoogleApiClient.connect();

            }
        });
        if(getArguments() != null){
            liveHR = getArguments().getLong(HeartRateService.EXTRA_LIVE_HR);
            if(liveHR > 0){
                gifHR.setVisibility(View.GONE);
                params.addRule(RelativeLayout.BELOW, tvLiveHR.getId());
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                measureHR.setLayoutParams(params);
                tvLiveHR.setVisibility(View.VISIBLE);
                tvLiveHR.setAlpha(0.7f);
                tvLiveHR.setText(Long.toString(liveHR));
            }
        }
        return rootView;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // is this a heartbeat event and does it have data?
        if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_BEAT && sensorEvent.values.length > 0) {
            int newValue = Math.round(sensorEvent.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}