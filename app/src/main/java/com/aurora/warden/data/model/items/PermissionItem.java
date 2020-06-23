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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.R;
import com.aurora.warden.data.model.Permission;
import com.aurora.warden.manager.camtono.CamtonoManager;
import com.aurora.warden.utils.Log;
import com.aurora.warden.utils.Util;
import com.aurora.warden.utils.app.ComponentUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionItem extends AbstractItem<PermissionItem.ViewHolder> {

    private Permission permission;
    private boolean isActive = false;

    public PermissionItem(Permission permission) {
        this.permission = permission;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_permission_info;
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

    static class ViewHolder extends FastAdapter.ViewHolder<PermissionItem> {
        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.line3)
        TextView line3;
        @BindView(R.id.line4)
        TextView line4;
        @BindView(R.id.component_switch)
        SwitchMaterial switchMaterial;
        CamtonoManager camtonoManager;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            camtonoManager = CamtonoManager.getInstance(itemView.getContext());
        }

        @Override
        public void bindView(@NotNull PermissionItem item, @NotNull List<?> list) {
            final Context context = itemView.getContext();
            final Permission permission = item.getPermission();

            img.setImageDrawable(permission.getIcon() != null
                    ? permission.getIcon()
                    : context.getDrawable(R.drawable.ic_unknown));
            line1.setText(permission.getName());
            line2.setText(permission.getPermission());
            line3.setText(ComponentUtil.getPermissionLevelString(context, permission.getProtectionLevel()));
            line3.setTextColor(Util.getProtectionColor(context, permission.getProtectionLevel()));
            line4.setText(permission.getDescription());

            switchMaterial.setChecked(item.getPermission().isGranted());
            switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
                boolean result;

                if (isChecked) {
                    result = camtonoManager.camtono()
                            .grant(item.getPermission().getPackageName(), item.getPermission().getPermission());
                } else {
                    result = camtonoManager.camtono()
                            .revoke(item.getPermission().getPackageName(), item.getPermission().getPermission());
                }

                if (result) {
                    item.setActive(isChecked);
                    Log.e("Permission changed -> %s : %s", permission.getPackageName(), permission.getPermission());
                    Toast.makeText(context, context.getString(R.string.txt_camtono_success), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Permission un-changed -> %s : %s", permission.getPackageName(), permission.getPermission());
                    Toast.makeText(context, String.format(context.getString(R.string.txt_camtono_failed) + " : %s",
                            AuroraApplication.isRooted()
                                    ? context.getString(R.string.txt_camtono_immutable_permission)
                                    : context.getString(R.string.txt_camtono_no_root)),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void unbindView(@NotNull PermissionItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
            switchMaterial.setOnCheckedChangeListener(null);
        }
    }
}
