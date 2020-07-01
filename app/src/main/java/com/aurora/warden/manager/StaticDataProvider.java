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

package com.aurora.warden.manager;

import android.content.Context;

import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class StaticDataProvider {

    private static volatile StaticDataProvider INSTANCE;

    private Context context;
    private Gson gson;

    public StaticDataProvider(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    public static StaticDataProvider getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (StaticDataProvider.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StaticDataProvider(context);
                }
            }
        }
        return INSTANCE;
    }

    public HashMap<Integer, Tracker> getKnownTrackersList() {
        try {
            final InputStream inputStream = context.getAssets().open("trackers.json");
            final byte[] bytes = new byte[inputStream.available()];
            IOUtils.read(inputStream, bytes);
            final String json = new String(bytes, StandardCharsets.UTF_8);
            final Type type = new TypeToken<HashMap<Integer, Tracker>>() {
            }.getType();
            return gson.fromJson(json, type);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    public HashMap<Integer, Logger> getKnownLoggerList() {
        try {
            final InputStream inputStream = context.getAssets().open("loggers.json");
            final byte[] bytes = new byte[inputStream.available()];
            IOUtils.read(inputStream, bytes);
            final String json = new String(bytes, StandardCharsets.UTF_8);
            final Type type = new TypeToken<HashMap<Integer, Logger>>() {
            }.getType();
            return gson.fromJson(json, type);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
}
