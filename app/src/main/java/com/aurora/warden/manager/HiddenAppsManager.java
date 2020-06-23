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

import com.aurora.warden.Constants;
import com.aurora.warden.data.model.HiddenApp;
import com.aurora.warden.utils.PrefUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HiddenAppsManager {

    private final HashMap<String, HiddenApp> hashMap = new HashMap<>();

    private Context context;
    private Gson gson;

    public HiddenAppsManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.hashMap.putAll(getDefault());
    }

    public List<HiddenApp> getAll() {
        return new ArrayList<>(hashMap.values());
    }

    public boolean add(HiddenApp hiddenApp) {
        synchronized (hashMap) {
            if (hashMap.containsKey(hiddenApp.getPackageName())) {
                return false;
            } else {
                hashMap.put(hiddenApp.getPackageName(), hiddenApp);
                save();
                return true;
            }
        }
    }

    public boolean isHidden(String packageName) {
        return hashMap.containsKey(packageName);
    }

    public HiddenApp get(String packageName) {
        synchronized (hashMap) {
            if (hashMap.containsKey(packageName))
                return hashMap.get(packageName);
            else
                return null;
        }
    }

    public void remove(HiddenApp hiddenApp) {
        remove(hiddenApp.getPackageName());
    }

    public void remove(String packageName) {
        synchronized (hashMap) {
            hashMap.remove(packageName);
            save();
        }
    }

    private void save() {
        synchronized (hashMap) {
            PrefUtil.putString(context, Constants.PREFERENCE_HIDDEN_APPS, gson.toJson(hashMap));
        }
    }

    public void clear() {
        synchronized (hashMap) {
            hashMap.clear();
            save();
        }
    }

    private HashMap<String, HiddenApp> getDefault() {
        final String rawList = PrefUtil.getString(context, Constants.PREFERENCE_HIDDEN_APPS);
        final Type type = new TypeToken<HashMap<String, HiddenApp>>() {
        }.getType();

        final HashMap<String, HiddenApp> wallHashMap = gson.fromJson(rawList, type);

        if (wallHashMap == null || wallHashMap.isEmpty())
            return new HashMap<>();
        else
            return wallHashMap;
    }
}
