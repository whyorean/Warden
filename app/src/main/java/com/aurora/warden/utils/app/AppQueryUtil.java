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

package com.aurora.warden.utils.app;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.aurora.warden.IPackageStatsObserver;
import com.aurora.warden.data.model.AppData;
import com.aurora.warden.utils.Log;

import java.lang.reflect.Method;

public class AppQueryUtil {

    public static void queryAppSize(Context context, String packageName, Callback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            queryAfterTarget26(context, packageName, callback);
        } else {
            queryBeforeTarget26(context, packageName, callback);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void queryAfterTarget26(final Context context, String packageName, final Callback callback) {
        new Thread(() -> {
            StorageStatsManager statsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            Handler handler = new Handler(Looper.getMainLooper());
            UserHandle userHandle = UserHandle.getUserHandleForUid(-2);
            try {
                AppData appData = new AppData();
                if (statsManager != null) {
                    StorageStats stats = statsManager.queryStatsForPackage(StorageManager.UUID_DEFAULT, packageName, userHandle);
                    appData.setCodeBytes(stats.getAppBytes());
                    appData.setCacheBytes(stats.getCacheBytes());
                    appData.setDataBytes(stats.getDataBytes());
                    handler.post(() -> callback.onSuccess(appData));
                }
            } catch (Exception e) {
                if (e instanceof SecurityException) {
                    handler.post(() -> Toast.makeText(context, "App usage access permission required", Toast.LENGTH_SHORT).show());
                }
                handler.post(() -> callback.onError(new AppData(), e.getMessage()));
            }
        }).start();
    }

    private static void queryBeforeTarget26(final Context context, String packageName, final Callback callback) {
        new Thread(() -> {
            PackageManager packageManager = context.getPackageManager();
            IPackageStatsObserver packageStatsObserver = new IPackageStatsObserver.Stub() {
                @Override
                public void onGetStatsCompleted(PackageStats packageStats, boolean succeeded) throws RemoteException {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        AppData appData = new AppData();
                        appData.setCodeBytes(packageStats.codeSize);
                        appData.setCacheBytes(packageStats.cacheSize);
                        appData.setDataBytes(packageStats.dataSize);
                        callback.onSuccess(appData);
                    });
                }
            };

            try {
                Method method = PackageManager.class.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                method.invoke(packageManager, info.packageName, packageStatsObserver);
            } catch (Exception e) {
                Log.e(e.getMessage());
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    callback.onError(new AppData(), e.getMessage());
                });
            }
        });
    }

    public interface Callback {
        void onSuccess(AppData appData);

        void onError(AppData appData, String error);
    }
}
