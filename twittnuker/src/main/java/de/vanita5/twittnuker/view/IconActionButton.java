/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.ImageButton;

import de.vanita5.twittnuker.R;

public class IconActionButton extends ImageButton {

	private final int mColor, mActivatedColor;

	public IconActionButton(Context context) {
		this(context, null);
	}

	public IconActionButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IconActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		final TypedArray defaultValues = context.obtainStyledAttributes(
				new int[]{android.R.attr.colorForeground, android.R.attr.colorActivatedHighlight});
		final int defaultColor = defaultValues.getColor(0, 0);
		final int defaultActivatedColor = defaultValues.getColor(1, 0);
		defaultValues.recycle();
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton);
        mColor = a.getColor(R.styleable.IconActionButton_iabColor, defaultColor);
        mActivatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, defaultActivatedColor);
		a.recycle();
		updateColorFilter();
	}

	@Override
	public void setActivated(boolean activated) {
		super.setActivated(activated);
		updateColorFilter();
	}

	private void updateColorFilter() {
		setColorFilter(isActivated() ? mActivatedColor : mColor, Mode.SRC_ATOP);
	}
}