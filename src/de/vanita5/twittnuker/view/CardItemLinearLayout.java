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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.ViewConfiguration;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.util.ArrayUtils;
import de.vanita5.twittnuker.util.ThemeUtils;

public class CardItemLinearLayout extends ColorLabelLinearLayout {

	private Drawable mItemSelector;

	public CardItemLinearLayout(final Context context) {
		this(context, null);
	}

	public CardItemLinearLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CardItemLinearLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { R.attr.cardItemSelector });
		setItemSelector(a.getDrawable(0));
		ThemeUtils.applyThemeAlphaToDrawable(context, getBackground());
		a.recycle();
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (mItemSelector != null) {
			mItemSelector.jumpToCurrentState();
		}
	}

	public void setItemSelector(final Drawable drawable) {
		if (mItemSelector != null) {
			unscheduleDrawable(mItemSelector);
			mItemSelector.setCallback(null);
		}
		mItemSelector = drawable;
		setWillNotDraw(drawable != null);
		if (drawable != null) {
			if (drawable.isStateful()) {
				drawable.setState(getDrawableState());
			}
			drawable.setCallback(this);
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mItemSelector != null && mItemSelector.isStateful()) {
			final int[] state = getDrawableState();
			mItemSelector.setState(state);
			final Drawable layer = mItemSelector instanceof LayerDrawable ? ((LayerDrawable) mItemSelector)
					.findDrawableByLayerId(R.id.card_item_selector) : null;
			final Drawable current = layer != null ? layer.getCurrent() : mItemSelector.getCurrent();
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

	@Override
	protected void onDraw(final Canvas canvas) {
		if (mItemSelector != null) {
			mItemSelector.draw(canvas);
		}
		super.onDraw(canvas);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mItemSelector != null) {
			mItemSelector.setBounds(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
		}
	}

	@Override
	protected boolean verifyDrawable(final Drawable who) {
		return super.verifyDrawable(who) || who == mItemSelector;
	}

}
