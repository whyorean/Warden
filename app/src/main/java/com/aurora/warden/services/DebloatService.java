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

package com.aurora.warden.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.App;
import com.aurora.warden.events.Event;
import com.aurora.warden.manager.camtono.CamtonoManager;
import com.aurora.warden.utils.Log;
import com.aurora.warden.utils.Util;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.utils.app.PackageUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DebloatService extends Service {

    public static DebloatService instance = null;

    private Gson gson = new Gson();
    private Type type = new TypeToken<Set<String>>() {
    }.getType();
    private Set<String> packageNameSet = new HashSet<>();
    private NotificationManager notificationManager;
    private CamtonoManager camtonoManager;
    private CompositeDisposable disposable = new CompositeDisposable();

    public static boolean isServiceRunning() {
        try {
            return instance != null && instance.isRunning();
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean isRunning() {
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String rawPackageSet = intent.getStringExtra(Constants.STRING_EXTRA);
        packageNameSet = gson.fromJson(rawPackageSet, type);
        startDebloating();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        camtonoManager = CamtonoManager.getInstance(this);

        AuroraApplication.rxNotify(new Event(Event.SubType.DEBLOAT_INIT));

        notifyServiceStart();
    }

    private void startDebloating() {
        final PackageManager packageManager = getPackageManager();
        String actionType = Util.getDefaultDebloatAction(this);
        disposable.add(Observable.fromIterable(packageNameSet)
                .map(packageName -> PackageUtil.getMinimalAppByPackageInfo(packageManager, packageName))
                .sorted((o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()))
                .concatMap(app -> Observable.just(app).delay(500, TimeUnit.MILLISECONDS))
                .doOnNext(app -> {
                    boolean result = false;
                    switch (actionType) {
                        case "0":
                            result = camtonoManager.camtono().uninstall(app.getPackageName());
                            break;
                        case "1":
                            result = camtonoManager.camtono().disable(app.getPackageName());
                            break;
                        case "2":
                            result = camtonoManager.camtono().hide(app.getPackageName());
                            break;
                    }
                    if (result) {
                        Log.d("App Removed : %s", app.getDisplayName());
                        updateNotification(app, getString(R.string.txt_camtono_success));
                    } else {
                        Log.d("Failed to remove : %s", app.getDisplayName());
                        updateNotification(app, getString(R.string.txt_camtono_failed));
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .subscribe(apps -> {
                    AuroraApplication.rxNotify(new Event(Event.SubType.DEBLOAT_COMPLETED));
                    stopSelf();
                }, throwable -> {
                    AuroraApplication.rxNotify(new Event(Event.SubType.DEBLOAT_FAILED));
                    stopSelf();
                }));
    }

    private void notifyServiceStart() {
        startForeground(Constants.TAG.hashCode(), getNotification(null, null).build());
    }

    private void updateNotification(App app, String status) {
        Log.e(app.getDisplayName());
        final NotificationCompat.Builder builder = getNotification(app, status);
        notificationManager.notify(Constants.TAG.hashCode(), builder.build());
    }

    private NotificationCompat.Builder getNotification(App app, String status) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_SERVICE);
        builder.setCategory(Notification.CATEGORY_SERVICE)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setContentText(app == null
                        ? getString(R.string.notification_debloat)
                        : app.getDisplayName()
                )
                .setContentTitle(status == null
                        ? getString(R.string.notification_debloat_desc)
                        : status)
                .setLocalOnly(true)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification);

        if (app != null) {
            builder.setLargeIcon(ViewUtil.getBitmapFromDrawable(app.getIconDrawable()));
        }

        return builder;
    }

    @Override
    public void onDestroy() {
        try {
            disposable.dispose();
            notificationManager.cancel(Constants.TAG, Constants.TAG.hashCode());
            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
