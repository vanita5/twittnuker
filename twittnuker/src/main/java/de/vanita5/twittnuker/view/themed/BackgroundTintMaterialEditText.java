/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
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

package de.vanita5.twittnuker.view.themed;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.rengwuxian.materialedittext.MaterialEditText;

import de.vanita5.twittnuker.view.iface.IThemeBackgroundTintView;

public class BackgroundTintMaterialEditText extends MaterialEditText implements IThemeBackgroundTintView {

	public BackgroundTintMaterialEditText(Context context) {
		super(context);
	}

	public BackgroundTintMaterialEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BackgroundTintMaterialEditText(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);
	}

	@Override
    public void setBackgroundTintColor(@NonNull ColorStateList color) {
		setPrimaryColor(color.getDefaultColor());
	}

}