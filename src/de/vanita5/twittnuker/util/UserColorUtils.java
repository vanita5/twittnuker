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

package de.vanita5.twittnuker.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.support.v4.util.LongSparseArray;

import de.vanita5.twittnuker.TwittnukerConstants;

import java.util.Map;

public class UserColorUtils implements TwittnukerConstants {

	private static LongSparseArray<Integer> sUserColors = new LongSparseArray<Integer>();

	private UserColorUtils() {
		throw new AssertionError();
	}

	public static void clearUserColor(final Context context, final long user_id) {
		if (context == null) return;
		sUserColors.remove(user_id);
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Long.toString(user_id));
		editor.commit();
	}

	public static int getUserColor(final Context context, final long user_id) {
		return getUserColor(context, user_id, false);
	}

	public static int getUserColor(final Context context, final long user_id, final boolean ignore_cache) {
		if (context == null || user_id == -1) return Color.TRANSPARENT;
		if (!ignore_cache && sUserColors.indexOfKey(user_id) >= 0) return sUserColors.get(user_id);
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int color = prefs.getInt(Long.toString(user_id), Color.TRANSPARENT);
		sUserColors.put(user_id, color);
		return color;
	}

	public static void initUserColor(final Context context) {
		if (context == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		for (final Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
			sUserColors.put(ParseUtils.parseLong(entry.getKey()),
					ParseUtils.parseInt(ParseUtils.parseString(entry.getValue())));
		}
	}

	public static void registerOnUserColorChangedListener(final Context context,
			final OnUserColorChangedListener listener) {

		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(new OnColorPreferenceChangeListener(listener));
	}

	public static void setUserColor(final Context context, final long user_id, final int color) {
		if (context == null || user_id == -1) return;
		sUserColors.put(user_id, color);
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(String.valueOf(user_id), color);
		editor.commit();
	}

	public static interface OnUserColorChangedListener {
		void onUserColorChanged(long userId, int color);
	}

	private static final class OnColorPreferenceChangeListener implements OnSharedPreferenceChangeListener {

		private final OnUserColorChangedListener mListener;

		OnColorPreferenceChangeListener(final OnUserColorChangedListener listener) {
			mListener = listener;
		}

		@Override
		public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
			final long userId = ParseUtils.parseLong(key, -1);
			if (mListener != null) {
				mListener.onUserColorChanged(userId, sharedPreferences.getInt(key, 0));
			}
		}

	}
}
