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

package com.aurora.warden.viewmodel;

import android.app.Application;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurora.warden.data.model.items.AppUsageItem;
import com.aurora.warden.tasks.AppUsageTask;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppUsageViewModel extends AndroidViewModel {

    private MutableLiveData<List<AppUsageItem>> data = new MutableLiveData<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getData() != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case Intent.ACTION_PACKAGE_REMOVED:
                    case Intent.ACTION_PACKAGE_FULLY_REMOVED:
                    case Intent.ACTION_UNINSTALL_PACKAGE: {
                        fetchRecentApps(UsageStatsManager.INTERVAL_DAILY);
                        break;
                    }
                }
            }
        }
    };

    public AppUsageViewModel(@NonNull final Application application) {
        super(application);
        application.registerReceiver(broadcastReceiver, getFilter());
        fetchRecentApps(UsageStatsManager.INTERVAL_DAILY);
    }

    public LiveData<List<AppUsageItem>> getRecentApps() {
        return data;
    }

    public void fetchRecentApps(int interval) {
        disposable.add(Observable.fromCallable(() -> new AppUsageTask(getApplication())
                .getAllAppsWithUsageStats(interval))
                .subscribeOn(Schedulers.io())
                .flatMap(apps -> Observable.fromIterable(apps)
                        .sorted((o1, o2) -> Long.compare(o2.getUsageStats().getTotalTimeInForeground(), o1.getUsageStats().getTotalTimeInForeground()))
                        .map(AppUsageItem::new))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apps -> {
                    data.setValue(apps);
                }, err -> {
                    err.printStackTrace();
                    if (err instanceof SecurityException) {
                        Toast.makeText(getApplication(), "Usage Stats permission required", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void fetchUsageEvents() {
        disposable.add(Observable.fromCallable(() -> new AppUsageTask(getApplication())
                .getAllEventsStats(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24), System.currentTimeMillis()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eventStats -> {

                }, err -> {
                    err.printStackTrace();
                    if (err instanceof SecurityException) {
                        Toast.makeText(getApplication(), "Usage Stats permission required", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    @Override
    protected void onCleared() {
        try {
            disposable.dispose();
            getApplication().unregisterReceiver(broadcastReceiver);
        } catch (Exception ignored) {

        }
        super.onCleared();
    }

    private IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        filter.addAction(Intent.ACTION_UNINSTALL_PACKAGE);
        return filter;
    }
}