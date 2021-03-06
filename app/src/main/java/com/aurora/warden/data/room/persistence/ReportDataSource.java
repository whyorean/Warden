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

import com.aurora.warden.data.model.report.StaticReport;
import com.aurora.warden.data.room.IReportDatabase;

import java.util.List;

public class ReportDataSource implements IReportDatabase {
    private final ReportDao reportDao;

    public ReportDataSource(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public LiveData<List<StaticReport>> getLiveReports() {
        return reportDao.getLiveReports();
    }

    @Override
    public List<StaticReport> getReports() {
        return reportDao.getReports();
    }

    @Override
    public void insertOrUpdate(StaticReport staticReport) {
        reportDao.insertReport(staticReport);
    }

    @Override
    public void insertOrUpdateAll(List<StaticReport> staticReportList) {
        reportDao.insertReport(staticReportList);
    }

    @Override
    public void clearReport(StaticReport staticReport) {
        reportDao.clearReport(staticReport.getReportId());
    }

    @Override
    public void clearAllReports() {
        reportDao.clear();
    }
}
