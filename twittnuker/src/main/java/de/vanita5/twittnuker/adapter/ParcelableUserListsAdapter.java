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

package de.vanita5.twittnuker.adapter;

import android.content.Context;

import java.util.List;

import de.vanita5.twittnuker.model.ParcelableUserList;
import de.vanita5.twittnuker.view.holder.UserListViewHolder;

public class ParcelableUserListsAdapter extends AbsUserListsAdapter<List<ParcelableUserList>> {

	private List<ParcelableUserList> mData;


	public ParcelableUserListsAdapter(Context context, boolean compact) {
		super(context, compact);
	}

	@Override
	public List<ParcelableUserList> getData() {
		return mData;
	}


	@Override
	public void setData(List<ParcelableUserList> data) {
		mData = data;
		notifyDataSetChanged();
	}

	@Override
	protected void bindUserList(UserListViewHolder holder, int position) {
		holder.displayUserList(getUserList(position));
	}

	@Override
	public int getItemCount() {
		return getUserListsCount() + (isLoadMoreIndicatorVisible() ? 1 : 0);
	}

	@Override
	public ParcelableUserList getUserList(int position) {
		if (position == getUserListsCount()) return null;
		return mData.get(position);
	}

	@Override
	public long getUserListId(int position) {
		if (position == getUserListsCount()) return -1;
		return mData.get(position).id;
	}

	@Override
	public int getUserListsCount() {
		if (mData == null) return 0;
		return mData.size();
	}
}