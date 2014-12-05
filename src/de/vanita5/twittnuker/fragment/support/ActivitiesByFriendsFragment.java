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

import static de.vanita5.twittnuker.util.Utils.openStatus;
import static de.vanita5.twittnuker.util.Utils.openStatuses;
import static de.vanita5.twittnuker.util.Utils.openUserProfile;
import static de.vanita5.twittnuker.util.Utils.openUsers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import de.vanita5.twittnuker.adapter.BaseParcelableActivitiesAdapter;
import de.vanita5.twittnuker.adapter.ParcelableActivitiesByFriendsAdapter;
import de.vanita5.twittnuker.loader.support.ActivitiesAboutMeLoader;
import de.vanita5.twittnuker.model.ParcelableActivity;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.ParcelableUser;

import java.util.Arrays;
import java.util.List;

public class ActivitiesByFriendsFragment extends BaseActivitiesListFragment {

	@Override
	public BaseParcelableActivitiesAdapter createListAdapter(final Context context, final boolean compactCards,
															 final boolean plainListStyle) {
		return new ParcelableActivitiesByFriendsAdapter(context, compactCards, plainListStyle);
	}

	@Override
	public Loader<List<ParcelableActivity>> onCreateLoader(final int id, final Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		return new ActivitiesAboutMeLoader(getActivity(), getAccountIds(), getData(), getSavedActivitiesFileArgs(),
				getTabPosition() >= 0);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final int adapterPos = position - l.getHeaderViewsCount();
		final ParcelableActivity item = getListAdapter().getItem(adapterPos);
		if (item == null) return;
		final ParcelableUser[] sources = item.sources;
		if (sources == null || sources.length == 0) return;
		final ParcelableStatus[] targetStatuses = item.target_statuses;
		final ParcelableUser[] targetUsers = item.target_users;
		final ParcelableStatus[] target_object_statuses = item.target_object_statuses;
		switch (item.action) {
			case ParcelableActivity.ACTION_FAVORITE: {
				if (targetStatuses == null || targetStatuses.length == 0) return;
				if (targetStatuses.length == 1) {
					openStatus(getActivity(), targetStatuses[0]);
				} else {
					final List<ParcelableStatus> statuses = Arrays.asList(targetStatuses);
					openStatuses(getActivity(), statuses);
				}
				break;
				}
			case ParcelableActivity.ACTION_FOLLOW: {
				if (targetUsers == null || targetUsers.length == 0) return;
				if (targetUsers.length == 1) {
					openUserProfile(getActivity(), targetUsers[0], null);
				} else {
					final List<ParcelableUser> users = Arrays.asList(targetUsers);
					openUsers(getActivity(), users);
				}
				break;
			}
			case ParcelableActivity.ACTION_MENTION: {
				if (target_object_statuses != null && target_object_statuses.length > 0) {
					openStatus(getActivity(), target_object_statuses[0]);
				}
				break;
			}
			case ParcelableActivity.ACTION_REPLY: {
				if (targetStatuses != null && targetStatuses.length > 0) {
					openStatus(getActivity(), targetStatuses[0]);
				}
				break;
			}
			case ParcelableActivity.ACTION_RETWEET: {
				if (targetStatuses == null || targetStatuses.length == 0) return;
				if (targetStatuses.length == 1) {
					openStatus(getActivity(), targetStatuses[0]);
				} else {
					final List<ParcelableStatus> statuses = Arrays.asList(targetStatuses);
					openStatuses(getActivity(), statuses);
				}
				break;
			}
		}
	}

	@Override
	protected String[] getSavedActivitiesFileArgs() {
		final Bundle args = getArguments();
		if (args != null && args.containsKey(EXTRA_ACCOUNT_ID)) {
			final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
			return new String[] { AUTHORITY_ACTIVITIES_BY_FRIENDS, "account" + account_id };
		}
		return new String[] { AUTHORITY_ACTIVITIES_BY_FRIENDS };
	}

}
