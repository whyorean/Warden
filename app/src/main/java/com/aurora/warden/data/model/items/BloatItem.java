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

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.warden.R;
import com.aurora.warden.data.model.App;
import com.aurora.warden.data.model.items.base.ListItem;
import com.aurora.warden.data.model.items.base.ListViewHolder;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.select.SelectExtension;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BloatItem extends ListItem {

    private App app;

    public BloatItem(App app) {
        this.app = app;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_debloat_app;
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
        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.line3)
        TextView line3;
        @BindView(R.id.checkbox)
        MaterialCheckBox materialCheckBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindView(@NotNull ListItem item, @NotNull List<?> list) {
            if (item instanceof BloatItem) {
                App app = ((BloatItem) item).getApp();
                line1.setText(app.getDisplayName());
                line2.setText(app.getPackageName());
                line3.setText(StringUtils.joinWith(".", app.getVersionName(), app.getVersionCode()));
                line2.setTextColor(itemView.getContext().getResources().getColor(app.isSystem()
                        ? R.color.colorOrange
                        : R.color.colorGreen));
                img.setImageDrawable(app.getIconDrawable());
                materialCheckBox.setChecked(item.isSelected());
            }
        }

        @Override
        public void unbindView(@NotNull ListItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
            img.setImageDrawable(null);
        }
    }

    public static final class CheckBoxClickEvent extends ClickEventHook<BloatItem> {
        @Nullable
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder instanceof BloatItem.ViewHolder
                    ? ((ViewHolder) viewHolder).materialCheckBox
                    : null;
        }

        @Override
        public void onClick(@NotNull View view, int position, @NotNull FastAdapter<BloatItem> fastAdapter, @NotNull BloatItem item) {
            SelectExtension<BloatItem> selectExtension = fastAdapter.getExtension(SelectExtension.class);
            if (selectExtension != null) {
                selectExtension.toggleSelection(position);
            }
        }
    }
}
