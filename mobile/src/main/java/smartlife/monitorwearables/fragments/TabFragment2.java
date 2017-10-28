package smartlife.monitorwearables.fragments;

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

import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.R;
import smartlife.monitorwearables.db.HRMonitorDbHelper;
import smartlife.monitorwearables.service.HeartRateService;
import pl.droidsonroids.gif.GifTextView;

/**
 * Created by Joana on 8/15/2017.
 */

public class TabFragment2 extends Fragment {

    private Button measureHR;
    private TextView tvLiveHR;
    private long liveHR;
    private GifTextView gifHR;
    private HRMonitorDbHelper mDbHelper;
    private static SharedPreferences sharedPrefs;
    public static final String IS_HR_LIVE_TAB_ACTIVE = "isHRLiveTabActive";

    public TabFragment2(){}

    public static Fragment newInstance() {
        TabFragment2 myFragment = new TabFragment2();
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

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
                GBApplication.deviceService().onHeartRateTest();
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
}