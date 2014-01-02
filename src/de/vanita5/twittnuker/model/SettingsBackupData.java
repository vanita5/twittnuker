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

package de.vanita5.twittnuker.model;

import android.content.Context;
import android.content.SharedPreferences;

import de.vanita5.twittnuker.Constants;

import org.json.JSONObject;
import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;

import java.util.HashMap;
import java.util.Map;

public class SettingsBackupData implements JSONParcelable, Constants {

	public static final Creator<SettingsBackupData> JSON_CREATOR = new Creator<SettingsBackupData>() {

		@Override
		public SettingsBackupData createFromParcel(final JSONParcel in) {
			return new SettingsBackupData(in);
		}

		@Override
		public SettingsBackupData[] newArray(final int size) {
			return new SettingsBackupData[size];
		}
	};

	private final Map<String, Object> settings_map = new HashMap<String, Object>();

	public SettingsBackupData(final Context context) {
		final SharedPreferences settings = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		settings_map.putAll(settings.getAll());
	}

	public SettingsBackupData(final JSONParcel in) {
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		final JSONObject settings = new JSONObject();
	}

	private static boolean isTypeSupported(final Object object) {
		return object instanceof Boolean || object instanceof Integer || object instanceof Float
				|| object instanceof Long || object instanceof Double || object instanceof String;
	}

}
