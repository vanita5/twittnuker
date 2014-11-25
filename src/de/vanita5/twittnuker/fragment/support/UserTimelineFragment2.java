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
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import java.util.List;
import java.util.Locale;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.decorator.DividerItemDecoration;
import de.vanita5.twittnuker.adapter.iface.IStatusesAdapter;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.loader.support.UserTimelineLoader;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.ImageLoaderWrapper;
import de.vanita5.twittnuker.util.ImageLoadingHandler;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.ShortTimeView;

public class UserTimelineFragment2 extends BaseSupportFragment
		implements LoaderCallbacks<List<ParcelableStatus>> {

	private RecyclerView mRecyclerView;
	private ProgressBar mProgress;

	private ParcelableTimelineAdapter mAdapter;
	private OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		}
	};

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final View view = getView();
		assert view != null;
		final Context context = view.getContext();
		final boolean compact = Utils.isCompactCards(context);
		mAdapter = new ParcelableTimelineAdapter(context, compact);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(layoutManager);
		if (compact) {
			mRecyclerView.addItemDecoration(new DividerItemDecoration(context, layoutManager.getOrientation()));
		}
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setOnScrollListener(mOnScrollListener);
		getLoaderManager().initLoader(0, getArguments(), this);
		setListShown(false);
	}

	public void setListShown(boolean shown) {
		mRecyclerView.setVisibility(shown ? View.VISIBLE : View.GONE);
		mProgress.setVisibility(shown ? View.GONE : View.VISIBLE);
	}

	public int getStatuses(final long maxId, final long sinceId) {
		final Bundle args = new Bundle(getArguments());
		args.putLong(EXTRA_MAX_ID, maxId);
		args.putLong(EXTRA_SINCE_ID, sinceId);
		getLoaderManager().restartLoader(0, args, this);
		return -1;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
		mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
	}

	@Override
	protected void fitSystemWindows(Rect insets) {
		super.fitSystemWindows(insets);
		mRecyclerView.setClipToPadding(false);
		mRecyclerView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_recycler_view, container, false);
	}

	@Override
	public Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
		final Context context = getActivity();
		final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
		final long maxId = args.getLong(EXTRA_MAX_ID, -1);
		final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
		final long userId = args.getLong(EXTRA_USER_ID, -1);
		final String screenName = args.getString(EXTRA_SCREEN_NAME);
		final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
		return new UserTimelineLoader(context, accountId, userId, screenName, maxId, sinceId, null,
				null, tabPosition);
	}

	@Override
	public void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
		mAdapter.setData(data);
		setListShown(true);
	}

	@Override
	public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {
		mAdapter.setData(null);
	}

	private static class ParcelableTimelineAdapter extends Adapter<ViewHolder> implements IStatusesAdapter, OnClickListener {

		private static final int ITEM_VIEW_TYPE_STATUS = 1;
		private static final int ITEM_VIEW_TYPE_LOAD_INDICATOR = 2;

		private final Context mContext;
		private final LayoutInflater mInflater;
		private final ImageLoaderWrapper mImageLoader;
		private final ImageLoadingHandler mLoadingHandler;
		private final int mCardLayoutResource;
		private List<ParcelableStatus> mData;
		private boolean mHasMoreItem;

		ParcelableTimelineAdapter(Context context, boolean compact) {
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
					return new MediaTimelineViewHolder(this, view);
				}
				case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
					final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent, false);
					return new LoadIndicatorViewHolder(view);
				}
			}
			throw new IllegalStateException("Unknown view type " + viewType);
		}

		public void setHasMoreItem(boolean hasMoreItem) {
			if (mHasMoreItem == hasMoreItem) return;
			mHasMoreItem = hasMoreItem;
			notifyDataSetChanged();
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			if (mData == null) return;
			if (holder instanceof MediaTimelineViewHolder) {
				((MediaTimelineViewHolder) holder).displayStatus(this, getStatus(position));
			}
		}

		public void setData(List<ParcelableStatus> data) {
			mData = data;
			notifyDataSetChanged();
		}

		@Override
		public int getItemViewType(int position) {
			if (position == getItemCount() - 1) {
				return ITEM_VIEW_TYPE_LOAD_INDICATOR;
			}
			return ITEM_VIEW_TYPE_STATUS;
		}

		@Override
		public int getItemCount() {
			if (mData == null) return mHasMoreItem ? 1 : 0;
			return mData.size() + (mHasMoreItem ? 1 : 0);
		}

		public ImageLoaderWrapper getImageLoader() {
			return mImageLoader;
		}

		public Context getContext() {
			return mContext;
		}

		public ImageLoadingHandler getImageLoadingHandler() {
			return mLoadingHandler;
		}

		@Override
		public void onClick(View v) {

		}


		@Override
		public ParcelableStatus getStatus(int position) {
			if (mData == null || (mHasMoreItem && position == mData.size() - 1)) return null;
			return mData.get(position);
		}
	}

	private static class LoadIndicatorViewHolder extends ViewHolder {
		public LoadIndicatorViewHolder(View view) {
			super(view);
		}
	}

	private static class MediaTimelineViewHolder extends ViewHolder implements OnClickListener {

		private final IStatusesAdapter adapter;

		private final ImageView mediaPreviewView;
		private final CircularImageView profileImageView;
		private final TextView textView;
		private final TextView nameView;
		private final ShortTimeView timeView;
		private final View mediaPreviewContainer;
		private final View replyIndicator, retweetIndicator, favoriteIndicator;
		private final TextView replyCountView, retweetCountView, favoriteCountView;

		public MediaTimelineViewHolder(IStatusesAdapter adapter, View itemView) {
			super(itemView);
			this.adapter = adapter;
			itemView.findViewById(R.id.item_content).setOnClickListener(this);
			profileImageView = (CircularImageView) itemView.findViewById(R.id.profile_image);
			textView = (TextView) itemView.findViewById(R.id.text);
			nameView = (TextView) itemView.findViewById(R.id.name);
			timeView = (ShortTimeView) itemView.findViewById(R.id.time);

			mediaPreviewContainer = itemView.findViewById(R.id.media_preview_container);
			mediaPreviewView = (ImageView) itemView.findViewById(R.id.media_preview);

			replyIndicator = itemView.findViewById(R.id.reply_indicator);
			retweetIndicator = itemView.findViewById(R.id.retweet_indicator);
			favoriteIndicator = itemView.findViewById(R.id.favorite_indicator);

			replyCountView = (TextView) itemView.findViewById(R.id.reply_count);
			retweetCountView = (TextView) itemView.findViewById(R.id.retweet_count);
			favoriteCountView = (TextView) itemView.findViewById(R.id.favorite_count);

			profileImageView.setSelectorColor(ThemeUtils.getUserHighlightColor(itemView.getContext()));

			itemView.setOnClickListener(this);
			profileImageView.setOnClickListener(this);
			mediaPreviewContainer.setOnClickListener(this);
			replyIndicator.setOnClickListener(this);
			retweetIndicator.setOnClickListener(this);
			favoriteIndicator.setOnClickListener(this);
		}

		public void displayStatus(ParcelableTimelineAdapter adapter, ParcelableStatus status) {
			final ImageLoaderWrapper loader = adapter.getImageLoader();
			final Context context = adapter.getContext();
			final ParcelableMedia[] media = status.medias;

			nameView.setText(status.user_name);
			timeView.setTime(status.timestamp);
			loader.displayProfileImage(profileImageView, status.user_profile_image_url);

//            profileImageView.setBorderColor(UserColorNicknameUtils.getUserColor(context, status.user_id));
			if (media != null && media.length > 0) {
				final ParcelableMedia firstMedia = media[0];
				if (status.text_plain.codePointCount(0, status.text_plain.length()) == firstMedia.end) {
					textView.setText(status.text_unescaped.substring(0, firstMedia.start));
				} else {
					textView.setText(status.text_unescaped);
				}
				loader.displayPreviewImageWithCredentials(mediaPreviewView, firstMedia.media_url,
						status.account_id, adapter.getImageLoadingHandler());
				mediaPreviewContainer.setVisibility(View.VISIBLE);
			} else {
				loader.cancelDisplayTask(mediaPreviewView);
				textView.setText(status.text_unescaped);
				mediaPreviewContainer.setVisibility(View.GONE);
			}
			retweetIndicator.setActivated(status.is_retweet);
			favoriteIndicator.setActivated(status.is_favorite);

			replyCountView.setVisibility(status.reply_count > 0 ? View.VISIBLE : View.INVISIBLE);
			retweetCountView.setVisibility(status.retweet_count > 0 ? View.VISIBLE : View.INVISIBLE);
			favoriteCountView.setVisibility(status.favorite_count > 0 ? View.VISIBLE : View.INVISIBLE);
			replyCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.reply_count));
			retweetCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.retweet_count));
			favoriteCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.favorite_count));
		}

		@Override
		public void onClick(View v) {
			final Context context = itemView.getContext();
			final ParcelableStatus status = adapter.getStatus(getPosition());
			switch (v.getId()) {
				case R.id.item_content: {
					Utils.openStatus(context, status);
					break;
				}
				case R.id.profile_image: {
					Utils.openUserProfile(context, status.account_id, status.user_id, status.user_screen_name);
					break;
				}
				case R.id.reply_indicator: {
					final Intent intent = new Intent(INTENT_ACTION_REPLY);
					intent.setPackage(context.getPackageName());
					intent.putExtra(EXTRA_STATUS, status);
					context.startActivity(intent);
					break;
				}
			}
		}
	}
}