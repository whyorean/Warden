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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.ui.custom.view.PermissionView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppPermissionSheet extends BaseBottomSheet {

    public static final String TAG = "APP_PERMISSIONS_SHEET";

    @BindView(R.id.title)
    AppCompatTextView title;
    @BindView(R.id.subtitle)
    AppCompatTextView subtitle;
    @BindView(R.id.layout_permissions)
    LinearLayout layoutPermissions;

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_permissions, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            List<String> stringList = bundle.getStringArrayList(Constants.STRING_EXTRA);
            if (stringList != null) {
                setupPermissions(stringList);
            } else {
                dismissAllowingStateLoss();
            }

        } else {
            dismissAllowingStateLoss();
        }
    }

    private void setupPermissions(List<String> stringList) {
        layoutPermissions.removeAllViews();
        for (String permission : stringList) {
            final PermissionView permissionView = new PermissionView(requireContext());
            permissionView.setPermission(permission);
            layoutPermissions.addView(permissionView);
        }
    }
}
