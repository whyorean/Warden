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

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.App;
import com.aurora.warden.data.model.AppData;
import com.aurora.warden.data.model.HiddenApp;
import com.aurora.warden.manager.HiddenAppsManager;
import com.aurora.warden.manager.camtono.CamtonoManager;
import com.aurora.warden.ui.custom.layout.MultiTextLayout;
import com.aurora.warden.ui.custom.layout.app.AppLayout;
import com.aurora.warden.ui.custom.layout.app.BundleCountLayout;
import com.aurora.warden.ui.custom.view.CircleMenuView;
import com.aurora.warden.ui.sheets.AppPermissionSheet;
import com.aurora.warden.ui.sheets.BaseBottomSheet;
import com.aurora.warden.ui.sheets.component.ComponentSheet;
import com.aurora.warden.ui.sheets.component.PermissionSheet;
import com.aurora.warden.utils.DateTimeUtil;
import com.aurora.warden.utils.Log;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.utils.app.AppQueryUtil;
import com.aurora.warden.utils.app.PackageUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class AppDetailsActivity extends BaseActivity implements AppQueryUtil.Callback {

    @BindView(R.id.action1)
    AppCompatImageView action1;
    @BindView(R.id.multi_text_layout)
    MultiTextLayout multiTextLayout;
    @BindView(R.id.action2)
    AppCompatImageView action2;

    @BindView(R.id.txt_category)
    TextView textCategory;
    @BindView(R.id.txt_sdk)
    TextView textSDK;
    @BindView(R.id.txt_bundle)
    TextView textBundle;
    @BindView(R.id.txt_installed)
    TextView textInstalled;
    @BindView(R.id.txt_updated)
    TextView textUpdated;
    @BindView(R.id.txt_installer)
    TextView textInstaller;

    @BindView(R.id.txt_size)
    TextView txtSize;
    @BindView(R.id.txt_cache)
    TextView txtCache;
    @BindView(R.id.txt_data)
    TextView txtData;
    @BindView(R.id.txt_total)
    TextView txtTotal;

    @BindView(R.id.card_activities)
    BundleCountLayout cardActivities;
    @BindView(R.id.card_providers)
    BundleCountLayout cardProviders;
    @BindView(R.id.card_services)
    BundleCountLayout cardServices;
    @BindView(R.id.card_receivers)
    BundleCountLayout cardReceivers;
    @BindView(R.id.card_permissions)
    BundleCountLayout cardPermissions;
    @BindView(R.id.app_bundle)
    AppLayout appBundle;
    @BindView(R.id.view_advance)
    FrameLayout viewAdvance;

    boolean isEnabled = true;
    boolean isSuspended = false;
    boolean isHidden = true;

    private App app;
    private String packageName;
    private CamtonoManager camtonoManager;
    private PackageManager packageManager;
    private HiddenAppsManager hiddenAppsManager;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        if (intent != null) {
            packageName = intent.getStringExtra(Constants.INTENT_PACKAGE_NAME);
            if (StringUtils.isNotEmpty(packageName)) {
                init();
            }
        } else {
            finishAfterTransition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    private void init() {
        camtonoManager = CamtonoManager.getInstance(this);
        packageManager = getPackageManager();
        hiddenAppsManager = new HiddenAppsManager(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            AppQueryUtil.queryAppSize(this, packageName, this);

        setupToolbar();
        fetchAppDetails();

        if (AuroraApplication.isRooted()) {
            setupAdvanceActions();
        }

        disposable.add(AuroraApplication
                .getRelay()
                .subscribe(event -> {
                    String intentPackage = event.getStringExtra();
                    switch (event.getSubType()) {
                        case PACKAGE_UNINSTALLED:
                            if (StringUtils.isNotEmpty(intentPackage) && intentPackage.equals(packageName)) {
                                Toast.makeText(this, "App uninstalled", Toast.LENGTH_SHORT).show();
                                finishAfterTransition();
                            }
                        case PACKAGE_INSTALLED:
                            /*Least likely to get triggered, if at all mostly due to unhide/unsuspend*/
                            if (StringUtils.isNotEmpty(intentPackage) && intentPackage.equals(packageName)) {
                                Toast.makeText(this, "App reinstated", Toast.LENGTH_SHORT).show();
                                finishAfterTransition();
                            }
                            break;
                        case PACKAGE_SUSPENDED:
                            if (StringUtils.isNotEmpty(intentPackage) && intentPackage.equals(packageName)) {
                                Toast.makeText(this, "App suspended", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case PACKAGE_UNSUSPENDED:
                            if (StringUtils.isNotEmpty(intentPackage) && intentPackage.equals(packageName)) {
                                Toast.makeText(this, "App un-suspended", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case PACKAGE_CLEARED:
                            if (StringUtils.isNotEmpty(intentPackage) && intentPackage.equals(packageName)) {
                                Toast.makeText(this, "App cleared", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case PACKAGE_CHANGED:
                            if (StringUtils.isNotEmpty(intentPackage) && intentPackage.equals(packageName)) {
                                Toast.makeText(this, "App configuration changed", Toast.LENGTH_SHORT).show();
                                //finishAfterTransition();
                            }
                            break;
                    }
                }));
    }

    private void setupToolbar() {
        action1.setImageDrawable(getDrawable(R.drawable.ic_arrow_left));
        action1.setOnClickListener(v -> onBackPressed());
        multiTextLayout.setTxtPrimary(getString(R.string.app_name));
        multiTextLayout.setTxtSecondary(getString(R.string.app_details));
    }

    private void setupAdvanceActions() {
        try {
            int flags = PackageManager.GET_META_DATA;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                flags = flags | PackageManager.GET_DISABLED_COMPONENTS;
            } else {
                flags = flags | PackageManager.MATCH_DISABLED_COMPONENTS;
            }
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, flags);
            isEnabled = applicationInfo.enabled;
        } catch (Exception ignore) {
        }

        isHidden = hiddenAppsManager.isHidden(packageName);

        List<Integer> iconList = new ArrayList<>();
        iconList.add(R.drawable.ic_uninstall);
        iconList.add(isEnabled ? R.drawable.ic_disable : R.drawable.ic_enable);
        iconList.add(isHidden ? R.drawable.ic_unhide : R.drawable.ic_hide);
        iconList.add(R.drawable.ic_clear);

        List<Integer> colorList = new ArrayList<>();
        colorList.add(R.color.colorShade01);
        colorList.add(R.color.colorShade02);
        colorList.add(R.color.colorShade03);
        colorList.add(R.color.colorShade04);

        List<Integer> actionList = new ArrayList<>();
        actionList.add(R.string.action_uninstall);
        actionList.add(isEnabled ? R.string.action_disable : R.string.action_enable);
        actionList.add(isHidden ? R.string.action_unhide : R.string.action_hide);
        actionList.add(R.string.action_clear);

        if (Build.VERSION.SDK_INT >= 29) {
            try {
                isSuspended = packageManager.isPackageSuspended(packageName);
                colorList.add(R.color.colorShade05);
                iconList.add(isSuspended ? R.drawable.ic_unsuspend : R.drawable.ic_suspend);
                actionList.add(isSuspended ? R.string.action_unsuspend : R.string.action_suspend);
            } catch (Exception ignored) {
            }
        }

        CircleMenuView circleMenuView = new CircleMenuView(this, iconList, colorList, actionList);
        circleMenuView.setEventListener(new CircleMenuView.EventListener() {
            @Override
            public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {
                super.onButtonClickAnimationStart(view, buttonIndex);
                switch (buttonIndex) {
                    case 0:
                        Log.d("Package Uninstall");
                        camtonoManager.camtono().uninstall(packageName);
                        break;
                    case 1:
                        Log.d("Package Enable/Disable");
                        if (isEnabled)
                            camtonoManager.camtono().disable(packageName);
                        else
                            camtonoManager.camtono().enable(packageName);
                        break;
                    case 2:
                        Log.d("Package Hide/UnHide");
                        if (app != null) {
                            if (isHidden) {
                                boolean result = camtonoManager.camtono().unhide(packageName);
                                if (result)
                                    hiddenAppsManager.remove(packageName);
                            } else {
                                boolean result = camtonoManager.camtono().hide(packageName);
                                if (result)
                                    hiddenAppsManager.add(new HiddenApp(app));
                            }
                        }
                        break;
                    case 3:
                        Log.d("Package Clear");
                        camtonoManager.camtono().clearDefault(packageName);
                        break;
                    case 4:
                        Log.d("Package Suspend/Unsuspend");
                        if (isSuspended)
                            camtonoManager.camtono().unsuspend(packageName);
                        else
                            camtonoManager.camtono().suspend(packageName);
                        break;
                }
            }
        });

        viewAdvance.addView(circleMenuView);
    }

    private void fetchAppDetails() {
        disposable.add(Observable.fromCallable(() -> PackageUtil
                .getAppByPackageName(getPackageManager(), packageName))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(app -> {
                    if (app == null) {
                        Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        this.app = app;
                        drawDetails(app);
                    }
                }, err -> {
                    Log.e("Unable to fetch app details");
                    err.printStackTrace();
                })
        );
    }

    private void drawDetails(App app) {
        if (app != null) {
            appBundle.setApp(app);

            textCategory.setText(app.getCategory());
            textSDK.setText(String.valueOf(app.getTargetSDK()));
            textBundle.setText(String.valueOf(app.isSplit()));
            textInstalled.setText(DateTimeUtil.getDate(app.getInstalledTime()));
            textUpdated.setText(DateTimeUtil.getDate(app.getLastUpdated()));
            textInstaller.setText(app.getInstaller());

            cardActivities.setCount(app.getActivityCount());
            cardServices.setCount(app.getServiceCount());
            cardReceivers.setCount(app.getReceiverCount());
            cardProviders.setCount(app.getProviderCount());
            cardPermissions.setCount(app.getPermissionCount());
        }
    }

    @OnClick(R.id.btn_trackers)
    public void showTrackers() {
        startShitScan(0);
    }

    @OnClick(R.id.btn_loggers)
    public void showLoggers() {
        startShitScan(1);
    }

    private void startShitScan(int intExtra) {
        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        Intent intent = new Intent(this, AnalyticsActivity.class);
        String mimeType = typeMap.getMimeTypeFromExtension("apk");
        intent.setDataAndType(Uri.fromFile(new File(app.getInstallLocation())), mimeType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.STRING_EXTRA, packageName);
        intent.putExtra(Constants.INT_EXTRA, intExtra);
        startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
    }

    private void openBottomSheet(BaseBottomSheet baseBottomSheet, int componentType) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment fragment = fragmentManager.findFragmentByTag(ComponentSheet.TAG);
        final Bundle bundle = new Bundle();

        if (fragment != null)
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        bundle.putString(Constants.STRING_EXTRA, packageName);
        bundle.putInt(Constants.INT_EXTRA, componentType);
        baseBottomSheet.setArguments(bundle);
        fragmentManager.beginTransaction()
                .add(baseBottomSheet, ComponentSheet.TAG)
                .commitAllowingStateLoss();
    }

    @OnClick(R.id.card_activities)
    public void openActivities() {
        openBottomSheet(new ComponentSheet(), 0);
    }

    @OnClick(R.id.card_services)
    public void openServices() {
        openBottomSheet(new ComponentSheet(), 1);
    }

    @OnClick(R.id.card_receivers)
    public void openReceivers() {
        openBottomSheet(new ComponentSheet(), 2);
    }

    @OnClick(R.id.card_providers)
    public void openProviders() {
        openBottomSheet(new ComponentSheet(), 3);
    }

    @OnClick(R.id.card_permissions)
    public void openPermissions() {
        openBottomSheet(new PermissionSheet(), 4);
    }

    @Override
    public void onSuccess(AppData appData) {
        txtSize.setText(Formatter.formatFileSize(this, appData.getCodeBytes()));
        txtCache.setText(Formatter.formatFileSize(this, appData.getCacheBytes()));
        txtData.setText(Formatter.formatFileSize(this, appData.getDataBytes()));
        txtTotal.setText(Formatter.formatFileSize(this, appData.getAppSize()));
    }

    @Override
    public void onError(AppData appData, String error) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        final AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);

        if (appOpsManager != null) {
            int mode = appOpsManager.checkOpNoThrow("android:get_usage_stats",
                    Process.myUid(),
                    getPackageName());

            final ArrayList<String> stringArrayList = new ArrayList<>();

            if (mode != AppOpsManager.MODE_ALLOWED) {
                stringArrayList.add(Manifest.permission.PACKAGE_USAGE_STATS);
            }

            final FragmentManager fragmentManager = getSupportFragmentManager();
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
