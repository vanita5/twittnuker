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

package de.vanita5.twittnuker.view.themed;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageButton;

import de.vanita5.twittnuker.view.iface.IThemeBackgroundTintView;

public class BackgroundTintImageButton extends ImageButton implements IThemeBackgroundTintView {

	private final int mDefaultColor;

	public BackgroundTintImageButton(Context context) {
		this(context, null);
	}

	public BackgroundTintImageButton(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.imageButtonStyle);
	}

	public BackgroundTintImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.color,
				android.R.attr.colorForeground});
		if (a.hasValue(0)) {
			mDefaultColor = a.getColor(0, 0);
		} else {
			mDefaultColor = a.getColor(1, 0);
		}
		setColorFilter(mDefaultColor, Mode.SRC_ATOP);
		a.recycle();
	}

	public int getDefaultColor() {
		return mDefaultColor;
	}


	@Override
    public void setBackgroundTintColor(@NonNull ColorStateList color) {
		setColorFilter(color.getDefaultColor());
	}
}