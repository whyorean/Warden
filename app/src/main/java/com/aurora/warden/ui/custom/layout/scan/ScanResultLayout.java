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

package com.aurora.warden.ui.custom.layout.scan;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.R;
import com.aurora.warden.events.Event;
import com.google.android.material.button.MaterialButton;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanResultLayout extends RelativeLayout {

    @BindView(R.id.img)
    AppCompatImageView imageView;
    @BindView(R.id.txt)
    AppCompatTextView textView;
    @BindView(R.id.btn)
    MaterialButton button;

    public ScanResultLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ScanResultLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScanResultLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ScanResultLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_scan_result, this);
        ButterKnife.bind(this, view);
    }

    public void setResult(Event event) {
        String text = StringUtils.EMPTY;
        int textColor = Color.BLACK;
        Drawable drawable = null;

        switch (event.getSubType()) {
            case SCAN_COMPLETED:
                text = getContext().getString(R.string.action_completed);
                textColor = getContext().getResources().getColor(R.color.colorGreen);
                drawable = getContext().getDrawable(R.drawable.ic_undraw_completed);
                break;
            case SCAN_FAILED:
                text = getContext().getString(R.string.action_failed);
                textColor = getContext().getResources().getColor(R.color.colorRed);
                drawable = getContext().getDrawable(R.drawable.ic_undraw_fail);
                break;
            case SCAN_CANCELED:
                text = getContext().getString(R.string.action_canceled);
                textColor = getContext().getResources().getColor(R.color.colorRed);
                drawable = getContext().getDrawable(R.drawable.ic_undraw_cancel);
                break;
        }

        textView.setText(text);
        textView.setTextColor(textColor);
        imageView.setImageDrawable(drawable);
        invalidate();
    }

    @OnClick(R.id.btn)
    public void startReportsActivity() {
        AuroraApplication.rxNotify(new Event(Event.SubType.NAVIGATED, R.id.navigation_stats));
    }
}
