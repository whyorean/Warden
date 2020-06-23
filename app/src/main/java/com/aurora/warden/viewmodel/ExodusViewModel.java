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
import androidx.lifecycle.MutableLiveData;

import com.aurora.warden.data.model.exodus.ExodusReport;
import com.aurora.warden.data.model.items.ExodusReportItem;
import com.aurora.warden.retro.ExodusService;
import com.aurora.warden.retro.RetroClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExodusViewModel extends AndroidViewModel {

    private static final String EXODUS_API_KEY = "Token bbe6ebae4ad45a9cbacb17d69739799b8df2c7ae";

    private MutableLiveData<List<ExodusReportItem>> data = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private CompositeDisposable disposable = new CompositeDisposable();
    private Gson gson = new Gson();

    public ExodusViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<ExodusReportItem>> getData() {
        return data;
    }

    public MutableLiveData<String> getError() {
        return error;
    }

    public void fetchReport(String packageName) {
        final ExodusService service = RetroClient.getInstance().create(ExodusService.class);
        final Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Accept", "application/json");
        headerMap.put("Authorization", EXODUS_API_KEY);

        final Call<ResponseBody> call = service.getReport(packageName, headerMap);

        call.enqueue(new Callback<ResponseBody>() {
            @SneakyThrows
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        final Type type = new TypeToken<HashMap<String, ExodusReport>>() {
                        }.getType();
                        final HashMap<String, ExodusReport> reportHashMap = gson.fromJson(response.body().string(), type);
                        final ExodusReport exodusReport = reportHashMap.get(packageName);
                        if (exodusReport != null)
                            parseTrackers(exodusReport);
                        else
                            error.setValue("No reports found");
                    } catch (Exception e) {
                        error.setValue(e.getMessage());
                    }
                } else {
                    error.setValue("No reports found");
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable throwable) {
                error.setValue(throwable.getMessage());
            }
        });
    }

    private void parseTrackers(ExodusReport exodusReport) {
        disposable.add(Observable.fromIterable(exodusReport.getReports())
                .sorted((Report1, Report2) -> Report2.getCreationDate().compareTo(Report1.getCreationDate()))
                .map(ExodusReportItem::new)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(exodusReportItems -> data.setValue(exodusReportItems), Throwable::printStackTrace));
    }

    @Override
    protected void onCleared() {
        disposable.dispose();
        super.onCleared();
    }
}