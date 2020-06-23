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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import com.aurora.warden.data.model.App;
import com.aurora.warden.data.model.Permission;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PackageUtil {

    public static PackageInfo getPackageInfo(PackageManager packageManager, String packageName) {
        try {
            return packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getInstallLocation(PackageManager packageManager, String packageName) {
        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_META_DATA);
            return packageInfo.applicationInfo.sourceDir;
        } catch (Exception e) {
            //Least likely to happen
            return StringUtils.EMPTY;
        }

    }

    public static Drawable getIconFromPackageName(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getApplicationIcon(packageName);
        } catch (Exception e) {
            return null;
        }
    }

    public static App getMinimalAppByPackageInfo(PackageManager packageManager, String packageName) {
        try {
            final int flags = getAllFlags();
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, flags);
            return getMinimalAppByPackageInfo(packageManager, packageInfo);
        } catch (Exception e) {
            return null;
        }
    }

    public static App getMinimalAppByPackageInfo(PackageManager packageManager, PackageInfo packageInfo) {
        final ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        final App.AppBuilder builder = App.builder();

        builder
                .packageName(packageInfo.packageName)
                .displayName(applicationInfo.loadLabel(packageManager).toString())
                .iconDrawable(applicationInfo.loadIcon(packageManager))
                .installLocation(packageInfo.applicationInfo.sourceDir)
                .versionName(packageInfo.versionName)
                .versionCode((long) packageInfo.versionCode);

        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            builder.isSystem(true);

        return builder.build();
    }

    public static App getAppByPackageName(PackageManager packageManager, String packageName) {
        try {

            int flags = getAllFlags()
                    | PackageManager.GET_PERMISSIONS
                    | PackageManager.GET_PROVIDERS;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                flags = flags | PackageManager.GET_DISABLED_COMPONENTS;
                flags = flags | PackageManager.GET_UNINSTALLED_PACKAGES;
            } else {
                flags = flags | PackageManager.MATCH_DISABLED_COMPONENTS;
                flags = flags | PackageManager.MATCH_UNINSTALLED_PACKAGES;
            }

            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, flags);

            if (packageInfo == null)
                return null;

            final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, flags);
            final App.AppBuilder builder = App.builder();

            builder
                    .packageName(packageInfo.packageName)
                    .displayName(applicationInfo.loadLabel(packageManager).toString())
                    .iconDrawable(applicationInfo.loadIcon(packageManager))
                    .installedTime(packageInfo.firstInstallTime)
                    .installLocation(packageInfo.applicationInfo.sourceDir)
                    .lastUpdated(packageInfo.lastUpdateTime)
                    .versionName(packageInfo.versionName)
                    .versionCode((long) packageInfo.versionCode)
                    .targetSDK(applicationInfo.targetSdkVersion);

            final String installer = packageManager.getInstallerPackageName(packageName);
            final String description = String.valueOf(applicationInfo.loadDescription(packageManager));

            builder
                    .installer(installer != null ? installer : "Unknown")
                    .description(description);

            builder
                    .activityCount(packageInfo.activities != null ? packageInfo.activities.length : 0)
                    .serviceCount(packageInfo.services != null ? packageInfo.services.length : 0)
                    .permissionCount(packageInfo.requestedPermissions != null ? packageInfo.requestedPermissions.length : 0)
                    .providerCount(packageInfo.providers != null ? packageInfo.providers.length : 0)
                    .receiverCount(packageInfo.receivers != null ? packageInfo.receivers.length : 0);

            if (packageInfo.splitNames != null && packageInfo.splitNames.length > 0)
                builder.isSplit(true);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                builder.isSystem(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.category(getCategoryString(packageInfo.applicationInfo.category));
            }
            return builder.build();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static int getAllFlags() {
        int flags = PackageManager.GET_META_DATA
                | PackageManager.GET_ACTIVITIES
                | PackageManager.GET_SERVICES
                | PackageManager.GET_PROVIDERS
                | PackageManager.GET_RECEIVERS;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            flags = flags | PackageManager.GET_DISABLED_COMPONENTS;
            flags = flags | PackageManager.GET_UNINSTALLED_PACKAGES;
        } else {
            flags = flags | PackageManager.MATCH_DISABLED_COMPONENTS;
            flags = flags | PackageManager.MATCH_UNINSTALLED_PACKAGES;
        }
        return flags;
    }

    public static List<Permission> getPermissionsByPackageName(PackageManager packageManager, String packageName) {
        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS);

            final List<Permission> permissionList = new ArrayList<>();
            final String[] permissions = packageInfo.requestedPermissions;
            final int[] permissionsFlags = packageInfo.requestedPermissionsFlags;

            for (int i = 0; i < permissions.length; i++) {

                String permission = permissions[i];
                int permissionFlag = permissionsFlags[i];

                Permission info = new Permission();
                info.setPackageName(packageName);
                info.setPermission(permission);
                info.setName(getReadablePermissionLabel(permission));
                info.setGranted(permissionFlag == 3);

                try {
                    final PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, PackageManager.GET_META_DATA);
                    if (permissionInfo != null) {
                        if (permissionInfo.group != null) {
                            PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, PackageManager.GET_META_DATA);
                            CharSequence description = permissionGroupInfo.loadDescription(packageManager);
                            if (StringUtils.isNotEmpty(description)) {
                                info.setDescription(StringUtils.capitalize(description.toString()));
                            }
                            info.setIcon(permissionGroupInfo.loadIcon(packageManager));
                        }
                        info.setProtectionLevel(permissionInfo.protectionLevel);
                    }
                } catch (Exception ignored) {
                    info.setDescription("Not available");
                }
                permissionList.add(info);
            }

            return permissionList;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static String getReadablePermissionLabel(String label) {
        if (TextUtils.isEmpty(label)) {
            return "";
        }
        try {
            int lastDotIndex = label.lastIndexOf(".");
            label = label.substring(lastDotIndex + 1);
            label = label.replace("_", " ").toLowerCase();
            return StringUtils.capitalize(label);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getCategoryString(Integer categoryId) {
        switch (categoryId) {
            case ApplicationInfo.CATEGORY_AUDIO:
                return "Audio";
            case ApplicationInfo.CATEGORY_GAME:
                return "Game";
            case ApplicationInfo.CATEGORY_IMAGE:
                return "Images";
            case ApplicationInfo.CATEGORY_MAPS:
                return "Maps";
            case ApplicationInfo.CATEGORY_NEWS:
                return "News";
            case ApplicationInfo.CATEGORY_SOCIAL:
                return "Social";
            case ApplicationInfo.CATEGORY_VIDEO:
                return "Video";
            case ApplicationInfo.CATEGORY_PRODUCTIVITY:
                return "Productivity";
            case ApplicationInfo.CATEGORY_UNDEFINED:
                return "Undefined";
            default:
                return "Unknown";
        }
    }
}
