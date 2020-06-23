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

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.aurora.warden.events.Event;
import com.aurora.warden.utils.Log;
import com.jakewharton.rxrelay3.PublishRelay;
import com.jakewharton.rxrelay3.Relay;
import com.topjohnwu.superuser.Shell;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;


public class AuroraApplication extends Application {

    private static boolean isRooted = false;
    private static Relay<Event> relay = PublishRelay.create();

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    private BroadcastReceiver globalInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getData() != null && intent.getAction() != null) {
                String packageName = intent.getData().getSchemeSpecificPart();
                String action = intent.getAction();

                if (StringUtils.isNotEmpty(packageName) && StringUtils.isNotEmpty(action)) {
                    switch (intent.getAction()) {
                        case Intent.ACTION_PACKAGE_ADDED:
                        case Intent.ACTION_PACKAGE_REPLACED:
                        case Intent.ACTION_PACKAGE_INSTALL:
                            rxNotify(new Event(Event.SubType.PACKAGE_INSTALLED, packageName));
                            break;
                        case Intent.ACTION_PACKAGE_REMOVED:
                        case Intent.ACTION_PACKAGE_FULLY_REMOVED:
                        case Intent.ACTION_UNINSTALL_PACKAGE:
                            rxNotify(new Event(Event.SubType.PACKAGE_UNINSTALLED, packageName));
                            break;
                        case Intent.ACTION_PACKAGE_DATA_CLEARED:
                            rxNotify(new Event(Event.SubType.PACKAGE_CLEARED, packageName));
                            break;
                        case Intent.ACTION_PACKAGES_SUSPENDED:
                            rxNotify(new Event(Event.SubType.PACKAGE_SUSPENDED, packageName));
                            break;
                        case Intent.ACTION_PACKAGES_UNSUSPENDED:
                            rxNotify(new Event(Event.SubType.PACKAGE_UNSUSPENDED, packageName));
                            break;
                        case Intent.ACTION_PACKAGE_CHANGED:
                            rxNotify(new Event(Event.SubType.PACKAGE_CHANGED, packageName));
                            break;
                    }
                }
            }
        }
    };

    public static boolean isRooted() {
        return isRooted;
    }

    public static Relay<Event> getRelay() {
        return relay;
    }

    public static void rxNotify(Event event) {
        relay.accept(event);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setupTheme();
        createNotificationChannel();

        Shell.getShell(shell -> {
            if (shell.isRoot()) {
                Log.i("Root Available");
                isRooted = true;
            } else {
                Log.e("Root Unavailable");
                isRooted = false;
            }
        });

        registerReceiver(globalInstallReceiver, getFilter());

        RxJavaPlugins.setErrorHandler(err -> Log.e(err.getMessage()));
        RxJavaPlugins.setErrorHandler(err -> {
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            unregisterReceiver(globalInstallReceiver);
        } catch (Exception ignored) {
        }
    }

    private void setupTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            final ArrayList<NotificationChannel> channels = new ArrayList<>();

            channels.add(new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ALERT,
                    getString(R.string.notification_channel_alert),
                    NotificationManager.IMPORTANCE_HIGH));

            channels.add(new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_SERVICE,
                    getString(R.string.notification_channel_service),
                    NotificationManager.IMPORTANCE_LOW));

            final NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannels(channels);
            }
        }
    }

    private IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        filter.addAction(Intent.ACTION_UNINSTALL_PACKAGE);

        filter.addAction(Intent.ACTION_PACKAGE_INSTALL);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);

        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            filter.addAction(Intent.ACTION_PACKAGES_SUSPENDED);
            filter.addAction(Intent.ACTION_PACKAGES_UNSUSPENDED);
        }
        return filter;
    }
}
