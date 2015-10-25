/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
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

package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.util.TwidereArrayUtils;
import de.vanita5.twittnuker.util.ParseUtils;

import java.util.Map;

public class ValueDependencyCheckBoxPreference extends AutoFixCheckBoxPreference implements OnSharedPreferenceChangeListener {

	private final String mDependencyKey, mDependencyValueDefault;
	private final String[] mDependencyValues;

	public ValueDependencyCheckBoxPreference(final Context context) {
		this(context, null);
	}

	public ValueDependencyCheckBoxPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.checkBoxPreferenceStyle);
	}

	public ValueDependencyCheckBoxPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final Resources res = context.getResources();
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ValueDependencyPreference, defStyle, 0);
		mDependencyKey = a.getString(R.styleable.ValueDependencyPreference_dependencyKey);
		final int dependencyValueRes = a.getResourceId(R.styleable.ValueDependencyPreference_dependencyValues, 0);
		mDependencyValues = dependencyValueRes > 0 ? res.getStringArray(dependencyValueRes) : null;
		mDependencyValueDefault = a.getString(R.styleable.ValueDependencyPreference_dependencyValueDefault);
		a.recycle();
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (key.equals(mDependencyKey)) {
			updateEnableState();
		}
	}
	
	@Override
	protected void notifyHierarchyChanged() {
		super.notifyHierarchyChanged();
		updateEnableState();
	}

	@Override
	protected void onAttachedToHierarchy(final PreferenceManager preferenceManager) {
		super.onAttachedToHierarchy(preferenceManager);
		final SharedPreferences prefs = getSharedPreferences();
		if (prefs != null) {
			prefs.registerOnSharedPreferenceChangeListener(this);
		}
		updateEnableState();
	}

	private void updateEnableState() {
		final SharedPreferences prefs = getSharedPreferences();
		if (prefs == null || mDependencyKey == null || mDependencyValues == null) return;
		final Map<String, ?> all = prefs.getAll();
		final String valueString = ParseUtils.parseString(all.get(mDependencyKey), mDependencyValueDefault);
		setEnabled(TwidereArrayUtils.contains(mDependencyValues, valueString));
	}

}
