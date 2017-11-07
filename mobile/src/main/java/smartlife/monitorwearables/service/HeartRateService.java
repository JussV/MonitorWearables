package smartlife.monitorwearables.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import smartlife.monitorwearables.activities.CollectionDemoActivity;
import smartlife.monitorwearables.db.HRMonitorContract;
import smartlife.monitorwearables.db.HRMonitorDbHelper;
import smartlife.monitorwearables.db.HRMonitorLocalDBOperations;
import smartlife.monitorwearables.fragments.wear.TabFragment1;

public class HeartRateService extends IntentService {

    public final static String ACTION_MEASURE_HR = "smartlife.monitorwearables.measurehr";
    public final static String EXTRA_LIVE_HR = "smartlife.monitorwearables.extralivehr";
    public final static String DEVICE_TYPE_KEY  = "smartlife.monitorwearables.devicetypekey";

    private HRMonitorDbHelper mDbHelper;

    public HeartRateService(){
        super("HeartRateService");
        mDbHelper = new HRMonitorDbHelper(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_MEASURE_HR.equals(intent.getAction())) {
            measureHeartRate(intent);
        }else {
            throw new UnsupportedOperationException("Action not supported: " + intent.getAction());
        }
    }

    public void measureHeartRate(Intent intent) {
        long heartRate = intent.getLongExtra(EXTRA_LIVE_HR, -1);
        int deviceTypeKey = intent.getIntExtra(DEVICE_TYPE_KEY, -1);
        HRMonitorLocalDBOperations.insertHeartRate(this, heartRate, deviceTypeKey);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CollectionDemoActivity.ACTION_BROADCAST_HR);
        broadcastIntent.putExtra(EXTRA_LIVE_HR, heartRate);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    public static void startMeasureHeartRate(Context context, long heartRate, int deviceTypeKey) {
        Intent intent = new Intent(context, HeartRateService.class);
        intent.setAction(ACTION_MEASURE_HR);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_LIVE_HR, heartRate);
        intent.putExtra(DEVICE_TYPE_KEY, deviceTypeKey);
        context.startService(intent);
    }

}
