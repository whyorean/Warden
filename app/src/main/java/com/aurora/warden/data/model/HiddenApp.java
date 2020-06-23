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

package com.aurora.warden.data.model;

import android.graphics.Bitmap;

import com.aurora.warden.utils.ViewUtil;

import lombok.Data;

@Data
public class HiddenApp {
    private String packageName;
    private String displayName;
    private String rawBitmap;
    private long timestamp;

    public HiddenApp(App app) {
        this.packageName = app.getPackageName();
        this.displayName = app.getDisplayName();
        this.timestamp = System.currentTimeMillis();

        final Bitmap bitmap = ViewUtil.getBitmapFromDrawable(app.getIconDrawable());
        this.rawBitmap = ViewUtil.getRawStringFromBitmap(bitmap);
    }
}
