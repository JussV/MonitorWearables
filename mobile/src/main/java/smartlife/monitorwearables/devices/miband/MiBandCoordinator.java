/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Christian
    Fischer, Daniele Gobbetti, Szymon Tomasz Stefanek

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

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;

import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.entities.DaoSession;
import smartlife.monitorwearables.entities.Device;
import smartlife.monitorwearables.impl.GBDevice;
import smartlife.monitorwearables.impl.GBDeviceCandidate;
import smartlife.monitorwearables.model.DeviceType;
import smartlife.monitorwearables.service.AbstractDeviceCoordinator;
import smartlife.monitorwearables.util.Prefs;


public class MiBandCoordinator extends AbstractDeviceCoordinator {

    public MiBandCoordinator() {}

    @NonNull
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        ParcelUuid mi1Service = new ParcelUuid(MiBandService.UUID_SERVICE_MIBAND_SERVICE);
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(mi1Service).build();
        return Collections.singletonList(filter);
    }

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        String macAddress = candidate.getMacAddress().toUpperCase();
        if (macAddress.startsWith(MiBandService.MAC_ADDRESS_FILTER_1_1A)
                || macAddress.startsWith(MiBandService.MAC_ADDRESS_FILTER_1S)) {
            return DeviceType.MIBAND;
        }
        if (candidate.supportsService(MiBandService.UUID_SERVICE_MIBAND_SERVICE)
                && !candidate.supportsService(MiBandService.UUID_SERVICE_MIBAND2_SERVICE)) {
            return DeviceType.MIBAND;
        }
        // and a heuristic
        try {
            BluetoothDevice device = candidate.getDevice();
            if (isHealthWearable(device)) {
                String name = device.getName();
                if (name != null && name.toUpperCase().startsWith(MiBandConst.MI_GENERAL_NAME_PREFIX.toUpperCase())) {
                    return DeviceType.MIBAND;
                }
            }
        } catch (Exception ex) {
            //LOG.error("unable to check device support", ex);
        }
        return DeviceType.UNKNOWN;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.MIBAND;
    }

    @Override
    public Class<? extends Activity> getPairingActivity() {
        return MiBandPairingActivity.class;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return true;
    }

    @Override
    public boolean supportsScreenshots() {
        return false;
    }

    @Override
    public boolean supportsAlarmConfiguration() {
        return true;
    }

    @Override
    public boolean supportsSmartWakeup(GBDevice device) {
        return true;
    }

    @Override
    public boolean supportsActivityTracking() {
        return true;
    }

    @Override
    public String getManufacturer() {
        return "Xiaomi";
    }

    @Override
    public boolean supportsAppsManagement() {
        return false;
    }

    @Override
    public Class<? extends Activity> getAppsManagementActivity() {
        return null;
    }

    @Override
    public boolean supportsCalendarEvents() {
        return false;
    }

    @Override
    public boolean supportsRealtimeData() {
        return true;
    }

    public static boolean hasValidUserInfo() {
        String dummyMacAddress = MiBandService.MAC_ADDRESS_FILTER_1_1A + ":00:00:00";
        try {
           // UserInfo userInfo = getConfiguredUserInfo(dummyMacAddress);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static int getWearLocation(String miBandAddress) throws IllegalArgumentException {
        int location = 0; //left hand
        Prefs prefs = GBApplication.getPrefs();
        if ("right".equals(prefs.getString(MiBandConst.PREF_MIBAND_WEARSIDE, "left"))) {
            location = 1; // right hand
        }
        return location;
    }

    public static int getDeviceTimeOffsetHours() throws IllegalArgumentException {
        Prefs prefs = GBApplication.getPrefs();
        return prefs.getInt(MiBandConst.PREF_MIBAND_DEVICE_TIME_OFFSET_HOURS, 0);
    }

    public static boolean getHeartrateSleepSupport(String miBandAddress) throws IllegalArgumentException {
        Prefs prefs = GBApplication.getPrefs();
        return prefs.getBoolean(MiBandConst.PREF_MIBAND_USE_HR_FOR_SLEEP_DETECTION, false);
    }

    public static int getReservedAlarmSlots(String miBandAddress) throws IllegalArgumentException {
        Prefs prefs = GBApplication.getPrefs();
        return prefs.getInt(MiBandConst.PREF_MIBAND_RESERVE_ALARM_FOR_CALENDAR, 0);
    }

    @Override
    public boolean supportsHeartRateMeasurement(GBDevice device) {
        String hwVersion = device.getModel();
        return isMi1S(hwVersion) || isMiPro(hwVersion);
    }

    @Override
    public final boolean supports(GBDeviceCandidate candidate) {
        return getSupportedType(candidate).isSupported();
    }

    private boolean isMi1S(String hardwareVersion) {
        return MiBandConst.MI_1S.equals(hardwareVersion);
    }

    private boolean isMiPro(String hardwareVersion) {
        return MiBandConst.MI_PRO.equals(hardwareVersion);
    }

    @Override
    public Class<? extends Activity> getPrimaryActivity() {
        return null;
    }


    @Override
    public void deleteDevice(final GBDevice gbDevice) throws Exception {
      //  LOG.info("will try to delete device: " + gbDevice.getName());
        if (gbDevice.isConnected() || gbDevice.isConnecting()) {
            GBApplication.deviceService().disconnect();
        }
    }
}
