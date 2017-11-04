/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, João Paulo Barraca

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

import android.support.annotation.DrawableRes;

import smartlife.monitorwearables.Constants;
import smartlife.monitorwearables.R;

/**
 * For every supported device, a device type constant must exist.
 *
 * Note: they key of every constant is stored in the DB, so it is fixed forever,
 * and may not be changed.
 */
public enum DeviceType {
    UNKNOWN(-1, R.drawable.unknown_device, R.drawable.unknown_device_disabled),
    MIBAND(10, R.drawable.miband, R.drawable.miband2_disabled),
    MIBAND2(11, R.drawable.miband, R.drawable.miband2_disabled),
    ANDROIDWEAR_MOTO360SPORT(200, R.drawable.android_wear, R.drawable.android_wear_disabled),
    TEST(1000, R.drawable.unknown_device, R.drawable.unknown_device_disabled);


    private final int key;
    @DrawableRes
    private final int defaultIcon;
    @DrawableRes
    private final int disabledIcon;

    DeviceType(int key, int defaultIcon, int disabledIcon) {
        this.key = key;
        this.defaultIcon = defaultIcon;
        this.disabledIcon = disabledIcon;
    }

    public int getKey() {
        return key;
    }

    public boolean isSupported() {
        return this != UNKNOWN;
    }

    public static DeviceType fromKey(int key) {
        for (DeviceType type : values()) {
            if (type.key == key) {
                return type;
            }
        }
        return DeviceType.UNKNOWN;
    }

    @DrawableRes
    public int getIcon() {
        return defaultIcon;
    }

    @DrawableRes
    public int getDisabledIcon() {
        return disabledIcon;
    }

    public static int getKeyByWearDeviceName(String wearDeviceName){
        int key = DeviceType.UNKNOWN.getKey();
        switch (wearDeviceName){
            case Constants.WEAR_MODEL_MOTO_360:
                key = DeviceType.ANDROIDWEAR_MOTO360SPORT.getKey();
                break;
            default:
               break;
        }
        return key;
    }
}
