package smartlife.monitorwearables.fragments.wear;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import smartlife.monitorwearables.R;

public class TabFragment2 extends Fragment {

    public TabFragment2(){}

    public static Fragment newInstance() {
        TabFragment2 myFragment = new TabFragment2();
        return myFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate( R.layout.tab_2, container, false); Bundle bundle = this.getArguments();

        return rootView;
    }

}