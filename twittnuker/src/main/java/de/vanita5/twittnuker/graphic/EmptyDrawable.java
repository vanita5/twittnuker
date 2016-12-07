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

package de.vanita5.twittnuker.graphic;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class EmptyDrawable extends Drawable {

    private final int mIntrinsicWidth, mIntrinsicHeight, mMinimumWidth, mMinimumHeight;

    @Override
    public int getMinimumHeight() {
        return mMinimumHeight;
    }

    @Override
    public int getMinimumWidth() {
        return mMinimumWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }


    public EmptyDrawable() {
        this(0, 0, -1, -1);
    }

    public EmptyDrawable(int minimumWidth, int minimumHeight, int intrinsicWidth, int intrinsicHeight) {
        mMinimumWidth = minimumWidth;
        mMinimumHeight = minimumHeight;
        mIntrinsicWidth = intrinsicWidth;
        mIntrinsicHeight = intrinsicHeight;
    }

    public EmptyDrawable(Drawable drawableToCopySize) {
        mMinimumWidth = drawableToCopySize.getMinimumWidth();
        mMinimumHeight = drawableToCopySize.getMinimumHeight();
        mIntrinsicWidth = drawableToCopySize.getIntrinsicWidth();
        mIntrinsicHeight = drawableToCopySize.getIntrinsicHeight();
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void setAlpha(final int alpha) {

    }

    @Override
    public void setColorFilter(final ColorFilter cf) {

    }

}