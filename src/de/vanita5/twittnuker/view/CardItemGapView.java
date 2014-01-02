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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.util.ArrayUtils;
import de.vanita5.twittnuker.view.themed.ThemedTextView;

public class CardItemGapView extends ThemedTextView {

	public CardItemGapView(final Context context) {
		this(context, null);
	}

	public CardItemGapView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CardItemGapView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		return false;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		final Drawable bg = getBackground();
		if (bg != null && bg.isStateful()) {
			final int[] state = getDrawableState();
			final Drawable layer = bg instanceof LayerDrawable ? ((LayerDrawable) bg)
					.findDrawableByLayerId(R.id.card_item_selector) : null;
			final Drawable current = layer != null ? layer.getCurrent() : bg.getCurrent();
			if (current instanceof TransitionDrawable) {
				final TransitionDrawable td = (TransitionDrawable) current;
				if (ArrayUtils.contains(state, android.R.attr.state_pressed)) {
					td.startTransition(ViewConfiguration.getLongPressTimeout());
				} else {
					td.resetTransition();
				}
			}
		}
	}

}
