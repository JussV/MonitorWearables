/*  Copyright (C) 2016-2017 Carsten Pfeiffer

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
package smartlife.monitorwearables.devices.wear;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.devices.miband.MiBand2Service;
import smartlife.monitorwearables.devices.miband.MiBandConst;
import smartlife.monitorwearables.devices.miband.MiBandCoordinator;
import smartlife.monitorwearables.devices.miband.MiBandPairingActivity;
import smartlife.monitorwearables.devices.miband.MiBandService;
import smartlife.monitorwearables.impl.GBDevice;
import smartlife.monitorwearables.impl.GBDeviceCandidate;
import smartlife.monitorwearables.model.DeviceType;
import smartlife.monitorwearables.service.AbstractDeviceCoordinator;
import smartlife.monitorwearables.util.Prefs;

public class AndroidWearCoordinator extends AbstractDeviceCoordinator {

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.ANDROIDWEAR;
    }

    @NonNull
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Collection<? extends ScanFilter> createBLEScanFilters() {
       return null;
    }

    @Override
    public Class<? extends Activity> getPairingActivity() {
        return null;
    }

    @Override
    public boolean supports(GBDevice device) {
        return getDeviceType().equals(device.getType());
    }

    @Override
    public final boolean supports(GBDeviceCandidate candidate) {
        return getSupportedType(candidate).isSupported();
    }

    @Override
    public boolean supportsScreenshots() {
        return false;
    }

    @Override
    public Class<? extends Activity> getPrimaryActivity() {
        return null;
    }

    @Override
    public String getManufacturer() {
        return "Motorola";
    }

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
            return DeviceType.ANDROIDWEAR;
    }

    @Override
    public boolean supportsHeartRateMeasurement(GBDevice device) {
        return true;
    }

    @Override
    public boolean supportsAlarmConfiguration() {
        return true;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return true;
    }

    @Override
    public boolean supportsSmartWakeup(GBDevice device) {
        return false;
    }

    @Override
    public boolean supportsActivityTracking() {
        return true;
    }

    @Override
    public Class<? extends Activity> getAppsManagementActivity() {
        return null;
    }

    @Override
    public void deleteDevice(final GBDevice gbDevice) throws Exception {
        if (gbDevice.isConnected() || gbDevice.isConnecting()) {
            GBApplication.deviceService().disconnect();
        }
    }

    @Override
    public boolean supportsAppsManagement() {
        return false;
    }

    @Override
    public boolean supportsCalendarEvents() {
        return true;
    }

    @Override
    public boolean supportsRealtimeData() {
        return true;
    }
}
