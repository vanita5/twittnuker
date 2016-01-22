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

package de.vanita5.twittnuker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MediaSizeImageView extends ImageView {

	private int mMediaWidth, mMediaHeight;

	public MediaSizeImageView(final Context context) {
		this(context, null);
	}

	public MediaSizeImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MediaSizeImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setMediaSize(int width, int height) {
		mMediaWidth = width;
		mMediaHeight = height;
		requestLayout();
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		if (mMediaWidth == 0 || mMediaHeight == 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		final float whRatio = (float) mMediaWidth / mMediaHeight;
		final int width = MeasureSpec.getSize(widthMeasureSpec), height = MeasureSpec.getSize(heightMeasureSpec);
		final ViewGroup.LayoutParams lp = getLayoutParams();
		if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT && lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
			final int calcWidth = Math.round(height * whRatio);
			super.onMeasure(MeasureSpec.makeMeasureSpec(calcWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
			setMeasuredDimension(calcWidth, height);
		} else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
			final int calcHeight = Math.round(width / whRatio);
			super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(calcHeight, MeasureSpec.EXACTLY));
			setMeasuredDimension(width, calcHeight);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}