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

package com.aurora.warden.tasks;

import android.app.usage.EventStats;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.aurora.warden.data.model.App;
import com.aurora.warden.utils.Util;
import com.aurora.warden.utils.app.PackageUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppUsageTask extends ContextCompat {

    private final Map<String, App> appHashMap = new HashMap<>();
    private Context context;

    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;

    public AppUsageTask(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= 22) {
            this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        }
    }

    public List<UsageStats> getAllUsageStats(long beginTime, long endTime, int interval) {
        if (usageStatsManager != null) {
            return usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    beginTime,
                    endTime);
        }
        return new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public List<EventStats> getAllEventsStats(long beginTime, long endTime) {
        if (usageStatsManager != null) {
            return usageStatsManager.queryEventStats(
                    UsageStatsManager.INTERVAL_BEST,
                    beginTime,
                    endTime);
        }
        return new ArrayList<>();
    }

    public UsageEvents getUsageEvents(long beginTime, long endTime) {
        if (usageStatsManager != null) {
            return usageStatsManager.queryEvents(
                    beginTime,
                    endTime);
        }
        return null;
    }

    public List<App> getAllAppsWithUsageStats(int interval) {
        Calendar calendar = Calendar.getInstance();
        Long beginTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
        switch (interval) {
            case UsageStatsManager.INTERVAL_DAILY:
                calendar.add(Calendar.HOUR_OF_DAY, -calendar.get(Calendar.HOUR_OF_DAY));
                break;
            case UsageStatsManager.INTERVAL_WEEKLY:
                calendar.add(Calendar.DAY_OF_WEEK, -7);
                break;
            case UsageStatsManager.INTERVAL_MONTHLY:
                calendar.add(Calendar.MONTH, -1);
                break;
            case UsageStatsManager.INTERVAL_YEARLY:
                calendar.add(Calendar.YEAR, -1);
                break;
        }

        final List<UsageStats> stats = getAllUsageStats(calendar.getTimeInMillis(), System.currentTimeMillis(), interval);

        //Collections.sort(stats, (o1, o2) -> Long.compare(o1.getTotalTimeInForeground(), o2.getTotalTimeInForeground()));

        for (UsageStats usageStats : stats) {

            if (usageStats.getPackageName().equals("com.aurora.warden"))
                continue;

            if (!Util.isSystemEnabled(context) && usageStats.getPackageName().startsWith("com.android")) {
                continue;
            }

            if (usageStats.getLastTimeUsed() < 0)
                continue;

            if (usageStats.getLastTimeUsed() > calendar.getTimeInMillis()) {
                final String packageName = usageStats.getPackageName();
                final PackageInfo packageInfo = PackageUtil.getPackageInfo(packageManager, packageName);

                if (packageInfo == null)
                    continue;

                final App app = PackageUtil.getMinimalAppByPackageInfo(packageManager, packageInfo);
                app.setUsageStats(usageStats);

                synchronized (appHashMap) {
                    if (appHashMap.containsKey(app.getPackageName())) {
                        final App tempApp = appHashMap.get(app.getPackageName());
                        if (tempApp != null) {
                            final UsageStats tempUsageStat = tempApp.getUsageStats();
                            if (tempUsageStat != null && tempUsageStat.getLastTimeUsed() < usageStats.getLastTimeUsed())
                                appHashMap.put(app.getPackageName(), app);
                        }
                    } else {
                        app.setUsageStats(usageStats);
                        appHashMap.put(app.getPackageName(), app);
                    }
                }
            }
        }
        return new ArrayList<>(appHashMap.values());
    }
}
