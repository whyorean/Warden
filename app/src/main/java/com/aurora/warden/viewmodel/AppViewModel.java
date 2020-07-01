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
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurora.warden.data.model.App;
import com.aurora.warden.data.model.items.AppItem;
import com.aurora.warden.data.model.items.BloatItem;
import com.aurora.warden.tasks.AppsTask;
import com.aurora.warden.utils.app.PackageUtil;

import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppViewModel extends AndroidViewModel {

    private MutableLiveData<List<AppItem>> data = new MutableLiveData<>();
    private MutableLiveData<List<BloatItem>> bloatApps = new MutableLiveData<>();

    private CompositeDisposable disposable = new CompositeDisposable();
    private PackageManager packageManager;

    public AppViewModel(@NonNull final Application application) {
        super(application);
        packageManager = getApplication().getPackageManager();
    }

    public void fetchAllApps() {
        disposable.add(Observable.fromCallable(() -> new AppsTask(getApplication()).getAllPackages())
                .flatMap(packageInfoSet -> Observable.fromIterable(packageInfoSet)
                        .map(packageInfo -> App.builder()
                                .packageName(packageInfo.applicationInfo.packageName)
                                .displayName(packageInfo.applicationInfo.loadLabel(packageManager).toString())
                                .iconDrawable(packageInfo.applicationInfo.loadIcon(packageManager))
                                .versionCode((long) packageInfo.versionCode)
                                .versionName(packageInfo.versionName)
                                .lastUpdated(packageInfo.lastUpdateTime)
                                .installedTime(packageInfo.firstInstallTime)
                                .uid(packageInfo.applicationInfo.uid)
                                .build()))
                .sorted((o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()))
                .map(AppItem::new)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apps -> {
                    data.setValue(apps);
                }, Throwable::printStackTrace));
    }

    public void fetchAllApps(Set<String> packageNames) {
        disposable.add(Observable.fromIterable(new AppsTask(getApplication()).getAllPackageNames())
                .filter(packageNames::contains)
                .map(packageName -> PackageUtil.getMinimalAppByPackageInfo(packageManager, packageName))
                .filter(app -> app != null)
                .sorted((o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()))
                .map(app -> {
                    BloatItem bloatItem = new BloatItem(app);
                    bloatItem.setSelected(true);
                    return bloatItem;
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apps -> {
                    bloatApps.setValue(apps);
                }, Throwable::printStackTrace));
    }

    public LiveData<List<AppItem>> getAppList() {
        return data;
    }

    public LiveData<List<BloatItem>> getBloatAppList() {
        return bloatApps;
    }

    @Override
    protected void onCleared() {
        disposable.dispose();
        super.onCleared();
    }
}