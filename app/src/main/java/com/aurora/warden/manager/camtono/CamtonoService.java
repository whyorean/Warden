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

package com.aurora.warden.manager.camtono;

import android.content.Context;

public class CamtonoService extends CamtonoBase {

    public CamtonoService(Context context) {
        super(context);
    }

    @Override
    public boolean switchComponent(String packageName, String componentName, int state) {
        return false;
    }

    @Override
    public boolean switchPermission(String packageName, String permissionName, boolean grant) {
        return false;
    }

    @Override
    public boolean enable(String packageName, String componentName) {
        return false;
    }

    @Override
    public boolean disable(String packageName, String componentName) {
        return false;
    }

    @Override
    public boolean grant(String packageName, String permissionName) {
        return false;
    }

    @Override
    public boolean revoke(String packageName, String permissionName) {
        return false;
    }

    @Override
    public boolean resetToDefault(String packageName, String componentName) {
        return false;
    }

    @Override
    public boolean isEnabled(String packageName, String componentName) {
        return false;
    }

    @Override
    public boolean disable(String packageName) {
        return false;
    }

    @Override
    public boolean enable(String packageName) {
        return false;
    }

    @Override
    public boolean uninstall(String packageName) {
        return false;
    }

    @Override
    public boolean hide(String packageName) {
        return false;
    }

    @Override
    public boolean unhide(String packageName) {
        return false;
    }

    @Override
    public boolean clearDefault(String packageName) {
        return false;
    }

    @Override
    public boolean suspend(String packageName) {
        return false;
    }

    @Override
    public boolean unsuspend(String packageName) {
        return false;
    }
}
