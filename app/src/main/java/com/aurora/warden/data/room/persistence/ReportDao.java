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

package com.aurora.warden.data.room.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.aurora.warden.data.model.report.StaticReport;

import java.util.List;

@Dao
public interface ReportDao {
    @Query("SELECT * FROM StaticReport")
    LiveData<List<StaticReport>> getLiveReports();

    @Query("SELECT * FROM StaticReport")
    List<StaticReport> getReports();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReport(StaticReport staticReport);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReport(List<StaticReport> staticReportList);

    @Query("DELETE FROM StaticReport WHERE reportId = :reportId")
    void clearReport(long reportId);

    @Query("DELETE FROM StaticReport")
    void clear();
}
