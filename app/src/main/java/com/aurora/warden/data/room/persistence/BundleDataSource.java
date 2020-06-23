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

import com.aurora.warden.data.model.report.Bundle;
import com.aurora.warden.data.room.IBundleDatabase;

import java.util.List;

public class BundleDataSource implements IBundleDatabase {
    private final BundleDao bundleDao;

    public BundleDataSource(BundleDao bundleDao) {
        this.bundleDao = bundleDao;
    }

    @Override
    public LiveData<List<Bundle>> getBundles() {
        return bundleDao.getBundles();
    }

    @Override
    public LiveData<List<Bundle>> getBundlesByPackage(String packageName) {
        return bundleDao.getBundlesByPackage(packageName);
    }

    @Override
    public Bundle getBundleByVersion(Long versionCode) {
        return bundleDao.getBundleByVersion(versionCode);
    }

    @Override
    public void insertOrUpdate(Bundle bundle) {
        bundleDao.insertBundle(bundle);
    }

    @Override
    public void insertOrUpdateAll(List<Bundle> bundleList) {
        bundleDao.insertBundle(bundleList);
    }

    @Override
    public void clearBundleByVersion(Bundle bundle) {
        bundleDao.clearBundle(bundle.getVersionCode());
    }

    @Override
    public void clearAllBundles() {
        bundleDao.clear();
    }
}
