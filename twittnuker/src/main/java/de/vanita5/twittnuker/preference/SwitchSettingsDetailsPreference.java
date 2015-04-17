/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
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
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.fragment.SettingsDetailsFragment;
import de.vanita5.twittnuker.util.ViewUtils;

public class SwitchSettingsDetailsPreference extends SwitchPreference implements Constants {

    private View mView;

	public SwitchSettingsDetailsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.src});
		setFragment(SettingsDetailsFragment.class.getName());
		final Bundle extras = getExtras();
		extras.putInt(EXTRA_RESID, a.getResourceId(0, 0));
		a.recycle();

	}

	public SwitchSettingsDetailsPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.switchPreferenceStyle);
	}

	public SwitchSettingsDetailsPreference(Context context) {
		this(context, null);
	}

	@Override
	protected void onBindView(@NonNull View view) {
		super.onBindView(view);
		if (view instanceof ViewGroup) {
			((ViewGroup) view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		}
        final Switch switchView = ViewUtils.findViewByType(view, Switch.class);
		if (switchView != null) {
			switchView.setClickable(true);
			switchView.setFocusable(true);
		}
	}

    @Override
    protected View onCreateView(ViewGroup parent) {
        if (mView != null) return mView;
        return mView = super.onCreateView(parent);
    }

	@Override
	protected void onClick() {

	}
}