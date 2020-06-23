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
import android.app.AppOpsManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.App;
import com.aurora.warden.data.model.items.AppUsageItem;
import com.aurora.warden.ui.activities.AppDetailsActivity;
import com.aurora.warden.ui.custom.view.ViewFlipper2;
import com.aurora.warden.ui.sheets.AppPermissionSheet;
import com.aurora.warden.utils.DateTimeUtil;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.viewmodel.AppUsageViewModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppUsageFragment extends Fragment {

    @BindView(R.id.chart)
    PieChart chart;
    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
    @BindView(R.id.recycler)
    RecyclerView recycler;

    private AppUsageViewModel viewModel;
    private FastAdapter<AppUsageItem> fastAdapter;
    private ItemAdapter<AppUsageItem> itemAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_app_usage, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopPieChart();
        setupRecycler();

        viewModel = new ViewModelProvider(this).get(AppUsageViewModel.class);
        viewModel.getRecentApps().observe(getViewLifecycleOwner(), appUsageItems -> {
            setData(appUsageItems);
            dispatchAppsToAdapter(appUsageItems);
        });
    }

    private void dispatchAppsToAdapter(List<AppUsageItem> appUsageItems) {
        itemAdapter.clear();
        itemAdapter.add(appUsageItems);
        recycler.scheduleLayoutAnimation();

        if (itemAdapter != null && itemAdapter.getAdapterItems().size() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
            chart.clear();
        }
    }

    @OnClick(R.id.chip_daily)
    public void fetchDailyUsageStats() {
        viewModel.fetchRecentApps(UsageStatsManager.INTERVAL_DAILY);
    }

    @OnClick(R.id.chip_weekly)
    public void fetchWeeklyUsageStats() {
        viewModel.fetchRecentApps(UsageStatsManager.INTERVAL_WEEKLY);
    }

    @OnClick(R.id.chip_monthly)
    public void fetchMonthlyUsageStats() {
        viewModel.fetchRecentApps(UsageStatsManager.INTERVAL_MONTHLY);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
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
    }

    private void setupTopPieChart() {

        final int colorBackground = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.colorBackground);
        final int textColorPrimary = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.textColorPrimary);
        final int colorAccent = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.colorAccent);

        chart.setNoDataTextColor(colorAccent);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDragDecelerationFrictionCoef(0.95f);

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(colorBackground);
        chart.setHoleRadius(56f);

        chart.setTransparentCircleColor(colorBackground);
        chart.setTransparentCircleAlpha(200);
        chart.setTransparentCircleRadius(64f);

        chart.setDrawCenterText(true);
        chart.setDrawEntryLabels(false);

        chart.setRotationAngle(180f);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        // add a selection listener
        //chart.setOnChartValueSelectedListener(this);

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextColor(textColorPrimary);
        legend.setWordWrapEnabled(true);
        legend.setDrawInside(false);
        legend.setEnabled(true);
    }

    private void setData(List<AppUsageItem> recentItemList) {
        final int textColorPrimary = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.textColorPrimary);
        final ArrayList<PieEntry> entries = new ArrayList<>();
        long totalDuration = 0;

        for (AppUsageItem appUsageItem : recentItemList.subList(0, Math.min(6, recentItemList.size()))) {
            App app = appUsageItem.getApp();
            if (app.getUsageStats() != null) {
                long duration = app.getUsageStats().getTotalTimeInForeground();
                totalDuration += duration;
                entries.add(new PieEntry(duration, app.getDisplayName()));
            }
        }

        final PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ViewUtil.getColors(requireContext()));
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLineColor(textColorPrimary);
        dataSet.setValueLinePart1OffsetPercentage(2);

        final PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(textColorPrimary);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                return DateTimeUtil.millisToTimeDurationMinimal((long) pieEntry.getValue());
            }
        });

        chart.setCenterText(generateCenterSpannableText(totalDuration));
        chart.setCenterTextColor(textColorPrimary);
        chart.setData(pieData);
        chart.highlightValues(null);
        chart.animateY(1000, Easing.EaseInOutQuad);
        chart.invalidate();
    }

    private SpannableString generateCenterSpannableText(long totalDuration) {
        return new SpannableString(DateTimeUtil.millisToTimeDurationMinimal(totalDuration));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        final AppOpsManager appOpsManager = (AppOpsManager) requireContext().getSystemService(Context.APP_OPS_SERVICE);

        if (appOpsManager != null) {
            int mode = appOpsManager.checkOpNoThrow("android:get_usage_stats",
                    Process.myUid(),
                    requireContext().getPackageName());

            final ArrayList<String> stringArrayList = new ArrayList<>();

            if (mode != AppOpsManager.MODE_ALLOWED) {
                stringArrayList.add(Manifest.permission.PACKAGE_USAGE_STATS);
            }

            final FragmentManager fragmentManager = getChildFragmentManager();
            final Fragment fragment = fragmentManager.findFragmentByTag(AppPermissionSheet.TAG);
            if (fragment != null)
                fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();

            if (!stringArrayList.isEmpty()) {
                final Bundle bundle = new Bundle();
                bundle.putStringArrayList(Constants.STRING_EXTRA, stringArrayList);

                final AppPermissionSheet permissionSheet = new AppPermissionSheet();
                permissionSheet.setArguments(bundle);

                fragmentManager.beginTransaction()
                        .add(permissionSheet, AppPermissionSheet.TAG)
                        .commitAllowingStateLoss();
            }
        }
    }
}