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
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;

public class ColorPickerPresetsView extends LinearLayout implements View.OnClickListener, Constants {

	private final static int[] COLORS = { HOLO_RED_DARK, HOLO_RED_LIGHT, HOLO_ORANGE_DARK, HOLO_ORANGE_LIGHT,
			HOLO_GREEN_LIGHT, HOLO_GREEN_DARK, HOLO_BLUE_LIGHT, HOLO_BLUE_DARK, HOLO_PURPLE_DARK, HOLO_PURPLE_LIGHT,
			Color.WHITE };

	private OnColorClickListener mOnColorClickListener;

	public ColorPickerPresetsView(final Context context) {
		this(context, null);
	}

	public ColorPickerPresetsView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorPickerPresetsView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setOrientation(HORIZONTAL);
		final LayoutInflater inflater = LayoutInflater.from(context);
		for (final int color : COLORS) {
			final ColorView v = (ColorView) inflater.inflate(R.layout.color_picker_preset_item, this, false);
			v.setColor(color);
			v.setOnClickListener(this);
			addView(v);
		}
	}

	@Override
	public void onClick(final View v) {
		if (!(v instanceof ColorView) || mOnColorClickListener == null) return;
		mOnColorClickListener.onColorClick(((ColorView) v).getColor());
	}

	public void setOnColorClickListener(final OnColorClickListener listener) {
		mOnColorClickListener = listener;
	}

	public interface OnColorClickListener {
		void onColorClick(int color);
	}
}
