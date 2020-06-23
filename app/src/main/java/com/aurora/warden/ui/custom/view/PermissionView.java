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

package com.aurora.warden.ui.custom.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.R;
import com.google.android.material.button.MaterialButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PermissionView extends RelativeLayout {

    @BindView(R.id.line1)
    AppCompatTextView line1;
    @BindView(R.id.line2)
    AppCompatTextView line2;
    @BindView(R.id.btn)
    MaterialButton btn;

    public PermissionView(Context context) {
        super(context);
        init(context, null);
    }

    public PermissionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PermissionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public PermissionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_app_permission, this);
        ButterKnife.bind(this, view);
    }

    public void setPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setAction(permission);
        }
        invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setAction(String permission) {
        switch (permission) {
            case Manifest.permission.WRITE_SETTINGS:
                line1.setText(getResources().getString(R.string.permission_name_settings));
                line2.setText(permission);
                btn.setOnClickListener(v -> getContext().startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)));
                break;
            case Manifest.permission.PACKAGE_USAGE_STATS:
                line1.setText(getResources().getString(R.string.permission_name_app_usage));
                line2.setText(permission);
                btn.setOnClickListener(v -> getContext().startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
                break;
            case Manifest.permission.ACCESS_NOTIFICATION_POLICY:
                line1.setText(getResources().getString(R.string.permission_name_notification));
                line2.setText(permission);
                btn.setOnClickListener(v -> getContext().startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)));
                break;
        }
        invalidate();
    }
}
