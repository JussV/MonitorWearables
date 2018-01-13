/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer

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
package smartlife.monitorwearables.util;

import android.text.format.DateUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import smartlife.monitorwearables.GBApplication;


public class DateTimeUtils {
    private static SimpleDateFormat DAY_STORAGE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static String formatDateTime(Date date) {
        return DateUtils.formatDateTime(GBApplication.getContext(), date.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
    }

    public static Date parseTimeStamp(int timestamp) {
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000L); // make sure it's converted to long
        return cal.getTime();
    }

    public static Date dayFromString(String day) throws ParseException {
        return DAY_STORAGE_FORMAT.parse(day);
    }

}
