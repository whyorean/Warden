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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.R;
import com.aurora.warden.ui.custom.view.WaveView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WaveLayout extends FrameLayout {
    @BindView(R.id.img)
    WaveView wave1;
    @BindView(R.id.wave2)
    WaveView wave2;
    @BindView(R.id.wave3)
    WaveView wave3;
    @BindView(R.id.txt)
    AppCompatTextView txt;

    private int lastState;

    public WaveLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public WaveLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaveLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public WaveLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_wave, this);
        ButterKnife.bind(this, view);
    }

    public void setState(int state) {
        if (lastState == state)
            return;

        switch (state) {
            case 1:
                updateGradient(0XFF00B09B, 0XFF96C93d);
                break;
            case 2:
                updateGradient(0XFFE96443, 0XFF904E95);
                break;
            case 3:
                updateGradient(0XFFE0EAFC, 0XFFCFDEF3);
                break;
        }
        lastState = state;
    }

    public void updateGradient(@ColorInt int startGradientColor, @ColorInt int endGradientColor) {
        wave1.updateGradient(startGradientColor, endGradientColor);
        wave2.updateGradient(startGradientColor, endGradientColor);
        wave3.updateGradient(startGradientColor, endGradientColor);
        invalidate();
    }

    public void setText(String text) {
        txt.setText(text);
        invalidate();
    }
}
