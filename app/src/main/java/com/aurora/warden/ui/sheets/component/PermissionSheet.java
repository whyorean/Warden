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

package com.aurora.warden.ui.sheets.component;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.items.PermissionItem;
import com.aurora.warden.ui.sheets.BaseBottomSheet;
import com.aurora.warden.utils.Log;
import com.aurora.warden.utils.app.PackageUtil;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PermissionSheet extends BaseBottomSheet {

    public static final String TAG = "PROVIDER_SHEET";

    @BindView(R.id.title)
    AppCompatTextView title;
    @BindView(R.id.subtitle)
    AppCompatTextView subtitle;
    @BindView(R.id.recycler)
    RecyclerView recycler;

    private FastAdapter<PermissionItem> fastAdapter;
    private ItemAdapter<PermissionItem> itemAdapter;

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_component, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            packageName = bundle.getString(Constants.STRING_EXTRA);
            if (StringUtils.isNotEmpty(packageName)) {
                populateData(packageName);
            } else {
                dismissAllowingStateLoss();
            }
        } else {
            dismissAllowingStateLoss();
        }
    }

    @OnClick(R.id.img_close)
    public void closeSheet() {
        dismissAllowingStateLoss();
    }

    private void populateData(String packageName) {
        title.setText(getString(R.string.title_permissions));
        subtitle.setText(getString(R.string.title_permission_desc));

        setupRecycler();
        fetchComponentInfo(packageName);
    }

    private void setupRecycler() {
        fastAdapter = new FastItemAdapter<>();
        itemAdapter = new ItemAdapter<>();

        fastAdapter.addAdapter(0, itemAdapter);

        fastAdapter.setOnClickListener((view, activityItemIAdapter, activityItem, integer) -> {
            return false;
        });
        recycler.setAdapter(fastAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    }

    private void fetchComponentInfo(String packageName) {
        disposable.add(Observable.fromCallable(() -> PackageUtil
                .getPermissionsByPackageName(requireContext().getPackageManager(), packageName))
                .subscribeOn(Schedulers.io())
                .flatMap(bundlePermissions -> Observable
                        .fromIterable(bundlePermissions)
                        .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                        .map(PermissionItem::new))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(permissionItem -> itemAdapter.add(permissionItem), throwable -> Log.e(throwable.getMessage())));
    }
}
