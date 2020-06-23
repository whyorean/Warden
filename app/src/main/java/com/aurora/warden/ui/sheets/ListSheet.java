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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.aurora.warden.data.model.items.AppItem;
import com.aurora.warden.data.model.items.DebloatProfileItem;
import com.aurora.warden.data.model.items.LoggerItem;
import com.aurora.warden.data.model.items.ReportItem;
import com.aurora.warden.data.model.items.TrackerItem;
import com.aurora.warden.data.model.items.base.ListItem;
import com.aurora.warden.data.model.report.StaticReport;
import com.aurora.warden.events.Event;
import com.aurora.warden.manager.StaticDataProvider;
import com.aurora.warden.tasks.DebloatProfileTask;
import com.aurora.warden.ui.activities.AppDetailsActivity;
import com.aurora.warden.ui.activities.GenericActivity;
import com.aurora.warden.ui.custom.view.ViewFlipper2;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.utils.app.PackageUtil;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ListSheet extends BaseBottomSheet {

    public static final String TAG = "TRACKER_SHEET";

    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
    @BindView(R.id.title)
    AppCompatTextView txtTitle;
    @BindView(R.id.recycler)
    RecyclerView recycler;

    private Set<StaticReport> staticReportSet;
    private Set<Integer> integerSet;
    private Set<String> stringSet;

    private FastAdapter<ListItem> fastAdapter;
    private ItemAdapter<ListItem> itemAdapter;

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);

        setupRecycler();

        if (getArguments() != null) {

            final Bundle bundle = getArguments();
            final int intExtra = bundle.getInt(Constants.INT_EXTRA, 0);
            final String stringExtra = bundle.getString(Constants.STRING_EXTRA);
            final String listExtra = bundle.getString(Constants.LIST_EXTRA);

            /*
             * INT_EXTRA Values
             * 0 : Trackers
             * 1 : Loggers
             * 2 : Apps
             * 3 : Report
             * 4 : Debloat Profile
             *
             */

            if (intExtra == 0 || intExtra == 1) {
                integerSet = gson.fromJson(listExtra, new TypeToken<Set<Integer>>() {
                }.getType());
            }

            if (intExtra == 2)
                stringSet = gson.fromJson(listExtra, new TypeToken<Set<String>>() {
                }.getType());

            if (intExtra == 3)
                staticReportSet = gson.fromJson(listExtra, new TypeToken<Set<StaticReport>>() {
                }.getType());

            if (integerSet != null && !integerSet.isEmpty()) {
                //Populate Trackers or Loggers
                populateData(intExtra, integerSet);
            } else if (stringSet != null && !stringSet.isEmpty()) {
                //Populate Apps
                populateData(stringExtra, stringSet);
            } else if (staticReportSet != null) {
                //Populate Reports
                populateData(stringExtra, staticReportSet, 0 /*Will use later*/);
            } else {
                //Debloat Profiles
                if (intExtra == 4) {
                    populateData(stringExtra);
                } else
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

    private void populateData(int listType, Set<Integer> integerSet) {

        txtTitle.setText(getString(listType == 0
                ? R.string.action_trackers
                : R.string.action_loggers));

        StaticDataProvider dataProvider = StaticDataProvider.getInstance(requireContext());

        disposable.add(Observable.fromIterable(listType == 0
                ? dataProvider.getKnownTrackersList().values()
                : dataProvider.getKnownLoggerList().values())
                .filter(obj -> {
                    if (obj instanceof Tracker) {
                        return integerSet.contains(((Tracker) obj).getId());
                    } else {
                        return integerSet.contains(((Logger) obj).getId());
                    }
                })
                .map(obj -> {
                    if (obj instanceof Tracker)
                        return new TrackerItem((Tracker) obj);
                    else
                        return new LoggerItem((Logger) obj);
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((items, throwable) -> {
                    dispatchAppsToAdapter(items);
                }));
    }

    private void populateData(String title, Set<String> stringList) {
        txtTitle.setText(title);
        PackageManager packageManager = requireContext().getPackageManager();
        disposable.add(Observable.fromIterable(stringList)
                .map(packageName -> PackageUtil.getMinimalAppByPackageInfo(packageManager, packageName))
                .map(AppItem::new)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((items, throwable) -> {
                    dispatchAppsToAdapter(items);
                }));
    }

    private void populateData(String title, Set<StaticReport> staticReports, int reserved) {
        txtTitle.setText(title);
        disposable.add(Observable.fromIterable(staticReports)
                .map(ReportItem::new)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((items, throwable) -> {
                    dispatchAppsToAdapter(items);
                }));
    }

    private void populateData(String title) {
        txtTitle.setText(title);
        disposable.add(Observable.fromCallable(() -> new DebloatProfileTask(requireContext()).getAllProfiles())
                .flatMap(debloatProfiles -> Observable.fromIterable(debloatProfiles)
                        .map(DebloatProfileItem::new))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::dispatchAppsToAdapter, throwable -> {
                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    dismissAllowingStateLoss();
                }));
    }

    private void dispatchAppsToAdapter(List<? extends ListItem> listItems) {
        final FastAdapterDiffUtil fastAdapterDiffUtil = FastAdapterDiffUtil.INSTANCE;
        final DiffUtil.DiffResult diffResult = fastAdapterDiffUtil.calculateDiff(itemAdapter, listItems);
        fastAdapterDiffUtil.set(itemAdapter, diffResult);
        recycler.scheduleLayoutAnimation();

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

            if (item instanceof AppItem) {
                final Intent intent = new Intent(requireContext(), AppDetailsActivity.class);
                intent.putExtra(Constants.INTENT_PACKAGE_NAME, ((AppItem) item).getApp().getPackageName());
                startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            }

            if (item instanceof ReportItem) {
                Long reportId = ((ReportItem) item).getStaticReport().getReportId();
                AuroraApplication.rxNotify(new Event(Event.SubType.SCAN_REPORT_CHANGED, reportId.toString()));
                dismissAllowingStateLoss();
            }

            if (item instanceof DebloatProfileItem) {
                final Intent intent = new Intent(requireContext(), GenericActivity.class);
                intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_DEBLOAT);
                intent.putExtra(Constants.STRING_EXTRA, gson.toJson(((DebloatProfileItem) item).getDebloatProfile()));
                startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
                dismissAllowingStateLoss();
            }

            return false;
        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        recycler.setAdapter(fastAdapter);
    }
}
