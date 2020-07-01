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

package com.aurora.warden;

public class Constants {

    public static final String TAG = "Warden";
    public static final String SHARED_PREFERENCES_KEY = "com.aurora.warden";
    public static final String UPDATE_URL = "https://gitlab.com/AuroraOSS/AppWarden/raw/master/updates.json";

    public static final String NOTIFICATION_CHANNEL_ALERT = "NOTIFICATION_CHANNEL_ALERT";
    public static final String NOTIFICATION_CHANNEL_SERVICE = "NOTIFICATION_CHANNEL_SERVICE";

    public static final String INTENT_PACKAGE_NAME = "INTENT_PACKAGE_NAME";

    public static final String INT_EXTRA = "INT_EXTRA";
    public static final String FLOAT_EXTRA = "FLOAT_EXTRA";
    public static final String STRING_EXTRA = "STRING_EXTRA";
    public static final String LIST_EXTRA = "LIST_EXTRA";

    public static final String PREFERENCE_THEME = "PREFERENCE_THEME";
    public static final String PREFERENCE_HIDDEN_APPS = "PREFERENCE_HIDDEN_APPS";
    public static final String PREFERENCE_INCLUDE_SYSTEM = "PREFERENCE_INCLUDE_SYSTEM";
    public static final String PREFERENCE_DEVICE_AWAKE = "PREFERENCE_DEVICE_AWAKE";
    public static final String PREFERENCE_DEBLOAT_ACTION = "PREFERENCE_DEBLOAT_ACTION";

    public static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    public static final String FRAGMENT_ABOUT = "FRAGMENT_ABOUT";
    public static final String FRAGMENT_LAB = "FRAGMENT_LAB";
    public static final String FRAGMENT_DEBLOAT = "FRAGMENT_DEBLOAT";
    public static final String FRAGMENT_HIDDEN = "FRAGMENT_HIDDEN";
    public static final String FRAGMENT_TRACKERS = "FRAGMENT_TRACKERS";
    public static final String FRAGMENT_LOGGERS = "FRAGMENT_LOGGERS";
    public static final String FRAGMENT_APP_USAGE = "FRAGMENT_APP_USAGE";
}
