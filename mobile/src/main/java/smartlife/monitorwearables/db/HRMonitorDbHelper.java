package smartlife.monitorwearables.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import smartlife.monitorwearables.db.HRMonitorContract.Device;
import smartlife.monitorwearables.db.HRMonitorContract.HeartRate;

/**
 * Created by Joana on 9/24/2017.
 */

public class HRMonitorDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "hrMonitor.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String DATETIME_TYPE = " DATETIME";
    private static final String COMMA_SEP = ",";

    public HRMonitorDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DEVICE_ENTRIES);
        db.execSQL(SQL_CREATE_HR_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_DEVICE_ENTRIES);
        db.execSQL(SQL_DELETE_HR_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private static final String SQL_CREATE_DEVICE_ENTRIES =
            "CREATE TABLE " + Device.TABLE_NAME + " (" +
                    Device._ID + INT_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    Device.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    Device.COLUMN_MANUFACTURER + TEXT_TYPE + " )";

    private static final String SQL_DELETE_DEVICE_ENTRIES =
            "DROP TABLE IF EXISTS " + Device.TABLE_NAME;

    private static final String SQL_CREATE_HR_ENTRIES =
            "CREATE TABLE " + HeartRate.TABLE_NAME + " (" +
                    HeartRate._ID +  INT_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    HeartRate.COLUMN_CREATED_AT + DATETIME_TYPE + COMMA_SEP +
                    HeartRate.COLUMN_VALUE + INT_TYPE + " )";

    private static final String SQL_DELETE_HR_ENTRIES =
            "DROP TABLE IF EXISTS " + HeartRate.TABLE_NAME;
}
