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


package com.aurora.warden.ui.sheets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aurora.warden.BuildConfig;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.Update;
import com.aurora.warden.services.SelfUpdateService;
import com.aurora.warden.utils.CertUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lombok.SneakyThrows;

public class UpdateSheet extends BaseBottomSheet {

    public static final String TAG = "UPDATE_SHEET";

    @BindView(R.id.txt_changelog)
    TextView txtChangelog;

    private Update update;

    public UpdateSheet() {
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_update, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            stringExtra = bundle.getString(Constants.STRING_EXTRA);
            update = gson.fromJson(stringExtra, Update.class);
            if (update != null)
                populateData(update);
            else
                dismissAllowingStateLoss();
        } else {
            dismissAllowingStateLoss();
        }
    }

    @OnClick(R.id.btn_negative)
    public void closeSheet() {
        dismissAllowingStateLoss();
    }

    @SneakyThrows
    @OnClick(R.id.btn_positive)
    public void startUpdate() {
        final Intent intent = new Intent(requireContext(), SelfUpdateService.class);
        final boolean isFDroidBuild = CertUtil.isFDroidApp(requireContext(), BuildConfig.APPLICATION_ID);
        intent.putExtra(Constants.STRING_EXTRA, isFDroidBuild ? update.getFdroidBuild() : update.getAuroraBuild());
        requireContext().startService(intent);
        dismissAllowingStateLoss();
    }

    private void populateData(Update update) {
        txtChangelog.setText(update.getChangelog());
    }
}
