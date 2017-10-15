/*  Copyright (C) 2015-2017 Andreas Shimokawa, Avamander, Carsten Pfeiffer,
    Daniele Gobbetti, Daniel Hauck, ivanovlev, João Paulo Barraca, Julien
    Pivotto, Kasha, Sergey Trofimov, Steffen Liebergeld, Uwe Hermann

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
package smartlife.monitorwearables.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import smartlife.monitorwearables.Constants;
import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.R;
import smartlife.monitorwearables.externalevents.BluetoothConnectReceiver;
import smartlife.monitorwearables.externalevents.BluetoothPairingRequestReceiver;
import smartlife.monitorwearables.impl.GBDevice;
import smartlife.monitorwearables.model.NotificationSpec;
import smartlife.monitorwearables.service.volley.VolleySingleton;
import smartlife.monitorwearables.util.DeviceHelper;
import smartlife.monitorwearables.util.GB;
import smartlife.monitorwearables.util.GBPrefs;
import smartlife.monitorwearables.util.Prefs;

import static smartlife.monitorwearables.model.DeviceService.ACTION_CONNECT;
import static smartlife.monitorwearables.model.DeviceService.ACTION_DELETE_NOTIFICATION;
import static smartlife.monitorwearables.model.DeviceService.ACTION_DISCONNECT;
import static smartlife.monitorwearables.model.DeviceService.ACTION_ENABLE_HEARTRATE_SLEEP_SUPPORT;
import static smartlife.monitorwearables.model.DeviceService.ACTION_ENABLE_REALTIME_HEARTRATE_MEASUREMENT;
import static smartlife.monitorwearables.model.DeviceService.ACTION_ENABLE_REALTIME_STEPS;
import static smartlife.monitorwearables.model.DeviceService.ACTION_FETCH_ACTIVITY_DATA;
import static smartlife.monitorwearables.model.DeviceService.ACTION_FIND_DEVICE;
import static smartlife.monitorwearables.model.DeviceService.ACTION_HEARTRATE_TEST;
import static smartlife.monitorwearables.model.DeviceService.ACTION_INSTALL;
import static smartlife.monitorwearables.model.DeviceService.ACTION_NOTIFICATION;
import static smartlife.monitorwearables.model.DeviceService.ACTION_REBOOT;
import static smartlife.monitorwearables.model.DeviceService.ACTION_REQUEST_DEVICEINFO;
import static smartlife.monitorwearables.model.DeviceService.ACTION_SEND_CONFIGURATION;
import static smartlife.monitorwearables.model.DeviceService.ACTION_SETTIME;
import static smartlife.monitorwearables.model.DeviceService.ACTION_SET_CONSTANT_VIBRATION;
import static smartlife.monitorwearables.model.DeviceService.ACTION_START;
import static smartlife.monitorwearables.model.DeviceService.ACTION_TEST_NEW_FUNCTION;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_BOOLEAN_ENABLE;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_CONFIG;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_CONNECT_FIRST_TIME;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_FIND_START;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_NOTIFICATION_BODY;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_NOTIFICATION_FLAGS;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_NOTIFICATION_ID;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_NOTIFICATION_PHONENUMBER;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_NOTIFICATION_SENDER;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_NOTIFICATION_SOURCENAME;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_NOTIFICATION_SUBJECT;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_NOTIFICATION_TITLE;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_URI;
import static smartlife.monitorwearables.model.DeviceService.EXTRA_VIBRATION_INTENSITY;


public class DeviceCommunicationService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    @SuppressLint("StaticFieldLeak") // only used for test cases
    private static DeviceSupportFactory DEVICE_SUPPORT_FACTORY = null;

    public final static String LAST_DEVICE_ADDRESS = "last_device_address";

    private boolean mStarted = false;

    private DeviceSupportFactory mFactory;
    private GBDevice mGBDevice = null;
    private DeviceSupport mDeviceSupport;

   // private TimeChangeReceiver mTimeChangeReceiver = null;
    private BluetoothConnectReceiver mBlueToothConnectReceiver = null;
    private BluetoothPairingRequestReceiver mBlueToothPairingRequestReceiver = null;

    private Random mRandom = new Random();


    /**
     * For testing!
     *
     * @param factory
     */
    public static void setDeviceSupportFactory(DeviceSupportFactory factory) {
        DEVICE_SUPPORT_FACTORY = factory;
    }

    public DeviceCommunicationService() { }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GBDevice.ACTION_DEVICE_CHANGED)) {
                GBDevice device = intent.getParcelableExtra(GBDevice.EXTRA_DEVICE);
                if (mGBDevice != null && mGBDevice.equals(device)) {
                    mGBDevice = device;
                    boolean enableReceivers = mDeviceSupport != null && (mDeviceSupport.useAutoConnect() || mGBDevice.isInitialized());
                    setReceiversEnableState(enableReceivers, mGBDevice.isInitialized(), DeviceHelper.getInstance().getCoordinator(device));
                  //  GB.updateNotification(mGBDevice.getName() + " " + mGBDevice.getStateString(), mGBDevice.isInitialized(), context);
                } else {
                   // LOG.error("Got ACTION_DEVICE_CHANGED from unexpected device: " + device);
                }
            }
        }
    };

    @Override
    public void onCreate() {
       // LOG.debug("DeviceCommunicationService is being created");
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(GBDevice.ACTION_DEVICE_CHANGED));
        mFactory = getDeviceSupportFactory();

        if (hasPrefs()) {
            getPrefs().getPreferences().registerOnSharedPreferenceChangeListener(this);
        }
    }

    private DeviceSupportFactory getDeviceSupportFactory() {
        if (DEVICE_SUPPORT_FACTORY != null) {
            return DEVICE_SUPPORT_FACTORY;
        }
        return new DeviceSupportFactory(this);
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
         //   LOG.info("no intent");
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        boolean firstTime = intent.getBooleanExtra(EXTRA_CONNECT_FIRST_TIME, false);

        if (action == null) {
         //   LOG.info("no action");
            return START_NOT_STICKY;
        }

      //  LOG.debug("Service startcommand: " + action);

        if (!action.equals(ACTION_START) && !action.equals(ACTION_CONNECT)) {
            if (!mStarted) {
                // using the service before issuing ACTION_START
          //      LOG.info("Must start service with " + ACTION_START + " or " + ACTION_CONNECT + " before using it: " + action);
                return START_NOT_STICKY;
            }

            if (mDeviceSupport == null || (!isInitialized() && !mDeviceSupport.useAutoConnect())) {
                // trying to send notification without valid Bluetooth connection
                if (mGBDevice != null) {
                    // at least send back the current device state
                    mGBDevice.sendDeviceUpdateIntent(this);
                }
                return START_STICKY;
            }
        }

        // when we get past this, we should have valid mDeviceSupport and mGBDevice instances

        Prefs prefs = getPrefs();
        switch (action) {
            case ACTION_START:
                start();
                break;
            case ACTION_CONNECT:
                start(); // ensure started
                GBDevice gbDevice = intent.getParcelableExtra(GBDevice.EXTRA_DEVICE);
                String btDeviceAddress = null;
                if (gbDevice == null) {
                    if (prefs != null) { // may be null in test cases
                        btDeviceAddress = prefs.getString(LAST_DEVICE_ADDRESS, null);
                        if (btDeviceAddress != null) {
                            gbDevice = DeviceHelper.getInstance().findAvailableDevice(btDeviceAddress, this);
                        }
                    }
                } else {
                    btDeviceAddress = gbDevice.getAddress();
                }

                boolean autoReconnect = GBPrefs.AUTO_RECONNECT_DEFAULT;
                if (prefs != null && prefs.getPreferences() != null) {
                    prefs.getPreferences().edit().putString(LAST_DEVICE_ADDRESS, btDeviceAddress).apply();
                    autoReconnect = getGBPrefs().getAutoReconnect();
                }

                if (gbDevice != null && !isConnecting() && !isConnected()) {
                    setDeviceSupport(null);
                    try {
                        DeviceSupport deviceSupport = mFactory.createDeviceSupport(gbDevice);
                        if (deviceSupport != null) {
                            setDeviceSupport(deviceSupport);
                            if (firstTime) {
                                deviceSupport.connectFirstTime();
                            } else {
                                deviceSupport.setAutoReconnect(autoReconnect);
                                deviceSupport.connect();
                            }

                            final GBDevice gbDeviceFinal = gbDevice;
                            // Store connected device in remote db
                            JsonArrayRequest jsArrRequest = new JsonArrayRequest
                                    (Request.Method.GET, Constants.URL.concat(Constants.DEVICE_API), null, new Response.Listener<JSONArray>() {

                                @Override
                                public void onResponse(JSONArray response) {
                                   // if gbDevice's key is not in remote db add it
                                    List<Integer> remoteDeviceKeys = new ArrayList<Integer>();
                                    for (int i = 0; i < response.length() ; i++) {
                                        try {
                                            JSONObject mJsonObject = (JSONObject)response.get(i);
                                            remoteDeviceKeys.add((Integer)mJsonObject.get(Constants.DEVICE_KEY));
                                        } catch (JSONException ex){
                                            ex.printStackTrace();
                                        }
                                    }
                                    if(remoteDeviceKeys.size() == 0 || !remoteDeviceKeys.contains(gbDeviceFinal.getType().getKey())){
                                        JSONObject device = new JSONObject();
                                        try {
                                            device.put(Constants.DEVICE_KEY, gbDeviceFinal.getType().getKey());
                                            device.put(Constants.DEVICE_NAME, gbDeviceFinal.getName());
                                            addDeviceToRemoteDb(device);
                                        } catch (JSONException ex){
                                            ex.printStackTrace();
                                        }

                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                }
                            });
                            VolleySingleton.getInstance(this).addToRequestQueue(jsArrRequest);

                        } else {
                            GB.toast(this, getString(R.string.cannot_connect, "Can't create device support"), Toast.LENGTH_SHORT, GB.ERROR);
                        }
                    } catch (Exception e) {
                        GB.toast(this, getString(R.string.cannot_connect, e.getMessage()), Toast.LENGTH_SHORT, GB.ERROR, e);
                        setDeviceSupport(null);
                    }
                } else if (mGBDevice != null) {
                    // send an update at least
                    mGBDevice.sendDeviceUpdateIntent(this);
                }
                break;
            case ACTION_REQUEST_DEVICEINFO:
                mGBDevice.sendDeviceUpdateIntent(this);
                break;
            case ACTION_NOTIFICATION: {
                NotificationSpec notificationSpec = new NotificationSpec();
                notificationSpec.phoneNumber = intent.getStringExtra(EXTRA_NOTIFICATION_PHONENUMBER);
                notificationSpec.sender = intent.getStringExtra(EXTRA_NOTIFICATION_SENDER);
                notificationSpec.subject = intent.getStringExtra(EXTRA_NOTIFICATION_SUBJECT);
                notificationSpec.title = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE);
                notificationSpec.body = intent.getStringExtra(EXTRA_NOTIFICATION_BODY);
                notificationSpec.sourceName = intent.getStringExtra(EXTRA_NOTIFICATION_SOURCENAME);
             //   notificationSpec.type = (NotificationType) intent.getSerializableExtra(EXTRA_NOTIFICATION_TYPE);
                notificationSpec.id = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
                notificationSpec.flags = intent.getIntExtra(EXTRA_NOTIFICATION_FLAGS, 0);


                if (((notificationSpec.flags & NotificationSpec.FLAG_WEARABLE_REPLY) > 0)
                        || ( notificationSpec.phoneNumber != null)) {
                    // NOTE: maybe not where it belongs
                    if (prefs.getBoolean("pebble_force_untested", false)) {
                        // I would rather like to save that as an array in ShadredPreferences
                        // this would work but I dont know how to do the same in the Settings Activity's xml
                        ArrayList<String> replies = new ArrayList<>();
                        for (int i = 1; i <= 16; i++) {
                            String reply = prefs.getString("canned_reply_" + i, null);
                            if (reply != null && !reply.equals("")) {
                                replies.add(reply);
                            }
                        }
                        notificationSpec.cannedReplies = replies.toArray(new String[replies.size()]);
                    }
                }

                mDeviceSupport.onNotification(notificationSpec);
                break;
            }
            case ACTION_DELETE_NOTIFICATION: {
                mDeviceSupport.onDeleteNotification(intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1));
                break;
            }
            case ACTION_REBOOT: {
                mDeviceSupport.onReboot();
                break;
            }
            case ACTION_HEARTRATE_TEST: {
                mDeviceSupport.onHeartRateTest();
                break;
            }
            case ACTION_FETCH_ACTIVITY_DATA: {
                mDeviceSupport.onFetchActivityData();
                break;
            }
            case ACTION_DISCONNECT: {
                mDeviceSupport.dispose();
                if (mGBDevice != null && mGBDevice.getState() == GBDevice.State.WAITING_FOR_RECONNECT) {
                    setReceiversEnableState(false, false, null);
                    mGBDevice.setState(GBDevice.State.NOT_CONNECTED);
                    mGBDevice.sendDeviceUpdateIntent(this);
                }
                mDeviceSupport = null;
                break;
            }
            case ACTION_FIND_DEVICE: {
                boolean start = intent.getBooleanExtra(EXTRA_FIND_START, false);
                mDeviceSupport.onFindDevice(start);
                break;
            }
            case ACTION_SET_CONSTANT_VIBRATION: {
                int intensity = intent.getIntExtra(EXTRA_VIBRATION_INTENSITY, 0);
                mDeviceSupport.onSetConstantVibration(intensity);
                break;
            }
            case ACTION_SETTIME:
                mDeviceSupport.onSetTime();
                break;
            case ACTION_INSTALL:
                Uri uri = intent.getParcelableExtra(EXTRA_URI);
                if (uri != null) {
               //     LOG.info("will try to install app/fw");
                    mDeviceSupport.onInstallApp(uri);
                }
                break;
            case ACTION_ENABLE_REALTIME_STEPS: {
                boolean enable = intent.getBooleanExtra(EXTRA_BOOLEAN_ENABLE, false);
                mDeviceSupport.onEnableRealtimeSteps(enable);
                break;
            }
            case ACTION_ENABLE_HEARTRATE_SLEEP_SUPPORT: {
                boolean enable = intent.getBooleanExtra(EXTRA_BOOLEAN_ENABLE, false);
                mDeviceSupport.onEnableHeartRateSleepSupport(enable);
                break;
            }
            case ACTION_ENABLE_REALTIME_HEARTRATE_MEASUREMENT: {
                boolean enable = intent.getBooleanExtra(EXTRA_BOOLEAN_ENABLE, false);
                mDeviceSupport.onEnableRealtimeHeartRateMeasurement(enable);
                break;
            }
            case ACTION_SEND_CONFIGURATION: {
                String config = intent.getStringExtra(EXTRA_CONFIG);
                mDeviceSupport.onSendConfiguration(config);
                break;
            }
            case ACTION_TEST_NEW_FUNCTION: {
                mDeviceSupport.onTestNewFunction();
                break;
            }

        }

        return START_STICKY;
    }

    /**
     * Disposes the current DeviceSupport instance (if any) and sets a new device support instance
     * (if not null).
     *
     * @param deviceSupport
     */
    private void setDeviceSupport(@Nullable DeviceSupport deviceSupport) {
        if (deviceSupport != mDeviceSupport && mDeviceSupport != null) {
            mDeviceSupport.dispose();
            mDeviceSupport = null;
            mGBDevice = null;
        }
        mDeviceSupport = deviceSupport;
        mGBDevice = mDeviceSupport != null ? mDeviceSupport.getDevice() : null;
    }

    private void start() {
        if (!mStarted) {
            startForeground(GB.NOTIFICATION_ID, GB.createNotification(getString(R.string.wearablehrmonitor_running), false, this));
            mStarted = true;
        }
    }

    public boolean isStarted() {
        return mStarted;
    }

    private boolean isConnected() {
        return mGBDevice != null && mGBDevice.isConnected();
    }

    private boolean isConnecting() {
        return mGBDevice != null && mGBDevice.isConnecting();
    }

    private boolean isInitialized() {
        return mGBDevice != null && mGBDevice.isInitialized();
    }


    private void setReceiversEnableState(boolean enable, boolean initialized, DeviceCoordinator coordinator) {
      //  LOG.info("Setting broadcast receivers to: " + enable);

        if (enable) {
            if (mBlueToothConnectReceiver == null) {
                mBlueToothConnectReceiver = new BluetoothConnectReceiver(this);
                registerReceiver(mBlueToothConnectReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            }
            if (mBlueToothPairingRequestReceiver == null) {
                mBlueToothPairingRequestReceiver = new BluetoothPairingRequestReceiver(this);
                registerReceiver(mBlueToothPairingRequestReceiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
            }
        } else {
            if (mBlueToothConnectReceiver != null) {
                unregisterReceiver(mBlueToothConnectReceiver);
                mBlueToothConnectReceiver = null;
            }

            if (mBlueToothPairingRequestReceiver != null) {
                unregisterReceiver(mBlueToothPairingRequestReceiver);
                mBlueToothPairingRequestReceiver = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        if (hasPrefs()) {
            getPrefs().getPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

      //  LOG.debug("DeviceCommunicationService is being destroyed");
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        setReceiversEnableState(false, false, null); // disable BroadcastReceivers

        setDeviceSupport(null);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(GB.NOTIFICATION_ID); // need to do this because the updated notification won't be cancelled when service stops
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (GBPrefs.AUTO_RECONNECT.equals(key)) {
            boolean autoReconnect = getGBPrefs().getAutoReconnect();
            if (mDeviceSupport != null) {
                mDeviceSupport.setAutoReconnect(autoReconnect);
            }
        }
    }

    protected boolean hasPrefs() {
        return getPrefs().getPreferences() != null;
    }

    public Prefs getPrefs() {
        return GBApplication.getPrefs();
    }

    public GBPrefs getGBPrefs() {
        return GBApplication.getGBPrefs();
    }

    public GBDevice getGBDevice() {
        return mGBDevice;
    }

    private void addDeviceToRemoteDb(JSONObject device){
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
            (Request.Method.POST, Constants.URL.concat(Constants.DEVICE_API), device, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    int i = 0;
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
        VolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

}
