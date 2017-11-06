/*  Copyright (C) 2016-2017 Carsten Pfeiffer, JohnnySun, Uwe Hermann

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static smartlife.monitorwearables.service.btle.AbstractBTLEDeviceSupport.BASE_UUID;

public class MiBand2Service {


    public static final UUID UUID_SERVICE_MIBAND_SERVICE = UUID.fromString(String.format(BASE_UUID, "FEE0"));
    public static final UUID UUID_SERVICE_MIBAND2_SERVICE = UUID.fromString(String.format(BASE_UUID, "FEE1"));
    public static final UUID UUID_SERVICE_HEART_RATE = UUID.fromString(String.format(BASE_UUID, "180D"));
    public static final UUID UUID_SERVICE_PULSE_OXIMETER_SERVICE =  UUID.fromString(String.format(BASE_UUID, "1822"));
    public static final UUID UUID_SERVICE_FIRMWARE_SERVICE = UUID.fromString("00001530-0000-3512-2118-0009af100700");

    public static final UUID UUID_CHARACTERISTIC_FIRMWARE = UUID.fromString("00001531-0000-3512-2118-0009af100700");
    public static final UUID UUID_CHARACTERISTIC_FIRMWARE_DATA = UUID.fromString("00001532-0000-3512-2118-0009af100700");

    public static final UUID UUID_UNKNOWN_CHARACTERISTIC0 = UUID.fromString("00000000-0000-3512-2118-0009af100700");
    public static final UUID UUID_UNKNOWN_CHARACTERISTIC1 = UUID.fromString("00000001-0000-3512-2118-0009af100700");
    public static final UUID UUID_UNKNOWN_CHARACTERISTIC2 = UUID.fromString("00000002-0000-3512-2118-0009af100700");
    /**
     * Alarms, Display and other configuration.
     */
    public static final UUID UUID_CHARACTERISTIC_3_CONFIGURATION = UUID.fromString("00000003-0000-3512-2118-0009af100700");
    public static final UUID UUID_UNKNOWN_CHARACTERISTIC4 = UUID.fromString("00000004-0000-3512-2118-0009af100700");
    public static final UUID UUID_CHARACTERISTIC_5_ACTIVITY_DATA = UUID.fromString("00000005-0000-3512-2118-0009af100700");
    public static final UUID UUID_CHARACTERISTIC_6_BATTERY_INFO = UUID.fromString("00000006-0000-3512-2118-0009af100700");
    public static final UUID UUID_CHARACTERISTIC_7_REALTIME_STEPS = UUID.fromString("00000007-0000-3512-2118-0009af100700");
    public static final UUID UUID_UNKNOWN_CHARACTERISTIC8 = UUID.fromString("00000008-0000-3512-2118-0009af100700");
    // service uuid fee1
    public static final UUID UUID_CHARACTERISTIC_AUTH = UUID.fromString("00000009-0000-3512-2118-0009af100700");
    public static final UUID UUID_CHARACTERISTIC_10_BUTTON = UUID.fromString("00000010-0000-3512-2118-0009af100700");

    private static final Map<UUID, String> MIBAND_DEBUG;

    /**
     * Mi Band 2 authentication has three steps.
     * This is step 1: sending a "secret" key to the band.
     * This is byte 0, followed by {@link #AUTH_BYTE} and then the key.
     * In the response, it is byte 1 in the byte[] value.
     */
    public static final byte AUTH_SEND_KEY = 0x01;
    /**
     * Mi Band 2 authentication has three steps.
     * This is step 2: requesting a random authentication key from the band.
     * This is byte 0, followed by {@link #AUTH_BYTE}.
     * In the response, it is byte 1 in the byte[] value.
     */
    public static final byte AUTH_REQUEST_RANDOM_AUTH_NUMBER = 0x02;
    /**
     * Mi Band 2 authentication has three steps.
     * This is step 3: sending the encrypted random authentication key to the band.
     * This is byte 0, followed by {@link #AUTH_BYTE} and then the encrypted random authentication key.
     * In the response, it is byte 1 in the byte[] value.
     */
    public static final byte AUTH_SEND_ENCRYPTED_AUTH_NUMBER = 0x03;

    /**
     * Received in response to any authentication requests (byte 0 in the byte[] value.
     */
    public static final byte AUTH_RESPONSE = 0x10;
    /**
     * Received in response to any authentication requests (byte 2 in the byte[] value.
     * 0x01 means success.
     */
    public static final byte AUTH_SUCCESS = 0x01;
    /**
     * Received in response to any authentication requests (byte 2 in the byte[] value.
     * 0x04 means failure.
     */
    public static final byte AUTH_FAIL = 0x04;
    /**
     * In some logs it's 0x0...
     */
    public static final byte AUTH_BYTE = 0x8;



    public static byte ENDPOINT_DISPLAY_ITEMS = 0x0a;

    public static byte DISPLAY_ITEM_BIT_CLOCK = 0x01;
    public static byte DISPLAY_ITEM_BIT_STEPS = 0x02;
    public static byte DISPLAY_ITEM_BIT_DISTANCE = 0x04;
    public static byte DISPLAY_ITEM_BIT_CALORIES= 0x08;
    public static byte DISPLAY_ITEM_BIT_HEART_RATE = 0x10;
    public static byte DISPLAY_ITEM_BIT_BATTERY = 0x20;

    // Second byte must be a bitwise OR combination of the above
    // The clock can't be disabled
    public static int SCREEN_CHANGE_BYTE = 1;
    public static final byte[] COMMAND_CHANGE_SCREENS = new byte[]{ENDPOINT_DISPLAY_ITEMS, DISPLAY_ITEM_BIT_CLOCK, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05};

    public static byte ENDPOINT_DISPLAY = 0x06;

    public static final byte[] DATEFORMAT_DATE_TIME = new byte[] {ENDPOINT_DISPLAY, 0x0a, 0x0, 0x03 };
    public static final byte[] DATEFORMAT_TIME = new byte[] {ENDPOINT_DISPLAY, 0x0a, 0x0, 0x0 };
    public static final byte[] DATEFORMAT_TIME_12_HOURS = new byte[] {ENDPOINT_DISPLAY, 0x02, 0x0, 0x0 };
    public static final byte[] DATEFORMAT_TIME_24_HOURS = new byte[] {ENDPOINT_DISPLAY, 0x02, 0x0, 0x1 };
    public static final byte[] COMMAND_ENABLE_DISPLAY_ON_LIFT_WRIST = new byte[]{ENDPOINT_DISPLAY, 0x05, 0x00, 0x01};
    public static final byte[] COMMAND_DISABLE_DISPLAY_ON_LIFT_WRIST = new byte[]{ENDPOINT_DISPLAY, 0x05, 0x00, 0x00};
    public static final byte[] COMMAND_ENABLE_GOAL_NOTIFICATION = new byte[]{ENDPOINT_DISPLAY, 0x06, 0x00, 0x01};
    public static final byte[] COMMAND_DISABLE_GOAL_NOTIFICATION = new byte[]{ENDPOINT_DISPLAY, 0x06, 0x00, 0x00};
    public static final byte[] COMMAND_ENABLE_ROTATE_WRIST_TO_SWITCH_INFO = new byte[]{ENDPOINT_DISPLAY, 0x0d, 0x00, 0x01};
    public static final byte[] COMMAND_DISABLE_ROTATE_WRIST_TO_SWITCH_INFO = new byte[]{ENDPOINT_DISPLAY, 0x0d, 0x00, 0x00};
    public static final byte COMMAND_FIRMWARE_REBOOT = 0x05; // to UUID_CHARACTERISTIC_FIRMWARE
    public static final byte[] WEAR_LOCATION_LEFT_WRIST = new byte[] { 0x20, 0x00, 0x00, 0x02 };
    public static final byte[] WEAR_LOCATION_RIGHT_WRIST = new byte[] { 0x20, 0x00, 0x00, (byte) 0x82};

    public static final byte[] COMMAND_ENABLE_HR_SLEEP_MEASUREMENT = new byte[]{0x15, 0x00, 0x01};
    public static final byte[] COMMAND_DISABLE_HR_SLEEP_MEASUREMENT = new byte[]{0x15, 0x00, 0x00};


    static {
        MIBAND_DEBUG = new HashMap<>();
        MIBAND_DEBUG.put(UUID_SERVICE_MIBAND_SERVICE, "MiBand Service");
        MIBAND_DEBUG.put(UUID_SERVICE_HEART_RATE, "MiBand HR Service");
        MIBAND_DEBUG.put(UUID_SERVICE_PULSE_OXIMETER_SERVICE, "Mi Band Pulse Service");
    }

}
