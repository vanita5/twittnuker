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

package de.vanita5.twittnuker.text;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

import de.vanita5.twittnuker.preference.LinkHighlightPreference;


public class TwidereHighLightStyle extends CharacterStyle {

	private final int option;

	public TwidereHighLightStyle(final int option) {
		this.option = option;
	}

	@Override
	public void updateDrawState(final TextPaint ds) {
		switch (option) {
			case LinkHighlightPreference.LINK_HIGHLIGHT_OPTION_CODE_BOTH:
				ds.setUnderlineText(true);
				ds.setColor(ds.linkColor);
				break;
			case LinkHighlightPreference.LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE:
				ds.setUnderlineText(true);
				break;
			case LinkHighlightPreference.LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT:
				ds.setColor(ds.linkColor);
				break;
			default:
				break;
		}
	}
}