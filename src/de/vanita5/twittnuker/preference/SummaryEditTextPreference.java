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
import android.util.AttributeSet;

public class SummaryEditTextPreference extends AutoFixEditTextPreference {

	public SummaryEditTextPreference(final Context context) {
		super(context);
	}

	public SummaryEditTextPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public SummaryEditTextPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

    @Override
    public CharSequence getSummary() {
        return getText();
    }

	@Override
	public void setText(final String text) {
		super.setText(text);
		setSummary(text);
	}

}