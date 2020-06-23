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

package com.aurora.warden.data.model;

import android.app.usage.UsageStats;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class App {
    private String displayName;
    private String packageName;
    private String versionName;
    private String installer;
    private String installLocation;
    private String category;
    private String description;
    private boolean isSystem;
    private boolean isBackupAvailable;
    private boolean isSplit;
    private Integer targetSDK;
    private Integer uid;
    private Long lastUpdated;
    private Long installedTime;
    private Long versionCode;
    private String process;
    private String icon;

    //Counts
    private int activityCount;
    private int permissionCount;
    private int providerCount;
    private int serviceCount;
    private int receiverCount;

    @Nullable
    private transient UsageStats usageStats;
    @Nullable
    private transient Drawable iconDrawable;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof App))
            return false;
        return packageName.equals(((App) obj).getPackageName());
    }

    @Override
    public int hashCode() {
        return (packageName.isEmpty()) ? 0 : packageName.hashCode();
    }
}
