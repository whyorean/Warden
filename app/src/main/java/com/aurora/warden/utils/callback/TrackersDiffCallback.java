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

import com.aurora.warden.data.model.items.TrackerItem;
import com.mikepenz.fastadapter.diff.DiffCallback;

import org.jetbrains.annotations.Nullable;

public class TrackersDiffCallback implements DiffCallback<TrackerItem> {

    @Override
    public boolean areContentsTheSame(TrackerItem oldItem, TrackerItem newItem) {
        return oldItem.getTracker().getId().equals(newItem.getTracker().getId());
    }

    @Override
    public boolean areItemsTheSame(TrackerItem oldItem, TrackerItem newItem) {
        return oldItem.getTracker().getId().equals(newItem.getTracker().getId());
    }

    @Nullable
    @Override
    public Object getChangePayload(TrackerItem oldItem, int oldPosition, TrackerItem newItem, int newPosition) {
        return null;
    }
}