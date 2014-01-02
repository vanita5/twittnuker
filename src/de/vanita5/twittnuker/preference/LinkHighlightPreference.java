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

package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.text.SpannableString;
import android.util.AttributeSet;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.text.TwidereHighLightStyle;

public class LinkHighlightPreference extends AutoInvalidateListPreference implements Constants {

	private static final int[] ENTRIES_RES = { R.string.none, R.string.highlight, R.string.underline, R.string.both };
	private static final String[] VALUES = { LINK_HIGHLIGHT_OPTION_NONE, LINK_HIGHLIGHT_OPTION_HIGHLIGHT,
			LINK_HIGHLIGHT_OPTION_UNDERLINE, LINK_HIGHLIGHT_OPTION_BOTH };

	public LinkHighlightPreference(final Context context) {
		this(context, null);
	}

	public LinkHighlightPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		final CharSequence[] entries = new CharSequence[VALUES.length];
		for (int i = 0, j = entries.length; i < j; i++) {
			final int res = ENTRIES_RES[i];
			final int option;
			switch (res) {
				case R.string.both: {
					option = LINK_HIGHLIGHT_OPTION_CODE_BOTH;
					break;
				}
				case R.string.highlight: {
					option = LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT;
					break;
				}
				case R.string.underline: {
					option = LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE;
					break;
				}
				default: {
					option = LINK_HIGHLIGHT_OPTION_CODE_NONE;
					break;
				}
			}
			final SpannableString str = new SpannableString(context.getString(res));
			str.setSpan(new TwidereHighLightStyle(option), 0, str.length(), 0);
			entries[i] = str;
		}
		setEntries(entries);
		setEntryValues(VALUES);
	}

}
