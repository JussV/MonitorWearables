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
package smartlife.monitorwearables.devices.miband;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.impl.GBDevice;
import smartlife.monitorwearables.impl.GBDeviceCandidate;
import smartlife.monitorwearables.model.DeviceType;
import smartlife.monitorwearables.util.Prefs;

public class MiBand2Coordinator extends MiBandCoordinator {

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.MIBAND2;
    }

    @NonNull
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        ParcelUuid mi2Service = new ParcelUuid(MiBandService.UUID_SERVICE_MIBAND2_SERVICE);
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(mi2Service).build();
        return Collections.singletonList(filter);
    }

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        if (candidate.supportsService(MiBand2Service.UUID_SERVICE_MIBAND2_SERVICE)) {
            return DeviceType.MIBAND2;
        }

        // and a heuristic for now
        try {
            BluetoothDevice device = candidate.getDevice();
//            if (isHealthWearable(device)) {
                String name = device.getName();
                if (name != null && name.equalsIgnoreCase(MiBandConst.MI_BAND2_NAME)) {
                    return DeviceType.MIBAND2;
                }
//            }
        } catch (Exception ex) {
      //      LOG.error("unable to check device support", ex);
        }
        return DeviceType.UNKNOWN;

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


    public static boolean getActivateDisplayOnLiftWrist() {
        Prefs prefs = GBApplication.getPrefs();
        return prefs.getBoolean(MiBandConst.PREF_MI2_ACTIVATE_DISPLAY_ON_LIFT, true);
    }

    public static Set<String> getDisplayItems() {
        Prefs prefs = GBApplication.getPrefs();
        return prefs.getStringSet(MiBandConst.PREF_MI2_DISPLAY_ITEMS, null);
    }

    public static boolean getGoalNotification() {
        Prefs prefs = GBApplication.getPrefs();
        return prefs.getBoolean(MiBandConst.PREF_MI2_GOAL_NOTIFICATION, false);
    }

    public static boolean getRotateWristToSwitchInfo() {
        Prefs prefs = GBApplication.getPrefs();
        return prefs.getBoolean(MiBandConst.PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO, false);
    }

    @Override
    public boolean supportsSmartWakeup(GBDevice device) {
        return false;
    }
}
