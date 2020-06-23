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

package com.aurora.warden.utils.app;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PermissionInfo;
import android.os.Build;

import com.aurora.warden.R;

public class ComponentUtil {

    private static final String SEPARATOR = " | ";

    public static String getPermissionLevelString(Context context, int level) {
        String protectionLevel = context.getString(R.string.permission_unknown);

        switch (level & PermissionInfo.PROTECTION_MASK_BASE) {
            case PermissionInfo.PROTECTION_DANGEROUS:
                protectionLevel = context.getString(R.string.permission_dangerous);
                break;
            case PermissionInfo.PROTECTION_NORMAL:
                protectionLevel = context.getString(R.string.permission_safe);
                break;
            case PermissionInfo.PROTECTION_SIGNATURE:
                protectionLevel = context.getString(R.string.permission_signature);
                break;
            case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM:
                protectionLevel = context.getString(R.string.permission_system);
                break;
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if ((level & PermissionInfo.PROTECTION_FLAG_PRIVILEGED) != 0)
                protectionLevel += SEPARATOR + context.getString(R.string.permission_privileged);
            if ((level & PermissionInfo.PROTECTION_FLAG_PRE23) != 0)
                protectionLevel += SEPARATOR + context.getString(R.string.permission_pre23);
            if ((level & PermissionInfo.PROTECTION_FLAG_INSTALLER) != 0)
                protectionLevel += SEPARATOR + context.getString(R.string.permission_installer);
            if ((level & PermissionInfo.PROTECTION_FLAG_VERIFIER) != 0)
                protectionLevel += SEPARATOR + context.getString(R.string.permission_verifier);
            if ((level & PermissionInfo.PROTECTION_FLAG_PREINSTALLED) != 0)
                protectionLevel += SEPARATOR + context.getString(R.string.permission_preinstalled);
            if ((level & PermissionInfo.PROTECTION_FLAG_SETUP) != 0)
                protectionLevel += SEPARATOR + context.getString(R.string.permission_setup);
            if ((level & PermissionInfo.PROTECTION_FLAG_RUNTIME_ONLY) != 0)
                protectionLevel += SEPARATOR + context.getString(R.string.permission_runtime);
            if ((level & PermissionInfo.PROTECTION_FLAG_INSTANT) != 0)
                protectionLevel += SEPARATOR + context.getString(R.string.permission_instant);
        } else if ((level & PermissionInfo.PROTECTION_FLAG_SYSTEM) != 0) {
            protectionLevel += SEPARATOR + context.getString(R.string.permission_system);
        }

        if ((level & PermissionInfo.PROTECTION_FLAG_DEVELOPMENT) != 0) {
            protectionLevel += SEPARATOR + context.getString(R.string.permission_development);
        }

        if ((level & PermissionInfo.PROTECTION_FLAG_APPOP) != 0) {
            protectionLevel += SEPARATOR + context.getString(R.string.permission_appop);
        }
        return protectionLevel;
    }

    public static String getConfigString(int config) {
        switch (config) {
            case ActivityInfo.CONFIG_MCC:
                return "MCC";
            case ActivityInfo.CONFIG_LOCALE:
                return "Locale";
            case ActivityInfo.CONFIG_TOUCHSCREEN:
                return "Touch screen";
            case ActivityInfo.CONFIG_KEYBOARD:
                return "Keyboard";
            case ActivityInfo.CONFIG_NAVIGATION:
                return "Navigation";
            case ActivityInfo.CONFIG_ORIENTATION:
                return "Orientation";
            case ActivityInfo.CONFIG_SCREEN_LAYOUT:
                return "Screen layout";
            case ActivityInfo.CONFIG_DENSITY:
                return "Density";
            case ActivityInfo.CONFIG_LAYOUT_DIRECTION:
                return "Direction";
            case ActivityInfo.CONFIG_FONT_SCALE:
                return "Font scale";
            default:
                return "None";
        }
    }

    public static String getLaunchModeString(int mode) {
        switch (mode) {
            case ActivityInfo.LAUNCH_MULTIPLE:
                return "Multiple";
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE:
                return "Single instance";
            case ActivityInfo.LAUNCH_SINGLE_TASK:
                return "Single task";
            case ActivityInfo.LAUNCH_SINGLE_TOP:
                return "Single top";
            default:
                return "None";
        }
    }

    public static String getPersistModeString(int mode) {
        switch (mode) {
            case ActivityInfo.PERSIST_ACROSS_REBOOTS:
                return "Reboots";
            case ActivityInfo.PERSIST_ROOT_ONLY:
                return "Root only";
            case ActivityInfo.PERSIST_NEVER:
                return "Never";
            default:
                return "Unknown";
        }
    }
}
