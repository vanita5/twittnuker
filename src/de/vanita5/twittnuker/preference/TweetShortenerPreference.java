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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;

import java.util.ArrayList;

public class TweetShortenerPreference extends DialogPreference implements Constants {

	private SharedPreferences mPreferences;

	private TweetShortenerSpec[] mAvailableTweetShorteners;

	public TweetShortenerPreference(final Context context) {
		this(context, null);
	}

	public TweetShortenerPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public TweetShortenerPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences.Editor editor = getEditor();
		if (editor == null) return;
		final TweetShortenerSpec spec = mAvailableTweetShorteners[which];
		if (spec != null) {
			editor.putString(KEY_TWEET_SHORTENER, spec.service);
			editor.commit();
		}
		dialog.dismiss();
	}

	@Override
	public void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
		super.onPrepareDialogBuilder(builder);
		mPreferences = getSharedPreferences();
		if (mPreferences == null) return;
		final String component = mPreferences.getString(KEY_TWEET_SHORTENER, null);
		final ArrayList<TweetShortenerSpec> specs = new ArrayList<TweetShortenerSpec>();
		//Available tweet shortening services
		specs.add(new TweetShortenerSpec(getContext().getString(R.string.tweet_shortener_default), null));
		specs.add(new TweetShortenerSpec(getContext().getString(R.string.tweet_shortener_hototin), SERVICE_SHORTENER_HOTOTIN));
		mAvailableTweetShorteners = specs.toArray(new TweetShortenerSpec[specs.size()]);
		builder.setSingleChoiceItems(mAvailableTweetShorteners, getIndex(component), TweetShortenerPreference.this);
		builder.setNegativeButton(android.R.string.cancel, null);
	}

	private int getIndex(final String service) {
		if (mAvailableTweetShorteners == null) return -1;
		if (service == null) return 0;
		final int count = mAvailableTweetShorteners.length;
		for (int i = 0; i < count; i++) {
			final TweetShortenerSpec spec = mAvailableTweetShorteners[i];
			if (service.equals(spec.service)) return i;
		}
		return -1;
	}

	public static class TweetShortenerSpec implements CharSequence {

		private final String name, service;

		TweetShortenerSpec(final String name, final String service) {
			this.name = name;
			this.service = service;
		}

		@Override
		public int length() {
			return name.length();
		}

		@Override
		public char charAt(int i) {
			return name.charAt(i);
		}

		@Override
		public CharSequence subSequence(int i, int i2) {
			return name.subSequence(i, i2);
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
