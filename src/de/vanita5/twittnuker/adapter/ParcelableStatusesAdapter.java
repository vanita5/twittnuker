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

package de.vanita5.twittnuker.adapter;

import android.content.Context;

import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.view.holder.StatusViewHolder;

import java.util.List;

public class ParcelableStatusesAdapter extends AbsStatusesAdapter<List<ParcelableStatus>> {

	private List<ParcelableStatus> mData;

	public ParcelableStatusesAdapter(Context context, boolean compact) {
		super(context, compact);
	}

	@Override
    public boolean isGapItem(int position) {
        return getStatus(position).is_gap;
    }

    @Override
	protected void bindStatus(StatusViewHolder holder, int position) {
		holder.displayStatus(getStatus(position));
	}

	@Override
	public ParcelableStatus getStatus(int position) {
		if (hasLoadMoreIndicator() && position == getStatusCount() - 1) return null;
		return mData.get(position);
	}

	@Override
	public int getStatusCount() {
		if (mData == null) return 0;
		return mData.size();
    }

    @Override
    public int getMediaPreviewStyle() {
        return 0;
	}

	public void setData(List<ParcelableStatus> data) {
		mData = data;
		notifyDataSetChanged();
	}

	public List<ParcelableStatus> getData() {
		return mData;
	}

}
