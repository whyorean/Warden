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

import lombok.Getter;

@Getter
public abstract class CamtonoBase implements ICamtono {
    protected final String ENABLE_COMPONENT_TEMPLATE = "pm enable %s/%s";
    protected final String DEFAULT_COMPONENT_TEMPLATE = "pm default-state %s/%s";
    protected final String DISABLE_COMPONENT_TEMPLATE = "pm disable %s/%s";
    protected final String GRANT_PERMISSION_TEMPLATE = "pm grant %s %s";
    protected final String REVOKE_PERMISSION_TEMPLATE = "pm revoke %s %s";

    protected final String DISABLE_PACKAGE_TEMPLATE = "pm disable %s";
    protected final String ENABLE_PACKAGE_TEMPLATE = "pm enable %s";
    protected final String UNINSTALL_PACKAGE_TEMPLATE = "su pm uninstall %s";
    protected final String HIDE_PACKAGE_TEMPLATE = "pm hide %s";
    protected final String UNHIDE_PACKAGE_TEMPLATE = "pm unhide %s";
    protected final String SUSPEND_PACKAGE_TEMPLATE = "pm suspend %s";
    protected final String UNSUSPEND_PACKAGE_TEMPLATE = "pm unsuspend %s";
    protected final String CLEAR_PACKAGE_TEMPLATE = "pm clear %s";

    private Context context;
    private PackageManager packageManager;

    public CamtonoBase(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }

    protected boolean isComponentEnabled(ComponentName componentName) {
        int state;
        try {
            state = packageManager.getComponentEnabledSetting(componentName);
        } catch (Exception ignored) {
            return false;
        }
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
    }

    protected String removeEscapeCharacter(String s) {
        return s.replace("$", "\\$");
    }
}
