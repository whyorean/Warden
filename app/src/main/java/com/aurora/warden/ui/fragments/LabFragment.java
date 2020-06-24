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

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.services.DebloatService;
import com.aurora.warden.services.TrackerAnalysisService;
import com.aurora.warden.ui.sheets.BaseBottomSheet;
import com.aurora.warden.ui.sheets.ListSheet;
import com.aurora.warden.ui.sheets.NukeSheet;
import com.google.android.material.button.MaterialButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LabFragment extends Fragment {

    @BindView(R.id.btn_debloater)
    MaterialButton btnDebloater;
    @BindView(R.id.btn_nuke)
    MaterialButton btnNuke;
    @BindView(R.id.txt_root_required)
    AppCompatTextView txtRootRequired;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lab, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkPermissions();
        if (!AuroraApplication.isRooted()) {
            btnNuke.setEnabled(false);
            txtRootRequired.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TrackerAnalysisService.isServiceRunning()) {
            btnNuke.setEnabled(false);
        }

        if (DebloatService.isServiceRunning()) {
            btnDebloater.setEnabled(false);
        }
    }

    @OnClick(R.id.btn_nuke)
    public void disableAllTrackers() {
        if (!TrackerAnalysisService.isServiceRunning()) {
            showBottomSheet(new NukeSheet(), 0, getString(R.string.action_nuke_it));
        } else {
            Toast.makeText(requireContext(), R.string.string_nuke_running, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_debloater)
    public void startDebloat() {
        showBottomSheet(new ListSheet(), 4, getString(R.string.title_debloat_profiles));
    }

    private void showBottomSheet(BaseBottomSheet baseBottomSheet, int intExtra,
                                 String stringExtra) {
        final FragmentManager fragmentManager = getChildFragmentManager();
        final Fragment fragment = fragmentManager.findFragmentByTag(baseBottomSheet.getTag());
        final Bundle bundle = new Bundle();

        if (fragment != null)
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();

        bundle.putInt(Constants.INT_EXTRA, intExtra);
        bundle.putString(Constants.STRING_EXTRA, stringExtra);
        baseBottomSheet.setArguments(bundle);
        fragmentManager.beginTransaction().add(baseBottomSheet, baseBottomSheet.getTag()).commitAllowingStateLoss();
    }

    private void checkPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1337);
    }
}