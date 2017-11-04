package smartlife.monitorwearables.db;

import android.provider.BaseColumns;

public class HRMonitorContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private HRMonitorContract() {}

    /* Inner class that defines the table contents */
    public static class Device implements BaseColumns {
        public static final String TABLE_NAME = "device";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MANUFACTURER = "manufacturer";
    }

    public static class HeartRate implements BaseColumns {
        public static final String TABLE_NAME = "heartRate";
        public static final String COLUMN_CREATED_AT = "createdAt";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_DEVICE_TYPE_KEY = "deviceKey";
    }

}
