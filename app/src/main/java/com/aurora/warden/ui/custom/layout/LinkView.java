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
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.warden.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LinkView extends RelativeLayout {

    @BindView(R.id.line1)
    TextView line1;
    @BindView(R.id.line2)
    TextView line2;

    private String title;
    private String url;

    public LinkView(Context context, String title, String url) {
        super(context);
        this.title = title;
        this.url = url;
        init();
    }

    public LinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.item_link, this);
        ButterKnife.bind(this, view);

        line1.setText(title);
        line2.setText(url);

        view.setOnClickListener(click -> {
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(url));
            getContext().startActivity(browserIntent);
        });
    }
}
