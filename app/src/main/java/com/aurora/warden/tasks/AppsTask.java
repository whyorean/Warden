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

package com.aurora.warden.tasks;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.aurora.warden.utils.app.PackageUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppsTask {

    private Context context;

    public AppsTask(Context context) {
        this.context = context;
    }

    public Set<PackageInfo> getAllPackages() {
        final Set<PackageInfo> packageInfoSet = new HashSet<>();
        final PackageManager packageManager = context.getPackageManager();
        final int flags = PackageUtil.getAllFlags();
        final List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(flags);

        for (PackageInfo packageInfo : packageInfoList) {
            if (packageInfo.packageName != null && packageInfo.applicationInfo != null) {
                packageInfoSet.add(packageInfo);
            }
        }
        return packageInfoSet;
    }

    public Set<String> getAllPackageNames() {
        final Set<String> packageNames = new HashSet<>();
        final PackageManager packageManager = context.getPackageManager();
        final int flags = PackageUtil.getAllFlags();
        final List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(flags);
        for (PackageInfo packageInfo : packageInfoList) {
            if (packageInfo.packageName != null) {
                packageNames.add(packageInfo.packageName);
            }
        }
        return packageNames;
    }
}
