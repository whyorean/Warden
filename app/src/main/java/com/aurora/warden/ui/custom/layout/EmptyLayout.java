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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.R;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EmptyLayout extends RelativeLayout {

    @BindView(R.id.img_empty)
    AppCompatImageView imgEmpty;
    @BindView(R.id.txt_empty)
    AppCompatTextView txtEmpty;

    public EmptyLayout(Context context) {
        super(context);
    }

    public EmptyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EmptyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public EmptyLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = inflate(context, R.layout.layout_empty, this);
        ButterKnife.bind(this, view);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EmptyLayout);
        final String txt = typedArray.getString(R.styleable.EmptyLayout_emptyText);
        final Drawable drawable = typedArray.getDrawable(R.styleable.EmptyLayout_emptyImage);

        txtEmpty.setText(StringUtils.isEmpty(txt) ? getResources().getString(R.string.action_loading) : txt);
        imgEmpty.setImageDrawable(drawable);
        typedArray.recycle();
    }
}
