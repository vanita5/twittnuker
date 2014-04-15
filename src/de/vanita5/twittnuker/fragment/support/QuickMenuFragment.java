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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.content.TwidereContextThemeWrapper;
import de.vanita5.twittnuker.util.ThemeUtils;

public class QuickMenuFragment extends BaseSupportFragment {

	private SlidingUpPanelLayout mSlidingUpPanel;
	private SharedPreferences mPreferences;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (mPreferences.getBoolean(KEY_QUICK_MENU_EXPANDED, false)) {
		} else {
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final Context context = getActivity();
		final int themeResource = ThemeUtils.getDrawerThemeResource(context);
		final int accentColor = ThemeUtils.getUserThemeColor(context);
		final Context theme = new TwidereContextThemeWrapper(context, themeResource, accentColor);
		return LayoutInflater.from(theme).inflate(R.layout.fragment_quick_menu, container, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(KEY_QUICK_MENU_EXPANDED, mSlidingUpPanel.isExpanded());
		editor.apply();
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mSlidingUpPanel = (SlidingUpPanelLayout) view.findViewById(R.id.activities_drawer);
	}

}