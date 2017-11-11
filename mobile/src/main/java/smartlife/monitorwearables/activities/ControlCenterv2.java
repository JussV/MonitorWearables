/*  Copyright (C) 2016-2017 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package smartlife.monitorwearables.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.R;
import smartlife.monitorwearables.adapter.DeviceRecyclerViewAdapter;
import smartlife.monitorwearables.devices.DeviceManager;
import smartlife.monitorwearables.devices.wear.DataLayerListenerService;
import smartlife.monitorwearables.impl.GBDevice;
import smartlife.monitorwearables.model.DeviceType;
import smartlife.monitorwearables.service.volley.VolleyOperations;
import smartlife.monitorwearables.util.GB;
import smartlife.monitorwearables.util.Prefs;

public class ControlCenterv2 extends AppCompatActivity implements CapabilityApi.CapabilityListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener {

    private String TAG = "ControlCenterv2";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private DeviceManager deviceManager;
    private ImageView background;
    private List<GBDevice> deviceList;
    private DeviceRecyclerViewAdapter mGBDeviceAdapter;
    private RecyclerView deviceListView;
    private GoogleApiClient mGoogleApiClient;
    Set<Node> nodeList = null;
    private WifiManager wifiManager;
    WifiInfo wifiInfo;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("Wear Message: ", msg.toString());
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case GBApplication.ACTION_QUIT:
                    finish();
                    break;
                case DeviceManager.ACTION_DEVICES_CHANGED:
                    refreshPairedDevices();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.GadgetbridgeTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlcenterv2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDiscoveryActivity();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.controlcenter_navigation_drawer_open, R.string.controlcenter_navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //end of material design boilerplate
        deviceManager = ((GBApplication) getApplication()).getDeviceManager();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setHasFixedSize(true);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));
        background = findViewById(R.id.no_items_bg);

        deviceList = deviceManager.getDevices();
        //Android Wear devices are discovered by using Wearable API
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        getConnectedNodes();

        mGBDeviceAdapter = new DeviceRecyclerViewAdapter(this, deviceList);

        deviceListView.setAdapter(this.mGBDeviceAdapter);

        registerForContextMenu(deviceListView);

        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(GBApplication.ACTION_QUIT);
        filterLocal.addAction(DeviceManager.ACTION_DEVICES_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filterLocal);

        refreshPairedDevices();

        /*
         * Ask for permission to intercept notifications on first run.
         */
        Prefs prefs = GBApplication.getPrefs();
        if (prefs.getBoolean("firstrun", true)) {
            prefs.getPreferences().edit().putBoolean("firstrun", false).apply();
            //show alert dialog to remind the user to signup to cloud app
            new AlertDialog.Builder(this)
                    .setTitle(R.string.signup_title)
                    .setMessage(R.string.signup_for_cloud_app)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setNegativeButton(R.string.cancel, null).show();

          /*  Intent enableIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(enableIntent);*/
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        GBApplication.deviceService().start();

        if (GB.isBluetoothEnabled() && deviceList.isEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startActivity(new Intent(this, DiscoveryActivity.class));
        } else {
            GBApplication.deviceService().requestDeviceInfo();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register our handler with the DataLayerService. This ensures we get messages whenever the service receives something.
        DataLayerListenerService.setHandler(handler);
    }

    @Override
    protected void onPause() {
        // unregister our handler so the service does not need to send its messages anywhere.
        DataLayerListenerService.setHandler(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterForContextMenu(deviceListView);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            /*case R.id.action_debug:
                Intent debugIntent = new Intent(this, DebugActivity.class);
                startActivity(debugIntent);
                return true;*/
            case R.id.action_signup:
                /*Intent debugIntent = new Intent(this, DebugActivity.class);
                startActivity(debugIntent);*/
                return true;
        }

        return true;
    }

    private void launchDiscoveryActivity() {
        startActivity(new Intent(this, DiscoveryActivity.class));
    }

    private void refreshPairedDevices() {
        List<GBDevice> deviceList = deviceManager.getDevices();
        if (deviceList.isEmpty()) {
            background.setVisibility(View.VISIBLE);
        } else {
            background.setVisibility(View.INVISIBLE);
        }

        mGBDeviceAdapter.notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermissions() {
        List<String> wantedPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_PHONE_STATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CALENDAR);

        if (!wantedPermissions.isEmpty())
            ActivityCompat.requestPermissions(this, wantedPermissions.toArray(new String[wantedPermissions.size()]), 0);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + connectionResult);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.CapabilityApi.addCapabilityListener(mGoogleApiClient, this, "verify_remote_wear_app");
    }

    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        Node connectedNode = pickBestNode(connectedNodes);
        if(connectedNode != null){
            Log.d(TAG, "Wear Connected");
            for(GBDevice device: deviceList) {
                if (device.getType() == DeviceType.ANDROIDWEAR_MOTO360SPORT && connectedNode.getDisplayName().equals(device.getName())) {
                    device.setState(GBDevice.State.INITIALIZED);
                }
            }
        }else{
            Log.d(TAG, "Wear not connected");
            for(GBDevice device: deviceList) {
                if (device.getType() == DeviceType.ANDROIDWEAR_MOTO360SPORT) {
                    device.setState(GBDevice.State.NOT_CONNECTED);
                }
            }
        }
    }

    private Node pickBestNode(Set<Node> nodes) {
        Node bestNode = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node;
            }
            bestNode = node;
        }
        return bestNode;
    }

    /**
     * <p>
     * Not always Android Wear devices are returned as bonded devices.
     * <p>
     * Therefore we check if there are any other nodes connected and add them to deviceList.
     *
     */
    private void getConnectedNodes(){
        PendingResult<CapabilityApi.GetCapabilityResult> pendingResult = Wearable.CapabilityApi.getCapability(mGoogleApiClient, "verify_remote_wear_app", CapabilityApi.FILTER_REACHABLE);
        pendingResult.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
            @Override
            public void onResult(@NonNull CapabilityApi.GetCapabilityResult nodes) {
                nodeList = nodes.getCapability().getNodes();
                for(Node node: nodeList){
                    for(GBDevice device: deviceList){
                        if(!device.getName().equals(node.getDisplayName()) && device.getType()==DeviceType.ANDROIDWEAR_MOTO360SPORT){
                            GBDevice wearDevice = new GBDevice("", node.getDisplayName(), DeviceType.ANDROIDWEAR_MOTO360SPORT);
                            deviceList.add(wearDevice);
                        }
                        if(device.getType()==DeviceType.ANDROIDWEAR_MOTO360SPORT && device.getName().equals(node.getDisplayName())){
                            device.setState(GBDevice.State.INITIALIZED);
                            // Store connected device in remote db
                            VolleyOperations.storeDeviceToRemoteDB(device, getApplicationContext());
                        }
                    }
                }
            }
        }
        );

    }
}
