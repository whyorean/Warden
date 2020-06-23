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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.warden.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BundleCountLayout extends LinearLayout {

    @BindView(R.id.frame)
    FrameLayout frameLayout;
    @BindView(R.id.txt_count)
    TextView txtCount;
    @BindView(R.id.txt_title)
    TextView txtTitle;

    public BundleCountLayout(Context context) {
        super(context);
        init(context, null);
    }

    public BundleCountLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BundleCountLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public BundleCountLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_bundle, this);
        ButterKnife.bind(this, view);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BundleCountLayout);
        String title = typedArray.getString(R.styleable.BundleCountLayout_title);
        int count = typedArray.getInt(R.styleable.BundleCountLayout_count, 0);
        int color = typedArray.getColor(R.styleable.BundleCountLayout_tintColor, 0XFF00B09B);

        txtTitle.setText(title);
        frameLayout.setBackgroundTintList(ColorStateList.valueOf(color));

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            frameLayout.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
        }
    }

    public void setCount(int count) {
        txtCount.setText(String.valueOf(count));
        invalidate();
    }
}
