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

package com.aurora.warden.ui.custom.layout.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.R;
import com.aurora.warden.data.model.App;
import com.aurora.warden.data.model.AppBundle;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.utils.app.PackageUtil;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppLayout extends LinearLayout {

    @BindView(R.id.img)
    AppCompatImageView img;
    @BindView(R.id.line1)
    AppCompatTextView line1;
    @BindView(R.id.line2)
    AppCompatTextView line2;
    @BindView(R.id.line3)
    AppCompatTextView line3;

    public AppLayout(Context context) {
        super(context);
        init(context, null);
    }

    public AppLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AppLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public AppLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_bundle, this);
        ButterKnife.bind(this, view);
    }

    public void clear() {
        line1.setText(null);
        line2.setText(null);
        line3.setText(null);
        img.setImageDrawable(null);
        invalidate();
    }

    public void setAppBundle(AppBundle appBundle) {
        App app = appBundle.getApp();

        ViewUtil.switchText(getContext(), line1, app.getDisplayName());
        ViewUtil.switchText(getContext(), line2, app.getPackageName());
        ViewUtil.switchText(getContext(), line3, StringUtils.joinWith(" \u2022 ",
                appBundle.getTrackerCount() + StringUtils.SPACE + getContext().getString(R.string.txt_status_tracker),
                appBundle.getLoggerCount() + StringUtils.SPACE + getContext().getString(R.string.txt_status_loggers)));

        Drawable drawable = PackageUtil.getIconFromPackageName(getContext(), app.getPackageName());
        if (drawable != null) {
            ViewUtil.switchImage(getContext(), img, drawable);
        }
    }

    public void setApp(App app) {
        line1.setText(app.getDisplayName());
        line2.setText(app.getPackageName());
        line3.setText(new StringBuilder().append(app.getVersionName()).append(app.getVersionCode()));
        img.setImageDrawable(app.getIconDrawable());
        invalidate();
    }
}
