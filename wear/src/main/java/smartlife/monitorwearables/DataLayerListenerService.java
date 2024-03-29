package smartlife.monitorwearables;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.felkertech.settingsmanager.SettingsManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;


public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG_DATA_LAYER_LISTENER = "DataLayerSample";
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences prefs;
    private Node nearbyNode;
    WearActivity wearActivity = new WearActivity();
    private SettingsManager mSettingsManager;

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable(TAG_DATA_LAYER_LISTENER, Log.DEBUG)) {
            Log.d(TAG_DATA_LAYER_LISTENER, "onDataChanged: " + dataEvents);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettingsManager = new SettingsManager(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG_DATA_LAYER_LISTENER, "Failed to connect to GoogleApiClient.");
            return;
        }
       // getNearestConnectedNode();
        PendingResult<NodeApi.GetConnectedNodesResult> result = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        // Loop through the events and send a message
        // to the node that created the data item.
        for (DataEvent event : dataEvents) {
            if(event != null) {
                Uri uri = event.getDataItem().getUri();
                DataItem item = event.getDataItem();
                // Get the node id from the host value of the URI
                String nodeId = uri.getHost();
                // Set the data of the message to be the bytes of the URI
                NodeApi.GetConnectedNodesResult getConnectedNodesResult = result.await();
                for(Node node: getConnectedNodesResult.getNodes()){
                    if(node.isNearby()){
                        nearbyNode = node;
                    }
                }
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                if (nodeId.equals(nearbyNode.getId())) {
                //    prefs.edit().putBoolean(getString(R.string.key_enable_wear_continuous_monitoring), dataMap.getBoolean(getString(R.string.key_enable_wear_continuous_monitoring))).apply();
                    mSettingsManager.setBoolean(getString(R.string.key_enable_wear_continuous_monitoring), dataMap.getBoolean(getString(R.string.key_enable_wear_continuous_monitoring)));
                    wearActivity.toggleServiceRunning(mSettingsManager);
                }
            }

        }
    }

    public synchronized Node getNearestConnectedNode(){
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for(Node node: getConnectedNodesResult.getNodes()){
                    if(node.isNearby()){
                        nearbyNode = node;
                    }
                }
                Log.d(TAG_DATA_LAYER_LISTENER, "Nodes: " + getConnectedNodesResult.getNodes());
            }
        });
        return nearbyNode;
    }


}
