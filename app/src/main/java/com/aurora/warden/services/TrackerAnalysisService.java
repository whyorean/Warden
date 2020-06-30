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
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.TrackerAnalysisCancelReceiver;
import com.aurora.warden.data.model.App;
import com.aurora.warden.events.Event;
import com.aurora.warden.manager.camtono.CamtonoManager;
import com.aurora.warden.tasks.ApkClassTask;
import com.aurora.warden.tasks.AppsTask;
import com.aurora.warden.tasks.ComponentAnalyzerTask;
import com.aurora.warden.utils.IOUtils;
import com.aurora.warden.utils.Log;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.utils.app.PackageUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TrackerAnalysisService extends Service {

    public static TrackerAnalysisService instance = null;

    private NotificationManager notificationManager;
    private CamtonoManager camtonoManager;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
    private boolean isExportEnabled = false;

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
        int intExtra = intent.getIntExtra(Constants.INT_EXTRA, -1);
        isExportEnabled = intExtra == 1;
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
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        camtonoManager = CamtonoManager.getInstance(this);

        AuroraApplication.rxNotify(new Event(Event.SubType.NUKE_INIT));

        notifyServiceStart();
    }

    private void startAnalysis() {
        final PackageManager packageManager = getPackageManager();
        disposable.add(Observable.fromIterable(new AppsTask(getApplication()).getAllPackages())
                .map(packageInfo -> {
                    try {
                        final App app = PackageUtil.getMinimalAppByPackageInfo(packageManager, packageInfo);

                        final Uri uri = Uri.fromFile(new File(packageInfo.applicationInfo.sourceDir));
                        final Set<String> classList = new ApkClassTask(this, uri).getAllClasses();

                        final ComponentAnalyzerTask codeAnalyzerTask = new ComponentAnalyzerTask(this, classList, packageInfo);
                        final ComponentAnalyzerTask.Bundle trackerBundle = codeAnalyzerTask.getTrackerComponentBundle();
                        final ComponentAnalyzerTask.Bundle loggerBundle = codeAnalyzerTask.getLoggerComponentBundle();

                        updateNotification(app);

                        final List<String> trackerComponents = new ArrayList<>();
                        final List<String> loggerComponents = new ArrayList<>();

                        if (AuroraApplication.isRooted()) {
                            for (ComponentInfo componentInfo : trackerBundle.getComponents()) {
                                boolean result = camtonoManager.camtono().disable(componentInfo.packageName, componentInfo.name);
                                if (result)
                                    Log.d("Component changed -> %s : %s", componentInfo.packageName, componentInfo.name);
                                else
                                    Log.e("Component un-changed -> %s : %s", componentInfo.packageName, componentInfo.name);

                                if (isExportEnabled)
                                    trackerComponents.add(componentInfo.name);
                            }

                            for (ComponentInfo componentInfo : loggerBundle.getComponents()) {
                                boolean result = camtonoManager.camtono().disable(componentInfo.packageName, componentInfo.name);
                                if (result)
                                    Log.d("Component changed -> %s : %s", componentInfo.packageName, componentInfo.name);
                                else
                                    Log.e("Component un-changed -> %s : %s", componentInfo.packageName, componentInfo.name);

                                if (isExportEnabled)
                                    loggerComponents.add(componentInfo.name);
                            }
                        }

                        if (isExportEnabled) {
                            ExportBundle bundle = ExportBundle.builder()
                                    .name(app.getDisplayName())
                                    .packageName(app.getPackageName())
                                    .versionName(app.getVersionName())
                                    .versionCode(app.getVersionCode())
                                    .trackerComponents(trackerComponents)
                                    .loggerComponents(loggerComponents)
                                    .build();
                            exportBundleData(bundle);
                        }
                    } catch (Exception ignored) {
                    }
                    return packageInfo.packageName;
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .subscribe(packageList -> {
                    stopSelf();
                    AuroraApplication.rxNotify(new Event(Event.SubType.NUKE_COMPLETED));
                }, throwable -> {
                    stopSelf();
                    AuroraApplication.rxNotify(new Event(Event.SubType.NUKE_COMPLETED));
                }));
    }

    private void notifyServiceStart() {
        startForeground(Constants.TAG.hashCode(), getNotification(null).build());
    }

    private void updateNotification(App app) {
        final NotificationCompat.Builder builder = getNotification(app);
        notificationManager.notify(Constants.TAG.hashCode(), builder.build());
    }

    private NotificationCompat.Builder getNotification(App app) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_SERVICE);
        builder.setCategory(Notification.CATEGORY_SERVICE)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setContentText(app == null
                        ? getString(R.string.notification_tracker)
                        : app.getDisplayName()
                )
                .setContentTitle(app == null
                        ? getString(R.string.notification_tracker_desc)
                        : app.getPackageName())
                .setLocalOnly(true)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification);

        if (app != null) {
            builder.setLargeIcon(ViewUtil.getBitmapFromDrawable(app.getIconDrawable()));
            builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_close,
                    getString(R.string.action_cancel),
                    getCancelIntent())
                    .build());
        }

        return builder;
    }

    private PendingIntent getCancelIntent() {
        final Intent intent = new Intent(this, TrackerAnalysisCancelReceiver.class);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void exportBundleData(ExportBundle exportBundle) {
        try {
            final File baseDirectory = new File(Environment.getExternalStorageDirectory().getPath(), "Warden/Exports/");

            if (!baseDirectory.exists())
                baseDirectory.mkdir();

            final File file = FileUtils.getFile(baseDirectory, exportBundle.packageName + ".json");
            FileUtils.write(file, gson.toJson(exportBundle), Charset.defaultCharset());
        } catch (IOException e) {
            Log.e("Failed to export bundle : %s", exportBundle.packageName);
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            disposable.dispose();
            notificationManager.cancel(Constants.TAG, Constants.TAG.hashCode());
            //Clear cache
            IOUtils.deleteCache(this);
            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportBundle {
        private String name;
        private String packageName;
        private String versionName;
        private Long versionCode;

        private List<String> trackerComponents;
        private List<String> loggerComponents;
    }
}
