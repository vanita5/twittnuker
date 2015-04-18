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
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.view.holder.UserViewHolder;

import java.util.List;

public class ParcelableUsersAdapter extends AbsUsersAdapter<List<ParcelableUser>> {

    private List<ParcelableUser> mData;


    public ParcelableUsersAdapter(Context context, boolean compact) {
        super(context, compact);
	}

	@Override
    public List<ParcelableUser> getData() {
        return mData;
	}


	@Override
    public void setData(List<ParcelableUser> data) {
        mData = data;
        notifyDataSetChanged();
	}

    @Override
    protected void bindStatus(UserViewHolder holder, int position) {
        holder.displayUser(getUser(position));
	}

    @Override
    public int getItemCount() {
        return getUsersCount() + (isLoadMoreIndicatorVisible() ? 1 : 0);
	}

    @Override
    public ParcelableUser getUser(int position) {
        if (position == getUsersCount()) return null;
        return mData.get(position);
	}

    @Override
    public long getUserId(int position) {
        if (position == getUsersCount()) return -1;
        return mData.get(position).id;
	}

    @Override
    public int getUsersCount() {
        if (mData == null) return 0;
        return mData.size();
	}
}