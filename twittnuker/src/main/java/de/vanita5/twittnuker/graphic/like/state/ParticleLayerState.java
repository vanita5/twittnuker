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

package de.vanita5.twittnuker.graphic.like.state;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import de.vanita5.twittnuker.graphic.like.layer.ParticleLayerDrawable;
import de.vanita5.twittnuker.graphic.like.palette.Palette;

public class ParticleLayerState extends AbsLayerState {

    private final Paint mPaint;
    private float mFullRadius;
    private float mParticleSize;

    public ParticleLayerState(int intrinsicWidth, int intrinsicHeight, Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        setProgress(-1);
    }

    @Override
    public Drawable newDrawable() {
        return new ParticleLayerDrawable(mIntrinsicWidth, mIntrinsicHeight, mPalette);
    }

    @Override
    public int getChangingConfigurations() {
        return 0;
    }

    public float getFullRadius() {
        return mFullRadius;
    }

    public float getParticleSize() {
        return mParticleSize;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setFullRadius(int fullRadius) {
        mFullRadius = fullRadius;
        mParticleSize = fullRadius / 4f;
    }
}