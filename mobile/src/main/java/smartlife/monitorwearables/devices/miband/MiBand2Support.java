/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Christian
    Fischer, Daniele Gobbetti, JohnnySun, Julien Pivotto, Kasha, Sergey Trofimov,
    Steffen Liebergeld

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
package smartlife.monitorwearables.devices.miband;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import smartlife.monitorwearables.devices.miband.operations.InitOperation;
import smartlife.monitorwearables.impl.GBDevice;
import smartlife.monitorwearables.model.DeviceService;
import smartlife.monitorwearables.model.DeviceType;
import smartlife.monitorwearables.model.NotificationSpec;
import smartlife.monitorwearables.service.BLETypeConversions;
import smartlife.monitorwearables.service.HeartRateService;
import smartlife.monitorwearables.service.btle.AbstractBTLEDeviceSupport;
import smartlife.monitorwearables.service.btle.GattCharacteristic;
import smartlife.monitorwearables.service.btle.GattService;
import smartlife.monitorwearables.service.btle.TransactionBuilder;
import smartlife.monitorwearables.service.btle.actions.AbortTransactionAction;
import smartlife.monitorwearables.service.btle.actions.SetDeviceStateAction;
import smartlife.monitorwearables.service.profiles.HeartRateProfile;
import smartlife.monitorwearables.service.profiles.deviceinfo.DeviceInfoProfile;
import smartlife.monitorwearables.util.GB;

public class MiBand2Support extends AbstractBTLEDeviceSupport {

    //private static final Logger LOG = LoggerFactory.getLogger(MiBand2Support.class);
    private final DeviceInfoProfile<MiBand2Support> deviceInfoProfile;
    private final HeartRateProfile<MiBand2Support> heartRateProfile;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
           /* if (s.equals(DeviceInfoProfile.ACTION_DEVICE_INFO)) {
                handleDeviceInfo((smartlife.monitorwearables.service.btle.profiles.deviceinfo.DeviceInfo) intent.getParcelableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO));
            }*/
        }
    };

    private boolean needsAuth;
    private volatile boolean isLocatingDevice;

 //   private final GBDeviceEventVersionInfo versionCmd = new GBDeviceEventVersionInfo();
 //   private final GBDeviceEventBatteryInfo batteryCmd = new GBDeviceEventBatteryInfo();
 //   private RealtimeSamplesSupport realtimeSamplesSupport;
    private boolean alarmClockRinging;

    public MiBand2Support() {
        //super(LOG);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(GattService.UUID_SERVICE_HEART_RATE);
        addSupportedService(GattService.UUID_SERVICE_PULSE_OXIMETER);
        addSupportedService(GattService.UUID_SERVICE_IMMEDIATE_ALERT);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        addSupportedService(GattService.UUID_SERVICE_ALERT_NOTIFICATION);
        addSupportedService(MiBandService.UUID_SERVICE_MIBAND_SERVICE);
        addSupportedService(MiBandService.UUID_SERVICE_MIBAND2_SERVICE);
        addSupportedService(MiBand2Service.UUID_SERVICE_FIRMWARE_SERVICE);

        deviceInfoProfile = new DeviceInfoProfile<>(this);
        addSupportedProfile(deviceInfoProfile);
        heartRateProfile = new HeartRateProfile<>(this);
        addSupportedProfile(heartRateProfile);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DeviceInfoProfile.ACTION_DEVICE_INFO);
        intentFilter.addAction(DeviceService.ACTION_MIBAND2_AUTH);
        broadcastManager.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {

      /*  int alertLevel = MiBand2Service.ALERT_LEVEL_MESSAGE;
        if (notificationSpec.type == NotificationType.UNKNOWN) {
            alertLevel = MiBand2Service.ALERT_LEVEL_VIBRATE_ONLY;
        }
        String message = NotificationUtils.getPreferredTextFor(notificationSpec, 40, 40, getContext()).trim();
        String origin = notificationSpec.type.getGenericType();
        SimpleNotification simpleNotification = new SimpleNotification(message, BLETypeConversions.toAlertCategory(notificationSpec.type));
        performPreferredNotification(origin + " received", origin, simpleNotification, alertLevel, null);*/
    }

    @Override
    public void dispose() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        broadcastManager.unregisterReceiver(mReceiver);
        super.dispose();
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        try {
            boolean authenticate = needsAuth;
            needsAuth = false;
            new InitOperation(authenticate, this, builder).perform();
        } catch (IOException e) {
            GB.toast(getContext(), "Initializing Mi Band 2 failed", Toast.LENGTH_SHORT, GB.ERROR, e);
        }
        return builder;
    }

    public byte[] getTimeBytes(Calendar calendar, TimeUnit precision) {
        byte[] bytes;
        if (precision == TimeUnit.MINUTES) {
            bytes = BLETypeConversions.shortCalendarToRawBytes(calendar, true);
        } else if (precision == TimeUnit.SECONDS) {
            bytes = BLETypeConversions.calendarToRawBytes(calendar, true);
        } else {
            throw new IllegalArgumentException("Unsupported precision, only MINUTES and SECONDS are supported till now");
        }
        byte[] tail = new byte[] { 0, BLETypeConversions.mapTimeZone(calendar.getTimeZone()) }; // 0 = adjust reason bitflags? or DST offset?? , timezone
//        byte[] tail = new byte[] { 0x2 }; // reason
        byte[] all = BLETypeConversions.join(bytes, tail);
        return all;
    }

    public MiBand2Support setCurrentTimeWithService(TransactionBuilder builder) {
        GregorianCalendar now = BLETypeConversions.createCalendar();
        byte[] bytes = getTimeBytes(now, TimeUnit.SECONDS);
        builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_CURRENT_TIME), bytes);

//        byte[] localtime = BLETypeConversions.calendarToLocalTimeBytes(now);
//        builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_LOCAL_TIME_INFORMATION), localtime);
//        builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_CURRENT_TIME), new byte[] {0x2, 0x00});
//        builder.write(getCharacteristic(MiBand2Service.UUID_UNKNOQN_CHARACTERISTIC0), new byte[] {0x03,0x00,(byte)0x8e,(byte)0xce,0x5a,0x09,(byte)0xb3,(byte)0xd8,0x55,0x57,0x10,0x2a,(byte)0xed,0x7d,0x6b,0x78,(byte)0xc5,(byte)0xd2});
        return this;
    }

    public MiBand2Support setLowLatency(TransactionBuilder builder) {
        // TODO: low latency?
        return this;
    }

    public MiBand2Support setHighLatency(TransactionBuilder builder) {
        // TODO: high latency?
        return this;
    }

    /**
     * Last action of initialization sequence. Sets the device to initialized.
     * It is only invoked if all other actions were successfully run, so the device
     * must be initialized, then.
     *
     * @param builder
     */
    public void setInitialized(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
    }

    // MB2: AVL
    // TODO: tear down the notifications on quit
    public MiBand2Support enableNotifications(TransactionBuilder builder, boolean enable) {
         builder.notify(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_NOTIFICATION), enable);
        builder.notify(getCharacteristic(GattService.UUID_SERVICE_CURRENT_TIME), enable);
        // Notify CHARACTERISTIC9 to receive random auth code
        builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_AUTH), enable);
        return this;
    }

    public MiBand2Support enableFurtherNotifications(TransactionBuilder builder, boolean enable) {
        builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), enable);
        builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_6_BATTERY_INFO), enable);
        builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_10_BUTTON), enable);
        BluetoothGattCharacteristic heartrateCharacteristic = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT);
        if (heartrateCharacteristic != null) {
            builder.notify(heartrateCharacteristic, enable);
        }

        return this;
    }

    @Override
    public boolean useAutoConnect() {
        return true;
    }

    @Override
    public boolean connectFirstTime() {
        needsAuth = true;
        return super.connect();
    }

    private static final byte[] startHeartMeasurementManual = new byte[]{0x15, MiBandService.COMMAND_SET_HR_MANUAL, 1};
    private static final byte[] stopHeartMeasurementManual = new byte[]{0x15, MiBandService.COMMAND_SET_HR_MANUAL, 0};
    private static final byte[] startHeartMeasurementContinuous = new byte[]{0x15, MiBandService.COMMAND_SET__HR_CONTINUOUS, 1};
    private static final byte[] stopHeartMeasurementContinuous = new byte[]{0x15, MiBandService.COMMAND_SET__HR_CONTINUOUS, 0};

    private MiBand2Support requestBatteryInfo(TransactionBuilder builder) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_6_BATTERY_INFO);
        builder.read(characteristic);
        return this;
    }

    public MiBand2Support requestDeviceInfo(TransactionBuilder builder) {
        deviceInfoProfile.requestDeviceInfo(builder);
        return this;
    }

    /**
     * Part of device initialization process. Do not call manually.
     *
     * @param builder
     * @return
     */
    private MiBand2Support setWearLocation(TransactionBuilder builder) {
      //  LOG.info("Attempting to set wear location...");
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBand2Service.UUID_UNKNOWN_CHARACTERISTIC8);
        if (characteristic != null) {
            builder.notify(characteristic, true);
            int location = MiBandCoordinator.getWearLocation(getDevice().getAddress());
            switch (location) {
                case 0: // left hand
                    builder.write(characteristic, MiBand2Service.WEAR_LOCATION_LEFT_WRIST);
                    break;
                case 1: // right hand
                    builder.write(characteristic, MiBand2Service.WEAR_LOCATION_RIGHT_WRIST);
                    break;
            }
            builder.notify(characteristic, false); // TODO: this should actually be in some kind of finally-block in the queue. It should also be sent asynchronously after the notifications have completely arrived and processed.
        }
        return this;
    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
        try {
            TransactionBuilder builder = performInitialized("enable heart rate sleep support: " + enable);
            setHeartrateSleepSupport(builder);
            builder.queue(getQueue());
        } catch (IOException e) {
            GB.toast(getContext(), "Error toggling heart rate sleep support: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
        }
    }

    /**
     * Part of device initialization process. Do not call manually.
     *
     * @param builder
     */
    private MiBand2Support setHeartrateSleepSupport(TransactionBuilder builder) {
        BluetoothGattCharacteristic characteristicHRControlPoint = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT);
        final boolean enableHrSleepSupport = MiBandCoordinator.getHeartrateSleepSupport(getDevice().getAddress());
        if (characteristicHRControlPoint != null) {
            builder.notify(characteristicHRControlPoint, true);
            if (enableHrSleepSupport) {
           //     LOG.info("Enabling heartrate sleep support...");
                builder.write(characteristicHRControlPoint, MiBand2Service.COMMAND_ENABLE_HR_SLEEP_MEASUREMENT);
            } else {
            //    LOG.info("Disabling heartrate sleep support...");
                builder.write(characteristicHRControlPoint, MiBand2Service.COMMAND_DISABLE_HR_SLEEP_MEASUREMENT);
            }
            builder.notify(characteristicHRControlPoint, false); // TODO: this should actually be in some kind of finally-block in the queue. It should also be sent asynchronously after the notifications have completely arrived and processed.
        }
        return this;
    }

    @Override
    public void onDeleteNotification(int id) {
        alarmClockRinging = false; // we should have the notificationtype at least to check
    }

    @Override
    public void onSetTime() {
        try {
            TransactionBuilder builder = performInitialized("Set date and time");
            setCurrentTimeWithService(builder);
            //TODO: once we have a common strategy for sending events (e.g. EventHandler), remove this call from here. Meanwhile it does no harm.
          //  sendCalendarEvents(builder);
            builder.queue(getQueue());
        } catch (IOException ex) {
         //  LOG.error("Unable to set time on MI device", ex);
        }
    }


    @Override
    public void onReboot() {
        try {
            TransactionBuilder builder = performInitialized("Reboot");
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_FIRMWARE), new byte[] { MiBand2Service.COMMAND_FIRMWARE_REBOOT});
            builder.queue(getQueue());
        } catch (IOException ex) {
          //  LOG.error("Unable to reboot MI", ex);
        }
    }

    @Override
    public void onHeartRateTest() {
        try {
            TransactionBuilder builder = performInitialized("HeartRateTest");
         /*   builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementContinuous);
            builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementManual);*/
         //   builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), startHeartMeasurementContinuous);
            builder.notify(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), true);
            builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), startHeartMeasurementManual);
            builder.queue(getQueue());
        } catch (IOException ex) {
           // LOG.error("Unable to read HearRate with MI2", ex);
        }
    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {
        try {
            TransactionBuilder builder = performInitialized("Enable realtime heart rate measurement");
            if (enable) {
                builder.notify(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), true);
                builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementManual);
                builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), startHeartMeasurementContinuous);
               // builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), new byte[] {0x15, 0x00, 0x01});
                builder.notify(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), true);
            } else {
                builder.write(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementContinuous);
            }
            builder.queue(getQueue());
            //enableRealtimeSamplesTimer(enable);
        } catch (IOException ex) {
         //   LOG.error("Unable to enable realtime heart rate measurement in  MI1S", ex);
        }
    }

    @Override
    public void onFindDevice(boolean start) {
        isLocatingDevice = start;

        if (start) {
            AbortTransactionAction abortAction = new AbortTransactionAction() {
                @Override
                protected boolean shouldAbort() {
                    return !isLocatingDevice;
                }
            };
         /*   SimpleNotification simpleNotification = new SimpleNotification(getContext().getString(R.string.find_device_you_found_it), AlertCategory.HighPriorityAlert);
            performDefaultNotification("locating device", simpleNotification, (short) 255, abortAction);*/
        }
    }

    @Override
    public void onSetConstantVibration(int intensity) {

    }

    @Override
    public void onFetchActivityData() {
      /*  try {
            new FetchActivityOperation(this).perform();
        } catch (IOException ex) {
            LOG.error("Unable to fetch MI activity data", ex);
        }*/
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
        try {
            TransactionBuilder builder = performInitialized(enable ? "Enabling realtime steps notifications" : "Disabling realtime steps notifications");
            if (enable) {
                builder.read(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_7_REALTIME_STEPS));
            }
            builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_7_REALTIME_STEPS), enable);
            builder.queue(getQueue());
            enableRealtimeSamplesTimer(enable);
        } catch (IOException e) {
           // LOG.error("Unable to change realtime steps notification to: " + enable, e);
        }
    }

    private byte[] getHighLatency() {
        int minConnectionInterval = 460;
        int maxConnectionInterval = 500;
        int latency = 0;
        int timeout = 500;
        int advertisementInterval = 0;

        return getLatency(minConnectionInterval, maxConnectionInterval, latency, timeout, advertisementInterval);
    }

    private byte[] getLatency(int minConnectionInterval, int maxConnectionInterval, int latency, int timeout, int advertisementInterval) {
        byte result[] = new byte[12];
        result[0] = (byte) (minConnectionInterval & 0xff);
        result[1] = (byte) (0xff & minConnectionInterval >> 8);
        result[2] = (byte) (maxConnectionInterval & 0xff);
        result[3] = (byte) (0xff & maxConnectionInterval >> 8);
        result[4] = (byte) (latency & 0xff);
        result[5] = (byte) (0xff & latency >> 8);
        result[6] = (byte) (timeout & 0xff);
        result[7] = (byte) (0xff & timeout >> 8);
        result[8] = 0;
        result[9] = 0;
        result[10] = (byte) (advertisementInterval & 0xff);
        result[11] = (byte) (0xff & advertisementInterval >> 8);

        return result;
    }

    private byte[] getLowLatency() {
        int minConnectionInterval = 39;
        int maxConnectionInterval = 49;
        int latency = 0;
        int timeout = 500;
        int advertisementInterval = 0;

        return getLatency(minConnectionInterval, maxConnectionInterval, latency, timeout, advertisementInterval);
    }

    @Override
    public void onInstallApp(Uri uri) {
       /* try {
            new UpdateFirmwareOperation(uri, this).perform();
        } catch (IOException ex) {
            GB.toast(getContext(), "Firmware cannot be installed: " + ex.getMessage(), Toast.LENGTH_LONG, GB.ERROR, ex);
        }*/
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        UUID characteristicUUID = characteristic.getUuid();
        if (MiBand2Service.UUID_CHARACTERISTIC_6_BATTERY_INFO.equals(characteristicUUID)) {
           // handleBatteryInfo(characteristic.getValue(), BluetoothGatt.GATT_SUCCESS);
            return true;
        } else if (MiBandService.UUID_CHARACTERISTIC_REALTIME_STEPS.equals(characteristicUUID)) {
            handleRealtimeSteps(characteristic.getValue());
            return true;
        } else if (GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT.equals(characteristicUUID)) {
            int heartRate = handleHeartrate(characteristic.getValue());
            HeartRateService.startMeasureHeartRate(getContext(), heartRate, DeviceType.MIBAND2.getKey());
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_AUTH.equals(characteristicUUID)) {
      //      LOG.info("AUTHENTICATION?? " + characteristicUUID);
            //logMessageContent(characteristic.getValue());
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_10_BUTTON.equals(characteristicUUID)) {
            handleButtonPressed(characteristic.getValue());
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_7_REALTIME_STEPS.equals(characteristicUUID)) {
            handleRealtimeSteps(characteristic.getValue());
            return true;
        } else {
          //  LOG.info("Unhandled characteristic changed: " + characteristicUUID);
          //  logMessageContent(characteristic.getValue());
        }
        return false;
    }

    private void handleButtonPressed(byte[] value) {
   //     LOG.info("Button pressed");
      //  logMessageContent(value);
    }

    private void handleUnknownCharacteristic(byte[] value) {

    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        UUID characteristicUUID = characteristic.getUuid();
        if (GattCharacteristic.UUID_CHARACTERISTIC_GAP_DEVICE_NAME.equals(characteristicUUID)) {
            handleDeviceName(characteristic.getValue(), status);
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_6_BATTERY_INFO.equals(characteristicUUID)) {
           // handleBatteryInfo(characteristic.getValue(), status);
            return true;
        } else if (GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT.equals(characteristicUUID)) {
            logHeartrate(characteristic.getValue(), status);
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_7_REALTIME_STEPS.equals(characteristicUUID)) {
            handleRealtimeSteps(characteristic.getValue());
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_10_BUTTON.equals(characteristicUUID)) {
            handleButtonPressed(characteristic.getValue());
            return true;
        } else {
         //   LOG.info("Unhandled characteristic read: " + characteristicUUID);
         //   logMessageContent(characteristic.getValue());
        }
        return false;
    }

    @Override
    public boolean onCharacteristicWrite(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
        UUID characteristicUUID = characteristic.getUuid();
        if (MiBand2Service.UUID_CHARACTERISTIC_AUTH.equals(characteristicUUID)) {
        //    LOG.info("KEY AES SEND");
        //    logMessageContent(characteristic.getValue());
            return true;
        }
        return false;
    }

    public void logHeartrate(byte[] value, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS && value != null) {
        //    LOG.info("Got heartrate:");
            if (value.length == 2 && value[0] == 0) {
                int hrValue = (value[1] & 0xff);
                GB.toast(getContext(), "Heart Rate measured: " + hrValue, Toast.LENGTH_LONG, GB.INFO);
            }
            return;
        }
       // logMessageContent(value);
    }

    private int handleHeartrate(byte[] value) {
        int hrValue = 0;
        if (value.length == 2 && value[0] == 0) {
            hrValue = (value[1] & 0xff);

          /*  if (LOG.isDebugEnabled()) {
                LOG.debug("heart rate: " + hrValue);
            }*/
        //    RealtimeSamplesSupport realtimeSamplesSupport = getRealtimeSamplesSupport();
           /* realtimeSamplesSupport.setHeartrateBpm(hrValue);
            if (!realtimeSamplesSupport.isRunning()) {
                // single shot measurement, manually invoke storage and result publishing
                realtimeSamplesSupport.triggerCurrentSample();
            }*/
        }
        return  hrValue;
    }

    private void handleRealtimeSteps(byte[] value) {
        if (value == null) {
     //       LOG.error("realtime steps: value is null");
            return;
        }

        if (value.length == 13) {
            byte[] stepsValue = new byte[] {value[1], value[2]};
           // int steps = BLETypeConversions.toUint16(stepsValue);
            /*if (LOG.isDebugEnabled()) {
                LOG.debug("realtime steps: " + steps);
            }
            getRealtimeSamplesSupport().setSteps(steps);*/
        } else {
            //LOG.warn("Unrecognized realtime steps value: " + Logging.formatBytes(value));
        }
    }

    private void enableRealtimeSamplesTimer(boolean enable) {
     /*   if (enable) {
            getRealtimeSamplesSupport().start();
        } else {
            if (realtimeSamplesSupport != null) {
                realtimeSamplesSupport.stop();
            }
        }*/
    }
/*
    public MiBandActivitySample createActivitySample(Device device, User user, int timestampInSeconds, SampleProvider provider) {
        MiBandActivitySample sample = new MiBandActivitySample();
        sample.setDevice(device);
        sample.setUser(user);
        sample.setTimestamp(timestampInSeconds);
        sample.setProvider(provider);

        return sample;
    }*/

   /* private RealtimeSamplesSupport getRealtimeSamplesSupport() {
        if (realtimeSamplesSupport == null) {
            realtimeSamplesSupport = new RealtimeSamplesSupport(1000, 1000) {
                @Override
                public void doCurrentSample() {

                    try (DBHandler handler = GBApplication.acquireDB()) {
                        DaoSession session = handler.getDaoSession();

                        Device device = DBHelper.getDevice(getDevice(), session);
                        User user = DBHelper.getUser(session);
                        int ts = (int) (System.currentTimeMillis() / 1000);
                        MiBand2SampleProvider provider = new MiBand2SampleProvider(gbDevice, session);
                        MiBandActivitySample sample = createActivitySample(device, user, ts, provider);
                        sample.setHeartRate(getHeartrateBpm());
                        sample.setSteps(getSteps());
                        sample.setRawIntensity(ActivitySample.NOT_MEASURED);
                        sample.setRawKind(MiBand2SampleProvider.TYPE_ACTIVITY); // to make it visible in the charts TODO: add a MANUAL kind for that?

                        provider.addGBActivitySample(sample);

                        // set the steps only afterwards, since realtime steps are also recorded
                        // in the regular samples and we must not count them twice
                        // Note: we know that the DAO sample is never committed again, so we simply
                        // change the value here in memory.
                        sample.setSteps(getSteps());

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("realtime sample: " + sample);
                        }

                        Intent intent = new Intent(DeviceService.ACTION_REALTIME_SAMPLES)
                                .putExtra(DeviceService.EXTRA_REALTIME_SAMPLE, sample);
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                    } catch (Exception e) {
                        LOG.warn("Unable to acquire db for saving realtime samples", e);
                    }
                }
            };
        }
        return realtimeSamplesSupport;
    }*/

    private void handleDeviceName(byte[] value, int status) {
//        if (status == BluetoothGatt.GATT_SUCCESS) {
//            versionCmd.hwVersion = new String(value);
//            handleGBDeviceEvent(versionCmd);
//        }
    }

    @Override
    public void onSendConfiguration(String config) {
        TransactionBuilder builder;
        try {
            builder = performInitialized("Sending configuration for option: " + config);
            switch (config) {
                case MiBandConst.PREF_MI2_GOAL_NOTIFICATION:
                    setGoalNotification(builder);
                    break;
                case MiBandConst.PREF_MI2_ACTIVATE_DISPLAY_ON_LIFT:
                    setActivateDisplayOnLiftWrist(builder);
                    break;
                case MiBandConst.PREF_MI2_DISPLAY_ITEMS:
                    setDisplayItems(builder);
                    break;
                case MiBandConst.PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO:
                    setRotateWristToSwitchInfo(builder);
                    break;
            }
            builder.queue(getQueue());
        } catch (IOException e) {
            GB.toast("Error setting configuration", Toast.LENGTH_LONG, GB.ERROR, e);
        }
    }

    @Override
    public void onTestNewFunction() {
        try {
            TransactionBuilder builder = performInitialized("test realtime steps");
            builder.read(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_7_REALTIME_STEPS));
            builder.queue(getQueue());
        } catch (IOException e) {
        }
    }

   /* private MiBand2Support setDateDisplay(TransactionBuilder builder) {
        DateTimeDisplay dateTimeDisplay = MiBand2Coordinator.getDateDisplay(getContext());
        LOG.info("Setting date display to " + dateTimeDisplay);
        switch (dateTimeDisplay) {
            case TIME:
                builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.DATEFORMAT_TIME);
                break;
            case DATE_TIME:
                builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.DATEFORMAT_DATE_TIME);
                break;
        }
        return this;
    }*/

    private MiBand2Support setTimeFormat(TransactionBuilder builder) {
        boolean is24Format = DateFormat.is24HourFormat(getContext());
     //   LOG.info("Setting 24h time format to " + is24Format);
        if (is24Format) {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.DATEFORMAT_TIME_24_HOURS);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.DATEFORMAT_TIME_12_HOURS);
        }
        return this;
    }

    private MiBand2Support setGoalNotification(TransactionBuilder builder) {
        boolean enable = MiBand2Coordinator.getGoalNotification();
    //    LOG.info("Setting goal notification to " + enable);
        if (enable) {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_ENABLE_GOAL_NOTIFICATION);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DISABLE_GOAL_NOTIFICATION);
        }
        return this;
    }

    private MiBand2Support setActivateDisplayOnLiftWrist(TransactionBuilder builder) {
        boolean enable = MiBand2Coordinator.getActivateDisplayOnLiftWrist();
      //  LOG.info("Setting activate display on lift wrist to " + enable);
        if (enable) {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_ENABLE_DISPLAY_ON_LIFT_WRIST);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DISABLE_DISPLAY_ON_LIFT_WRIST);
        }
        return this;
    }

    private MiBand2Support setDisplayItems(TransactionBuilder builder) {
        Set<String> pages = MiBand2Coordinator.getDisplayItems();
     //   LOG.info("Setting display items to " + (pages == null ? "none" : pages));

        byte[] data = MiBand2Service.COMMAND_CHANGE_SCREENS.clone();

        if (pages != null) {
            if (pages.contains(MiBandConst.PREF_MI2_DISPLAY_ITEM_STEPS)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_STEPS;
            }
            if (pages.contains(MiBandConst.PREF_MI2_DISPLAY_ITEM_DISTANCE)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_DISTANCE;
            }
            if (pages.contains(MiBandConst.PREF_MI2_DISPLAY_ITEM_CALORIES)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_CALORIES;
            }
            if (pages.contains(MiBandConst.PREF_MI2_DISPLAY_ITEM_HEART_RATE)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_HEART_RATE;
            }
            if (pages.contains(MiBandConst.PREF_MI2_DISPLAY_ITEM_BATTERY)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_BATTERY;
            }
        }

        builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), data);
        return this;
    }

    private MiBand2Support setRotateWristToSwitchInfo(TransactionBuilder builder) {
        boolean enable = MiBand2Coordinator.getRotateWristToSwitchInfo();
      //  LOG.info("Setting rotate wrist to cycle info to " + enable);
        if (enable) {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_ENABLE_ROTATE_WRIST_TO_SWITCH_INFO);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DISABLE_ROTATE_WRIST_TO_SWITCH_INFO);
        }
        return this;
    }

    private MiBand2Support requestBodySensorLocation(TransactionBuilder builder) {
        // LOG.debug("Requesting Device Info!");
        BluetoothGattCharacteristic bodySensorLocation = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_BODY_SENSOR_LOCATION);
        builder.read(bodySensorLocation);
   //     builder.notify(bodySensorLocation, true);
        return this;
    }

    public void phase2Initialize(TransactionBuilder builder) {
        requestBodySensorLocation(builder);
        enableFurtherNotifications(builder, true);
        requestBatteryInfo(builder);
        setTimeFormat(builder);
        setWearLocation(builder);
        setDisplayItems(builder);
        setRotateWristToSwitchInfo(builder);
        setActivateDisplayOnLiftWrist(builder);
        setGoalNotification(builder);
        setHeartrateSleepSupport(builder);
    }
}
