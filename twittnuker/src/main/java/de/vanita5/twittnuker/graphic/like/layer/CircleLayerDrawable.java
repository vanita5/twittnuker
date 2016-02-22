/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.graphic.like.layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import de.vanita5.twittnuker.graphic.like.LikeAnimationDrawable;

public class CircleLayerDrawable extends AnimationLayerDrawable {

    public CircleLayerDrawable(final int intrinsicWidth, final int intrinsicHeight,
                               final LikeAnimationDrawable.Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    protected CircleState createConstantState(final int intrinsicWidth,
                                              final int intrinsicHeight,
                                              final LikeAnimationDrawable.Palette palette) {
        return new CircleState(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    public void draw(final Canvas canvas) {
        final CircleState state = (CircleState) mState;
        final float progress = getProgress();
        final Rect bounds = getBounds();
        final float radius;
        final Paint paint = state.getPaint();
        final int fullRadius = state.getFullRadius();
        if (progress < 0.5f) {
            paint.setStyle(Paint.Style.FILL);
            final float sizeProgress = Math.min(1, progress * 2);
            radius = sizeProgress * fullRadius;
        } else {
            paint.setStyle(Paint.Style.STROKE);
            final float innerLeftRatio = 1 - (progress - 0.5f) * 2f;
            final float strokeWidth = fullRadius * innerLeftRatio;
            paint.setStrokeWidth(strokeWidth);
            radius = fullRadius - strokeWidth / 2;
            if (strokeWidth <= 0) return;
        }
        paint.setColor(state.getPalette().getCircleColor(progress));
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, paint);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        final CircleState state = (CircleState) mState;
        state.setFullRadius(Math.min(bounds.width(), bounds.height()) / 2);
    }

    /**
     * Created by mariotaku on 16/2/22.
     */
    static class CircleState extends AnimationLayerState {
        private final Paint mPaint;
        private int mFullRadius;

        public CircleState(int intrinsicWidth, int intrinsicHeight, LikeAnimationDrawable.Palette palette) {
            super(intrinsicWidth, intrinsicHeight, palette);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        @Override
        public Drawable newDrawable() {
            return new CircleLayerDrawable(mIntrinsicWidth, mIntrinsicHeight, mPalette);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

        public void setFullRadius(int fullRadius) {
            mFullRadius = fullRadius;
        }

        public Paint getPaint() {
            return mPaint;
        }

        public int getFullRadius() {
            return mFullRadius;
        }
    }
}