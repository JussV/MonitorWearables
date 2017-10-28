package smartlife.monitorwearables;

/**
 * Created by Joana on 9/27/2017.
 */

public class Constants {
   // public static final String URL ="http://192.168.0.105:3000";
    public static final String URL ="https://unlock-your-wearable.herokuapp.com";
    public static final String DEVICE_API ="/api/devices";
    public static final String HEART_RATE_API ="/api/heartrates";
    public static final String DEVICE_KEY ="key";
    public static final String DEVICE_NAME ="name";
    public static final String LATEST_SYNC_DATE = "/sync/latest";
    public static final String BULK = "/bulk";

    public static final String HR_COLUMN_DATE = "date";
    public static final String HR_COLUMN_DEVICE = "device";
    public static final String HR_COLUMN_UPID = "uniquePhoneId";
    public static final String HR_COLUMN_VALUE = "value";
}
