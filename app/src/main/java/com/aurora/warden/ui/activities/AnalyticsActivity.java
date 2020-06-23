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

package com.aurora.warden.ui.activities;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.aurora.warden.data.model.items.LoggerItem;
import com.aurora.warden.data.model.items.TrackerItem;
import com.aurora.warden.data.model.items.base.ListItem;
import com.aurora.warden.tasks.ApkClassTask;
import com.aurora.warden.tasks.ComponentAnalyzerTask;
import com.aurora.warden.ui.custom.layout.AnalyticsLayout;
import com.aurora.warden.ui.custom.layout.MultiTextLayout;
import com.aurora.warden.ui.sheets.component.ComponentSheet;
import com.aurora.warden.ui.sheets.component.LiveComponentSheet;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.utils.app.PackageUtil;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AnalyticsActivity extends BaseActivity {

    @BindView(R.id.action1)
    AppCompatImageView action1;
    @BindView(R.id.multi_text_layout)
    MultiTextLayout multiTextLayout;
    @BindView(R.id.action2)
    AppCompatImageView action2;
    @BindView(R.id.img)
    ImageView imgScan;
    @BindView(R.id.txt_status)
    AppCompatTextView txtStatus;
    @BindView(R.id.layout_shit)
    AnalyticsLayout analyticsLayout;
    @BindView(R.id.fab_disable)
    ExtendedFloatingActionButton fabDisable;

    private Uri uri;
    private int intExtra;
    private String packageName;
    private Set<ComponentInfo> components = new HashSet<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);
        setupToolbar();

        final Intent bundle = getIntent();
        if (bundle != null) {
            uri = getIntent().getData();
            packageName = bundle.getStringExtra(Constants.STRING_EXTRA);
            intExtra = bundle.getIntExtra(Constants.INT_EXTRA, 0);
            startScan();
        } else {
            finishAfterTransition();
        }
    }

    private void setupToolbar() {
        action1.setImageDrawable(getDrawable(R.drawable.ic_arrow_left));
        action1.setOnClickListener(v -> onBackPressed());
        multiTextLayout.setTxtPrimary(getString(R.string.app_name));
        multiTextLayout.setTxtSecondary(getString(R.string.action_trackers));
    }

    @OnClick(R.id.fab_disable)
    public void disableComponents() {
        LiveComponentSheet componentSheet = new LiveComponentSheet();
        componentSheet.setComponentInfoHashSet(components);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment fragment = fragmentManager.findFragmentByTag(ComponentSheet.TAG);

        if (fragment != null)
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        componentSheet.setCancelable(false);
        fragmentManager.beginTransaction()
                .add(componentSheet, ComponentSheet.TAG)
                .commitAllowingStateLoss();
    }

    private void startScan() {
        analyticsLayout.setType(intExtra);
        try {
            int flags = PackageUtil.getAllFlags();
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, flags);

            disposable.add(Observable.fromCallable(() -> new ApkClassTask(this, uri).getAllClasses())
                    .subscribeOn(Schedulers.io())
                    .map(classNames -> intExtra == 0
                            ? new ComponentAnalyzerTask(this, classNames, packageInfo).getTrackerComponentBundle()
                            : new ComponentAnalyzerTask(this, classNames, packageInfo).getLoggerComponentBundle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bundle -> {
                        parseToListItem(bundle);
                        if (!bundle.getComponents().isEmpty() /*&& AuroraApplication.isRooted()*/) {
                            components.addAll(bundle.getComponents());
                            fabDisable.show();
                        } else {
                            fabDisable.hide();
                        }
                    }, throwable -> {
                        ViewUtil.switchText(this, txtStatus, getString(R.string.txt_status_failed));
                        ViewUtil.switchImage(this, imgScan, getResources().getDrawable(R.drawable.ic_undraw_fail));

                    }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseToListItem(ComponentAnalyzerTask.Bundle bundle) {
        disposable.add(Observable.fromIterable(intExtra == 0
                ? bundle.getTrackerSet()
                : bundle.getLoggerSet())
                .map(item -> {
                    if (intExtra == 0)
                        return new TrackerItem((Tracker) item);
                    else
                        return new LoggerItem((Logger) item);
                })
                .toList()
                .subscribe(this::populateData, Throwable::printStackTrace));
    }

    private void populateData(List<ListItem> listItems) {
        if (listItems.isEmpty()) {
            ViewUtil.switchImage(this, imgScan, getResources().getDrawable(R.drawable.ic_undraw_result));
            ViewUtil.switchText(this, txtStatus,
                    getString(intExtra == 0
                            ? R.string.txt_status_no_tracker
                            : R.string.txt_status_no_loggers
                    ));
            analyticsLayout.switchState(AnalyticsLayout.State.EMPTY);
        } else {
            ViewUtil.switchImage(this, imgScan, getResources().getDrawable(R.drawable.ic_undraw_code_review));
            ViewUtil.switchText(this, txtStatus, StringUtils.joinWith(StringUtils.SPACE,
                    listItems.size(),
                    getString(intExtra == 0
                            ? R.string.txt_status_tracker
                            : R.string.txt_status_loggers)
            ));
            analyticsLayout.switchState(AnalyticsLayout.State.RESULT);
            analyticsLayout.setTrackerData(listItems);
        }
    }
}
