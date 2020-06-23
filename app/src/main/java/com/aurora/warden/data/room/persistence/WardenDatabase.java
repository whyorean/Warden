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

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.aurora.warden.data.model.report.Bundle;
import com.aurora.warden.data.model.report.StaticReport;

@Database(entities = {StaticReport.class, Bundle.class}, version = 1, exportSchema = false)
public abstract class WardenDatabase extends RoomDatabase {

    private static volatile WardenDatabase INSTANCE;

    public static WardenDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WardenDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WardenDatabase.class,
                            "warden_01.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ReportDao reportDao();

    public abstract BundleDao bundleDao();
}
