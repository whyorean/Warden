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

package com.aurora.warden.data.model.report;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.aurora.warden.data.room.RoomTypeConverter;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(tableName = "Bundle", primaryKeys = {"versionCode", "packageName", "reportId"})
@TypeConverters(RoomTypeConverter.class)
public class Bundle {
    @NonNull
    private Long versionCode = 0L;
    @NonNull
    private Long reportId = System.currentTimeMillis();
    @NonNull
    private String packageName = StringUtils.EMPTY;
    private List<Tracker> trackersList;
    private List<Logger> loggerList;
}
