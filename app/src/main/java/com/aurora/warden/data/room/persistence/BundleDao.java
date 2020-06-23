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

import com.aurora.warden.data.model.report.Bundle;

import java.util.List;

@Dao
public interface BundleDao {
    @Query("SELECT * FROM Bundle")
    LiveData<List<Bundle>> getBundles();

    @Query("SELECT * FROM Bundle WHERE packageName = :packageName")
    LiveData<List<Bundle>> getBundlesByPackage(String packageName);

    @Query("SELECT * FROM Bundle WHERE versionCode = :versionCode")
    Bundle getBundleByVersion(Long versionCode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBundle(Bundle bundle);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBundle(List<Bundle> bundleList);

    @Query("DELETE FROM Bundle WHERE versionCode = :versionCode")
    void clearBundle(Long versionCode);

    @Query("DELETE FROM Bundle")
    void clear();
}
