package smartlife.monitorwearables.fragments.wear;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import smartlife.monitorwearables.R;
import smartlife.monitorwearables.activities.CollectionDemoActivity;
import smartlife.monitorwearables.adapter.HeartRateAdapter;
import smartlife.monitorwearables.db.HRMonitorContract;
import smartlife.monitorwearables.db.HRMonitorLocalDBOperations;
import smartlife.monitorwearables.entities.HeartRate;
import smartlife.monitorwearables.model.DeviceType;

public class TabFragment1 extends Fragment {

    String[] projection = {
            HRMonitorContract.HeartRate._ID,
            HRMonitorContract.HeartRate.COLUMN_VALUE,
            HRMonitorContract.HeartRate.COLUMN_CREATED_AT
    };
    String sortOrder =
            HRMonitorContract.HeartRate.COLUMN_CREATED_AT + " DESC";

    String selection = HRMonitorContract.HeartRate.COLUMN_DEVICE_TYPE_KEY  + "=?";
    String[] selectionArgs = new String[1];

    public TabFragment1(){}

    public static Fragment newInstance() {
        return new TabFragment1();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate( R.layout.tab_1, container, false); Bundle bundle = this.getArguments();
        if (bundle != null) {
            selectionArgs[0] = String.valueOf(bundle.getInt("device", DeviceType.UNKNOWN.getKey()));
        } else {
            selectionArgs = null;
        }

        RecyclerView mRecyclerView = rootView.findViewById(R.id.rv_heart_rates);
        ArrayList<HeartRate> heartRateArray = HRMonitorLocalDBOperations
                .selectHeartRatesByDevice(getContext(), projection, selection, selectionArgs, null, null, sortOrder);
        RecyclerView.Adapter mAdapter = new HeartRateAdapter(heartRateArray);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }

}