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

package com.aurora.warden.utils.callback;

import com.aurora.warden.data.model.items.AppUsageItem;
import com.mikepenz.fastadapter.diff.DiffCallback;

import org.jetbrains.annotations.Nullable;

public class AppUsageDiffCallback implements DiffCallback<AppUsageItem> {

    @Override
    public boolean areContentsTheSame(AppUsageItem oldItem, AppUsageItem newItem) {
        return oldItem.getApp().getPackageName().equals(newItem.getApp().getPackageName());
    }

    @Override
    public boolean areItemsTheSame(AppUsageItem oldItem, AppUsageItem newItem) {
        return oldItem.getApp().getPackageName().equals(newItem.getApp().getPackageName());
    }

    @Nullable
    @Override
    public Object getChangePayload(AppUsageItem oldItem, int oldPosition, AppUsageItem newItem, int newPosition) {
        return null;
    }
}