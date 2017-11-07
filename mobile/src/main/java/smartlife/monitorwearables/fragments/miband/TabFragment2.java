package smartlife.monitorwearables.fragments.miband;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import pl.droidsonroids.gif.GifTextView;
import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.R;
import smartlife.monitorwearables.service.HeartRateService;
public class TabFragment2 extends Fragment {

    private Button measureHR;
    private TextView tvLiveHR;
    private GifTextView gifHR;
    private static SharedPreferences sharedPrefs;
    public static final String IS_HR_LIVE_TAB_ACTIVE = "isHRLiveTabActive";

    public TabFragment2(){}

    public static Fragment newInstance() {
        return new TabFragment2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);
        View rootView = inflater.inflate(R.layout.tab_2, container, false);
        tvLiveHR = rootView.findViewById(R.id.tv_live_hr);
        measureHR = rootView.findViewById(R.id.btn_measure_hr);
        gifHR = rootView.findViewById(R.id.gif_hr);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        setButtonClickability();
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
                GBApplication.deviceService().onHeartRateTest();
            }
        });
        if(getArguments() != null){
            long liveHR = getArguments().getLong(HeartRateService.EXTRA_LIVE_HR);
            if(liveHR > 0){
                gifHR.setVisibility(View.GONE);
                params.addRule(RelativeLayout.BELOW, tvLiveHR.getId());
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                measureHR.setLayoutParams(params);
                tvLiveHR.setVisibility(View.VISIBLE);
                tvLiveHR.setAlpha(0.7f);
                tvLiveHR.setText(String.format(Locale.getDefault(), "%d", liveHR));
            }
        }

        return rootView;
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals(getString(R.string.key_enable_continuous_monitoring)) && getActivity().getClass().getName()!= null) {
               setButtonClickability();
            }
        }
    };

    private void setButtonClickability(){
        boolean isContinuousHREnabled = sharedPrefs.getBoolean(getString(R.string.key_enable_continuous_monitoring), false);
        if(isContinuousHREnabled){
            measureHR.setClickable(false);
            measureHR.setVisibility(View.INVISIBLE);
        } else {
            measureHR.setClickable(true);
            measureHR.setVisibility(View.VISIBLE);
        }
    }

}