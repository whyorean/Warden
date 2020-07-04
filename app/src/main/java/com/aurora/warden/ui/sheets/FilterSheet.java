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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.Filter;
import com.aurora.warden.utils.PrefUtil;
import com.google.android.material.chip.Chip;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterSheet extends BaseBottomSheet {

    public static final String TAG = "FILTER_SHEET";

    @BindView(R.id.title)
    AppCompatTextView title;
    @BindView(R.id.chip_filter_system)
    Chip chipFilterSystem;
    @BindView(R.id.chip_filter_user)
    Chip chipFilterUser;
    @BindView(R.id.chip_filter_disabled)
    Chip chipFilterDisabled;
    @BindView(R.id.chip_filter_debugging)
    Chip chipFilterDebugging;
    @BindView(R.id.chip_filter_stopped)
    Chip chipFilterStopped;
    @BindView(R.id.chip_filter_suspended)
    Chip chipFilterSuspended;
    @BindView(R.id.chip_filter_large_heap)
    Chip chipFilterLargeHeap;
    @BindView(R.id.chip_filter_launchable)
    Chip chipFilterLaunchable;


    private Filter filter;

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_filter, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);

        filter = gson.fromJson(PrefUtil.getString(requireContext(), Constants.PREFERENCE_FILTER), Filter.class);

        if (filter == null)
            filter = Filter.getDefault();

        setupChips();
    }

    @OnClick(R.id.btn_positive)
    public void applyFilter() {
        PrefUtil.putString(requireContext(), Constants.PREFERENCE_FILTER, gson.toJson(filter));
        dismissAllowingStateLoss();
    }

    @OnClick(R.id.btn_negative)
    public void closeFilter() {
        dismissAllowingStateLoss();
    }

    private void setupChips() {
        chipFilterSystem.setChecked(filter.isSystem());
        chipFilterUser.setChecked(filter.isUser());
        chipFilterDisabled.setChecked(filter.isDisabled());
        chipFilterDebugging.setChecked(filter.isDebugging());
        chipFilterStopped.setChecked(filter.isStopped());
        chipFilterSuspended.setChecked(filter.isSuspended());
        chipFilterLargeHeap.setChecked(filter.isLargeHeap());
        chipFilterLaunchable.setChecked(filter.isLaunchable());

        chipFilterSystem.setOnCheckedChangeListener((v, isChecked) -> filter.setSystem(isChecked));
        chipFilterUser.setOnCheckedChangeListener((v, isChecked) -> filter.setUser(isChecked));
        chipFilterDisabled.setOnCheckedChangeListener((v, isChecked) -> filter.setDisabled(isChecked));
        chipFilterDebugging.setOnCheckedChangeListener((v, isChecked) -> filter.setDebugging(isChecked));
        chipFilterStopped.setOnCheckedChangeListener((v, isChecked) -> filter.setStopped(isChecked));
        chipFilterSuspended.setOnCheckedChangeListener((v, isChecked) -> filter.setSuspended(isChecked));
        chipFilterLargeHeap.setOnCheckedChangeListener((v, isChecked) -> filter.setLargeHeap(isChecked));
        chipFilterLaunchable.setOnCheckedChangeListener((v, isChecked) -> filter.setLaunchable(isChecked));
    }
}
