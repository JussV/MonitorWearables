package smartlife.monitorwearables.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;

import smartlife.monitorwearables.entities.HeartRate;

public class HRMonitorLocalDBOperations {
    private static HRMonitorDbHelper mDbHelper;
    private final static long DAY_IN_MS = 1000 * 60 * 60 * 24;

    private static SQLiteDatabase db;

    public static ArrayList<HeartRate> selectHeartRatesByDateAndDevice(Context cxt, String selection, String[] selectionArgs, String sortOrder){
        mDbHelper = new HRMonitorDbHelper(cxt);
        db = mDbHelper.getReadableDatabase();
        ArrayList<HeartRate> heartRateArray = new ArrayList<HeartRate>();

        Cursor cursor = db.query(
                HRMonitorContract.HeartRate.TABLE_NAME,                   // The table to query
                new String[]{ "value", "createdAt" },                     // The columns to return
                selection,                                                // The columns for the WHERE clause
                selectionArgs,                                            // The values for the WHERE clause
                null,                                                     // don't group the rows
                null,                                                     // don't filter by row groups
                sortOrder                                                 // The sort order
        );

        while(cursor.moveToNext()) {
            int itemValue = cursor.getInt(cursor.getColumnIndexOrThrow(HRMonitorContract.HeartRate.COLUMN_VALUE));
            long itemCreatedAtMiliseconds = cursor.getLong(cursor.getColumnIndexOrThrow(HRMonitorContract.HeartRate.COLUMN_CREATED_AT));
            Date itemCreatedAt = new Date(itemCreatedAtMiliseconds);
            HeartRate heartRateItem = new HeartRate(itemValue, itemCreatedAt);
            heartRateArray.add(heartRateItem);
        }
        cursor.close();
        db.close();
        return heartRateArray;
    }

    public static int removeHeartRatesExceptFromLastTwoDays (Context cxt) {
        long twoDaysAgo = new Date().getTime() - 2 * DAY_IN_MS;

        mDbHelper = new HRMonitorDbHelper(cxt);
        db = mDbHelper.getReadableDatabase();
        int rowsDeleted = db.delete(HRMonitorContract.HeartRate.TABLE_NAME, HRMonitorContract.HeartRate.COLUMN_CREATED_AT + "<= ?", new String[]{ String.valueOf(twoDaysAgo) } );
        db.close();
        return rowsDeleted;
    }

    public static ArrayList<HeartRate> selectHeartRatesByDevice(Context ctx, String[] projection, String selection, String[] selectionArgs,  String groupBy, String having, String sortOrder ){
        mDbHelper = new HRMonitorDbHelper(ctx);
        db = mDbHelper.getReadableDatabase();
        ArrayList<HeartRate> heartRateArray = new ArrayList<HeartRate>();
        Cursor cursor = db.query(
                HRMonitorContract.HeartRate.TABLE_NAME,         // The table to query
                projection,                                     // The columns to return
                selection,                                      // The columns for the WHERE clause
                selectionArgs,                                  // The values for the WHERE clause
                groupBy,                                        // don't group the rows
                having,                                         // don't filter by row groups
                sortOrder                                       // The sort order
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
        return heartRateArray;
    }

    public static long insertHeartRate(Context ctx, long heartRate, int deviceTypeKey){
        mDbHelper = new HRMonitorDbHelper(ctx);
        db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(HRMonitorContract.HeartRate.COLUMN_VALUE, heartRate);
        //Gives the number of milliseconds since January 1, 1970 00:00:00 UTC
        values.put(HRMonitorContract.HeartRate.COLUMN_CREATED_AT, System.currentTimeMillis());
        values.put(HRMonitorContract.HeartRate.COLUMN_DEVICE_TYPE_KEY, deviceTypeKey);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(HRMonitorContract.HeartRate.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }


}
