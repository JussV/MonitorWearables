package smartlife.monitorwearables.fragments.wear;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import smartlife.monitorwearables.R;
import smartlife.monitorwearables.devices.wear.DataLayerListenerService;
import smartlife.monitorwearables.service.HeartRateService;

public class TabFragment2 extends Fragment {

    public TabFragment2(){}
    private TextView tvLiveHR;
    private Button measureHR;

    public static Fragment newInstance() {
        return new TabFragment2();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate( R.layout.tab_2, container, false); Bundle bundle = this.getArguments();
        tvLiveHR = rootView.findViewById(R.id.tv_live_hr);
        measureHR = rootView.findViewById(R.id.btn_measure_hr);
        measureHR.setVisibility(View.GONE);
        measureHR.setEnabled(false);
        if(getArguments() != null){
            long liveHR = getArguments().getLong(DataLayerListenerService.EXTRA_LIVE_HR_WEAR);
            if(liveHR > 0){
                tvLiveHR.setVisibility(View.VISIBLE);
                tvLiveHR.setAlpha(0.7f);
                tvLiveHR.setText(String.format(Locale.getDefault(), "%d", liveHR));
            }
        }
        return rootView;
    }

}