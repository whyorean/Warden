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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import org.apache.commons.lang3.StringUtils;

public class CamtonoRoot extends CamtonoBase {

    public CamtonoRoot(Context context) {
        super(context);
    }

    @Override
    public boolean switchComponent(String packageName, String componentName, int state) {
        String command = StringUtils.EMPTY;

        switch (state) {
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                command = removeEscapeCharacter(String.format(ENABLE_COMPONENT_TEMPLATE, packageName, componentName));
                break;
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                command = removeEscapeCharacter(String.format(DISABLE_COMPONENT_TEMPLATE, packageName, componentName));
                break;
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
                command = removeEscapeCharacter(String.format(DEFAULT_COMPONENT_TEMPLATE, packageName, componentName));
                break;
        }

        try {
            Shell.Result result = Shell.su(command).exec();
            return result.isSuccess();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Component not configurable", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public boolean switchPermission(String packageName, String permissionName, boolean grant) {
        String command = StringUtils.EMPTY;

        if (grant)
            command = String.format(GRANT_PERMISSION_TEMPLATE, packageName, permissionName);
        else
            command = String.format(REVOKE_PERMISSION_TEMPLATE, packageName, permissionName);

        try {
            Shell.Result result = Shell.su(command).exec();
            return result.isSuccess();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Permission not changeable", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public boolean enable(String packageName, String componentName) {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    @Override
    public boolean disable(String packageName, String componentName) {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    }

    @Override
    public boolean grant(String packageName, String permissionName) {
        return switchPermission(packageName, permissionName, true);
    }

    @Override
    public boolean revoke(String packageName, String permissionName) {
        return switchPermission(packageName, permissionName, false);
    }

    @Override
    public boolean resetToDefault(String packageName, String componentName) {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
    }

    @Override
    public boolean isEnabled(String packageName, String componentName) {
        return isComponentEnabled(new ComponentName(packageName, componentName));
    }

    @Override
    public boolean disable(String packageName) {
        String command = String.format(DISABLE_PACKAGE_TEMPLATE, packageName);
        return Shell.su(command).exec().isSuccess();
    }

    @Override
    public boolean enable(String packageName) {
        String command = String.format(ENABLE_PACKAGE_TEMPLATE, packageName);
        return Shell.su(command).exec().isSuccess();
    }

    @Override
    public boolean uninstall(String packageName) {
        String command = String.format(UNINSTALL_PACKAGE_TEMPLATE, packageName);
        return Shell.su(command).exec().isSuccess();
    }

    @Override
    public boolean hide(String packageName) {
        String command = String.format(HIDE_PACKAGE_TEMPLATE, packageName);
        return Shell.su(command).exec().isSuccess();
    }

    @Override
    public boolean unhide(String packageName) {
        String command = String.format(UNHIDE_PACKAGE_TEMPLATE, packageName);
        return Shell.su(command).exec().isSuccess();
    }

    @Override
    public boolean clearDefault(String packageName) {
        String command = String.format(CLEAR_PACKAGE_TEMPLATE, packageName);
        return Shell.su(command).exec().isSuccess();
    }

    @Override
    public boolean suspend(String packageName) {
        String command = String.format(SUSPEND_PACKAGE_TEMPLATE, packageName);
        return Shell.su(command).exec().isSuccess();
    }

    @Override
    public boolean unsuspend(String packageName) {
        String command = String.format(UNSUSPEND_PACKAGE_TEMPLATE, packageName);
        return Shell.su(command).exec().isSuccess();
    }
}
