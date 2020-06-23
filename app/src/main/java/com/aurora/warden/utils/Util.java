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

package com.aurora.warden.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PermissionInfo;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.aurora.warden.Constants;
import com.aurora.warden.R;

import org.apache.commons.lang3.StringUtils;

public class Util {

    public static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isSystemEnabled(Context context) {
        return PrefUtil.getBoolean(context, Constants.PREFERENCE_INCLUDE_SYSTEM);
    }

    public static boolean isKeepAwakeEnabled(Context context) {
        return PrefUtil.getBoolean(context, Constants.PREFERENCE_DEVICE_AWAKE);
    }

    public static String getDefaultDebloatAction(Context context) {
        String actionType = PrefUtil.getString(context, Constants.PREFERENCE_DEBLOAT_ACTION);
        if (StringUtils.isEmpty(actionType))
            actionType = "1";
        return actionType;
    }

    public static void copyToClipBoard(Context context, String dataToCopy) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Apk Url", dataToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    public static int getProtectionColor(Context context, int level) {

        int color = context.getResources().getColor(R.color.colorBlue);

        switch (level & PermissionInfo.PROTECTION_MASK_BASE) {
            case PermissionInfo.PROTECTION_DANGEROUS:
                color = context.getResources().getColor(R.color.colorRed);
                break;
            case PermissionInfo.PROTECTION_NORMAL:
                color = context.getResources().getColor(R.color.colorGreen);
                break;
            case PermissionInfo.PROTECTION_SIGNATURE:
            case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM:
                color = context.getResources().getColor(R.color.colorOrange);
                break;
        }

        if ((level & PermissionInfo.PROTECTION_FLAG_DEVELOPMENT) != 0) {
            color = context.getResources().getColor(R.color.colorRed);
        }

        if ((level & PermissionInfo.PROTECTION_FLAG_APPOP) != 0) {
            color = context.getResources().getColor(R.color.colorRed);
        }

        return color;
    }
}
