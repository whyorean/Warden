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

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.Tracker;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.noties.markwon.Markwon;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TrackerFullSheet extends BaseBottomSheet {

    public static final String TAG = "TRACKER_SHEET";
    @BindView(R.id.img_close)
    AppCompatImageView imgClose;
    @BindView(R.id.title)
    AppCompatTextView title;
    @BindView(R.id.subtitle)
    AppCompatTextView subtitle;
    @BindView(R.id.text_description)
    TextView textDescription;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.Aurora_BottomSheetDialog);

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_base, null);
        bottomSheetDialog.setContentView(dialogView);

        FrameLayout container = dialogView.findViewById(R.id.container);
        View contentView = onCreateContentView(LayoutInflater.from(requireContext()), container, savedInstanceState);
        if (contentView != null) {
            onContentViewCreated(contentView, savedInstanceState);
            container.addView(contentView);
        }

        bottomSheetDialog.setOnShowListener(d -> {
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null)
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        BottomSheetBehavior<FrameLayout> bottomSheetBehavior = bottomSheetDialog.getBehavior();
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        return bottomSheetDialog;
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_tracker_full, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            String stringExtra = bundle.getString(Constants.STRING_EXTRA, null);
            if (StringUtils.isNotEmpty(stringExtra)) {
                populateData(stringExtra);
            } else {
                dismissAllowingStateLoss();
            }
        } else {
            dismissAllowingStateLoss();
        }
    }

    @OnClick(R.id.img_close)
    public void closeSheet() {
        dismissAllowingStateLoss();
    }

    private void populateData(String rawTracker) {
        Tracker tracker = gson.fromJson(rawTracker, Tracker.class);
        if (tracker != null) {
            title.setText(tracker.getName());
            subtitle.setText(tracker.getCreationDate());

            if (StringUtils.isNotEmpty(tracker.getDescription())) {
                Observable.fromCallable(() -> Markwon.builder(requireContext())
                        .usePlugin(GlideImagesPlugin.create(Glide.with(requireContext())))
                        .build())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(markwon -> markwon.setMarkdown(textDescription, tracker.getDescription()));
            } else {
                textDescription.setText("No description available");
            }
        } else {
            dismissAllowingStateLoss();
        }
    }
}
