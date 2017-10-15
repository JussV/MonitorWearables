package smartlife.monitorwearables.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.underscore.$;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;

import smartlife.monitorwearables.Constants;
import smartlife.monitorwearables.R;
import smartlife.monitorwearables.adapter.DemoCollectionPagerAdapter;
import smartlife.monitorwearables.db.HRMonitorContract;
import smartlife.monitorwearables.db.HRMonitorLocalDBOperations;
import smartlife.monitorwearables.entities.Device;
import smartlife.monitorwearables.entities.HeartRate;
import smartlife.monitorwearables.fragments.TabFragment1;
import smartlife.monitorwearables.fragments.TabFragment2;
import smartlife.monitorwearables.fragments.TabFragment3;
import smartlife.monitorwearables.model.DeviceType;
import smartlife.monitorwearables.service.HeartRateService;
import smartlife.monitorwearables.service.volley.VolleyCallback;
import smartlife.monitorwearables.service.volley.VolleyOperations;
import smartlife.monitorwearables.util.AndroidUtils;

/**
 * Created by Joana on 8/15/2017.
 */

public class CollectionDemoActivity extends GBActivity {
    public final static int TAB_LIST_HR = 0;
    public final static int TAB_LIVE_HR = 1;

    private ViewPager viewPager;
    private List<Fragment> fragments = new Vector<Fragment>();
    private ScheduledExecutorService scheduledExecutorService;
    private static SharedPreferences sharedPrefs;
    private ImageView syncHR;
    private CoordinatorLayout coordinatorLayout;

    public final static String ACTION_BROADCAST_HR = "smartlife.monitorwearables.broadcasthr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        this.setTitle("Heart rate");
        setContentView(R.layout.activity_collection_demo);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_settings_light));

        TextView firstTab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        firstTab.setText("  ".concat(getResources().getString(R.string.daily_hr)).toUpperCase());
        firstTab.setTextColor(getResources().getColor(R.color.primarytext_dark));
        firstTab.setTypeface(null, Typeface.BOLD);
        firstTab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.daily_heartrates, 0, 0, 0);
        tabLayout.getTabAt(TAB_LIST_HR).setCustomView(firstTab);

        TextView secondTab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        secondTab.setText("  ".concat(getResources().getString(R.string.measure_hr)).toUpperCase());
        secondTab.setTextColor(getResources().getColor(R.color.primarytext_dark));
        secondTab.setTypeface(null, Typeface.BOLD);
        secondTab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pulse, 0, 0, 0);
        tabLayout.getTabAt(TAB_LIVE_HR).setCustomView(secondTab);

        Bundle page = new Bundle();
        fragments.add(Fragment.instantiate(this, TabFragment1.class.getName(), page));
        fragments.add(Fragment.instantiate(this, TabFragment2.class.getName(), page));
        fragments.add(Fragment.instantiate(this, TabFragment3.class.getName(), page));

     //   tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);

        final PagerAdapter adapter = new DemoCollectionPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), fragments);
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        syncHR = (ImageView) findViewById(R.id.iv_sync_hr);
        syncHR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Start syncing heart rates remotely ", Snackbar.LENGTH_LONG);
                snackbar.show();

                //Get last sync date
               // TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                final String uniquePhoneId = Device.getDeviceUniqueId(getApplicationContext());

                VolleyOperations.getLastSyncDate(uniquePhoneId, getApplicationContext(), new VolleyCallback(){
                    @Override
                    public void onSuccess(JSONObject result) throws JSONException {
                        if(result!=null && result.get("date") != null){
                            String lastSyncDate = (String)result.get("date");
                            // mongo date is not milisecond precise, therefore in order to escape inserting the same value twice we add 999 miliseconds to lastSyncDate
                            lastSyncDate = lastSyncDate.replace(lastSyncDate.subSequence(lastSyncDate.length()-4, lastSyncDate.length()-1), "999");
                            final Date lastSynchronizationDate = new Date(AndroidUtils.parseMongoDateToLocal(lastSyncDate));
                            // heart rate measurements exist for the specified date
                            ArrayList<HeartRate> heartRateListByLastSyncDate = HRMonitorLocalDBOperations.selectHeartRates(getApplicationContext(),
                                    HRMonitorContract.HeartRate.COLUMN_CREATED_AT + " > ? ",  new String[] { String.valueOf(AndroidUtils.parseMongoDateToLocal(lastSyncDate)) }, "createdAt ASC");
                            JSONArray mJSONArray = new JSONArray();
                            for (HeartRate heartRate : heartRateListByLastSyncDate) {
                                JSONObject hrObj = new JSONObject();
                                try {
                                    hrObj.put(Constants.HR_COLUMN_DATE, heartRate.getCreatedAt());
                                    hrObj.put(Constants.HR_COLUMN_VALUE, heartRate.getValue());
                                    hrObj.put(Constants.HR_COLUMN_UPID, uniquePhoneId);
                                    // TODO: deviceType should be taken from the device
                                    hrObj.put(Constants.HR_COLUMN_DEVICE, DeviceType.MIBAND2.getKey());
                                    mJSONArray.put(hrObj);
                                }catch (JSONException ex){
                                    ex.printStackTrace();
                                }
                            }
                            Log.d("debug", String.valueOf(mJSONArray.length()));

                            VolleyOperations.bulkInsertHeartRates(getApplicationContext(), uniquePhoneId, mJSONArray, new VolleyCallback() {
                                @Override
                                public void onSuccess(JSONObject result) throws JSONException {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Sync completed: " + result.get("length") + " records are synchronized since " + lastSynchronizationDate + "." , Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }

                                @Override
                                public void onFailure(){

                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(){
                        // no heart rate measurements exist for the specific uniquePhoneId
                        ArrayList<HeartRate> heartRateList = HRMonitorLocalDBOperations.selectHeartRates(getApplicationContext(), null, null, "createdAt ASC");
                        JSONArray mJSONArray = new JSONArray();
                        for (HeartRate heartRate : heartRateList) {
                            JSONObject hrObj = new JSONObject();
                            try {
                                hrObj.put(Constants.HR_COLUMN_DATE, heartRate.getCreatedAt());
                                hrObj.put(Constants.HR_COLUMN_VALUE, heartRate.getValue());
                                hrObj.put(Constants.HR_COLUMN_UPID, uniquePhoneId);
                                // TODO: deviceType should be taken from the device
                                hrObj.put(Constants.HR_COLUMN_DEVICE, DeviceType.MIBAND2.getKey());
                                mJSONArray.put(hrObj);
                            }catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }
                        Log.d("debug", String.valueOf(mJSONArray.length()));

                        VolleyOperations.bulkInsertHeartRates(getApplicationContext(), uniquePhoneId, mJSONArray, new VolleyCallback() {
                            @Override
                            public void onSuccess(JSONObject result) throws JSONException {
                                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Sync completed: " + result.get("length") + " records are synchronized." , Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }

                            @Override
                            public void onFailure(){

                            }
                        });
                    }

                });

            }
        });

    }

    @Override
    protected void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(ACTION_BROADCAST_HR));
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onPause();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Fragment currentFragment = getCurrentFragment();
            FragmentTransaction fragmentTransaction =  getSupportFragmentManager().beginTransaction();

            if(currentFragment instanceof TabFragment1){
              //  ((TabFragment1) currentFragment).getAdapter().notifyDataSetChanged();
                TabFragment1 fragment1 = (TabFragment1) ((DemoCollectionPagerAdapter)viewPager.getAdapter()).getRegisteredFragment(TAB_LIST_HR);
                fragmentTransaction
                        .detach(currentFragment)
                        .attach(fragment1)
                        .commit();
            } else if (currentFragment instanceof TabFragment2){
                Bundle bundle = new Bundle();
                bundle.putLong(HeartRateService.EXTRA_LIVE_HR, intent.getLongExtra(HeartRateService.EXTRA_LIVE_HR, -1));
                TabFragment2 fragment2 = (TabFragment2) ((DemoCollectionPagerAdapter)viewPager.getAdapter()).getRegisteredFragment(TAB_LIVE_HR);
                if (fragment2.getArguments() == null) {
                    fragment2.setArguments(bundle);
                } else {
                    fragment2.getArguments().putAll(bundle);
                }
                TabFragment1 fragment1 = (TabFragment1) ((DemoCollectionPagerAdapter)viewPager.getAdapter()).getRegisteredFragment(TAB_LIST_HR);
                fragmentTransaction
                        .detach(currentFragment)
                        .attach(fragment2)
                        .detach(fragments.get(TAB_LIST_HR))
                        .attach(fragment1)
                        .commit();
            }


        }
    };

    public void selectFragment(int position){
        viewPager.setCurrentItem(position, true);
    }

    private Fragment getCurrentFragment() {
        Fragment fragment = (Fragment) ((DemoCollectionPagerAdapter)viewPager.getAdapter()).getRegisteredFragment(viewPager.getCurrentItem());
        return fragment;
    }


}