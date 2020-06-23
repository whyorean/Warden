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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.ui.custom.layout.MultiTextLayout;
import com.aurora.warden.ui.fragments.AboutFragment;
import com.aurora.warden.ui.fragments.AnalyticsFragment;
import com.aurora.warden.ui.fragments.AppUsageFragment;
import com.aurora.warden.ui.fragments.DebloatFragment;
import com.aurora.warden.ui.fragments.HiddenAppsFragment;
import com.aurora.warden.ui.fragments.LabFragment;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GenericActivity extends BaseActivity {

    @BindView(R.id.action1)
    AppCompatImageView action1;
    @BindView(R.id.multi_text_layout)
    MultiTextLayout multiTextLayout;
    @BindView(R.id.action2)
    AppCompatImageView action2;

    private String txtTitleSecondary = StringUtils.EMPTY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic);
        ButterKnife.bind(this);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final String fragmentName = intent.getStringExtra(Constants.FRAGMENT_NAME);
        final String stringExtra = intent.getStringExtra(Constants.STRING_EXTRA);
        final Bundle bundle = new Bundle();

        Fragment fragment = null;

        switch (fragmentName) {
            case Constants.FRAGMENT_TRACKERS:
                bundle.putInt(Constants.INT_EXTRA, 0);
                bundle.putString(Constants.STRING_EXTRA, stringExtra);
                fragment = new AnalyticsFragment();
                fragment.setArguments(bundle);
                txtTitleSecondary = getString(R.string.action_trackers);
                break;
            case Constants.FRAGMENT_LOGGERS:
                bundle.putInt(Constants.INT_EXTRA, 1);
                fragment = new AnalyticsFragment();
                fragment.setArguments(bundle);
                txtTitleSecondary = getString(R.string.action_loggers);
                break;
            case Constants.FRAGMENT_HIDDEN:
                bundle.putInt(Constants.INT_EXTRA, 2);
                fragment = new HiddenAppsFragment();
                fragment.setArguments(bundle);
                txtTitleSecondary = getString(R.string.menu_apps);
                break;
            case Constants.FRAGMENT_LAB:
                bundle.putInt(Constants.INT_EXTRA, 3);
                fragment = new LabFragment();
                fragment.setArguments(bundle);
                txtTitleSecondary = getString(R.string.menu_lab);
                break;
            case Constants.FRAGMENT_DEBLOAT:
                bundle.putInt(Constants.INT_EXTRA, 4);
                bundle.putString(Constants.STRING_EXTRA, stringExtra);
                fragment = new DebloatFragment();
                fragment.setArguments(bundle);
                txtTitleSecondary = getString(R.string.menu_debloat);
                break;
            case Constants.FRAGMENT_ABOUT:
                fragment = new AboutFragment();
                fragment.setArguments(bundle);
                txtTitleSecondary = getString(R.string.menu_about);
                break;
            case Constants.FRAGMENT_APP_USAGE:
                fragment = new AppUsageFragment();
                fragment.setArguments(bundle);
                txtTitleSecondary = getString(R.string.menu_app_usage);
                break;
        }

        setupToolbar();

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, fragment)
                    .commit();
        }
    }

    private void setupToolbar() {
        action1.setImageDrawable(getDrawable(R.drawable.ic_arrow_left));
        action1.setOnClickListener(v -> onBackPressed());
        multiTextLayout.setTxtPrimary(getString(R.string.app_name));
        multiTextLayout.setTxtSecondary(txtTitleSecondary);
    }
}
