/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Julien Pivotto, Kasha, Steffen Liebergeld, Uwe Hermann

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
package smartlife.monitorwearables.devices;

import android.net.Uri;

import java.util.ArrayList;
import java.util.UUID;

import smartlife.monitorwearables.model.NotificationSpec;

/**
 * Specifies all events that Gadgetbridge intends to send to the gadget device.
 * Implementations can decide to ignore events that they do not support.
 * Implementations need to send/encode event to the connected device.
 */
public interface EventHandler {
    void onNotification(NotificationSpec notificationSpec);

    void onDeleteNotification(int id);

    void onSetTime();

    void onEnableRealtimeSteps(boolean enable);

    void onInstallApp(Uri uri);

    void onFetchActivityData();

    void onReboot();

    void onHeartRateTest();

    void onEnableRealtimeHeartRateMeasurement(boolean enable);

    void onFindDevice(boolean start);

    void onSetConstantVibration(int integer);

    void onEnableHeartRateSleepSupport(boolean enable);


    /**
     * Sets the given option in the device, typically with values from the preferences.
     * The config name is device specific.
     * @param config the device specific option to set on the device
     */
    void onSendConfiguration(String config);

    void onTestNewFunction();

}
