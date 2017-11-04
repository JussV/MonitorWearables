package smartlife.monitorwearables.fragments.miband;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import smartlife.monitorwearables.R;
import smartlife.monitorwearables.service.ContinuousMeasureScheduler;


/**
 * Created by Joana on 8/15/2017.
 */
public class TabFragment3 extends Fragment {
/*
    public static final String ENABLE_CONTINUOUS_MONITORING_KEY = "enableContinuousMonitoring";
    public static final String MONITOR_INTERVAL_KEY = "monitorInterval";*/

    private Spinner intervalSpinner;
    private TextView tvMonitorInterval;
    private TextView tvSetInterval;
    private Switch hrMonitorSwitch;
    private static SharedPreferences sharedPrefs;
    private int spinnerFirstPosition = 0;
    private boolean isSpinnerInitial = true;
    private ContinuousMeasureScheduler scheduler;
    /*private SettingsManager mSettingsManager;
    private WearSettingsManager mWearSettingsManager;
    private GoogleApiClient mGoogleApiClient;*/

    public TabFragment3(){
        scheduler = ContinuousMeasureScheduler.getInstance();
    }

    public static Fragment newInstance() {
        TabFragment3 myFragment = new TabFragment3();
        return myFragment;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab_3, container, false);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        intervalSpinner = (Spinner) rootView.findViewById(R.id.spinner_monitor_interval);
        int monitorIntervalPosition = sharedPrefs.getInt(getString(R.string.key_monitor_interval), spinnerFirstPosition);
        intervalSpinner.setSelection(monitorIntervalPosition);
        if(monitorIntervalPosition != spinnerFirstPosition) {
            toggleView(intervalSpinner, true, 1);
            intervalSpinner.setSelected(true);
        } else{
            toggleView(intervalSpinner, false, 0.5f);
            intervalSpinner.setSelected(false);
        }
        hrMonitorSwitch = (Switch) rootView.findViewById(R.id.switch_heartrate_monitor);
        boolean isHRMonitorEnabled = sharedPrefs.getBoolean(getString(R.string.key_enable_continuous_monitoring), false);
        if(isHRMonitorEnabled) {
            isSpinnerInitial = true;
        }
        hrMonitorSwitch.setChecked(isHRMonitorEnabled);

        tvMonitorInterval = (TextView) rootView.findViewById(R.id.tv_monitor_interval);
        tvSetInterval = (TextView) rootView.findViewById(R.id.tv_secondary_monitor_interval);

        if(hrMonitorSwitch.isChecked()){
            toggleView(tvMonitorInterval, true, 1);
            toggleView(tvSetInterval, true, 1);
            toggleView(intervalSpinner, true, 1);
        } else {
            toggleView(tvMonitorInterval, false, 0.3f);
            toggleView(tvSetInterval, false, 0.7f);
            toggleView(intervalSpinner, false, 0.5f);
        }


        hrMonitorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    sharedPrefs.edit().putBoolean(getString(R.string.key_enable_continuous_monitoring), true).apply();
                    toggleView(intervalSpinner, true, 1);
                    toggleView(tvMonitorInterval, true, 1);
                    toggleView(tvSetInterval, true, 1);
                } else {
                    sharedPrefs.edit().putBoolean(getString(R.string.key_enable_continuous_monitoring), false).apply();
                    sharedPrefs.edit().putInt(getString(R.string.key_monitor_interval), spinnerFirstPosition).apply();
                    toggleView(intervalSpinner, false, 0.5f);
                    toggleView(tvMonitorInterval, false, 0.3f);
                    toggleView(tvSetInterval, false, 0.7f);
                    intervalSpinner.setSelection(spinnerFirstPosition);
                    scheduler.end();
                }
            }
        });


        intervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(selectedItemView != null ) {
                    if (isSpinnerInitial) {
                        isSpinnerInitial = false;
                    } else {
                        int interval = !intervalSpinner.getSelectedItem().toString().equals("") ? Integer.valueOf(intervalSpinner.getSelectedItem().toString()) : 0;
                        sharedPrefs.edit().putInt(getString(R.string.key_monitor_interval), intervalSpinner.getSelectedItemPosition()).apply();
                        synchronized (this) {
                            scheduler.end();
                            scheduler.init(interval);
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        return rootView;
    }

    private void toggleView(View view, boolean enabled, float opacity){
        view.setEnabled(enabled);
        view.setAlpha(opacity);
    }


}