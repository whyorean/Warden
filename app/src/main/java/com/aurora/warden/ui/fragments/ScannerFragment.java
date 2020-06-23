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

package com.aurora.warden.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.R;
import com.aurora.warden.events.Event;
import com.aurora.warden.services.DeviceAnalysisService;
import com.aurora.warden.ui.custom.layout.scan.ScanProgressLayout;
import com.aurora.warden.ui.custom.layout.scan.ScanResultLayout;
import com.aurora.warden.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ScannerFragment extends Fragment {

    @BindView(R.id.view_flipper_bottom)
    ViewFlipper viewFlipper;
    @BindView(R.id.scan_progress_layout)
    ScanProgressLayout scanProgressLayout;
    @BindView(R.id.scan_result_layout)
    ScanResultLayout scanResultLayout;

    private CompositeDisposable disposable = new CompositeDisposable();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scanner, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        disposable.add(AuroraApplication
                .getRelay()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(event -> {
                    switch (event.getSubType()) {
                        case SCAN_COMPLETED:
                        case SCAN_FAILED:
                        case SCAN_CANCELED:
                            scanResultLayout.setResult(event);
                            viewFlipper.setDisplayedChild(2);
                            configScreen(false);
                            break;
                    }
                }).subscribe());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DeviceAnalysisService.isServiceRunning()) {
            viewFlipper.setDisplayedChild(1);
        }
    }

    @Override
    public void onDestroy() {
        configScreen(false);
        super.onDestroy();
    }

    @OnClick(R.id.btn_scan_device)
    public void startDeviceAnalysis() {
        Intent intent = new Intent(requireContext(), DeviceAnalysisService.class);
        requireContext().startService(intent);
        viewFlipper.setDisplayedChild(1);
        AuroraApplication.rxNotify(new Event(Event.SubType.SCAN_INIT));
        configScreen(true);
    }

    private void configScreen(boolean keepScreenOn) {
        if (Util.isKeepAwakeEnabled(requireContext())) {
            if (keepScreenOn)
                requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}