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

package com.aurora.warden.data.model.items;

import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aurora.warden.R;
import com.aurora.warden.data.model.DebloatProfile;
import com.aurora.warden.data.model.items.base.ListItem;
import com.aurora.warden.data.model.items.base.ListViewHolder;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DebloatProfileItem extends ListItem {

    private DebloatProfile debloatProfile;

    public DebloatProfileItem(DebloatProfile debloatProfile) {
        this.debloatProfile = debloatProfile;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_list;
    }

    @NotNull
    @Override
    public ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    static class ViewHolder extends ListViewHolder {
        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.line3)
        TextView line3;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindView(@NotNull ListItem item, @NotNull List<?> list) {
            final Context context = itemView.getContext();
            final DebloatProfile tracker = ((DebloatProfileItem) item).getDebloatProfile();
            line1.setText(tracker.getName());
            line2.setText(tracker.getDesc());
            line3.setText(StringUtils.joinWith(" ", tracker.getPackages().size(), "Apps"));
            itemView.setAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
        }

        @Override
        public void unbindView(@NotNull ListItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
        }
    }
}
