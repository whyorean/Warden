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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

public class RingEffectView extends View {

    private static final int STEP_DEGREE = 5;

    private final Paint paint;
    private final Path path = new Path();

    private float mangle;
    private float startAngle;
    private int radius;

    public RingEffectView(Context context) {
        this(context, null);
    }

    public RingEffectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!path.isEmpty()) {
            canvas.save();
            canvas.translate(getWidth() / 2.0f, getHeight() / 2.0f);
            canvas.drawPath(path, paint);
            canvas.restore();
        }
    }

    @Override
    public float getAlpha() {
        return paint.getAlpha() / 255.0f;
    }

    @Override
    public void setAlpha(@FloatRange(from = 0.0, to = 1.0) float alpha) {
        paint.setAlpha((int) (255 * alpha));
        invalidate();
    }

    public float getAngle() {
        return mangle;
    }

    public void setAngle(@FloatRange(from = 0.0, to = 360.0) float angle) {
        final float diff = angle - mangle;
        final int stepCount = (int) (diff / STEP_DEGREE);
        final float stepMod = diff % STEP_DEGREE;

        final float sw = paint.getStrokeWidth() * 0.5f;
        final float radius = this.radius - sw;

        for (int i = 1; i <= stepCount; i++) {
            final float stepAngel = startAngle + mangle + STEP_DEGREE * i;
            final float x = (float) Math.cos(Math.toRadians(stepAngel)) * radius;
            final float y = (float) Math.sin(Math.toRadians(stepAngel)) * radius;
            path.lineTo(x, y);
        }

        final float stepAngel = startAngle + mangle + STEP_DEGREE * stepCount + stepMod;
        final float x = (float) Math.cos(Math.toRadians(stepAngel)) * radius;
        final float y = (float) Math.sin(Math.toRadians(stepAngel)) * radius;
        path.lineTo(x, y);

        mangle = angle;

        invalidate();
    }

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(@FloatRange(from = 0.0, to = 360.0) float startAngle) {
        this.startAngle = startAngle;
        mangle = 0;

        final float sw = paint.getStrokeWidth() * 0.5f;
        final float radius = this.radius - sw;

        path.reset();
        final float x = (float) Math.cos(Math.toRadians(startAngle)) * radius;
        final float y = (float) Math.sin(Math.toRadians(startAngle)) * radius;
        path.moveTo(x, y);
    }

    public void setStrokeColor(int color) {
        paint.setColor(color);
    }

    public void setStrokeWidth(int width) {
        paint.setStrokeWidth(width);
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

}

