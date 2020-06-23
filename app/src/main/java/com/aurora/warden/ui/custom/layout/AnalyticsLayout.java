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
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.items.ExodusReportItem;
import com.aurora.warden.data.model.items.base.ListItem;
import com.aurora.warden.ui.activities.GenericActivity;
import com.aurora.warden.utils.ViewUtil;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Size;

public class AnalyticsLayout extends ViewFlipper {

    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.viewKonfetti)
    KonfettiView viewKonfetti;
    @BindView(R.id.view_flipper)
    ViewFlipper viewFlipper;
    @BindView(R.id.txt_analytics)
    InfoLayout txtAnalytics;
    @BindView(R.id.txt_working)
    InfoLayout txtWorking;

    private Gson gson = new Gson();
    private int type = 0;
    private FastItemAdapter<ListItem> fastItemAdapter;

    public AnalyticsLayout(Context context) {
        super(context);
        init(context, null);
    }

    public AnalyticsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_shit, this);
        ButterKnife.bind(this, view);
        setupRecycler();
        viewFlipper.setDisplayedChild(0);
    }

    public void setType(int type) {
        this.type = type;
        //Set Exodus working description
        if (type == 3) {
            txtWorking.setTxtSecondary(getContext().getString(R.string.string_info_tracker_working_exodus_desc));
        }
    }

    public void switchState(State state) {
        switch (state) {
            case PROGRESS:
                viewFlipper.setDisplayedChild(0);
                break;
            case RESULT:
                viewFlipper.setDisplayedChild(1);
                break;
            case EMPTY:
                viewFlipper.setDisplayedChild(2);
                txtAnalytics.setTxtPrimary(getContext().getString(type == 0
                        ? R.string.string_info_tracker_no_title
                        : R.string.string_info_logger_no_title
                ));
                txtAnalytics.setTxtSecondary(getContext().getString(type == 0
                        ? R.string.string_info_tracker_no_desc
                        : R.string.string_info_logger_no_desc
                ));
                startKonfetti();
                break;
        }
    }

    public void setTrackerData(List<? extends ListItem> items) {
        fastItemAdapter.clear();
        fastItemAdapter.add(items);
        recycler.scheduleLayoutAnimation();
    }

    private void setupRecycler() {
        fastItemAdapter = new FastItemAdapter<>();

        fastItemAdapter.setOnClickListener((view, listItemIAdapter, listItem, integer) -> {
            if (listItem instanceof ExodusReportItem) {
                final Intent intent = new Intent(getContext(), GenericActivity.class);
                intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_TRACKERS);
                intent.putExtra(Constants.STRING_EXTRA, gson.toJson(((ExodusReportItem) listItem).getReport()));
                getContext().startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) getContext()));
            }


            return false;
        });

        recycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recycler.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down));
        recycler.setAdapter(fastItemAdapter);
    }

    private void startKonfetti() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int width = getWidth() == 0 ? displayMetrics.widthPixels : getWidth();
        int height = getHeight() == 0 ? displayMetrics.heightPixels : getHeight();

        viewKonfetti.build()
                .addColors(
                        getResources().getColor(R.color.colorShade01),
                        getResources().getColor(R.color.colorShade02),
                        getResources().getColor(R.color.colorShade03),
                        getResources().getColor(R.color.colorShade04),
                        getResources().getColor(R.color.colorShade05)
                )
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .addSizes(new Size(10, 5f), new Size(12, 6f))
                .setPosition(width / 2.0f, height / 2.0f)
                .burst(300);
    }

    public enum State {
        PROGRESS,
        RESULT,
        EMPTY
    }
}
