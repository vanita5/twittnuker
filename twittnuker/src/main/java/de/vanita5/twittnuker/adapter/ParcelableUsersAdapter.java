/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.support.v7.widget.RecyclerView;

import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.view.holder.UserViewHolder;

import java.util.List;

public class ParcelableUsersAdapter extends AbsUsersAdapter<List<ParcelableUser>> {

    private List<ParcelableUser> mData;


    public ParcelableUsersAdapter(Context context) {
        super(context);
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
    protected void bindUser(UserViewHolder holder, int position) {
        holder.displayUser(getUser(position));
    }

    @Override
    public int getItemCount() {
        final int position = getLoadMoreIndicatorPosition();
        int count = getUserCount();
        if ((position & IndicatorPosition.START) != 0) {
            count++;
        }
        if ((position & IndicatorPosition.END) != 0) {
            count++;
        }
        return count;
    }

    @Override
    public ParcelableUser getUser(int adapterPosition) {
        int dataPosition = adapterPosition - getUserStartIndex();
        if (dataPosition < 0 || dataPosition >= getUserCount()) return null;
        return mData.get(dataPosition);
    }

    public int getUserStartIndex() {
        final int position = getLoadMoreIndicatorPosition();
        int start = 0;
        if ((position & IndicatorPosition.START) != 0) {
            start += 1;
        }
        return start;
    }

    @Override
    public long getUserId(int position) {
        if (position == getUserCount()) return -1;
        return mData.get(position).id;
    }

    @Override
    public int getUserCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    public boolean removeUserAt(int adapterPosition) {
        int dataPosition = adapterPosition - getUserStartIndex();
        if (dataPosition < 0 || dataPosition >= getUserCount()) return false;
        mData.remove(dataPosition);
        notifyItemRemoved(adapterPosition);
        return true;
    }

    public int findPosition(AccountKey accountKey, long userId) {
        return findPosition(accountKey.getId(), accountKey.getHost(), userId);
    }

    public int findPosition(long accountId, String accountHost, long userId) {
        if (mData == null) return RecyclerView.NO_POSITION;
        for (int i = getUserStartIndex(), j = i + getUserCount(); i < j; i++) {
            final ParcelableUser user = mData.get(i);
            if (user.account_id == accountId && user.id == userId) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }
}