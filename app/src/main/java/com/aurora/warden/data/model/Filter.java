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

import android.content.Context;

import com.aurora.warden.Constants;
import com.aurora.warden.utils.PrefUtil;
import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Filter {
    private boolean isSystem;
    private boolean isUser;
    private boolean isDisabled;
    private boolean isStopped;
    private boolean isSuspended;
    private boolean isDebugging;
    private boolean isRunning;
    private boolean isLargeHeap;
    private boolean isLaunchable;

    public static Filter getSavedFilter(Context context) {
        return new Gson().fromJson(PrefUtil.getString(context, Constants.PREFERENCE_FILTER), Filter.class);
    }

    public static Filter getDefault() {
        return new FilterBuilder()
                .isSystem(true)
                .isUser(true)
                .isDisabled(true)
                .isDebugging(false)
                .isSuspended(false)
                .isDebugging(false)
                .isRunning(false)
                .isLargeHeap(false)
                .isLaunchable(true)
                .build();
    }
}
