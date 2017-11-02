package smartlife.monitorwearables.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;

import smartlife.monitorwearables.entities.HeartRate;

/**
 * Created by Joana on 9/29/2017.
 */

public class HRMonitorLocalDBOperations {
    private static HRMonitorDbHelper mDbHelper;
    private final static long DAY_IN_MS = 1000 * 60 * 60 * 24;

    private static SQLiteDatabase db;

    public static ArrayList<HeartRate> selectHeartRates(Context cxt, String selection, String[] selectionArgs, String sortOrder){
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


}
