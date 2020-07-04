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

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.aurora.warden.data.model.report.StaticReport;
import com.aurora.warden.manager.StaticDataProvider;
import com.aurora.warden.ui.custom.layout.DisplayLayout;
import com.aurora.warden.ui.sheets.BaseBottomSheet;
import com.aurora.warden.ui.sheets.ListSheet;
import com.aurora.warden.utils.DateTimeUtil;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.viewmodel.StatsViewModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class StatisticsFragment extends Fragment {

    @BindView(R.id.top_tracker_chart)
    PieChart trackerChart;
    @BindView(R.id.top_logger_chart)
    PieChart loggerChart;
    @BindView(R.id.view_flipper)
    ViewFlipper viewFlipper;
    @BindView(R.id.txt_apps_count)
    DisplayLayout txtAppsCount;
    @BindView(R.id.txt_shit_count)
    DisplayLayout txtShitCount;
    @BindView(R.id.layout_timestamp)
    RelativeLayout layoutTimestamp;
    @BindView(R.id.txt_timestamp)
    AppCompatTextView txtTimestamp;

    private int textColorPrimary = Color.BLACK;
    private int appCount = 0;
    private int trackerCount = 0;
    private int loggerCount = 0;

    private Set<StaticReport> staticReportSet = new HashSet<>();
    private Set<Integer> trackerIdList = new HashSet<>();
    private Set<Integer> loggerIdList = new HashSet<>();

    private StatsViewModel viewModel;
    private StaticDataProvider staticDataProvider;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupDisplayText();
        setupPieChart(trackerChart);
        setupPieChart(loggerChart);

        textColorPrimary = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.textColorPrimary);
        staticDataProvider = StaticDataProvider.getInstance(requireContext());

        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);
        viewModel.getRecentApps().observe(getViewLifecycleOwner(), staticReports -> {
            staticReportSet.addAll(staticReports);
            populateReportData(staticReports.get(0));
        });

        disposable.add(AuroraApplication
                .getRelay()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(event -> {
                    switch (event.getSubType()) {
                        case SCAN_REPORT_CHANGED:
                            final String stringExtra = event.getStringExtra();
                            final Long reportId = Long.parseLong(stringExtra);
                            switchReport(reportId);
                            break;
                        case NAVIGATED:
                            break;
                    }
                })
                .subscribe());
    }

    @OnClick(R.id.chip_tracker)
    public void showTrackers() {
        viewFlipper.setDisplayedChild(0);
        txtShitCount.setTxtPrimary(String.valueOf(trackerCount));
        txtShitCount.setTxtSecondary(getString(R.string.action_trackers));

        txtShitCount.setOnClickListener(v -> showListSheet(new ListSheet(),
                0,
                null,
                trackerIdList)
        );
    }

    @OnClick(R.id.chip_logger)
    public void showLoggers() {
        viewFlipper.setDisplayedChild(1);
        txtShitCount.setTxtPrimary(String.valueOf(loggerCount));
        txtShitCount.setTxtSecondary(getString(R.string.action_loggers));

        txtShitCount.setOnClickListener(v -> showListSheet(new ListSheet(),
                1,
                null,
                loggerIdList)
        );
    }

    @OnClick(R.id.txt_timestamp)
    public void showReports() {
        showListSheet(new ListSheet(),
                3,
                getString(R.string.txt_reports),
                staticReportSet);
    }

    private void switchReport(Long reportId) {
        for (StaticReport staticReport : staticReportSet) {
            if (staticReport.getReportId().equals(reportId)) {
                if (isAdded()) {
                    populateReportData(staticReport);
                }
                break;
            }
        }
    }

    private void populateReportData(StaticReport staticReport) {
        setTrackerData(staticReport.getTrackerAppMap());
        setLoggerData(staticReport.getLoggerAppMap());

        trackerIdList = staticReport.getTrackerAppMap().keySet();
        loggerIdList = staticReport.getLoggerAppMap().keySet();

        appCount = staticReport.getPackageList().size();
        trackerCount = staticReport.getTrackerAppMap().size();
        loggerCount = staticReport.getLoggerAppMap().size();

        layoutTimestamp.setVisibility(View.VISIBLE);
        txtTimestamp.setText(StringUtils.joinWith(" : ",
                getString(R.string.txt_reports),
                DateTimeUtil.millisToDay(staticReport.getReportId())));

        txtAppsCount.setOnClickListener(v -> showListSheet(new ListSheet(),
                2,
                getString(R.string.menu_apps),
                new HashSet<>(staticReport.getPackageList()))
        );

        setupDisplayText();
    }

    private void setupDisplayText() {
        txtAppsCount.setTxtPrimary(String.valueOf(appCount));
        txtAppsCount.setTxtSecondary(getString(R.string.menu_apps));

        txtShitCount.setTxtPrimary(String.valueOf(trackerCount));
        txtShitCount.setTxtSecondary(getString(R.string.action_trackers));
        txtShitCount.setOnClickListener(v -> showListSheet(new ListSheet(),
                0,
                getString(R.string.action_trackers),
                trackerIdList)
        );
    }

    private void setupPieChart(PieChart pieChart) {

        final int colorBackground = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.colorBackground);
        final int textColorPrimary = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.textColorPrimary);
        final int colorAccent = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.colorAccent);

        pieChart.setNoDataTextColor(colorAccent);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(colorBackground);
        pieChart.setHoleRadius(56f);

        pieChart.setTransparentCircleColor(colorBackground);
        pieChart.setTransparentCircleAlpha(200);
        pieChart.setTransparentCircleRadius(64f);

        pieChart.setDrawCenterText(true);
        pieChart.setDrawEntryLabels(false);

        pieChart.setRotationAngle(180f);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextColor(textColorPrimary);
        legend.setWordWrapEnabled(true);
        legend.setDrawInside(false);
        legend.setEnabled(true);
    }

    private void setTrackerData(Map<Integer, Set<String>> trackerCountMap) {
        final HashMap<Integer, Tracker> trackerHashMap = staticDataProvider.getKnownTrackersList();
        final ArrayList<PieEntry> entries = new ArrayList<>();

        disposable.add(Observable.fromIterable(trackerCountMap.entrySet())
                .sorted((o1, o2) -> Integer.compare(o2.getValue().size(), o1.getValue().size()))
                .take(Math.min(trackerCountMap.size(), 6))
                .doOnComplete(() -> {
                    addDataSetToPieChart(trackerChart, entries, getString(R.string.action_trackers));
                    trackerChart.animateY(1000, Easing.EaseInOutQuad);
                })
                .subscribe(trackerId -> {
                    final Tracker tracker = trackerHashMap.get(trackerId.getKey());
                    final int count = trackerCountMap.get(trackerId.getKey()).size();
                    if (tracker != null) {
                        entries.add(new PieEntry(
                                count,
                                tracker.getName(),
                                tracker)
                        );
                    }
                }));

        trackerChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Object obj = e.getData();
                if (obj != null) {
                    Tracker tracker = (Tracker) obj;
                    showListSheet(new ListSheet(),
                            2,
                            tracker.getName(),
                            trackerCountMap.get(tracker.getId()));
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void setLoggerData(Map<Integer, Set<String>> loggerCountMap) {
        final HashMap<Integer, Logger> trackerHashMap = staticDataProvider.getKnownLoggerList();
        final ArrayList<PieEntry> entries = new ArrayList<>();

        disposable.add(Observable.fromIterable(loggerCountMap.entrySet())
                .sorted((o1, o2) -> Integer.compare(o2.getValue().size(), o1.getValue().size()))
                .take(Math.min(loggerCountMap.size(), 6))
                .doOnComplete(() -> {
                    addDataSetToPieChart(loggerChart, entries, getString(R.string.action_loggers));
                    loggerChart.animateY(1400, Easing.EaseInOutQuad);
                })
                .subscribe(loggerId -> {
                    final Logger logger = trackerHashMap.get(loggerId.getKey());
                    final int count = loggerCountMap.get(loggerId.getKey()).size();
                    if (logger != null) {
                        entries.add(new PieEntry(
                                count,
                                logger.getName(),
                                logger)
                        );
                    }
                }));

        loggerChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Object obj = e.getData();
                if (obj != null) {
                    final Logger logger = (Logger) obj;
                    showListSheet(new ListSheet(),
                            2,
                            logger.getName(),
                            loggerCountMap.get(logger.getId()));
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void addDataSetToPieChart(PieChart pieChart, List<PieEntry> pieEntries, String centerText) {
        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ViewUtil.getColors(requireContext()));
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLineColor(textColorPrimary);
        dataSet.setValueLinePart1OffsetPercentage(2);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(textColorPrimary);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                return StringUtils.joinWith(StringUtils.SPACE,
                        (int) pieEntry.getValue(),
                        pieEntry.getValue() > 1 ? "Apps" : "App");
            }
        });
        pieChart.setCenterText(centerText);
        pieChart.setCenterTextColor(textColorPrimary);
        pieChart.setData(pieData);
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }

    private void showListSheet(BaseBottomSheet baseBottomSheet, int intExtra,
                               String stringExtra,
                               Set<?> genericSet) {
        final FragmentManager fragmentManager = getChildFragmentManager();
        final Fragment fragment = fragmentManager.findFragmentByTag(ListSheet.TAG);
        final Bundle bundle = new Bundle();

        if (fragment != null)
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();

        bundle.putInt(Constants.INT_EXTRA, intExtra);
        bundle.putString(Constants.STRING_EXTRA, stringExtra);
        bundle.putString(Constants.LIST_EXTRA, new Gson().toJson(genericSet));

        baseBottomSheet.setArguments(bundle);
        fragmentManager.beginTransaction().add(baseBottomSheet, ListSheet.TAG).commitAllowingStateLoss();
    }
}