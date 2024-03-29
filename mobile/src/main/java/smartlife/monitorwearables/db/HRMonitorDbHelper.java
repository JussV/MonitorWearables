package smartlife.monitorwearables.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import smartlife.monitorwearables.db.HRMonitorContract.Device;
import smartlife.monitorwearables.db.HRMonitorContract.HeartRate;
import smartlife.monitorwearables.db.HRMonitorContract.User;

public class HRMonitorDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "hrMonitor.db";

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
        db.execSQL(SQL_CREATE_USER_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_DEVICE_ENTRIES);
        db.execSQL(SQL_DELETE_HR_ENTRIES);
        db.execSQL(SQL_DELETE_USER_ENTRIES);
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
                    HeartRate.COLUMN_DEVICE_TYPE_KEY + INT_TYPE + COMMA_SEP +
                    HeartRate.COLUMN_VALUE + INT_TYPE + " )";

    private static final String SQL_DELETE_HR_ENTRIES =
            "DROP TABLE IF EXISTS " + HeartRate.TABLE_NAME;

    private static final String SQL_CREATE_USER_ENTRIES =
            "CREATE TABLE " + User.TABLE_NAME + " (" +
                    User._ID + INT_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    User.COLUMN_USERNAME + TEXT_TYPE + COMMA_SEP +
                    User.COLUMN_UNIQUE_PHONE_ID + TEXT_TYPE + COMMA_SEP +
                    User.COLUMN_CREATED_AT + DATETIME_TYPE + COMMA_SEP +
                    User.COLUMN_EMAIl + TEXT_TYPE + " )";

    private static final String SQL_DELETE_USER_ENTRIES =
            "DROP TABLE IF EXISTS " + User.TABLE_NAME;
}
