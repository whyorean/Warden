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

package com.aurora.warden.ui.sheets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.services.TrackerAnalysisService;
import com.google.android.material.switchmaterial.SwitchMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NukeSheet extends BaseBottomSheet {

    public static final String TAG = "NUKE_SHEET";
    @BindView(R.id.switch_nuke_export)
    SwitchMaterial switchNukeExport;
    @BindView(R.id.switch_denuke)
    SwitchMaterial switchDeNuke;

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_nuke, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
    }

    @OnClick(R.id.btn_positive)
    public void nuke() {
        if (!TrackerAnalysisService.isServiceRunning()) {
            Intent intent = new Intent(requireContext(), TrackerAnalysisService.class);
            intent.putExtra(Constants.INT_EXTRA, switchNukeExport.isChecked() ? 1 : 0);
            intent.putExtra(Constants.INT_EXTRA_2, switchDeNuke.isChecked() ? 1 : 0);
            requireContext().startService(intent);
        }
        dismissAllowingStateLoss();
    }

    @OnClick(R.id.btn_negative)
    public void close() {
        dismissAllowingStateLoss();
    }
}
