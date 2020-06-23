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
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.AppBundle;
import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.aurora.warden.data.model.report.Bundle;
import com.aurora.warden.data.model.report.StaticReport;
import com.aurora.warden.data.room.persistence.BundleDataSource;
import com.aurora.warden.data.room.persistence.ReportDataSource;
import com.aurora.warden.data.room.persistence.WardenDatabase;
import com.aurora.warden.events.Event;
import com.aurora.warden.tasks.ApkClassTask;
import com.aurora.warden.tasks.AppsTask;
import com.aurora.warden.tasks.CodeAnalyzerTask;
import com.aurora.warden.utils.Log;
import com.aurora.warden.utils.Util;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.utils.app.PackageUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DeviceAnalysisService extends Service {

    public static DeviceAnalysisService instance = null;

    private NotificationManager notificationManager;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
    private CompositeDisposable disposable = new CompositeDisposable();

    private boolean completed = false;
    private StaticReport staticReport;
    private WardenDatabase wardenDatabase;
    private ReportDataSource reportDataSource;
    private BundleDataSource bundleDataSource;

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
        startAnalysis();
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
        wardenDatabase = WardenDatabase.getInstance(getApplicationContext());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        if (Util.isKeepAwakeEnabled(this)) {
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Warden::DoNotSleep");
                wakeLock.acquire(5 * 60 * 1000L /*Max 5 minutes, it should not ideally exceed this limit*/);
            }
        }

        reportDataSource = new ReportDataSource(wardenDatabase.reportDao());
        bundleDataSource = new BundleDataSource(wardenDatabase.bundleDao());
        staticReport = new StaticReport();
        staticReport.setReportId(System.currentTimeMillis());

        notifyServiceStart();
    }

    private void startAnalysis() {
        final PackageManager packageManager = getPackageManager();
        final Map<Integer, Set<String>> trackerAppMap = new HashMap<>();
        final Map<Integer, Set<String>> loggerAppMap = new HashMap<>();
        final List<Bundle> bundleList = new ArrayList<>();

        disposable.add(Observable.fromIterable(new AppsTask(getApplication()).getAllPackages())
                .map(packageInfo -> PackageUtil.getMinimalAppByPackageInfo(packageManager, packageInfo))
                .sorted((o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()))
                .concatMap(app -> Observable.just(app).delay(500, TimeUnit.MILLISECONDS))
                .map(app -> {
                    try {
                        final Uri uri = Uri.fromFile(new File(app.getInstallLocation()));
                        final Set<String> classList = new ApkClassTask(this, uri).getAllClasses();
                        final CodeAnalyzerTask codeAnalyzerTask = new CodeAnalyzerTask(this, classList);
                        final Map<String, Tracker> trackerMap = codeAnalyzerTask.getTrackers();
                        final Map<String, Logger> loggerMap = codeAnalyzerTask.getLoggers();

                        for (Tracker tracker : trackerMap.values()) {
                            Set<String> packageList = trackerAppMap.get(tracker.getId());
                            if (packageList == null)
                                packageList = new HashSet<>();
                            packageList.add(app.getPackageName());
                            trackerAppMap.put(tracker.getId(), packageList);
                        }

                        for (Logger logger : loggerMap.values()) {
                            Set<String> packageList = loggerAppMap.get(logger.getId());
                            if (packageList == null)
                                packageList = new HashSet<>();
                            packageList.add(app.getPackageName());
                            loggerAppMap.put(logger.getId(), packageList);
                        }

                        final AppBundle appBundle = new AppBundle();
                        appBundle.setApp(app);
                        appBundle.setTrackerCount(trackerMap.size());
                        appBundle.setLoggerCount(loggerMap.size());

                        updateNotification(appBundle);
                        AuroraApplication.rxNotify(new Event(Event.SubType.SCAN_UPDATE, gson.toJson(appBundle)));

                        final Bundle bundle = new Bundle();
                        bundle.setReportId(staticReport.getReportId());
                        bundle.setPackageName(app.getPackageName());
                        bundle.setVersionCode(app.getVersionCode());
                        bundle.setTrackersList(new ArrayList<>(trackerMap.values()));
                        bundle.setLoggerList(new ArrayList<>(loggerMap.values()));
                        bundleList.add(bundle);
                    } catch (Exception ignored) {
                    }
                    return app.getPackageName();
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .subscribe(packageList -> {
                    staticReport.setPackageList(packageList);
                    staticReport.setTrackerAppMap(trackerAppMap);
                    staticReport.setLoggerAppMap(loggerAppMap);
                    reportDataSource.insertOrUpdate(staticReport);
                    bundleDataSource.insertOrUpdateAll(bundleList);
                    completed = true;
                    AuroraApplication.rxNotify(new Event(Event.SubType.SCAN_COMPLETED));
                    stopSelf();
                }, throwable -> {
                    completed = true;
                    AuroraApplication.rxNotify(new Event(Event.SubType.SCAN_FAILED));
                    stopSelf();
                }));
    }

    private void notifyServiceStart() {
        startForeground(Constants.TAG.hashCode(), getNotification(null).build());
    }

    private void updateNotification(AppBundle appBundle) {
        final NotificationCompat.Builder builder = getNotification(appBundle);
        notificationManager.notify(Constants.TAG.hashCode(), builder.build());
    }

    private NotificationCompat.Builder getNotification(AppBundle appBundle) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_SERVICE);
        builder.setCategory(Notification.CATEGORY_SERVICE)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setContentText(appBundle == null
                        ? getString(R.string.notification_analysis)
                        : appBundle.getApp().getDisplayName()
                )
                .setContentTitle(appBundle == null
                        ? getString(R.string.notification_analysis_desc)
                        : appBundle.getApp().getPackageName())
                .setLocalOnly(true)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                // .setContentIntent(getContentIntent())
                .build();

        if (appBundle != null && appBundle.getApp() != null) {
            builder.setContentTitle(appBundle.getApp().getDisplayName())
                    .setSubText(appBundle.getApp().getPackageName())
                    .setContentText(StringUtils.joinWith(" \u2022 ",
                            appBundle.getTrackerCount() + StringUtils.SPACE + getString(R.string.txt_status_tracker),
                            appBundle.getLoggerCount() + StringUtils.SPACE + getString(R.string.txt_status_loggers))
                    )
                    .setLargeIcon(ViewUtil.getBitmapFromDrawable(appBundle.getApp().getIconDrawable()));
        }
        return builder;
    }

    @Override
    public void onDestroy() {
        try {
            //Release DeviceAwake lock
            if (wakeLock != null)
                wakeLock.release();
            //Dispose all observables
            disposable.dispose();
            //Clear all notifications
            notificationManager.cancel(Constants.TAG, Constants.TAG.hashCode());
            //Notify Cancel events
            if (!completed) {
                Log.e("Service Cancelled");
                AuroraApplication.rxNotify(new Event(Event.SubType.SCAN_CANCELED));
            }
            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
