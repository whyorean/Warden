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

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.aurora.warden.R;


public class WaveView extends View {

    private static final float LINE_SMOOTHNESS = 0.16f;

    private int pointCount;
    private int startGradientColor;
    private int endGradientColor;

    private float[] xs;
    private float[] ys;
    private float[] firstRadius;
    private float[] secondRadius;
    private float[] animationOffset;
    private float[] temp = new float[2];
    private float[] fractions;

    private float angleOffset;
    private float currentCx;
    private float currentCy;
    private float translationRadius;
    private float translationRadiusStep;
    private float lastTranslationAngle;
    private float innerSizeRatio = 0.75f;

    private ValueAnimator[] animators;
    private Path path;
    private Paint paint;
    private LinearGradient gradient;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        pointCount = typedArray.getInt(R.styleable.WaveView_pointCount, 6);
        startGradientColor = typedArray.getColor(R.styleable.WaveView_startGradientColor, 0XFF00B09B);
        endGradientColor = typedArray.getColor(R.styleable.WaveView_endGradientColor, 0XFF96C93d);
        typedArray.recycle();

        init(pointCount);

        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        float delta = 1.0f / pointCount;

        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = delta * i;
        }

        for (int i = 0; i < pointCount; i++) {
            int pos = (int) (Math.random() * pointCount);
            float[] dest = new float[pointCount * 2 + 1];
            int inc = 1;
            for (int j = 0; j < dest.length; j++) {
                dest[j] = fractions[pos];
                pos += inc;
                if (pos < 0 || pos >= fractions.length) {
                    inc = -inc;
                    pos += inc * 2;
                }
            }

            final ValueAnimator animator = ValueAnimator.ofFloat(dest).setDuration((long) (4000 + Math.random() * 2000));
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.start();
            if (i == 0) {
                animator.addUpdateListener(valueAnimator -> randomTranslate());
            }
            animators[i] = animator;
            animator.start();
        }
    }

    static float getFromArray(float[] arr, int pos) {
        return arr[(pos + arr.length) % arr.length];
    }

    static void getVector(float[] xs, float[] ys, int i, float[] out) {
        float nextX = getFromArray(xs, i + 1);
        float nextY = getFromArray(ys, i + 1);
        float prevX = getFromArray(xs, i - 1);
        float prevY = getFromArray(ys, i - 1);

        float vx = (nextX - prevX) * LINE_SMOOTHNESS;
        float vy = (nextY - prevY) * LINE_SMOOTHNESS;

        out[0] = vx;
        out[1] = vy;
    }

    private void randomTranslate() {
        float r = translationRadiusStep;
        float R = translationRadius;

        float cx = getWidth() / 2.0f;
        float cy = getHeight() / 2.0f;
        float vx = currentCx - cx;
        float vy = currentCy - cy;
        float ratio = 1 - r / R;
        float wx = vx * ratio;
        float wy = vy * ratio;
        float distRatio = (float) Math.random();

        lastTranslationAngle = (float) ((Math.random() - 0.5) * Math.PI / 4 + lastTranslationAngle);
        currentCx = (float) (cx + wx + r * distRatio * Math.cos(lastTranslationAngle));
        currentCy = (float) (cy + wy + r * distRatio * Math.sin(lastTranslationAngle));
        invalidate();
    }

    private void init(int pointCount) {
        firstRadius = new float[pointCount];
        secondRadius = new float[pointCount];
        animationOffset = new float[pointCount];

        xs = new float[pointCount];
        ys = new float[pointCount];

        fractions = new float[pointCount + 1];
        animators = new ValueAnimator[pointCount];
        angleOffset = (float) (Math.PI * 2 / pointCount * Math.random());
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        float radius = Math.min(width, height) / 2.0f;
        float innerRadius = radius * innerSizeRatio;
        float ringWidth = radius - innerRadius;

        for (int i = 0; i < pointCount; i++) {
            firstRadius[i] = (float) (innerRadius + ringWidth * Math.random());
            secondRadius[i] = (float) (innerRadius + ringWidth * Math.random());
            animationOffset[i] = (float) Math.random();
        }

        gradient = new LinearGradient(0, 0, 0, height,
                startGradientColor,
                endGradientColor,
                Shader.TileMode.REPEAT);

        paint.setShader(gradient);
        paint.setAlpha((int) (0.5f * 255));

        currentCx = width / 2.0f;
        currentCy = height / 2.0f;

        translationRadius = radius / 6;
        translationRadiusStep = radius / 4000;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < pointCount; i++) {
            float currentFraction = (float) animators[i].getAnimatedValue();
            float radius = firstRadius[i] * (1 - currentFraction) + secondRadius[i] * currentFraction;
            float angle = (float) (Math.PI * 2 / pointCount * i) + angleOffset;
            xs[i] = (float) (currentCx + radius * Math.cos(angle));
            ys[i] = (float) (currentCy + radius * Math.sin(angle));
        }

        path.reset();
        path.moveTo(xs[0], ys[0]);

        for (int i = 0; i < pointCount; i++) {
            float currX = getFromArray(xs, i);
            float currY = getFromArray(ys, i);
            float nextX = getFromArray(xs, i + 1);
            float nextY = getFromArray(ys, i + 1);

            getVector(xs, ys, i, temp);

            float vx = temp[0];
            float vy = temp[1];

            getVector(xs, ys, i + 1, temp);
            float vxNext = temp[0];
            float vyNext = temp[1];

            path.cubicTo(currX + vx, currY + vy, nextX - vxNext, nextY - vyNext, nextX, nextY);
        }

        canvas.drawPath(path, paint);
    }

    @Override
    protected void onDetachedFromWindow() {
        for (ValueAnimator animator : animators) {
            animator.end();
        }
        super.onDetachedFromWindow();
    }

    public void updateGradient(@ColorInt int startGradientColor, @ColorInt int endGradientColor) {
        this.startGradientColor = startGradientColor;
        this.endGradientColor = endGradientColor;
        onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
    }
}
