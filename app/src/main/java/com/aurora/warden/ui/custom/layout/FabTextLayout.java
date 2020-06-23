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

package com.aurora.warden.ui.custom.layout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

public class FabTextLayout extends RelativeLayout {

    @Getter
    @BindView(R.id.fab)
    FrameLayout frameLayout;
    @BindView(R.id.img)
    AppCompatImageView img;
    @BindView(R.id.txt_fab)
    AppCompatTextView txtFab;

    @Getter
    @Setter
    int color;

    public FabTextLayout(Context context) {
        super(context);
        init(context, null);
    }

    public FabTextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FabTextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public FabTextLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_fab_text, this);
        ButterKnife.bind(this, view);
    }

    public void setText(String text) {
        txtFab.setText(text);
        invalidate();
    }

    public void setupFab(int color, Drawable icon) {
        img.setImageDrawable(icon);
        img.setImageTintList(ColorStateList.valueOf(color));
        frameLayout.setClickable(false);
        frameLayout.setBackgroundResource(R.drawable.circle_bg);
        frameLayout.setBackgroundTintList(ColorStateList.valueOf(color));
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            frameLayout.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
        }
        invalidate();
    }
}
