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

package com.aurora.warden.ui.custom.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.aurora.warden.R;
import com.aurora.warden.ui.custom.layout.FabTextLayout;
import com.aurora.warden.utils.ViewUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * CircleMenuView
 */
public class CircleMenuView extends FrameLayout {
    private static final int DEFAULT_BUTTON_SIZE = 56;
    private static final float DEFAULT_DISTANCE = DEFAULT_BUTTON_SIZE * 1.5f;
    private static final float DEFAULT_RING_SCALE_RATIO = 1.3f;
    private static final float DEFAULT_CLOSE_ICON_ALPHA = 0.3f;

    private final List<FabTextLayout> extraFabTextLayouts = new ArrayList<>();
    private final Rect buttonRect = new Rect();
    @BindView(R.id.ring_view)
    RingEffectView ringEffectView;
    @BindView(R.id.circle_menu_main_button)
    FloatingActionButton fabMain;
    private boolean closedState = true;
    private boolean isAnimating = false;
    private int iconMenu;
    private int iconClose;
    private int durationRing;
    private int durationLongClick;
    private int durationOpen;
    private int durationClose;
    private int desiredSize;
    private int ringRadius;
    private float distance;
    private EventListener eventListener;

    public CircleMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleMenuView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs == null) {
            throw new IllegalArgumentException("No buttons icons or colors set");
        }

        final int menuButtonColor;
        final List<Integer> icons;
        final List<Integer> colors;
        final List<Integer> actionStrings;

        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleMenuView, 0, 0);
        try {
            final int iconArrayId = a.getResourceId(R.styleable.CircleMenuView_button_icons, 0);
            final int colorArrayId = a.getResourceId(R.styleable.CircleMenuView_button_colors, 0);
            final int stringArrayId = a.getResourceId(R.styleable.CircleMenuView_button_actions, 0);

            final TypedArray iconsIds = getResources().obtainTypedArray(iconArrayId);
            final TypedArray stringIds = getResources().obtainTypedArray(stringArrayId);
            try {
                final int[] colorsIds = getResources().getIntArray(colorArrayId);
                final int buttonsCount = Math.min(iconsIds.length(), colorsIds.length);

                icons = new ArrayList<>(buttonsCount);
                colors = new ArrayList<>(buttonsCount);
                actionStrings = new ArrayList<>(buttonsCount);

                for (int i = 0; i < buttonsCount; i++) {
                    icons.add(iconsIds.getResourceId(i, -1));
                    colors.add(colorsIds[i]);
                    actionStrings.add(stringIds.getResourceId(i, -1));
                }
            } finally {
                iconsIds.recycle();
                stringIds.recycle();
            }

            iconMenu = a.getResourceId(R.styleable.CircleMenuView_icon_menu, R.drawable.ic_hashtag);
            iconClose = a.getResourceId(R.styleable.CircleMenuView_icon_close, R.drawable.ic_menu_close);

            durationRing = a.getInteger(R.styleable.CircleMenuView_duration_ring, getResources().getInteger(android.R.integer.config_mediumAnimTime));
            durationLongClick = a.getInteger(R.styleable.CircleMenuView_long_click_duration_ring, getResources().getInteger(android.R.integer.config_longAnimTime));
            durationOpen = a.getInteger(R.styleable.CircleMenuView_duration_open, getResources().getInteger(android.R.integer.config_mediumAnimTime));
            durationClose = a.getInteger(R.styleable.CircleMenuView_duration_close, getResources().getInteger(android.R.integer.config_mediumAnimTime));

            final float density = context.getResources().getDisplayMetrics().density;
            final float defaultDistance = DEFAULT_DISTANCE * density;

            distance = a.getDimension(R.styleable.CircleMenuView_distance, defaultDistance);
            menuButtonColor = a.getColor(R.styleable.CircleMenuView_icon_color, Color.WHITE);
        } finally {
            a.recycle();
        }

        initLayout(context);
        initMenu(menuButtonColor);
        initButtons(context, icons, colors, actionStrings);
    }

    public CircleMenuView(@NonNull Context context, @NonNull List<Integer> icons, @NonNull List<Integer> colors, @NonNull List<Integer> stringAction) {
        super(context);

        final float density = context.getResources().getDisplayMetrics().density;
        final float defaultDistance = DEFAULT_DISTANCE * density;
        final int colorAccent = ViewUtil.getStyledAttribute(getContext(), R.attr.colorAccent);

        iconMenu = R.drawable.ic_hashtag;
        iconClose = R.drawable.ic_close;

        durationRing = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        durationLongClick = getResources().getInteger(android.R.integer.config_longAnimTime);
        durationOpen = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        durationClose = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        distance = defaultDistance;

        initLayout(context);
        initMenu(colorAccent);
        initButtons(context, icons, colors, stringAction);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = resolveSizeAndState(desiredSize, widthMeasureSpec, 0);
        final int height = resolveSizeAndState(desiredSize, heightMeasureSpec, 0);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!changed && isAnimating) {
            return;
        }

        fabMain.getMeasuredContentRect(buttonRect);
        ringEffectView.setStrokeWidth(buttonRect.width());
        ringEffectView.setRadius(ringRadius);

        final LayoutParams layoutParams = (LayoutParams) ringEffectView.getLayoutParams();
        layoutParams.width = right - left;
        layoutParams.height = bottom - top;
    }

    private void initLayout(@NonNull Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_circle_menu, this, true);
        ButterKnife.bind(this, view);

        setWillNotDraw(true);
        setClipChildren(false);
        setClipToPadding(false);

        final float density = context.getResources().getDisplayMetrics().density;
        final float buttonSize = DEFAULT_BUTTON_SIZE * density;

        ringRadius = (int) (buttonSize + (distance - buttonSize / 2));
        desiredSize = (int) (ringRadius * 2 * DEFAULT_RING_SCALE_RATIO);
    }

    private void initMenu(int menuButtonColor) {

        final AnimatorListenerAdapter animListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (eventListener != null) {
                    if (closedState) {
                        eventListener.onMenuOpenAnimationStart(CircleMenuView.this);
                    } else {
                        eventListener.onMenuCloseAnimationStart(CircleMenuView.this);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (eventListener != null) {
                    if (closedState) {
                        eventListener.onMenuOpenAnimationEnd(CircleMenuView.this);
                    } else {
                        eventListener.onMenuCloseAnimationEnd(CircleMenuView.this);
                    }
                }
                closedState = !closedState;
            }
        };

        fabMain.setImageResource(iconMenu);
        fabMain.setBackgroundTintList(ColorStateList.valueOf(menuButtonColor));
        fabMain.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        fabMain.setOnClickListener(view -> {
            if (isAnimating) {
                return;
            }

            final Animator animation = closedState ? getOpenMenuAnimation() : getCloseMenuAnimation();
            animation.setDuration(closedState ? durationClose : durationOpen);
            animation.addListener(animListener);
            animation.start();
        });
    }

    private void initButtons(@NonNull Context context, @NonNull List<Integer> icons, @NonNull List<Integer> colors, @NonNull List<Integer> actionStrings) {
        final int buttonsCount = Math.min(icons.size(), colors.size());
        for (int i = 0; i < buttonsCount; i++) {
            final int color = context.getResources().getColor(colors.get(i));
            final Drawable icon = context.getResources().getDrawable(icons.get(i));
            final FabTextLayout fabTextLayout = new FabTextLayout(context);
            fabTextLayout.setColor(color);
            fabTextLayout.setupFab(color, icon);
            fabTextLayout.setText(getContext().getString(actionStrings.get(i)));
            fabTextLayout.setScaleX(0);
            fabTextLayout.setScaleY(0);
            fabTextLayout.setClickable(true);
            fabTextLayout.setOnClickListener(new OnButtonClickListener());
            fabTextLayout.setOnLongClickListener(new OnButtonLongClickListener());
            fabTextLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            addView(fabTextLayout);
            extraFabTextLayouts.add(fabTextLayout);
        }
    }

    private void offsetAndScaleButtons(float centerX, float centerY, float angleStep, float offset, float scale) {
        for (int i = 0, cnt = extraFabTextLayouts.size(); i < cnt; i++) {
            final float angle = angleStep * i - 90;
            final float x = (float) Math.cos(Math.toRadians(angle)) * offset;
            final float y = (float) Math.sin(Math.toRadians(angle)) * offset;

            final FabTextLayout fabTextLayout = extraFabTextLayouts.get(i);
            fabTextLayout.setX(centerX + x);
            fabTextLayout.setY(centerY + y);
            fabTextLayout.setScaleX(1.0f * scale);
            fabTextLayout.setScaleY(1.0f * scale);
        }
    }

    private Animator getButtonClickAnimation(final @NonNull FabTextLayout fabTextLayout) {
        final int buttonNumber = extraFabTextLayouts.indexOf(fabTextLayout) + 1;
        final float stepAngle = 360.0f / extraFabTextLayouts.size();
        final float rOStartAngle = (270 - stepAngle + stepAngle * buttonNumber);
        final float rStartAngle = rOStartAngle > 360 ? rOStartAngle % 360 : rOStartAngle;

        final float x = (float) Math.cos(Math.toRadians(rStartAngle)) * distance;
        final float y = (float) Math.sin(Math.toRadians(rStartAngle)) * distance;

        final float pivotX = fabTextLayout.getPivotX();
        final float pivotY = fabTextLayout.getPivotY();

        fabTextLayout.setPivotX(pivotX - x);
        fabTextLayout.setPivotY(pivotY - y);

        final ObjectAnimator rotateButton = ObjectAnimator.ofFloat(fabTextLayout, "rotation", 0f, 360f);
        rotateButton.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fabTextLayout.setPivotX(pivotX);
                fabTextLayout.setPivotY(pivotY);
            }
        });

        final float elevation = fabMain.getCompatElevation();

        ringEffectView.setVisibility(View.INVISIBLE);
        ringEffectView.setStartAngle(rStartAngle);
        ringEffectView.setStrokeColor(ColorStateList.valueOf(fabTextLayout.getColor()).getDefaultColor());

        final ObjectAnimator ring = ObjectAnimator.ofFloat(ringEffectView, "angle", 360);
        final ObjectAnimator scaleX = ObjectAnimator.ofFloat(ringEffectView, "scaleX", 1f, DEFAULT_RING_SCALE_RATIO);
        final ObjectAnimator scaleY = ObjectAnimator.ofFloat(ringEffectView, "scaleY", 1f, DEFAULT_RING_SCALE_RATIO);
        final ObjectAnimator visible = ObjectAnimator.ofFloat(ringEffectView, "alpha", 1f, 0f);

        final AnimatorSet lastSet = new AnimatorSet();
        lastSet.playTogether(scaleX, scaleY, visible, getCloseMenuAnimation());

        final AnimatorSet firstSet = new AnimatorSet();
        firstSet.playTogether(rotateButton, ring);

        final AnimatorSet result = new AnimatorSet();
        result.play(firstSet).before(lastSet);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
                fabTextLayout.setElevation(elevation + 1);
                ViewCompat.setZ(ringEffectView, elevation + 1);

                for (FabTextLayout ftl : extraFabTextLayouts) {
                    if (fabTextLayout != ftl) {
                        fabTextLayout.setElevation(0);
                    }
                }

                ringEffectView.setScaleX(1f);
                ringEffectView.setScaleY(1f);
                ringEffectView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    for (View view : extraFabTextLayouts) {
                        view.setElevation(elevation);
                    }
                    ViewCompat.setZ(ringEffectView, elevation);
                }
            }
        });

        return result;
    }

    private Animator getOpenMenuAnimation() {
        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(fabMain, "alpha", DEFAULT_CLOSE_ICON_ALPHA);

        final Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        final Keyframe kf1 = Keyframe.ofFloat(0.5f, 60f);
        final Keyframe kf2 = Keyframe.ofFloat(1f, 0f);

        final PropertyValuesHolder valuesHolder = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        final ObjectAnimator rotateAnimation = ObjectAnimator.ofPropertyValuesHolder(fabMain, valuesHolder);

        rotateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private boolean iconChanged = false;

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float fraction = valueAnimator.getAnimatedFraction();
                if (fraction >= 0.5f && !iconChanged) {
                    iconChanged = true;
                    fabMain.setImageResource(iconClose);
                }
            }
        });

        final float centerX = fabMain.getX();
        final float centerY = fabMain.getY();

        final int buttonsCount = extraFabTextLayouts.size();
        final float angleStep = 360f / buttonsCount;

        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, distance);
        valueAnimator.setInterpolator(new OvershootInterpolator());
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (View view : extraFabTextLayouts) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        });

        valueAnimator.addUpdateListener(valueAnimator1 -> {
            final float fraction = valueAnimator1.getAnimatedFraction();
            final float value = (float) valueAnimator1.getAnimatedValue();
            offsetAndScaleButtons(centerX, centerY, angleStep, value, fraction);
        });

        final AnimatorSet result = new AnimatorSet();
        result.playTogether(objectAnimator, rotateAnimation, valueAnimator);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }
        });
        setBackground(getContext().getDrawable(R.drawable.circle_menu_bg));
        return result;
    }

    private Animator getCloseMenuAnimation() {
        final ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(fabMain, "scaleX", 0f);
        final ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(fabMain, "scaleY", 0f);
        final ObjectAnimator alpha1 = ObjectAnimator.ofFloat(fabMain, "alpha", 0f);
        final AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(scaleX1, scaleY1, alpha1);
        set1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (View view : extraFabTextLayouts) {
                    view.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fabMain.setRotation(60f);
                fabMain.setImageResource(iconMenu);
            }
        });

        final ObjectAnimator angle = ObjectAnimator.ofFloat(fabMain, "rotation", 0);
        final ObjectAnimator alpha2 = ObjectAnimator.ofFloat(fabMain, "alpha", 1f);
        final ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(fabMain, "scaleX", 1f);
        final ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(fabMain, "scaleY", 1f);

        final AnimatorSet set2 = new AnimatorSet();
        set2.setInterpolator(new OvershootInterpolator());
        set2.playTogether(angle, alpha2, scaleX2, scaleY2);

        final AnimatorSet result = new AnimatorSet();
        result.play(set1).before(set2);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }
        });
        setBackground(null);
        return result;
    }

    /**
     * See {@link EventListener }
     *
     * @return current event listener or null.
     */
    public EventListener getEventListener() {
        return eventListener;
    }

    /**
     * See {@link EventListener }
     *
     * @param listener new event listener or null.
     */
    public void setEventListener(@Nullable EventListener listener) {
        eventListener = listener;
    }

    private void openOrClose(boolean open, boolean animate) {
        if (isAnimating) {
            return;
        }

        if (open && !closedState) {
            return;
        }

        if (!open && closedState) {
            return;
        }

        if (animate) {
            fabMain.performClick();
        } else {
            closedState = !open;

            final float centerX = fabMain.getX();
            final float centerY = fabMain.getY();

            final int buttonsCount = extraFabTextLayouts.size();
            final float angleStep = 360f / buttonsCount;

            final float offset = open ? distance : 0f;
            final float scale = open ? 1f : 0f;

            fabMain.setImageResource(open ? iconClose : iconMenu);
            fabMain.setAlpha(open ? DEFAULT_CLOSE_ICON_ALPHA : 1f);

            final int visibility = open ? View.VISIBLE : View.INVISIBLE;
            for (FabTextLayout fabTextLayout : extraFabTextLayouts) {
                fabTextLayout.setVisibility(visibility);
            }

            offsetAndScaleButtons(centerX, centerY, angleStep, offset, scale);
        }
    }

    public void open(boolean animate) {
        openOrClose(true, animate);
    }

    public void close(boolean animate) {
        openOrClose(false, animate);
    }

    public static class EventListener {

        public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
        }

        public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
        }

        public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
        }

        public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
        }

        public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {
        }

        public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {
        }

        public boolean onButtonLongClick(@NonNull CircleMenuView view, int buttonIndex) {
            return false;
        }

        public void onButtonLongClickAnimationStart(@NonNull CircleMenuView view, int buttonIndex) {
        }

        public void onButtonLongClickAnimationEnd(@NonNull CircleMenuView view, int buttonIndex) {
        }
    }

    public class OnButtonClickListener implements OnClickListener {
        @Override
        public void onClick(final View view) {
            if (isAnimating) {
                return;
            }

            final Animator animation = getButtonClickAnimation((FabTextLayout) view);
            animation.setDuration(durationRing);
            animation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (eventListener != null) {
                        eventListener.onButtonClickAnimationStart(CircleMenuView.this, extraFabTextLayouts.indexOf(view));
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    closedState = true;
                    if (eventListener != null) {
                        eventListener.onButtonClickAnimationEnd(CircleMenuView.this, extraFabTextLayouts.indexOf(view));
                    }
                }
            });
            animation.start();
        }
    }

    public class OnButtonLongClickListener implements OnLongClickListener {
        @Override
        public boolean onLongClick(final View view) {
            if (eventListener == null) {
                return false;
            }

            final boolean result = eventListener.onButtonLongClick(CircleMenuView.this, extraFabTextLayouts.indexOf((FabTextLayout) view));

            if (result && !isAnimating) {

                final Animator animation = getButtonClickAnimation((FabTextLayout) view);
                animation.setDuration(durationLongClick);
                animation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        eventListener.onButtonLongClickAnimationStart(CircleMenuView.this, extraFabTextLayouts.indexOf(view));
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        closedState = true;
                        eventListener.onButtonLongClickAnimationEnd(CircleMenuView.this, extraFabTextLayouts.indexOf(view));
                    }
                });
                animation.start();
            }
            return result;
        }
    }

}

