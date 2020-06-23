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

package com.aurora.warden.ui.custom.view.carousel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


public class CarouselRecycler extends RecyclerView {

    private float mDownX;

    private CarouselLayoutManger.Builder builder;

    public CarouselRecycler(Context context) {
        super(context);
        init();
    }

    public CarouselRecycler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CarouselRecycler(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        createManageBuilder();
        setLayoutManager(builder.build());
        setChildrenDrawingOrderEnabled(true);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }


    private void createManageBuilder() {
        if (builder == null) {
            builder = new CarouselLayoutManger.Builder();
        }
    }


    public void setFlatFlow(boolean isFlat) {
        createManageBuilder();
        builder.setFlat(isFlat);
        setLayoutManager(builder.build());
    }


    public void setGreyItem(boolean greyItem) {
        createManageBuilder();
        builder.setGreyItem(greyItem);
        setLayoutManager(builder.build());
    }


    public void setAlphaItem(boolean alphaItem) {
        createManageBuilder();
        builder.setAlphaItem(alphaItem);
        setLayoutManager(builder.build());
    }


    public void setIntervalRatio(float intervalRatio) {
        createManageBuilder();
        builder.setRatio(intervalRatio);
        setLayoutManager(builder.build());
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (!(layout instanceof CarouselLayoutManger)) {
            throw new IllegalArgumentException("Invalid Layout Manager, expected CarouselLayoutManager");
        }
        super.setLayoutManager(layout);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int center = getCoverFlowLayout().getCenterPosition() - getCoverFlowLayout().getFirstVisiblePosition();

        if (center < 0)
            center = 0;
        else if (center > childCount)
            center = childCount;

        int order;
        if (i == center) {
            order = childCount - 1;
        } else if (i > center) {
            order = center + childCount - 1 - i;
        } else {
            order = i;
        }
        return order;
    }


    public CarouselLayoutManger getCoverFlowLayout() {
        return ((CarouselLayoutManger) getLayoutManager());
    }


    public int getSelectedPos() {
        return getCoverFlowLayout().getSelectedPos();
    }


    public void setOnItemSelectedListener(CarouselLayoutManger.OnSelectedListener onSelectedListener) {
        getCoverFlowLayout().setOnSelectedListener(onSelectedListener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if ((event.getX() > mDownX && getCoverFlowLayout().getCenterPosition() == 0)
                        || (event.getX() < mDownX && getCoverFlowLayout().getCenterPosition() == getCoverFlowLayout().getItemCount() - 1)) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
