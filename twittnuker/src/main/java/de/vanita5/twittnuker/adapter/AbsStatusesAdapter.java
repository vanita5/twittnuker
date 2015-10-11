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
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.iface.IStatusesAdapter;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.MediaLoadingHandler;
import de.vanita5.twittnuker.util.StatusAdapterLinkClickHandler;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.TwidereLinkify;
import de.vanita5.twittnuker.util.TwidereLinkify.HighlightStyle;
import de.vanita5.twittnuker.util.UserColorNameManager;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.CardMediaContainer.PreviewStyle;
import de.vanita5.twittnuker.view.ShapedImageView.ShapeStyle;
import de.vanita5.twittnuker.view.holder.GapViewHolder;
import de.vanita5.twittnuker.view.holder.LoadIndicatorViewHolder;
import de.vanita5.twittnuker.view.holder.StatusViewHolder;

public abstract class AbsStatusesAdapter<D> extends LoadMoreSupportAdapter<ViewHolder> implements Constants,
        IStatusesAdapter<D> {

    public static final int ITEM_VIEW_TYPE_STATUS = 2;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final MediaLoadingHandler mLoadingHandler;
    private final TwidereLinkify mLinkify;

    private StatusAdapterListener mStatusAdapterListener;

    private final int mCardBackgroundColor;
    private final int mTextSize;
    @ShapeStyle
    private final int mProfileImageStyle;
    @PreviewStyle
    private final int mMediaPreviewStyle;
    @HighlightStyle
    private final int mLinkHighlightingStyle;

    private final boolean mCompactCards;
    private final boolean mNameFirst;
    private final boolean mDisplayMediaPreview;
    private final boolean mDisplayProfileImage;
    private final boolean mSensitiveContentEnabled;
    private final boolean mHideCardActions;

    private boolean mShowInReplyTo;
    private boolean mShowAccountsColor;

    public AbsStatusesAdapter(Context context, boolean compact) {
        super(context);
        mContext = context;
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context, ThemeUtils.getThemeBackgroundOption(context), ThemeUtils.getUserThemeBackgroundAlpha(context));
        mInflater = LayoutInflater.from(context);
        mLoadingHandler = new MediaLoadingHandler(R.id.media_preview_progress);
        mTextSize = mPreferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mCompactCards = compact;
        mProfileImageStyle = Utils.getProfileImageStyle(mPreferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(mPreferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
        mLinkHighlightingStyle = Utils.getLinkHighlightingStyleInt(mPreferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null));
        mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, true);
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mDisplayMediaPreview = mPreferences.getBoolean(KEY_MEDIA_PREVIEW, true);
        mSensitiveContentEnabled = mPreferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, true);
        mHideCardActions = mPreferences.getBoolean(KEY_HIDE_CARD_ACTIONS, false);
        mLinkify = new TwidereLinkify(new StatusAdapterLinkClickHandler<>(this));
        setShowInReplyTo(true);
    }

    public abstract D getData();

    @Override
    public abstract void setData(D data);

    @Override
    public boolean shouldShowAccountsColor() {
        return mShowAccountsColor;
    }

    @NonNull
    @Override
    public final MediaLoaderWrapper getMediaLoader() {
        return mMediaLoader;
    }

    @NonNull
    @Override
    public final Context getContext() {
        return mContext;
    }

    @Override
    public final MediaLoadingHandler getMediaLoadingHandler() {
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

    @NonNull
    @Override
    public final AsyncTwitterWrapper getTwitterWrapper() {
        return mTwitterWrapper;
    }

    @NonNull
    @Override
    public UserColorNameManager getUserColorNameManager() {
        return mUserColorNameManager;
    }

    @Override
    public final float getTextSize() {
        return mTextSize;
    }

    @Override
    public TwidereLinkify getTwidereLinkify() {
        return mLinkify;
    }

    @Override
    public boolean isMediaPreviewEnabled() {
        return mDisplayMediaPreview;
    }

    @Override
    public int getLinkHighlightingStyle() {
        return mLinkHighlightingStyle;
    }

    @Override
    public boolean isNameFirst() {
        return mNameFirst;
    }

    @Override
    public boolean isSensitiveContentEnabled() {
        return mSensitiveContentEnabled;
    }

    @Override
    public boolean isCardActionsHidden() {
        return mHideCardActions;
    }

    @Override
    public boolean isProfileImageEnabled() {
        return mDisplayProfileImage;
    }

    @Override
    public boolean onStatusLongClick(StatusViewHolder holder, int position) {
        return mStatusAdapterListener != null && mStatusAdapterListener.onStatusLongClick(holder, position);
    }

    @Override
    public final void onStatusClick(StatusViewHolder holder, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onStatusClick(holder, position);
    }

    @Override
    public void onMediaClick(StatusViewHolder holder, View view, final ParcelableMedia media, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onMediaClick(holder, view, media, position);
    }

    @Override
    public void onUserProfileClick(final StatusViewHolder holder, final int position) {
        if (mStatusAdapterListener == null) return;
        final ParcelableStatus status = getStatus(position);
        if (status == null) return;
        mStatusAdapterListener.onUserProfileClick(holder, status, position);
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
        return getStatusesCount() + (isLoadMoreIndicatorVisible() ? 1 : 0);
    }

    @Override
    public final void onGapClick(ViewHolder holder, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onGapClick((GapViewHolder) holder, position);
    }

    @Override
    public void onItemActionClick(ViewHolder holder, int id, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onStatusActionClick((StatusViewHolder) holder, id, position);
    }

    @Override
    public void onItemMenuClick(ViewHolder holder, View menuView, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onStatusMenuClick((StatusViewHolder) holder, menuView, position);
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

    public interface StatusAdapterListener {
        void onGapClick(GapViewHolder holder, int position);

        void onMediaClick(StatusViewHolder holder, View view, ParcelableMedia media, int position);

        void onStatusActionClick(StatusViewHolder holder, int id, int position);

        void onStatusClick(StatusViewHolder holder, int position);

        boolean onStatusLongClick(StatusViewHolder holder, int position);

        void onStatusMenuClick(StatusViewHolder holder, View menuView, int position);

        void onUserProfileClick(StatusViewHolder holder, ParcelableStatus status, int position);
    }

}