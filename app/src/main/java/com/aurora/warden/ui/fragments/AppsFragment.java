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
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.Sort;
import com.aurora.warden.data.model.Filter;
import com.aurora.warden.data.model.items.AppItem;
import com.aurora.warden.ui.activities.AppDetailsActivity;
import com.aurora.warden.ui.custom.view.ViewFlipper2;
import com.aurora.warden.ui.sheets.FilterSheet;
import com.aurora.warden.utils.PrefUtil;
import com.aurora.warden.utils.Util;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.utils.callback.AppDiffCallback;
import com.aurora.warden.viewmodel.AppViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class AppsFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.chip_group)
    ChipGroup chipGroup;

    private AppViewModel model;
    private FastAdapter<AppItem> fastAdapter;
    private ItemAdapter<AppItem> itemAdapter;
    private List<AppItem> originalAppList = new ArrayList<>();
    private Filter filter;
    private SharedPreferences sharedPreferences;
    private CompositeDisposable disposable = new CompositeDisposable();


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_apps, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler();
        setupChip();
        sharedPreferences = Util.getPrefs(requireContext());
        model = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        model.getAppList().observe(getViewLifecycleOwner(), appItems -> {
            originalAppList.addAll(appItems);
            filterAndDispatch(appItems);
        });

        if (model.getAppList().getValue() == null)
            model.fetchAllApps();

        swipeContainer.setOnRefreshListener(() -> model.fetchAllApps());
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        try {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {

        }
        super.onDestroy();
    }

    @OnClick(R.id.filter_fab)
    public void showFilterDialog() {
        if (getChildFragmentManager().findFragmentByTag(FilterSheet.TAG) == null) {
            final FilterSheet filterSheet = new FilterSheet();
            filterSheet.show(getChildFragmentManager(), FilterSheet.TAG);
        }
    }

    private void filterAndDispatch(List<AppItem> appItems) {
        //Fetch new filter
        filter = new Gson().fromJson(PrefUtil.getString(requireContext(), Constants.PREFERENCE_FILTER), Filter.class);

        //Get default filter, if filter not set
        if (filter == null)
            filter = Filter.getDefault();

        //Clear old item list
        if (itemAdapter != null)
            itemAdapter.clear();

        //Filter the apps
        disposable.add(Observable.fromIterable(appItems)
                .filter(appItem -> !filter.isSystem() || appItem.getApp().isSystem())
                .filter(appItem -> !filter.isUser() || !appItem.getApp().isSystem())
                .filter(appItem -> !filter.isDisabled() || !appItem.getApp().getApplicationInfo().enabled)
                .filter(appItem -> !filter.isDebugging() || (appItem.getApp().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0)
                .filter(appItem -> !filter.isStopped() || (appItem.getApp().getApplicationInfo().flags & ApplicationInfo.FLAG_STOPPED) != 0)
                .filter(appItem -> !filter.isLargeHeap() || (appItem.getApp().getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0)
                .filter(appItem -> !filter.isLaunchable() || (requireContext().getPackageManager().getLaunchIntentForPackage(appItem.getApp().getPackageName())) != null)
                .filter(appItem -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        return !filter.isSuspended() || (appItem.getApp().getApplicationInfo().flags & ApplicationInfo.FLAG_SUSPENDED) != 0;
                    } else
                        return true;
                })
                .toList()
                .subscribe(this::dispatchAppsToAdapter, Throwable::printStackTrace));
    }

    private void dispatchAppsToAdapter(List<AppItem> appItems) {
        final FastAdapterDiffUtil fastAdapterDiffUtil = FastAdapterDiffUtil.INSTANCE;
        final AppDiffCallback diffCallback = new AppDiffCallback();
        final DiffUtil.DiffResult diffResult = fastAdapterDiffUtil.calculateDiff(itemAdapter, appItems, diffCallback);
        fastAdapterDiffUtil.set(itemAdapter, diffResult);

        swipeContainer.setRefreshing(false);

        if (itemAdapter != null && itemAdapter.getAdapterItems().size() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
        }
    }

    private void setupRecycler() {
        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();

        fastAdapter.addAdapter(0, itemAdapter);
        fastAdapter.setOnClickListener((view, adapter, item, position) -> {
            final Intent intent = new Intent(requireContext(), AppDetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, item.getApp().getPackageName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return false;
        });

        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        recycler.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));
        recycler.setAdapter(fastAdapter);

        new FastScrollerBuilder(recycler)
                .useMd2Style()
                .build();
    }

    private void setupChip() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.sort_name_az:
                    sortAppsBy(Sort.NAME_AZ);
                    break;
                case R.id.sort_name_za:
                    sortAppsBy(Sort.NAME_ZA);
                    break;
                case R.id.sort_package:
                    sortAppsBy(Sort.PACKAGE);
                    break;
                case R.id.sort_uid:
                    sortAppsBy(Sort.UID);
                    break;
                case R.id.sort_date_updated:
                    sortAppsBy(Sort.DATE_UPDATED);
                    break;
                case R.id.sort_date_installed:
                    sortAppsBy(Sort.DATE_INSTALLED);
                    break;
            }
        });
    }

    private void sortAppsBy(Sort sort) {
        if (itemAdapter != null) {
            sortAdapterItems(sort);
        }
    }

    public void sortAdapterItems(Sort sort) {
        switch (sort) {
            case NAME_AZ:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App1.getApp().getDisplayName().compareToIgnoreCase(App2.getApp().getDisplayName()));
                break;
            case NAME_ZA:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App2.getApp().getDisplayName().compareToIgnoreCase(App1.getApp().getDisplayName()));
                break;
            case UID:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        Integer.compare(App1.getApp().getApplicationInfo().uid, App2.getApp().getApplicationInfo().uid));
                break;
            case PACKAGE:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App1.getApp().getPackageName().compareTo(App2.getApp().getPackageName()));
                break;
            case DATE_UPDATED:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App2.getApp().getLastUpdated().compareTo(App1.getApp().getLastUpdated()));
                break;
            case DATE_INSTALLED:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App2.getApp().getInstalledTime().compareTo(App1.getApp().getInstalledTime()));
                break;
        }
        fastAdapter.notifyAdapterDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREFERENCE_FILTER:
                filterAndDispatch(originalAppList);
                break;
        }
    }
}