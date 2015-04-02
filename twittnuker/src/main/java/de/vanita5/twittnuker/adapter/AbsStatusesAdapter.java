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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.iface.IStatusesAdapter;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.fragment.support.UserFragment;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ImageLoadingHandler;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.holder.GapViewHolder;
import de.vanita5.twittnuker.view.holder.LoadIndicatorViewHolder;
import de.vanita5.twittnuker.view.holder.StatusViewHolder;

public abstract class AbsStatusesAdapter<D> extends Adapter<ViewHolder> implements Constants,
        IStatusesAdapter<D> {

    public static final int ITEM_VIEW_TYPE_STATUS = 0;
    public static final int ITEM_VIEW_TYPE_GAP = 1;
    public static final int ITEM_VIEW_TYPE_LOAD_INDICATOR = 2;

	private final Context mContext;
	private final LayoutInflater mInflater;
	private final MediaLoaderWrapper mImageLoader;
	private final ImageLoadingHandler mLoadingHandler;
    private final AsyncTwitterWrapper mTwitterWrapper;
    private final int mCardBackgroundColor;
    private final int mTextSize;
    private final int mProfileImageStyle, mMediaPreviewStyle;
    private final boolean mCompactCards;
    private final boolean mNameFirst;
    private final boolean mDisplayMediaPreview;
    private final boolean mDisplayProfileImage;
	private boolean mLoadMoreIndicatorEnabled;
    private StatusAdapterListener mStatusAdapterListener;
    private boolean mShowInReplyTo;
    private boolean mShowAccountsColor;

	public AbsStatusesAdapter(Context context, boolean compact) {
		mContext = context;
        final TwittnukerApplication app = TwittnukerApplication.getInstance(context);
		mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context);
		mInflater = LayoutInflater.from(context);
        mImageLoader = app.getImageLoaderWrapper();
		mLoadingHandler = new ImageLoadingHandler(R.id.media_preview_progress);
        mTwitterWrapper = app.getTwitterWrapper();
        final SharedPreferencesWrapper preferences = SharedPreferencesWrapper.getInstance(context,
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mTextSize = preferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mCompactCards = compact;
        mProfileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
        mNameFirst = preferences.getBoolean(KEY_NAME_FIRST, true);
        mDisplayProfileImage = preferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mDisplayMediaPreview = preferences.getBoolean(KEY_MEDIA_PREVIEW, false);
        setShowInReplyTo(true);
    }

    public abstract D getData();

    public abstract void setData(D data);

	@Override
    public boolean shouldShowAccountsColor() {
        return mShowAccountsColor;
    }

    @Override
    public final MediaLoaderWrapper getImageLoader() {
        return mImageLoader;
    }

    @Override
    public final Context getContext() {
        return mContext;
    }

    @Override
    public final ImageLoadingHandler getImageLoadingHandler() {
        return mLoadingHandler;
    }

    @Override
    public final int getProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public final int getMediaPreviewStyle() {
        return mMediaPreviewStyle;
    }

    @Override
    public final AsyncTwitterWrapper getTwitterWrapper() {
        return mTwitterWrapper;
    }

    @Override
    public final float getTextSize() {
        return mTextSize;
    }

    @Override
    public boolean hasLoadMoreIndicator() {
        return mLoadMoreIndicatorEnabled;
    }

    @Override
    public void setLoadMoreIndicatorEnabled(boolean enabled) {
        if (mLoadMoreIndicatorEnabled == enabled) return;
        mLoadMoreIndicatorEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public boolean isMediaPreviewEnabled() {
        return mDisplayMediaPreview;
    }

    @Override
    public boolean isProfileImageEnabled() {
        return mDisplayProfileImage;
    }

    @Override
    public boolean isNameFirst() {
        return mNameFirst;
    }

    public boolean isShowInReplyTo() {
        return mShowInReplyTo;
    }

    public void setShowInReplyTo(boolean showInReplyTo) {
        if (mShowInReplyTo == showInReplyTo) return;
        mShowInReplyTo = showInReplyTo;
        notifyDataSetChanged();
    }

    public boolean isStatus(int position) {
        return position < getStatusesCount();
    }

    @Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case ITEM_VIEW_TYPE_STATUS: {
                final View view;
                if (mCompactCards) {
                    view = mInflater.inflate(R.layout.card_item_status_compact, parent, false);
                    final View itemContent = view.findViewById(R.id.item_content);
                    itemContent.setBackgroundColor(mCardBackgroundColor);
                } else {
                    view = mInflater.inflate(R.layout.card_item_status, parent, false);
                    final CardView cardView = (CardView) view.findViewById(R.id.card);
                    cardView.setCardBackgroundColor(mCardBackgroundColor);
                }
                final StatusViewHolder holder = new StatusViewHolder(this, view);
                holder.setOnClickListeners();
                holder.setupViewOptions();
                return holder;
			}
            case ITEM_VIEW_TYPE_GAP: {
                final View view = mInflater.inflate(R.layout.card_item_gap, parent, false);
                return new GapViewHolder(this, view);
            }
			case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
				final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent, false);
				return new LoadIndicatorViewHolder(view);
			}
		}
		throw new IllegalStateException("Unknown view type " + viewType);
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

    @Override
	public int getItemViewType(int position) {
        if (position == getStatusesCount()) {
			return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP;
		}
		return ITEM_VIEW_TYPE_STATUS;
	}

	@Override
	public final int getItemCount() {
        return getStatusesCount() + (mLoadMoreIndicatorEnabled ? 1 : 0);
	}

	@Override
    public final void onGapClick(ViewHolder holder, int position) {
        if (mStatusAdapterListener != null) {
            mStatusAdapterListener.onGapClick((GapViewHolder) holder, position);
        }
    }

    @Override
    public void onItemActionClick(ViewHolder holder, int id, int position) {
        if (mStatusAdapterListener != null) {
            mStatusAdapterListener.onStatusActionClick((StatusViewHolder) holder, id, position);
        }
    }

    @Override
    public void onItemMenuClick(ViewHolder holder, View menuView, int position) {
        if (mStatusAdapterListener != null) {
            mStatusAdapterListener.onStatusMenuClick((StatusViewHolder) holder, menuView, position);
        }
    }

    @Override
    public final void onStatusClick(StatusViewHolder holder, int position) {
        if (mStatusAdapterListener != null) {
            mStatusAdapterListener.onStatusClick(holder, position);
        }
    }


    @Override
    public void onMediaClick(StatusViewHolder holder, ParcelableMedia media, int position) {
        if (mStatusAdapterListener != null) {
            mStatusAdapterListener.onMediaClick(holder, media, position);
        }
    }

    @Override
    public void onUserProfileClick(StatusViewHolder holder, int position) {
        final Context context = getContext();
        final ParcelableStatus status = getStatus(position);
        final View profileImageView = holder.getProfileImageView();
        final View profileTypeView = holder.getProfileTypeView();
        if (context instanceof FragmentActivity) {
            final Bundle options = Utils.makeSceneTransitionOption((FragmentActivity) context,
                    new Pair<>(profileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE),
                    new Pair<>(profileTypeView, UserFragment.TRANSITION_NAME_PROFILE_TYPE));
            Utils.openUserProfile(context, status.account_id, status.user_id, status.user_screen_name, options);
        } else {
            Utils.openUserProfile(context, status.account_id, status.user_id, status.user_screen_name, null);
        }
    }

    public void setListener(StatusAdapterListener listener) {
        mStatusAdapterListener = listener;
    }

    public void setShowAccountsColor(boolean showAccountsColor) {
        if (mShowAccountsColor == showAccountsColor) return;
        mShowAccountsColor = showAccountsColor;
        notifyDataSetChanged();
    }

    protected abstract void bindStatus(StatusViewHolder holder, int position);

    public static interface StatusAdapterListener {
        void onGapClick(GapViewHolder holder, int position);

        void onMediaClick(StatusViewHolder holder, ParcelableMedia media, int position);

        void onStatusActionClick(StatusViewHolder holder, int id, int position);

        void onStatusClick(StatusViewHolder holder, int position);

        void onStatusMenuClick(StatusViewHolder holder, View menuView, int position);
    }

}