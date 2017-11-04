package smartlife.monitorwearables.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;

import smartlife.monitorwearables.activities.CollectionDemoActivity;
import smartlife.monitorwearables.db.HRMonitorContract;
import smartlife.monitorwearables.db.HRMonitorDbHelper;

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
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(HRMonitorContract.HeartRate.COLUMN_VALUE, heartRate);
        //Gives the number of milliseconds since January 1, 1970 00:00:00 UTC
        values.put(HRMonitorContract.HeartRate.COLUMN_CREATED_AT, System.currentTimeMillis());
        values.put(HRMonitorContract.HeartRate.COLUMN_DEVICE_TYPE_KEY, deviceTypeKey);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(HRMonitorContract.HeartRate.TABLE_NAME, null, values);
        db.close();
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
