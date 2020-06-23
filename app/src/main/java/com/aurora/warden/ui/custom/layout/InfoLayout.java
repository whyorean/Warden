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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InfoLayout extends RelativeLayout {


    @BindView(R.id.txt_primary)
    AppCompatTextView txtPrimary;
    @BindView(R.id.txt_secondary)
    AppCompatTextView txtSecondary;

    public InfoLayout(Context context) {
        super(context);
        init(context, null);
    }

    public InfoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public InfoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public InfoLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_info, this);
        ButterKnife.bind(this, view);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.InfoLayout);
        String textPrimary = typedArray.getString(R.styleable.InfoLayout_infoPrimary);
        String textSecondary = typedArray.getString(R.styleable.InfoLayout_infoSecondary);

        txtPrimary.setText(textPrimary);
        txtSecondary.setText(textSecondary);

        typedArray.recycle();
    }

    public void setTxtPrimary(String text) {
        txtPrimary.setText(text);
        invalidate();
    }

    public void setTxtSecondary(String text) {
        txtSecondary.setText(text);
        invalidate();
    }
}
