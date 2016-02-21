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
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import de.vanita5.twittnuker.graphic.like.LikeAnimationDrawable;

public class IconLayerDrawable extends Drawable implements Drawable.Callback {
    private final Drawable mDrawable;
    private final Rect mTmpRect = new Rect();
    private boolean mMutated;
    private ScaleConstantState mState;

    public IconLayerDrawable(Drawable drawable) {
        if (drawable == null) throw new NullPointerException();
        mState = new ScaleConstantState(drawable);
        mDrawable = drawable;
        drawable.setCallback(this);
        setScale(1);
    }

    /**
     * Returns the drawable scaled by this ScaleDrawable.
     */
    public Drawable getDrawable() {
        return mDrawable;
    }

    // overrides from Drawable.Callback
    @Override
    public void invalidateDrawable(Drawable who) {
        if (getCallback() != null) {
            getCallback().invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (getCallback() != null) {
            getCallback().scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (getCallback() != null) {
            getCallback().unscheduleDrawable(this, what);
        }
    }

    // overrides from Drawable
    @Override
    public void draw(Canvas canvas) {
        if (mState.getScale() <= 0) return;
        mDrawable.draw(canvas);
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations()
                | mDrawable.getChangingConfigurations();
    }

    @Override
    public boolean getPadding(Rect padding) {
        // XXX need to adjust padding!
        return mDrawable.getPadding(padding);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        mDrawable.setVisible(visible, restart);
        return super.setVisible(visible, restart);
    }

    @Override
    public void setAlpha(int alpha) {
        mDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mDrawable.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return mDrawable.getOpacity();
    }

    @Override
    public boolean isStateful() {
        return mDrawable.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean changed = mDrawable.setState(state);
        onBoundsChange(getBounds());
        return changed;
    }

    @Override
    protected boolean onLevelChange(int level) {
        mDrawable.setLevel(level);
        onBoundsChange(getBounds());
        invalidateSelf();
        return true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        updateBounds(bounds);
    }

    @Override
    public int getIntrinsicWidth() {
        return mDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mDrawable.mutate();
            mMutated = true;
        }
        return this;
    }

    public float getScale() {
        return mState.getScale();
    }

    public void setScale(float scale) {
        mState.setScale(scale);
        updateBounds(getBounds());
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    static class ScaleConstantState extends ConstantState {

        private final Drawable mIcon;
        private float mScale;

        public ScaleConstantState(Drawable icon) {
            mIcon = icon;
        }

        @Override
        public Drawable newDrawable() {
            return new IconLayerDrawable(mIcon.mutate());
        }

        @Override
        public int getChangingConfigurations() {
            return mIcon.getChangingConfigurations();
        }

        public void setScale(float scale) {
            mScale = scale;
        }

        public float getScale() {
            return mScale;
        }
    }

    private void updateBounds(Rect bounds) {
        final Rect r = mTmpRect;
        final int w = Math.round(mDrawable.getIntrinsicWidth() * mState.getScale());
        final int h = Math.round(mDrawable.getIntrinsicHeight() * mState.getScale());
        Gravity.apply(Gravity.CENTER, w, h, bounds, r);

        if (w > 0 && h > 0) {
            mDrawable.setBounds(r.left, r.top, r.right, r.bottom);
        }
        invalidateSelf();
    }

}