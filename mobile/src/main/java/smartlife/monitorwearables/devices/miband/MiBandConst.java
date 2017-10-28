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

public final class MiBandConst {
    public static final String PREF_MIBAND_WEARSIDE = "mi_wearside";
    public static final String PREF_MIBAND_ADDRESS = "development_miaddr";  // FIXME: should be prefixed mi_
    public static final String PREF_MIBAND_DONT_ACK_TRANSFER = "mi_dont_ack_transfer";
    public static final String PREF_MIBAND_RESERVE_ALARM_FOR_CALENDAR = "mi_reserve_alarm_calendar";
    public static final String PREF_MIBAND_USE_HR_FOR_SLEEP_DETECTION = "mi_hr_sleep_detection";
	public static final String PREF_MIBAND_DEVICE_TIME_OFFSET_HOURS = "mi_device_time_offset_hours";
	public static final String PREF_MI2_DATEFORMAT = "mi2_dateformat";
    public static final String PREF_MI2_GOAL_NOTIFICATION = "mi2_goal_notification";
	public static final String PREF_MI2_DISPLAY_ITEMS = "mi2_display_items";
	public static final String PREF_MI2_DISPLAY_ITEM_STEPS = "steps";
	public static final String PREF_MI2_DISPLAY_ITEM_DISTANCE = "distance";
	public static final String PREF_MI2_DISPLAY_ITEM_CALORIES = "calories";
	public static final String PREF_MI2_DISPLAY_ITEM_HEART_RATE = "heart_rate";
	public static final String PREF_MI2_DISPLAY_ITEM_BATTERY = "battery";
	public static final String PREF_MI2_ACTIVATE_DISPLAY_ON_LIFT = "mi2_activate_display_on_lift_wrist";
    public static final String PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO = "mi2_rotate_wrist_to_switch_info";
    public static final String PREF_MIBAND_SETUP_BT_PAIRING = "mi_setup_bt_pairing";

    public static final String MI_GENERAL_NAME_PREFIX = "MI";
    public static final String MI_BAND2_NAME = "MI Band 2";
    public static final String MI_1 = "1";
    public static final String MI_1A = "1A";
    public static final String MI_1S = "1S";
    public static final String MI_AMAZFIT = "Amazfit";
    public static final String MI_PRO = "2";

}
