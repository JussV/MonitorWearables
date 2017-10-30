package smartlife.monitorwearables.fragments.wear;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.felkertech.settingsmanager.SettingsManager;
import com.felkertech.wearsettingsmanager.WearSettingsManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import smartlife.monitorwearables.R;


/**
 * Created by Joana on 10/29/2017.
 */
public class TabFragment3 extends Fragment {

    private Switch hrMonitorSwitch;
    private static SharedPreferences sharedPrefs;
    private int spinnerFirstPosition = 0;
    private SettingsManager mSettingsManager;
    private WearSettingsManager mWearSettingsManager;
    private GoogleApiClient mGoogleApiClient;

    public TabFragment3(){}

    public static Fragment newInstance() {
        TabFragment3 myFragment = new TabFragment3();
        return myFragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab_3_wear, container, false);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSettingsManager = new SettingsManager(getContext());
        mWearSettingsManager = new WearSettingsManager(getContext());
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        mWearSettingsManager.setSyncableSettingsManager(mGoogleApiClient);
        mWearSettingsManager.writeSnapshot();
        hrMonitorSwitch = (Switch) rootView.findViewById(R.id.switch_heartrate_monitor);
        boolean isHRMonitorEnabled = sharedPrefs.getBoolean(getString(R.string.key_enable_wear_continuous_monitoring), false);
        hrMonitorSwitch.setChecked(isHRMonitorEnabled);

        hrMonitorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                   // sharedPrefs.edit().putBoolean(ENABLE_CONTINUOUS_MONITORING_KEY, true).apply();
                    mSettingsManager.setBoolean(getString(R.string.key_enable_wear_continuous_monitoring), true);
                    mWearSettingsManager.pushData();
                } else {
                    //sharedPrefs.edit().putBoolean(ENABLE_CONTINUOUS_MONITORING_KEY, false).apply();
                   // sharedPrefs.edit().putInt(MONITOR_INTERVAL_KEY, spinnerFirstPosition).apply();
                    mSettingsManager.setBoolean(getString(R.string.key_enable_wear_continuous_monitoring), false);
                    mWearSettingsManager.pushData();
                }
            }
        });

        return rootView;
    }

    private void toggleView(View view, boolean enabled, float opacity){
        view.setEnabled(enabled);
        view.setAlpha(opacity);
    }


}