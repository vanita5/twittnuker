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
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.iface.IStatusesAdapter;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.ImageLoaderWrapper;
import de.vanita5.twittnuker.util.ImageLoadingHandler;
import de.vanita5.twittnuker.view.holder.LoadIndicatorViewHolder;
import de.vanita5.twittnuker.view.holder.StatusViewHolder;

public abstract class AbsStatusesAdapter extends Adapter<ViewHolder> implements IStatusesAdapter {

	private static final int ITEM_VIEW_TYPE_STATUS = 1;
	private static final int ITEM_VIEW_TYPE_LOAD_INDICATOR = 2;

	private final Context mContext;
	private final LayoutInflater mInflater;
	private final ImageLoaderWrapper mImageLoader;
	private final ImageLoadingHandler mLoadingHandler;
	private final int mCardLayoutResource;
	private boolean mLoadMoreIndicatorEnabled;

	public AbsStatusesAdapter(Context context, boolean compact) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mImageLoader = TwittnukerApplication.getInstance(context).getImageLoaderWrapper();
		mLoadingHandler = new ImageLoadingHandler(R.id.media_preview_progress);
		if (compact) {
			mCardLayoutResource = R.layout.card_item_list_status_compat;
		} else {
			mCardLayoutResource = R.layout.card_item_list_status;
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case ITEM_VIEW_TYPE_STATUS: {
				final View view = mInflater.inflate(mCardLayoutResource, parent, false);
				return new StatusViewHolder(this, view);
			}
			case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
				final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent, false);
				return new LoadIndicatorViewHolder(view);
			}
		}
		throw new IllegalStateException("Unknown view type " + viewType);
	}

	public void setLoadMoreIndicatorEnabled(boolean enabled) {
		if (mLoadMoreIndicatorEnabled == enabled) return;
		mLoadMoreIndicatorEnabled = enabled;
		notifyDataSetChanged();
	}

	public boolean hasLoadMoreIndicator() {
		return mLoadMoreIndicatorEnabled;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		switch (holder.getItemViewType()) {
			case ITEM_VIEW_TYPE_STATUS: {
				bindStatus(((StatusViewHolder) holder), position);
				break;
			}
		}
	}

	protected abstract void bindStatus(StatusViewHolder holder, int position);

	@Override
	public int getItemViewType(int position) {
		if (position == getItemCount() - 1) {
			return ITEM_VIEW_TYPE_LOAD_INDICATOR;
		}
		return ITEM_VIEW_TYPE_STATUS;
	}

	@Override
	public final int getItemCount() {
		return getStatusCount() + (mLoadMoreIndicatorEnabled ? 1 : 0);
	}

	@Override
	public ImageLoaderWrapper getImageLoader() {
		return mImageLoader;
	}

	@Override
	public Context getContext() {
		return mContext;
	}

	@Override
	public ImageLoadingHandler getImageLoadingHandler() {
		return mLoadingHandler;
	}


}