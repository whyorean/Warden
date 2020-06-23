/*
 * Warden
 * Copyright (C) 2020, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.aurora.warden.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtil {
    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String millisToDay(long millis) {
        millis = Calendar.getInstance().getTimeInMillis() - millis;
        int days = (int) TimeUnit.MILLISECONDS.toDays(millis);
        switch (days) {
            case 0:
                return "Today";
            case 1:
                return "Yesterday";
            default:
                return StringUtils.joinWith(StringUtils.SPACE, days, "days ago");
        }
    }

    public static String millisToTime(long millis) {
        millis = System.currentTimeMillis() - millis;
        return StringUtils.joinWith(StringUtils.SPACE,
                millisToTimeDurationMinimal(millis),
                "ago");
    }

    public static String millisToTimeDuration(long millis) {
        if (millis <= -1) {
            return "Calculating";
        }

        if (millis == 0) {
            return "Unknown";
        }

        String result = StringUtils.EMPTY;

        final long dd = TimeUnit.MILLISECONDS.toDays(millis);

        if (dd > 1)
            millis = millis - TimeUnit.DAYS.toMillis(dd);

        if (dd > 0)
            result = StringUtils.joinWith(StringUtils.SPACE, dd > 1 ? dd + " days" : dd + " day");

        final long hh = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));

        if (hh > 0)
            result = StringUtils.joinWith(StringUtils.SPACE, result, hh > 1 ? hh + " hrs" : hh + " hr");

        if (hh > 1)
            millis = millis - TimeUnit.HOURS.toMillis(hh);

        final long mm = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));

        if (mm > 0)
            result = StringUtils.joinWith(StringUtils.SPACE, result, mm > 1 ? mm + " mins" : mm + " min");

        final long ss = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        result = StringUtils.joinWith(StringUtils.SPACE, result, ss > 1 ? ss + " secs" : ss + " sec");

        return StringUtils.strip(result);
    }

    public static String millisToTimeDurationMinimal(long millis) {

        String result = StringUtils.EMPTY;

        final long hh = TimeUnit.MILLISECONDS.toHours(millis);

        if (hh > 0) {
            result = StringUtils.joinWith(StringUtils.SPACE, result, hh > 1 ? hh + " hrs" : hh + " hr");
            return StringUtils.strip(result);
        }

        final long mm = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));

        if (mm > 0) {
            result = StringUtils.joinWith(StringUtils.SPACE, result, mm > 1 ? mm + " mins" : mm + " min");
            return StringUtils.strip(result);
        }

        result = TimeUnit.MILLISECONDS.toSeconds(millis) + " sec";

        return StringUtils.strip(result);
    }
}
