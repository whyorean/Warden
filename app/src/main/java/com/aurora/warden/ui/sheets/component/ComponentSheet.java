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

import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.aurora.warden.data.model.items.ComponentItem;
import com.aurora.warden.manager.camtono.CamtonoManager;
import com.aurora.warden.ui.sheets.BaseBottomSheet;
import com.aurora.warden.utils.Log;
import com.aurora.warden.utils.app.PackageUtil;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ComponentSheet extends BaseBottomSheet {

    public static final String TAG = "ACTIVITY_SHEET";

    @BindView(R.id.title)
    AppCompatTextView title;
    @BindView(R.id.subtitle)
    AppCompatTextView subtitle;
    @BindView(R.id.recycler)
    RecyclerView recycler;

    private ComponentType componentType;
    private FastItemAdapter<ComponentItem> fastAdapter;
    private CamtonoManager camtonoManager;

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
            String stringExtra = bundle.getString(Constants.STRING_EXTRA);
            int intExtra = bundle.getInt(Constants.INT_EXTRA, 0);

            if (StringUtils.isNotEmpty(stringExtra)) {
                populateData(stringExtra, intExtra);
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

    private void populateData(String packageName, int type) {
        camtonoManager = CamtonoManager.getInstance(requireContext());
        setupRecycler();

        switch (type) {
            case 0:
                title.setText(getString(R.string.title_activities));
                subtitle.setText(getString(R.string.title_activities_desc));
                componentType = ComponentType.ACTIVITY;
                break;
            case 1:
                title.setText(getString(R.string.title_services));
                subtitle.setText(getString(R.string.title_services_desc));
                componentType = ComponentType.SERVICE;
                break;
            case 2:
                title.setText(getString(R.string.title_receivers));
                subtitle.setText(getString(R.string.title_receivers_desc));
                componentType = ComponentType.RECEIVER;
                break;
            case 3:
                title.setText(getString(R.string.title_providers));
                subtitle.setText(getString(R.string.title_providers_desc));
                componentType = ComponentType.PROVIDER;
                break;
        }

        fetchComponentInfo(packageName, componentType);
    }

    private void setupRecycler() {
        fastAdapter = new FastItemAdapter<>();
        recycler.setAdapter(fastAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    }

    private void fetchComponentInfo(String packageName, ComponentType componentType) {
        disposable.add(Observable.fromCallable(() -> getActivityBundle(packageName, componentType))
                .subscribeOn(Schedulers.io())
                .flatMap(components -> Observable
                        .fromIterable(Arrays.asList(components))
                        .sorted((o1, o2) -> o1.name.compareToIgnoreCase(o2.name))
                        .map(activityInfo -> {
                            boolean isEnabled = camtonoManager.camtono().isEnabled(activityInfo.packageName, activityInfo.name);
                            return new ComponentItem(activityInfo, isEnabled);
                        }))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(activityItems -> fastAdapter.add(activityItems), throwable -> Log.e(throwable.getMessage())));
    }

    private ComponentInfo[] getActivityBundle(String packageName, ComponentType componentType) {
        try {
            final int flags = PackageUtil.getAllFlags();
            PackageManager packageManager = requireContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, flags);

            switch (componentType) {
                case ACTIVITY:
                    return packageInfo.activities;
                case RECEIVER:
                    return packageInfo.receivers;
                case SERVICE:
                    return packageInfo.services;
                case PROVIDER:
                    return packageInfo.providers;
                default:
                    return new ActivityInfo[]{};
            }

        } catch (PackageManager.NameNotFoundException e) {
            return new ActivityInfo[]{};
        }
    }

    private enum ComponentType {
        ACTIVITY,
        SERVICE,
        RECEIVER,
        PROVIDER
    }
}
