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

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class ComponentStatePreference extends CheckBoxPreference {

	private final PackageManager mPackageManager;
	private final ComponentName mComponentName;

	public ComponentStatePreference(final Context context) {
		this(context, null);
	}

	public ComponentStatePreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.checkBoxPreferenceStyle);
	}

	public ComponentStatePreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.name });
		final String name = a.getString(0);
		if (name == null) throw new NullPointerException();
		mPackageManager = context.getPackageManager();
		mComponentName = new ComponentName(context.getPackageName(), name);
		setDefaultValue(isComponentEnabled());
	}

	@Override
	public boolean shouldDisableDependents() {
		final boolean disableDependentsState = getDisableDependentsState();
		final boolean value = isComponentEnabled();
		return disableDependentsState ? value : !value;
	}

	@Override
	protected boolean getPersistedBoolean(final boolean defaultReturnValue) {
		return isComponentEnabled();
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return isComponentEnabled();
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
		setChecked(getPersistedBoolean(true));
	}

	@Override
	protected boolean persistBoolean(final boolean value) {
		final int newState = value ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		mPackageManager.setComponentEnabledSetting(mComponentName, newState, PackageManager.DONT_KILL_APP);
		return true;
	}

	@Override
	protected boolean shouldPersist() {
		return true;
	}

	@SuppressLint("InlinedApi")
	private boolean isComponentEnabled() {
		final int state = mPackageManager.getComponentEnabledSetting(mComponentName);
		return state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
				&& state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
				&& state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED;
	}

}