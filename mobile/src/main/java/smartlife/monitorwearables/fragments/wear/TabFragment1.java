package smartlife.monitorwearables.fragments.wear;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import smartlife.monitorwearables.R;
import smartlife.monitorwearables.adapter.HeartRateAdapter;
import smartlife.monitorwearables.db.HRMonitorContract;
import smartlife.monitorwearables.db.HRMonitorDbHelper;
import smartlife.monitorwearables.entities.HeartRate;
import smartlife.monitorwearables.model.DeviceType;

public class TabFragment1 extends Fragment {
    List<HeartRate> heartRateArray;
    private int deviceTypeKey;

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
        TabFragment1 myFragment = new TabFragment1();
        return myFragment;
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
        HRMonitorDbHelper mDbHelper = new HRMonitorDbHelper(getContext());
        heartRateArray = new ArrayList<HeartRate>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                HRMonitorContract.HeartRate.TABLE_NAME,         // The table to query
                projection,                                    // The columns to return
                selection,                                    // The columns for the WHERE clause
                selectionArgs,                               // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                                 // The sort order
        );

        while(cursor.moveToNext()) {
            int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(HRMonitorContract.HeartRate._ID));
            int itemValue = cursor.getInt(cursor.getColumnIndexOrThrow(HRMonitorContract.HeartRate.COLUMN_VALUE));
            long itemCreatedAtMiliseconds = cursor.getLong(cursor.getColumnIndexOrThrow(HRMonitorContract.HeartRate.COLUMN_CREATED_AT));
            Date itemCreatedAt = new Date(itemCreatedAtMiliseconds);
            HeartRate heartRateItem = new HeartRate(itemId, itemValue, itemCreatedAt);
            heartRateArray.add(heartRateItem);
        }
        cursor.close();
        db.close();
        RecyclerView.Adapter mAdapter = new HeartRateAdapter(heartRateArray);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        return rootView;
    }

}