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

package de.vanita5.twittnuker.activity.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.mariotaku.menucomponent.internal.menu.MenuAdapter;
import org.mariotaku.menucomponent.internal.menu.MenuUtils;

import static de.vanita5.twittnuker.util.Utils.setMenuForStatus;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.iface.IThemedActivity;
import de.vanita5.twittnuker.fragment.support.BaseSupportDialogFragment;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.Utils;

public class StatusMenuDialogFragment extends BaseSupportDialogFragment implements OnItemClickListener {

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final FragmentActivity activity = getActivity();
		final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final Bundle args = getArguments();
		final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
		final int themeRes, accentColor;
		if (activity instanceof IThemedActivity) {
			themeRes = ((IThemedActivity) activity).getThemeResourceId();
			accentColor = ((IThemedActivity) activity).getThemeColor();
		} else {
			themeRes = ThemeUtils.getSettingsThemeResource(activity);
			accentColor = ThemeUtils.getUserThemeColor(activity);
		}
		final Context context = ThemeUtils.getThemedContextForActionIcons(activity, themeRes, accentColor);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final MenuAdapter adapter = new MenuAdapter(context);
		final ListView listView = new ListView(context);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		builder.setView(listView);
		final boolean longclickToOpenMenu = prefs.getBoolean(KEY_LONG_CLICK_TO_OPEN_MENU, false);
		final Menu menu = MenuUtils.createMenu(context);
		new MenuInflater(context).inflate(R.menu.action_status, menu);
		setMenuForStatus(getActivity(), menu, status);
		Utils.setMenuItemAvailability(menu, MENU_MULTI_SELECT, longclickToOpenMenu);
		adapter.setMenu(menu);
		return builder.create();
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final Fragment parentFragment = getParentFragment();
		final MenuItem item = (MenuItem) parent.getItemAtPosition(position);
		if (item.hasSubMenu()) {

		} else if (parentFragment instanceof OnMenuItemClickListener) {
			((OnMenuItemClickListener) parentFragment).onMenuItemClick(item);
			dismiss();
		}
	}

}