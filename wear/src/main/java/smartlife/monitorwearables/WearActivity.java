package smartlife.monitorwearables;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;


public class WearActivity extends WearableActivity implements HeartbeatService.OnChangeListener {


    private static final String LOG_TAG = "MyHeart";
    private static final String TAG_WEAR_ACTIVITY = "WearActivity";
    private TextView mTextView;
    public static ServiceConnection sc;
    private int mChinSize;
    private View mainView;
    private static SharedPreferences prefs;
    public static Activity self;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.wear_activity_main);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BODY_SENSORS}, 1);

        mainView = findViewById(R.id.box_inset);
        mainView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                mChinSize = insets.getSystemWindowInsetBottom();
                v.onApplyWindowInsets(insets);
                return insets;
            }
        });

        mTextView = findViewById(R.id.heartbeat);

        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                Log.d(LOG_TAG, "connected to service.");
                // set change listener to get change events
                ((HeartbeatService.HeartbeatServiceBinder)binder).setChangeListener(WearActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        intent = new Intent(WearActivity.this, HeartbeatService.class);
        bindService(intent, sc, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onValueChanged(int newValue) {
        // called by the service whenever the heartbeat value changes.
        mTextView.setText(Integer.toString(newValue));
    }

    public Intent getHeartBeatIntent(){
        return intent;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(sc);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onExitAmbient(){
        super.onExitAmbient();
    }

    public void toggleServiceRunning() {
        if(prefs.getBoolean(self.getResources().getString(R.string.key_enable_wear_continuous_monitoring), false)){
          //  startService(intent);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    self.recreate();
                }
            });
        }
    }

}
