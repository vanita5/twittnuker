/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import de.vanita5.twittnuker.R;

public class ProfileBannerSpace extends View {

    private int mStatusBarHeight, mToolbarHeight;
    private float mBannerAspectRatio;

    /**
     * {@inheritDoc}
     */
    public ProfileBannerSpace(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProfileBannerImageView);
        setBannerAspectRatio(a.getFraction(R.styleable.ProfileBannerImageView_bannerAspectRatio, 1, 1, 2f));
        a.recycle();
    }

    /**
     * Draw nothing.
     *
     * @param canvas an unused parameter.
     */
    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(@NonNull final Canvas canvas) {
    }

    public void setStatusBarHeight(int offset) {
        mStatusBarHeight = offset;
        requestLayout();
    }

    public void setToolbarHeight(int toolbarHeight) {
        mToolbarHeight = toolbarHeight;
        requestLayout();
    }

    public int getStatusBarHeight() {
        return mStatusBarHeight;
    }

    public int getToolbarHeight() {
        return mToolbarHeight;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = Math.round(width / mBannerAspectRatio) - mStatusBarHeight - mToolbarHeight;
        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    public void setBannerAspectRatio(final float bannerAspectRatio) {
        mBannerAspectRatio = bannerAspectRatio;
    }

    public float getBannerAspectRatio() {
        return mBannerAspectRatio;
    }
}