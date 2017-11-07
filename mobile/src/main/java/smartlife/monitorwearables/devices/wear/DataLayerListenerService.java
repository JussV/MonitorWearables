package smartlife.monitorwearables.devices.wear;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Locale;

import smartlife.monitorwearables.activities.CollectionDemoActivity;
import smartlife.monitorwearables.db.HRMonitorContract;
import smartlife.monitorwearables.db.HRMonitorDbHelper;
import smartlife.monitorwearables.db.HRMonitorLocalDBOperations;
import smartlife.monitorwearables.fragments.wear.TabFragment1;
import smartlife.monitorwearables.model.DeviceType;

public class DataLayerListenerService extends WearableListenerService {

    private static final String LOG_TAG = "WearableListener";
    public final static String EXTRA_LIVE_HR_WEAR = "smartlife.monitorwearables.extralivehr";

    private static Handler handler;
    private HRMonitorDbHelper mDbHelper = new HRMonitorDbHelper(this);;

    public static Handler getHandler() {
        return handler;
    }

    public static void setHandler(Handler handler) {
        int currentValue=0;
        DataLayerListenerService.handler = handler;
        // send current value as initial value.
        if(handler!=null)
            handler.sendEmptyMessage(currentValue);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        String id = peer.getId();
        String name = peer.getDisplayName();

        Log.d(LOG_TAG, "Connected peer name & ID: " + name + "|" + id);
    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/wear/heartRate")) {
            DataMap resultMap = DataMap.fromByteArray(messageEvent.getData());
            ArrayList<DataMap> resultArr = resultMap.get("key");
            Integer heartRate = Integer.valueOf(resultArr.get(0).get("heartRate").toString());
            String androidWearModel = resultArr.get(1).get("wearModel");
            int deviceKey = DeviceType.getKeyByWearDeviceName(androidWearModel);
            Log.v(LOG_TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.v(LOG_TAG, "Message received on watch is: " + heartRate);
            HRMonitorLocalDBOperations.insertHeartRate(this, heartRate, deviceKey);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }


}
