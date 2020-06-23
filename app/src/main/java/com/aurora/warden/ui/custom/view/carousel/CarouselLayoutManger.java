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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.recyclerview.widget.RecyclerView;

public class CarouselLayoutManger extends RecyclerView.LayoutManager {

    private static int SCROLL_LEFT = 1;
    private static int SCROLL_RIGHT = 2;
    private final int MAX_RECT_COUNT = 100;
    private int offsetAll = 0;
    private int decoratedChildWidth = 0;
    private int decoratedChildHeight = 0;
    private int startX = 0;
    private int startY = 0;
    private int selectedPosition = 0;
    private int lastSelectPosition = 0;
    private float ratio = 0.5f;

    private SparseArray<Rect> rectSparseArray = new SparseArray<>();
    private SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();
    private RecyclerView.Recycler recycler;
    private RecyclerView.State state;
    private ValueAnimator animator;
    private OnSelectedListener onSelectedListener;

    private boolean isFlat = false;
    private boolean isGreyItem = false;
    private boolean isAlphaItem = false;

    public CarouselLayoutManger(boolean isFlat, boolean isGreyItem, boolean isAlphaItem, float ratio) {
        this.isFlat = isFlat;
        this.isGreyItem = isGreyItem;
        this.isAlphaItem = isAlphaItem;
        if (ratio >= 0) {
            this.ratio = ratio;
        } else {
            if (this.isFlat) {
                this.ratio = 1.1f;
            }
        }
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() <= 0 || state.isPreLayout()) {
            offsetAll = 0;
            return;
        }
        rectSparseArray.clear();
        sparseBooleanArray.clear();
        View scrap = recycler.getViewForPosition(0);
        addView(scrap);
        measureChildWithMargins(scrap, 0, 0);
        decoratedChildWidth = getDecoratedMeasuredWidth(scrap);
        decoratedChildHeight = getDecoratedMeasuredHeight(scrap);
        startX = Math.round((getHorizontalSpace() - decoratedChildWidth) * 1.0f / 2);
        startY = Math.round((getVerticalSpace() - decoratedChildHeight) * 1.0f / 2);
        float offset = startX;
        for (int i = 0; i < getItemCount() && i < MAX_RECT_COUNT; i++) {
            Rect frame = rectSparseArray.get(i);
            if (frame == null) {
                frame = new Rect();
            }
            frame.set(Math.round(offset), startY, Math.round(offset + decoratedChildWidth), startY + decoratedChildHeight);
            rectSparseArray.put(i, frame);
            sparseBooleanArray.put(i, false);
            offset = offset + getIntervalDistance();
        }
        detachAndScrapAttachedViews(recycler);
        if ((this.recycler == null || this.state == null) && selectedPosition != 0) {
            offsetAll = calculateOffsetForPosition(selectedPosition);
            onSelectedCallBack();
        }
        layoutItems(recycler, state, SCROLL_RIGHT);
        this.recycler = recycler;
        this.state = state;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (animator != null && animator.isRunning()) animator.cancel();
        int travel = dx;
        if (dx + offsetAll < 0) {
            travel = -offsetAll;
        } else if (dx + offsetAll > getMaxOffset()) {
            travel = (int) (getMaxOffset() - offsetAll);
        }
        offsetAll += travel;
        layoutItems(recycler, state, dx > 0 ? SCROLL_RIGHT : SCROLL_LEFT);
        return travel;
    }

    private void layoutItems(RecyclerView.Recycler recycler, RecyclerView.State state, int scrollDirection) {
        if (state == null || state.isPreLayout()) return;
        Rect displayFrame = new Rect(offsetAll, 0, offsetAll + getHorizontalSpace(), getVerticalSpace());
        int position = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) continue;
            position = getPosition(child);
            Rect rect = getFrame(position);
            if (!Rect.intersects(displayFrame, rect)) {
                removeAndRecycleView(child, recycler);
                sparseBooleanArray.delete(position);
            } else {
                layoutItem(child, rect);
                sparseBooleanArray.put(position, true);
            }
        }
        if (position == 0) position = selectedPosition;
        int min = Math.max(position - 50, 0);
        int max = Math.min(position + 50, getItemCount());
        for (int i = min; i < max; i++) {
            Rect rect = getFrame(i);
            if (Rect.intersects(displayFrame, rect) && !sparseBooleanArray.get(i)) {
                View scrap = recycler.getViewForPosition(i);
                measureChildWithMargins(scrap, 0, 0);
                if (scrollDirection == SCROLL_LEFT || isFlat) {
                    addView(scrap, 0);
                } else {
                    addView(scrap);
                }
                layoutItem(scrap, rect);
                sparseBooleanArray.put(i, true);
            }
        }
    }

    private void layoutItem(View child, Rect frame) {
        layoutDecorated(child, frame.left - offsetAll, frame.top, frame.right - offsetAll, frame.bottom);
        if (!isFlat) {
            child.setScaleX(computeScale(frame.left - offsetAll));
            child.setScaleY(computeScale(frame.left - offsetAll));
        }
        if (isAlphaItem) {
            child.setAlpha(computeAlpha(frame.left - offsetAll));
        }
        if (isGreyItem) {
            greyItem(child, frame);
        }
    }

    private Rect getFrame(int index) {
        Rect frame = rectSparseArray.get(index);
        if (frame == null) {
            frame = new Rect();
            float offset = startX + getIntervalDistance() * index;
            frame.set(Math.round(offset), startY, Math.round(offset + decoratedChildWidth), startY + decoratedChildHeight);
        }
        return frame;
    }

    private void greyItem(View child, Rect frame) {
        float value = computeGreyScale(frame.left - offsetAll);
        ColorMatrix cm = new ColorMatrix(new float[]{
                value, 0, 0, 0, 120 * (1 - value),
                0, value, 0, 0, 120 * (1 - value),
                0, 0, value, 0, 120 * (1 - value),
                0, 0, 0, 1, 250 * (1 - value)
        });
        Paint greyPaint = new Paint();
        greyPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        child.setLayerType(View.LAYER_TYPE_HARDWARE, greyPaint);
        if (value >= 1) {
            child.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE:
                fixOffsetWhenFinishScroll();
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                break;
        }
    }

    @Override
    public void scrollToPosition(int position) {
        if (position < 0 || position > getItemCount() - 1) return;
        offsetAll = calculateOffsetForPosition(position);
        if (recycler == null || state == null) {
            selectedPosition = position;
        } else {
            layoutItems(recycler, state, position > selectedPosition ? SCROLL_RIGHT : SCROLL_LEFT);
            onSelectedCallBack();
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        int finalOffset = calculateOffsetForPosition(position);
        if (recycler == null || this.state == null) {
            selectedPosition = position;
        } else {
            startScroll(offsetAll, finalOffset);
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
        recycler = null;
        state = null;
        offsetAll = 0;
        selectedPosition = 0;
        lastSelectPosition = 0;
        sparseBooleanArray.clear();
        rectSparseArray.clear();
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    private float getMaxOffset() {
        return (getItemCount() - 1) * getIntervalDistance();
    }

    private float computeScale(int x) {
        float scale = 1 - Math.abs(x - startX) * 1.0f / Math.abs(startX + decoratedChildWidth / ratio);
        if (scale < 0) scale = 0;
        if (scale > 1) scale = 1;
        return scale;
    }

    private float computeGreyScale(int x) {
        float itemMidPos = x + decoratedChildWidth / 2f;
        float itemDx2Mid = Math.abs(itemMidPos - getHorizontalSpace() / 2f);
        float value = 1 - itemDx2Mid * 1.0f / (getHorizontalSpace() / 2f);
        if (value < 0.1) value = 0.1f;
        if (value > 1) value = 1;
        value = (float) Math.pow(value, .8);
        return value;
    }

    private float computeAlpha(int x) {
        float alpha = 1 - Math.abs(x - startX) * 1.0f / Math.abs(startX + decoratedChildWidth / ratio);
        if (alpha < 0.3f) alpha = 0.3f;
        if (alpha > 1) alpha = 1.0f;
        return alpha;
    }

    private int calculateOffsetForPosition(int position) {
        return Math.round(getIntervalDistance() * position);
    }

    private void fixOffsetWhenFinishScroll() {
        int scrollN = (int) (offsetAll * 1.0f / getIntervalDistance());
        float moreDx = (offsetAll % getIntervalDistance());
        if (moreDx > (getIntervalDistance() * 0.5)) {
            scrollN++;
        }
        int finalOffset = (int) (scrollN * getIntervalDistance());
        startScroll(offsetAll, finalOffset);
        selectedPosition = Math.round(finalOffset * 1.0f / getIntervalDistance());
    }

    private void startScroll(int from, int to) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        final int direction = from < to ? SCROLL_RIGHT : SCROLL_LEFT;
        animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            offsetAll = Math.round((float) animation.getAnimatedValue());
            layoutItems(recycler, state, direction);
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onSelectedCallBack();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    private float getIntervalDistance() {
        return decoratedChildWidth * ratio;
    }

    private void onSelectedCallBack() {
        selectedPosition = Math.round(offsetAll / getIntervalDistance());
        if (onSelectedListener != null && selectedPosition != lastSelectPosition) {
            onSelectedListener.onItemSelected(selectedPosition);
        }
        lastSelectPosition = selectedPosition;
    }

    public int getFirstVisiblePosition() {
        Rect displayFrame = new Rect(offsetAll, 0, offsetAll + getHorizontalSpace(), getVerticalSpace());
        int cur = getCenterPosition();
        for (int i = cur - 1; i >= 0; i--) {
            Rect rect = getFrame(i);
            if (!Rect.intersects(displayFrame, rect)) {
                return i + 1;
            }
        }
        return 0;
    }

    public int getLastVisiblePosition() {
        Rect displayFrame = new Rect(offsetAll, 0, offsetAll + getHorizontalSpace(), getVerticalSpace());
        int cur = getCenterPosition();
        for (int i = cur + 1; i < getItemCount(); i++) {
            Rect rect = getFrame(i);
            if (!Rect.intersects(displayFrame, rect)) {
                return i - 1;
            }
        }
        return cur;
    }

    public int getMaxVisibleCount() {
        int oneSide = (int) ((getHorizontalSpace() - startX) / (getIntervalDistance()));
        return oneSide * 2 + 1;
    }

    public int getCenterPosition() {
        int pos = (int) (offsetAll / getIntervalDistance());
        int more = (int) (offsetAll % getIntervalDistance());
        if (more > getIntervalDistance() * 0.5f) pos++;
        return pos;
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    public int getSelectedPos() {
        return selectedPosition;
    }

    public interface OnSelectedListener {
        void onItemSelected(int position);
    }

    public static class Builder {
        boolean isFlat = false;
        boolean isGreyItem = false;
        boolean isAlphaItem = false;
        float ratio = -1f;

        public Builder setFlat(boolean flat) {
            isFlat = flat;
            return this;
        }

        public Builder setGreyItem(boolean greyItem) {
            isGreyItem = greyItem;
            return this;
        }

        public Builder setAlphaItem(boolean alphaItem) {
            isAlphaItem = alphaItem;
            return this;
        }

        public Builder setRatio(float ratio) {
            this.ratio = ratio;
            return this;
        }

        public CarouselLayoutManger build() {
            return new CarouselLayoutManger(isFlat, isGreyItem, isAlphaItem, ratio);
        }
    }
}