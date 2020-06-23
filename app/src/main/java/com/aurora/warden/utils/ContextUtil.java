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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ContextUtil {

    public static void toast(Context context, int stringId, String... params) {
        toastLong(context, context.getString(stringId, (Object[]) params));
    }

    public static void toastShort(final Context context, final String message) {
        runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    public static void toastLong(final Context context, final String message) {
        runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }

    public static void runOnUiThread(final Runnable action) {
        if (isUiThread()) {
            action.run();
        } else {
            new Handler(Looper.getMainLooper()).post(action::run);
        }
    }

    public static boolean isUiThread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? Looper.getMainLooper().isCurrentThread()
                : Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public static boolean isAlive(Context context) {
        if (!(context instanceof Activity)) {
            return false;
        }
        Activity activity = (Activity) context;
        return !activity.isDestroyed();
    }
}
