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

import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import de.vanita5.twittnuker.graphic.like.LikeAnimationDrawable;

public abstract class AnimationLayerDrawable extends Drawable implements LikeAnimationDrawable.Layer {

    protected AnimationLayerState mState;
    private boolean mMutated;

    public AnimationLayerDrawable(final int intrinsicWidth, final int intrinsicHeight, final LikeAnimationDrawable.Palette palette) {
        mState = createConstantState(intrinsicWidth, intrinsicHeight, palette);
    }

    protected abstract AnimationLayerState createConstantState(int intrinsicWidth, int intrinsicHeight, final LikeAnimationDrawable.Palette palette);

    @Override
    public void setAlpha(final int alpha) {

    }

    @Override
    public final float getProgress() {
        return mState.getProgress();
    }

    @Override
    public final void setProgress(float progress) {
        mState.setProgress(progress);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(final ColorFilter colorFilter) {

    }

    @Override
    public final int getIntrinsicHeight() {
        return mState.getIntrinsicHeight();
    }

    @Override
    public final int getIntrinsicWidth() {
        return mState.getIntrinsicWidth();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState = createConstantState(mState.getIntrinsicWidth(), mState.getIntrinsicHeight(),
                    mState.getPalette());
            mMutated = true;
        }
        return this;
    }

    /**
     * Created by mariotaku on 16/2/22.
     */
    public abstract static class AnimationLayerState extends ConstantState {
        protected final int mIntrinsicWidth;
        protected final int mIntrinsicHeight;
        protected final LikeAnimationDrawable.Palette mPalette;

        private float mProgress;

        public AnimationLayerState(int intrinsicWidth, int intrinsicHeight, LikeAnimationDrawable.Palette palette) {
            this.mPalette = palette;
            this.mIntrinsicHeight = intrinsicHeight;
            this.mIntrinsicWidth = intrinsicWidth;
        }

        public final float getProgress() {
            return mProgress;
        }

        public final void setProgress(float progress) {
            mProgress = progress;
        }

        public final LikeAnimationDrawable.Palette getPalette() {
            return mPalette;
        }

        public final int getIntrinsicWidth() {
            return mIntrinsicWidth;
        }

        public final int getIntrinsicHeight() {
            return mIntrinsicHeight;
        }
    }
}