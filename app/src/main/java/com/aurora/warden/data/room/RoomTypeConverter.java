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

package com.aurora.warden.data.room;

import androidx.room.TypeConverter;

import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoomTypeConverter {

    @TypeConverter
    public static Map<Integer, Set<String>> toIntegerMap(String value) {
        Type mapType = new TypeToken<Map<Integer, Set<String>>>() {
        }.getType();
        return new Gson().fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromIntegerMap(Map<Integer, Set<String>> integerMap) {
        return new Gson().toJson(integerMap);
    }

    @TypeConverter
    public static List<Tracker> toTrackerString(String value) {
        Type mapType = new TypeToken<List<Tracker>>() {
        }.getType();
        return new Gson().fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromTrackerList(List<Tracker> trackersList) {
        return new Gson().toJson(trackersList);
    }

    @TypeConverter
    public static List<Logger> toLoggerList(String value) {
        Type mapType = new TypeToken<List<Logger>>() {
        }.getType();
        return new Gson().fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromLoggerList(List<Logger> loggerList) {
        return new Gson().toJson(loggerList);
    }

    @TypeConverter
    public static List<String> toList(String value) {
        Type mapType = new TypeToken<List<String>>() {
        }.getType();
        return new Gson().fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromList(List<String> loggerList) {
        return new Gson().toJson(loggerList);
    }
}
