package smartlife.monitorwearables.fragments.miband;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.R;
import smartlife.monitorwearables.service.ContinuousMeasureScheduler;

public class TabFragment3 extends Fragment {

    private Spinner intervalSpinner;
    private TextView tvMonitorInterval;
    private TextView tvSetInterval;
    private static SharedPreferences sharedPrefs;
    private int spinnerFirstPosition = 0;
    private boolean isSpinnerInitial = true;
    private ContinuousMeasureScheduler scheduler;

    public TabFragment3(){
        scheduler = ContinuousMeasureScheduler.getInstance();
    }

    public static Fragment newInstance() {
        return new TabFragment3();
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
        Switch hrMonitorSwitch = rootView.findViewById(R.id.switch_heartrate_monitor);
        boolean isHRMonitorEnabled = sharedPrefs.getBoolean(getString(R.string.key_enable_continuous_monitoring), false);
        if(isHRMonitorEnabled) {
            isSpinnerInitial = true;
        }
        hrMonitorSwitch.setChecked(isHRMonitorEnabled);

        tvMonitorInterval = rootView.findViewById(R.id.tv_monitor_interval);
        tvSetInterval = rootView.findViewById(R.id.tv_secondary_monitor_interval);

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
                    GBApplication.deviceService().onEnableRealtimeHeartRateMeasurement(true);
                } else {
                    sharedPrefs.edit().putBoolean(getString(R.string.key_enable_continuous_monitoring), false).apply();
                    sharedPrefs.edit().putInt(getString(R.string.key_monitor_interval), spinnerFirstPosition).apply();
                    toggleView(intervalSpinner, false, 0.5f);
                    toggleView(tvMonitorInterval, false, 0.3f);
                    toggleView(tvSetInterval, false, 0.7f);
                    intervalSpinner.setSelection(spinnerFirstPosition);
                    GBApplication.deviceService().onEnableRealtimeHeartRateMeasurement(false);
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
                      //      scheduler.init(interval);
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