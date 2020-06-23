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

package com.aurora.warden.ui.custom.layout.scan;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.R;
import com.aurora.warden.data.model.AppBundle;
import com.aurora.warden.services.DeviceAnalysisService;
import com.aurora.warden.ui.custom.layout.app.AppLayout;
import com.aurora.warden.utils.Log;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ScanProgressLayout extends RelativeLayout {

    @BindView(R.id.app_bundle)
    AppLayout appLayout;
    @BindView(R.id.txt)
    AppCompatTextView txt;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.btn)
    MaterialButton btn;

    private Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
    private CompositeDisposable disposable = new CompositeDisposable();

    public ScanProgressLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ScanProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScanProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ScanProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_scan_progress, this);
        ButterKnife.bind(this, view);

        disposable.add(AuroraApplication
                .getRelay()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(event -> {
                    switch (event.getSubType()) {
                        case SCAN_UPDATE:
                            final AppBundle appBundle = gson.fromJson(event.getStringExtra(), AppBundle.class);
                            appLayout.setAppBundle(appBundle);
                            break;
                        case SCAN_FAILED:
                        case SCAN_COMPLETED:
                        case SCAN_CANCELED:
                            appLayout.clear();
                            break;
                    }
                })
                .subscribe());
    }

    @OnClick(R.id.btn)
    public void stopDeviceAnalysis() {
        try {
            if (DeviceAnalysisService.isServiceRunning()) {
                Intent intent = new Intent(getContext(), DeviceAnalysisService.class);
                getContext().stopService(intent);
            }
        } catch (IllegalStateException e) {
            Log.e(e.getMessage());
        }
    }
}
