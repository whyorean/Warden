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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.events.Event;
import com.aurora.warden.ui.custom.layout.MultiTextLayout;
import com.aurora.warden.utils.ViewUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AuroraActivity extends BaseActivity {
    @BindView(R.id.action1)
    AppCompatImageView action1;
    @BindView(R.id.multi_text_layout)
    MultiTextLayout multiTextLayout;
    @BindView(R.id.action2)
    AppCompatImageView action2;
    @BindView(R.id.nav_view)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigation)
    NavigationView navigation;

    private CompositeDisposable disposable = new CompositeDisposable();

    static boolean matchDestination(@NonNull NavDestination destination, @IdRes int destId) {
        NavDestination currentDestination = destination;
        while (currentDestination.getId() != destId && currentDestination.getParent() != null) {
            currentDestination = currentDestination.getParent();
        }
        return currentDestination.getId() == destId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupToolbar();
        setupNavigation();
        setupDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupNavigation() {
        final int backGroundColor = ViewUtil.getStyledAttribute(this, android.R.attr.colorBackground);
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        bottomNavigationView.setBackgroundColor(ColorUtils.setAlphaComponent(backGroundColor, 245));
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.navigation_apps:
                    multiTextLayout.setTxtSecondary(getString(R.string.menu_apps));
                    break;
                case R.id.navigation_scanner:
                    multiTextLayout.setTxtSecondary(getString(R.string.menu_scanner));
                    break;
                case R.id.navigation_stats:
                    multiTextLayout.setTxtSecondary(getString(R.string.menu_stats));
                    break;
                case R.id.navigation_search:
                    multiTextLayout.setTxtSecondary(getString(R.string.menu_search));
                    break;
            }

            if (item.getItemId() == bottomNavigationView.getSelectedItemId())
                return false;
            NavigationUI.onNavDestinationSelected(item, navController);
            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            final Menu menu = bottomNavigationView.getMenu();
            final int size = menu.size();
            for (int i = 0; i < size; i++) {
                MenuItem item = menu.getItem(i);
                if (matchDestination(destination, item.getItemId())) {
                    item.setChecked(true);
                }
            }
        });

        disposable.add(AuroraApplication
                .getRelay()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event.getSubType() == Event.SubType.NAVIGATED) {
                        int intExtra = event.getIntExtra();
                        bottomNavigationView.setSelectedItemId(intExtra);
                    }
                }));
    }

    private void setupToolbar() {
        multiTextLayout.setTxtPrimary(getString(R.string.app_name));
        multiTextLayout.setTxtSecondary(getString(R.string.menu_scanner));
    }

    @OnClick(R.id.action1)
    public void openDrawer() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.openDrawer(GravityCompat.START, true);
    }

    private void setupDrawer() {
        navigation.setNavigationItemSelectedListener(item -> {
            Intent intent = new Intent(this, GenericActivity.class);
            switch (item.getItemId()) {
                case R.id.action_trackers:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_TRACKERS);
                    startActivity(intent);
                    break;
                case R.id.action_loggers:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_LOGGERS);
                    startActivity(intent);
                    break;
                case R.id.action_lab:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_LAB);
                    startActivity(intent);
                    break;
                case R.id.action_hidden:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_HIDDEN);
                    startActivity(intent);
                    break;
                case R.id.action_about:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_ABOUT);
                    startActivity(intent);
                    break;
                case R.id.action_app_usage:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_APP_USAGE);
                    startActivity(intent);
                    break;
                case R.id.action_setting:
                    startActivity(new Intent(this, SettingsActivity.class));
                    break;
            }
            return false;
        });
    }
}
