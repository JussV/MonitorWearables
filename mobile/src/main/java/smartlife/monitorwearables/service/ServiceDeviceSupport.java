/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Julien Pivotto, Kasha, Steffen Liebergeld

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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.Uri;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;

import smartlife.monitorwearables.impl.GBDevice;
import smartlife.monitorwearables.model.NotificationSpec;


/**
 * Wraps another device support instance and supports busy-checking and throttling of events.
 */
public class ServiceDeviceSupport implements DeviceSupport {

    enum Flags {
        THROTTLING,
        BUSY_CHECKING,
    }

   // private static final Logger LOG = LoggerFactory.getLogger(ServiceDeviceSupport.class);

    private static final long THROTTLING_THRESHOLD = 1000; // throttle multiple events in between one second
    private final DeviceSupport delegate;

    private long lastNotificationTime = 0;
    private String lastNotificationKind;
    private final EnumSet<Flags> flags;

    public ServiceDeviceSupport(DeviceSupport delegate, EnumSet<Flags> flags) {
        this.delegate = delegate;
        this.flags = flags;
    }

    @Override
    public void setContext(GBDevice gbDevice, BluetoothAdapter btAdapter, Context context) {
        delegate.setContext(gbDevice, btAdapter, context);
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public boolean connectFirstTime() {
        return delegate.connectFirstTime();
    }

    @Override
    public boolean connect() {
        return delegate.connect();
    }

    @Override
    public void setAutoReconnect(boolean enable) {
        delegate.setAutoReconnect(enable);
    }

    @Override
    public boolean getAutoReconnect() {
        return delegate.getAutoReconnect();
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }

    @Override
    public GBDevice getDevice() {
        return delegate.getDevice();
    }

    @Override
    public BluetoothAdapter getBluetoothAdapter() {
        return delegate.getBluetoothAdapter();
    }

    @Override
    public Context getContext() {
        return delegate.getContext();
    }

    @Override
    public boolean useAutoConnect() {
        return delegate.useAutoConnect();
    }

    private boolean checkBusy(String notificationKind) {
        if (!flags.contains(Flags.BUSY_CHECKING)) {
            return false;
        }
        if (getDevice().isBusy()) {
            //LOG.info("Ignoring " + notificationKind + " because we're busy with " + getDevice().getBusyTask());
            return true;
        }
        return false;
    }

    private boolean checkThrottle(String notificationKind) {
        if (!flags.contains(Flags.THROTTLING)) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastNotificationTime) < THROTTLING_THRESHOLD) {
            if (notificationKind != null && notificationKind.equals(lastNotificationKind)) {
            //    LOG.info("Ignoring " + notificationKind + " because of throttling threshold reached");
                return true;
            }
        }
        lastNotificationTime = currentTime;
        lastNotificationKind = notificationKind;
        return false;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        if (checkBusy("generic notification") || checkThrottle("generic notification")) {
            return;
        }
        delegate.onNotification(notificationSpec);
    }

    @Override
    public void onDeleteNotification(int id) {
        delegate.onDeleteNotification(id);
    }

    @Override
    public void onSetTime() {
        if (checkBusy("set time") || checkThrottle("set time")) {
            return;
        }
        delegate.onSetTime();
    }

    @Override
    public void onInstallApp(Uri uri) {
        if (checkBusy("install app")) {
            return;
        }
        delegate.onInstallApp(uri);
    }

    @Override
    public void onFetchActivityData() {
        if (checkBusy("fetch activity data")) {
            return;
        }
        delegate.onFetchActivityData();
    }

    @Override
    public void onReboot() {
        if (checkBusy("reboot")) {
            return;
        }
        delegate.onReboot();
    }

    @Override
    public void onHeartRateTest() {
        if (checkBusy("heartrate")) {
            return;
        }
        delegate.onHeartRateTest();
    }

    @Override
    public void onFindDevice(boolean start) {
        if (checkBusy("find device")) {
            return;
        }
        delegate.onFindDevice(start);
    }

    @Override
    public void onSetConstantVibration(int intensity) {
        if (checkBusy("set constant vibration")) {
            return;
        }
        delegate.onSetConstantVibration(intensity);
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
        if (checkBusy("enable realtime steps: " + enable)) {
            return;
        }
        delegate.onEnableRealtimeSteps(enable);
    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
        if (checkBusy("enable heartrate sleep support: " + enable)) {
            return;
        }
        delegate.onEnableHeartRateSleepSupport(enable);
    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {
        if (checkBusy("enable realtime heart rate measurement: " + enable)) {
            return;
        }
        delegate.onEnableRealtimeHeartRateMeasurement(enable);
    }

    @Override
    public void onSendConfiguration(String config) {
        if (checkBusy("send configuration: " + config)) {
            return;
        }
        delegate.onSendConfiguration(config);
    }

    @Override
    public void onTestNewFunction() {
        if (checkBusy("test new function event")) {
            return;
        }
        delegate.onTestNewFunction();
    }

}
