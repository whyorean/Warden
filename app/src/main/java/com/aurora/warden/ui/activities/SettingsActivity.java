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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.preference.PreferenceFragmentCompat;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.ui.custom.layout.MultiTextLayout;
import com.aurora.warden.utils.Util;
import com.aurora.warden.utils.ViewUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.action1)
    AppCompatImageView action1;
    @BindView(R.id.multi_text_layout)
    MultiTextLayout multiTextLayout;
    @BindView(R.id.action2)
    AppCompatImageView action2;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic);
        ButterKnife.bind(this);
        setupToolbar();
        sharedPreferences = Util.getPrefs(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }

    private void setupToolbar() {
        action1.setImageDrawable(getDrawable(R.drawable.ic_arrow_left));
        action1.setOnClickListener(v -> onBackPressed());
        multiTextLayout.setTxtPrimary(getString(R.string.app_name));
        multiTextLayout.setTxtSecondary(getString(R.string.menu_settings));
    }

    @Override
    protected void onDestroy() {
        try {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREFERENCE_THEME:
                ViewUtil.switchTheme(this);
                break;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

        }
    }
}