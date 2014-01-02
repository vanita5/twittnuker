/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View {

	private final Paint mPaint;

	public ColorView(final Context context) {
		this(context, null);
	}

	public ColorView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mPaint = new Paint();
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.color });
		setColor(a.getColor(0, Color.TRANSPARENT));
		a.recycle();
	}

	public int getColor() {
		return mPaint.getColor();
	}

	public void setColor(final int color) {
		mPaint.setColor(color);
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		final int w = getWidth(), h = getHeight();
		canvas.drawRect(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom(), mPaint);
	}

}
