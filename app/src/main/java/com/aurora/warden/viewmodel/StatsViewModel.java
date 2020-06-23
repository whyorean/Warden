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

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurora.warden.data.model.report.StaticReport;
import com.aurora.warden.data.room.persistence.ReportDataSource;
import com.aurora.warden.data.room.persistence.WardenDatabase;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StatsViewModel extends AndroidViewModel {

    private MutableLiveData<List<StaticReport>> data = new MutableLiveData<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    private WardenDatabase wardenDatabase;
    private ReportDataSource reportDataSource;

    public StatsViewModel(@NonNull final Application application) {
        super(application);
        wardenDatabase = WardenDatabase.getInstance(application);
        reportDataSource = new ReportDataSource(wardenDatabase.reportDao());
        fetchRecentReport();
    }

    public LiveData<List<StaticReport>> getRecentApps() {
        return data;
    }

    public void fetchRecentReport() {
        disposable.add(Observable.fromCallable(() -> reportDataSource.getReports())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(staticReports -> {
                    if (!staticReports.isEmpty()) {
                        Collections.sort(staticReports, (o1, o2) -> o2.getReportId().compareTo(o1.getReportId()));
                        data.postValue(staticReports);
                    }
                }, Throwable::printStackTrace));
    }

    @Override
    protected void onCleared() {
        try {
            disposable.dispose();
        } catch (Exception ignored) {

        }
        super.onCleared();
    }
}