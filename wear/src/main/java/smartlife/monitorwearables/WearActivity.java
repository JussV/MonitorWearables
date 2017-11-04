package smartlife.monitorwearables;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

import com.felkertech.settingsmanager.SettingsManager;

import java.util.Locale;


public class WearActivity extends WearableActivity implements HeartbeatService.OnChangeListener {

    private static final String TAG_WEAR_ACTIVITY = "WearActivity";
    private TextView mTextView;
    public static ServiceConnection sc;
    private int mChinSize;
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

        View mainView = findViewById(R.id.box_inset);
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
                Log.d(TAG_WEAR_ACTIVITY, "connected to service.");
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
        mTextView.setText(String.format(Locale.getDefault(), "%d", newValue));
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    self.recreate();
                }
            });
        }
    }

}
