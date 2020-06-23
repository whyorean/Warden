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
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.R;
import com.aurora.warden.manager.camtono.CamtonoManager;
import com.aurora.warden.utils.Log;
import com.aurora.warden.utils.app.ComponentUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentItem extends AbstractItem<ComponentItem.ViewHolder> {

    private ComponentInfo componentInfo;
    private boolean isActive = false;

    public ComponentItem(ComponentInfo componentInfo) {
        this.componentInfo = componentInfo;
    }

    public ComponentItem(ComponentInfo componentInfo, boolean isActive) {
        this.componentInfo = componentInfo;
        this.isActive = isActive;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_bundle_info;
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

    static class ViewHolder extends FastAdapter.ViewHolder<ComponentItem> {
        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.line3)
        TextView line3;
        @BindView(R.id.component_switch)
        @Getter
        @Setter
        SwitchMaterial switchMaterial;
        CamtonoManager camtonoManager;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            camtonoManager = CamtonoManager.getInstance(itemView.getContext());
        }

        @Override
        public void bindView(@NotNull ComponentItem item, @NotNull List<?> list) {
            final Context context = itemView.getContext();

            if (item.componentInfo instanceof ServiceInfo) {
                final ServiceInfo serviceInfo = (ServiceInfo) item.getComponentInfo();
                line1.setText(serviceInfo.name);
                line2.setText(context.getString(serviceInfo.enabled ? R.string.txt_enabled : R.string.txt_disabled));
                line3.setText(context.getString(serviceInfo.exported ? R.string.txt_exported : R.string.txt_not_exported));
            } else if (item.componentInfo instanceof ProviderInfo) {
                final ProviderInfo providerInfo = (ProviderInfo) item.getComponentInfo();
                line1.setText(providerInfo.name);
                line2.setText(context.getString(providerInfo.enabled ? R.string.txt_enabled : R.string.txt_disabled));
                line3.setText(context.getString(providerInfo.exported ? R.string.txt_exported : R.string.txt_not_exported));
            } else if (item.componentInfo instanceof ActivityInfo) {
                final ActivityInfo activityInfo = (ActivityInfo) item.getComponentInfo();
                line1.setText(activityInfo.name);
                line2.setText(StringUtils.joinWith(" • ",
                        ComponentUtil.getConfigString(activityInfo.configChanges),
                        ComponentUtil.getLaunchModeString(activityInfo.launchMode),
                        ComponentUtil.getPersistModeString(activityInfo.persistableMode)));
                line3.setText(StringUtils.EMPTY);
            }

            switchMaterial.setChecked(item.isActive());
            switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
                boolean result;
                if (isChecked) {
                    result = camtonoManager.camtono()
                            .enable(item.getComponentInfo().packageName, item.getComponentInfo().name);
                } else {
                    result = camtonoManager.camtono()
                            .disable(item.getComponentInfo().packageName, item.getComponentInfo().name);

                }

                if (result) {
                    item.setActive(isChecked);
                    Log.d("Component changed -> %s : %s", item.componentInfo.packageName, item.componentInfo.name);
                    Toast.makeText(context, context.getString(R.string.txt_camtono_success), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Component un-changed -> %s : %s", item.componentInfo.packageName, item.componentInfo.name);
                    Toast.makeText(context, String.format(context.getString(R.string.txt_camtono_failed) + " : %s",
                            AuroraApplication.isRooted()
                                    ? context.getString(R.string.txt_camtono_immutable_permission)
                                    : context.getString(R.string.txt_camtono_no_root)),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void unbindView(@NotNull ComponentItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
            switchMaterial.setOnCheckedChangeListener(null);
        }
    }
}
