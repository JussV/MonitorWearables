/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, ivanovlev,
    JohnnySun, Julien Pivotto, Kasha, Steffen Liebergeld

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
package smartlife.monitorwearables.model;

import android.support.annotation.Nullable;

import smartlife.monitorwearables.devices.EventHandler;
import smartlife.monitorwearables.impl.GBDevice;

/**
 *
 */
public interface DeviceService extends EventHandler {
    String PREFIX = "nodomain.freeyourgadget.gadgetbridge.devices";

    String ACTION_MIBAND2_AUTH = PREFIX + ".action.miban2_auth";
    String ACTION_START = PREFIX + ".action.start";
    String ACTION_CONNECT = PREFIX + ".action.connect";
    String ACTION_NOTIFICATION = PREFIX + ".action.notification";
    String ACTION_DELETE_NOTIFICATION = PREFIX + ".action.delete_notification";
    String ACTION_SETTIME = PREFIX + ".action.settime";
    String ACTION_REQUEST_DEVICEINFO = PREFIX + ".action.request_deviceinfo";
    String ACTION_INSTALL = PREFIX + ".action.install";
    String ACTION_REBOOT = PREFIX + ".action.reboot";
    String ACTION_HEARTRATE_TEST = PREFIX + ".action.heartrate_test";
    String ACTION_FETCH_ACTIVITY_DATA = PREFIX + ".action.fetch_activity_data";
    String ACTION_DISCONNECT = PREFIX + ".action.disconnect";
    String ACTION_FIND_DEVICE = PREFIX + ".action.find_device";
    String ACTION_SET_CONSTANT_VIBRATION = PREFIX + ".action.set_constant_vibration";
    String ACTION_ENABLE_REALTIME_STEPS = PREFIX + ".action.enable_realtime_steps";
    String ACTION_REALTIME_SAMPLES = PREFIX + ".action.realtime_samples";
    String ACTION_ENABLE_REALTIME_HEARTRATE_MEASUREMENT = PREFIX + ".action.realtime_hr_measurement";
    String ACTION_ENABLE_HEARTRATE_SLEEP_SUPPORT = PREFIX + ".action.enable_heartrate_sleep_support";
    String ACTION_HEARTRATE_MEASUREMENT = PREFIX + ".action.hr_measurement";
    String ACTION_SEND_CONFIGURATION = PREFIX + ".action.send_configuration";
    String ACTION_TEST_NEW_FUNCTION = PREFIX + ".action.test_new_function";
    String EXTRA_NOTIFICATION_BODY = "notification_body";
    String EXTRA_NOTIFICATION_FLAGS = "notification_flags";
    String EXTRA_NOTIFICATION_ID = "notification_id";
    String EXTRA_NOTIFICATION_PHONENUMBER = "notification_phonenumber";
    String EXTRA_NOTIFICATION_SENDER = "notification_sender";
    String EXTRA_NOTIFICATION_SOURCENAME = "notification_sourcename";
    String EXTRA_NOTIFICATION_SUBJECT = "notification_subject";
    String EXTRA_NOTIFICATION_TITLE = "notification_title";
    String EXTRA_NOTIFICATION_TYPE = "notification_type";
    String EXTRA_FIND_START = "find_start";
    String EXTRA_VIBRATION_INTENSITY = "vibration_intensity";
    String EXTRA_URI = "uri";
    String EXTRA_CONFIG = "config";
    String EXTRA_CONNECT_FIRST_TIME = "connect_first_time";
    String EXTRA_BOOLEAN_ENABLE = "enable_realtime_steps";

    /**
     * Use EXTRA_REALTIME_SAMPLE instead
     */
    @Deprecated
    String EXTRA_REALTIME_STEPS = "realtime_steps";
    String EXTRA_REALTIME_SAMPLE = "realtime_sample";
    String EXTRA_TIMESTAMP = "timestamp";
    /**
     * Use EXTRA_REALTIME_SAMPLE instead
     */
    @Deprecated
    String EXTRA_HEART_RATE_VALUE = "hr_value";

    void start();

    void connect();

    void connect(@Nullable GBDevice device);

    void connect(@Nullable GBDevice device, boolean performPair);

    void disconnect();

    void quit();

    /**
     * Requests information from the DeviceCommunicationService about the connection state,
     * firmware info, etc.
     * <p/>
     * Note that this will not need a connection to the device -- only the cached information
     * from the service will be reported.
     */
    void requestDeviceInfo();
}
