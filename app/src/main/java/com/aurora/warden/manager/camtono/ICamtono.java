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

public interface ICamtono {
    boolean switchComponent(String packageName, String componentName, int state);

    boolean switchPermission(String packageName, String permissionName, boolean grant);

    boolean enable(String packageName, String componentName);

    boolean disable(String packageName, String componentName);

    boolean grant(String packageName, String permissionName);

    boolean revoke(String packageName, String permissionName);

    boolean resetToDefault(String packageName, String componentName);

    boolean isEnabled(String packageName, String componentName);

    boolean disable(String packageName);

    boolean enable(String packageName);

    boolean uninstall(String packageName);

    boolean hide(String packageName);

    boolean unhide(String packageName);

    boolean clearDefault(String packageName);

    boolean suspend(String packageName);

    boolean unsuspend(String packageName);
}
