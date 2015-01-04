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

package de.vanita5.twittnuker.view.holder;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.iface.IStatusesAdapter;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.ParcelableStatus.CursorIndices;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ImageLoaderWrapper;
import de.vanita5.twittnuker.util.ImageLoadingHandler;
import de.vanita5.twittnuker.util.UserColorNameUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.CardMediaContainer;
import de.vanita5.twittnuker.view.ShapedImageView;
import de.vanita5.twittnuker.view.ShortTimeView;

import java.util.Locale;

import twitter4j.TranslationResult;

import static de.vanita5.twittnuker.util.Utils.getUserTypeIconRes;

public class StatusViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final IStatusesAdapter<?> adapter;

    private final ImageView retweetProfileImageView;
    private final ShapedImageView profileImageView;
    private final ImageView profileTypeView;
    private final TextView textView;
    private final TextView nameView, screenNameView;
    private final TextView replyRetweetView;
    private final ShortTimeView timeView;
    private final CardMediaContainer mediaPreviewContainer;
    private final TextView replyCountView, retweetCountView, favoriteCountView;


    public StatusViewHolder(View itemView) {
        this(null, itemView);
    }

    public StatusViewHolder(IStatusesAdapter<?> adapter, View itemView) {
        super(itemView);
        this.adapter = adapter;
        profileImageView = (ShapedImageView) itemView.findViewById(R.id.profile_image);
        profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
        textView = (TextView) itemView.findViewById(R.id.text);
        nameView = (TextView) itemView.findViewById(R.id.name);
        screenNameView = (TextView) itemView.findViewById(R.id.screen_name);
        retweetProfileImageView = (ImageView) itemView.findViewById(R.id.retweet_profile_image);
        replyRetweetView = (TextView) itemView.findViewById(R.id.reply_retweet_status);
        timeView = (ShortTimeView) itemView.findViewById(R.id.time);

        mediaPreviewContainer = (CardMediaContainer) itemView.findViewById(R.id.media_preview_container);

        replyCountView = (TextView) itemView.findViewById(R.id.reply_count);
        retweetCountView = (TextView) itemView.findViewById(R.id.retweet_count);
        favoriteCountView = (TextView) itemView.findViewById(R.id.favorite_count);
//TODO
//        profileImageView.setSelectorColor(ThemeUtils.getUserHighlightColor(itemView.getContext()));
    }

    public void setupViewListeners() {
        itemView.findViewById(R.id.item_content).setOnClickListener(this);
        itemView.findViewById(R.id.item_menu).setOnClickListener(this);

        itemView.setOnClickListener(this);
        profileImageView.setOnClickListener(this);
        mediaPreviewContainer.setOnClickListener(this);
        replyCountView.setOnClickListener(this);
        retweetCountView.setOnClickListener(this);
        favoriteCountView.setOnClickListener(this);
	}

    public void setupViewOptions() {
        setTextSize(adapter.getTextSize());
    }

    public void setTextSize(final float textSize) {
        nameView.setTextSize(textSize);
        textView.setTextSize(textSize);
        screenNameView.setTextSize(textSize * 0.85f);
        timeView.setTextSize(textSize * 0.85f);
        replyRetweetView.setTextSize(textSize * 0.75f);
        replyCountView.setTextSize(textSize);
        replyCountView.setTextSize(textSize);
        favoriteCountView.setTextSize(textSize);
    }

    public void displayStatus(final ParcelableStatus status) {
        displayStatus(adapter.getContext(), adapter.getImageLoader(),
                adapter.getImageLoadingHandler(), adapter.getTwitterWrapper(),
                adapter.getProfileImageStyle(), adapter.getMediaPreviewStyle(), status, null);
    }

    public void displayStatus(final Context context, final ImageLoaderWrapper loader,
                              final ImageLoadingHandler handler, final AsyncTwitterWrapper twitter,
                              final int profileImageStyle, final int mediaPreviewStyle,
                              @NonNull final ParcelableStatus status,
                              @Nullable final TranslationResult translation) {
        final ParcelableMedia[] media = status.media;

        if (status.retweet_id > 0) {
            replyRetweetView.setText(context.getString(R.string.name_retweeted, status.retweeted_by_name));
            replyRetweetView.setVisibility(View.VISIBLE);
            retweetProfileImageView.setVisibility(View.GONE);
        } else if (status.in_reply_to_status_id > 0 && status.in_reply_to_user_id > 0) {
            replyRetweetView.setText(context.getString(R.string.in_reply_to_name, status.in_reply_to_name));
            replyRetweetView.setVisibility(View.VISIBLE);
            retweetProfileImageView.setVisibility(View.GONE);
        } else {
            replyRetweetView.setText(null);
            replyRetweetView.setVisibility(View.GONE);
            replyRetweetView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            retweetProfileImageView.setVisibility(View.GONE);
	    }

        final int typeIconRes = getUserTypeIconRes(status.user_is_verified, status.user_is_protected);
        if (typeIconRes != 0) {
            profileTypeView.setImageResource(typeIconRes);
            profileTypeView.setVisibility(View.VISIBLE);
        } else {
            profileTypeView.setImageDrawable(null);
            profileTypeView.setVisibility(View.GONE);
	    }

        nameView.setText(status.user_name);
        screenNameView.setText("@" + status.user_screen_name);
        timeView.setTime(status.timestamp);

        final int userColor = UserColorNameUtils.getUserColor(context, status.user_id);
        profileImageView.setBorderColor(userColor);
        profileImageView.setStyle(profileImageStyle);

        loader.displayProfileImage(profileImageView, status.user_profile_image_url);

        mediaPreviewContainer.setStyle(mediaPreviewStyle);
        mediaPreviewContainer.displayMedia(media, loader, status.account_id, null, handler);
        if (translation != null) {
            textView.setText(translation.getText());
        } else {
            textView.setText(status.text_unescaped);
        }

        if (status.reply_count > 0) {
            replyCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.reply_count));
        } else {
            replyCountView.setText(null);
	    }

        final long retweet_count;
        if (twitter.isDestroyingStatus(status.account_id, status.my_retweet_id)) {
            retweetCountView.setActivated(false);
            retweet_count = Math.max(0, status.favorite_count - 1);
        } else {
            final boolean creatingRetweet = twitter.isCreatingRetweet(status.account_id, status.id);
            retweetCountView.setActivated(creatingRetweet || Utils.isMyRetweet(status));
            retweet_count = status.retweet_count + (creatingRetweet ? 1 : 0);
	    }
        if (retweet_count > 0) {
            retweetCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), retweet_count));
        } else {
            retweetCountView.setText(null);
        }
        retweetCountView.setEnabled(!status.user_is_protected);

        final long favorite_count;
        if (twitter.isDestroyingFavorite(status.account_id, status.id)) {
            favoriteCountView.setActivated(false);
            favorite_count = Math.max(0, status.favorite_count - 1);
        } else {
            final boolean creatingFavorite = twitter.isCreatingFavorite(status.account_id, status.id);
            favoriteCountView.setActivated(creatingFavorite || status.is_favorite);
            favorite_count = status.favorite_count + (creatingFavorite ? 1 : 0);
	    }
        if (favorite_count > 0) {
            favoriteCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), favorite_count));
        } else {
            favoriteCountView.setText(null);
        }
    }


    public void displayStatus(Cursor cursor, CursorIndices indices) {
        final ImageLoaderWrapper loader = adapter.getImageLoader();
        final AsyncTwitterWrapper twitter = adapter.getTwitterWrapper();
        final Context context = adapter.getContext();

        final long reply_count = cursor.getLong(indices.reply_count);
        final long retweet_count;
        final long favorite_count;

        final long account_id = cursor.getLong(indices.account_id);
        final long timestamp = cursor.getLong(indices.status_timestamp);
        final long user_id = cursor.getLong(indices.user_id);
        final long status_id = cursor.getLong(indices.status_id);
        final long retweet_id = cursor.getLong(indices.retweet_id);
        final long my_retweet_id = cursor.getLong(indices.my_retweet_id);
        final long retweeted_by_id = cursor.getLong(indices.retweeted_by_user_id);
        final long in_reply_to_status_id = cursor.getLong(indices.in_reply_to_status_id);
        final long in_reply_to_user_id = cursor.getLong(indices.in_reply_to_user_id);

        final boolean user_is_protected = cursor.getInt(indices.is_protected) == 1;

        final String user_name = cursor.getString(indices.user_name);
        final String user_screen_name = cursor.getString(indices.user_screen_name);
        final String user_profile_image_url = cursor.getString(indices.user_profile_image_url);
        final String retweeted_by_name = cursor.getString(indices.retweeted_by_user_name);
        final String in_reply_to_name = cursor.getString(indices.in_reply_to_user_name);

        final ParcelableMedia[] media = ParcelableMedia.fromJSONString(cursor.getString(indices.media));

        if (retweet_id > 0) {
            replyRetweetView.setText(context.getString(R.string.name_retweeted, retweeted_by_name));
            replyRetweetView.setVisibility(View.VISIBLE);
            retweetProfileImageView.setVisibility(View.GONE);
        } else if (in_reply_to_status_id > 0 && in_reply_to_user_id > 0) {
            replyRetweetView.setText(context.getString(R.string.in_reply_to_name, in_reply_to_name));
            replyRetweetView.setVisibility(View.VISIBLE);
            retweetProfileImageView.setVisibility(View.GONE);
        } else {
            replyRetweetView.setText(null);
            replyRetweetView.setVisibility(View.GONE);
            replyRetweetView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            retweetProfileImageView.setVisibility(View.GONE);
        }

        final int typeIconRes = getUserTypeIconRes(cursor.getInt(indices.is_verified) == 1,
                user_is_protected);
        if (typeIconRes != 0) {
            profileTypeView.setImageResource(typeIconRes);
            profileTypeView.setVisibility(View.VISIBLE);
        } else {
            profileTypeView.setImageDrawable(null);
            profileTypeView.setVisibility(View.GONE);
        }

        nameView.setText(user_name);
        screenNameView.setText("@" + user_screen_name);
        timeView.setTime(timestamp);

        final int userColor = UserColorNameUtils.getUserColor(context, user_id);
        profileImageView.setBorderColor(userColor);
        profileImageView.setStyle(adapter.getProfileImageStyle());

        loader.displayProfileImage(profileImageView, user_profile_image_url);

            final String text_unescaped = cursor.getString(indices.text_unescaped);
        mediaPreviewContainer.setStyle(adapter.getMediaPreviewStyle());
        mediaPreviewContainer.displayMedia(media, loader, account_id, null,
                adapter.getImageLoadingHandler());
        textView.setText(text_unescaped);

        if (reply_count > 0) {
            replyCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), reply_count));
        } else {
            replyCountView.setText(null);
        }

        if (twitter.isDestroyingStatus(account_id, my_retweet_id)) {
            retweetCountView.setActivated(false);
            retweet_count = Math.max(0, cursor.getLong(indices.retweet_count) - 1);
        } else {
            final boolean creatingRetweet = twitter.isCreatingRetweet(account_id, status_id);
            retweetCountView.setActivated(creatingRetweet || Utils.isMyRetweet(account_id,
                    retweeted_by_id, my_retweet_id));
            retweet_count = cursor.getLong(indices.retweet_count) + (creatingRetweet ? 1 : 0);
        }
        if (retweet_count > 0) {
            retweetCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), retweet_count));
        } else {
            retweetCountView.setText(null);
        }
        retweetCountView.setEnabled(!user_is_protected);

        favoriteCountView.setActivated(cursor.getInt(indices.is_favorite) == 1);
        if (twitter.isDestroyingFavorite(account_id, status_id)) {
            favoriteCountView.setActivated(false);
            favorite_count = Math.max(0, cursor.getLong(indices.favorite_count) - 1);
        } else {
            final boolean creatingFavorite = twitter.isCreatingFavorite(account_id, status_id);
            favoriteCountView.setActivated(creatingFavorite || cursor.getInt(indices.is_favorite) == 1);
            favorite_count = cursor.getLong(indices.favorite_count) + (creatingFavorite ? 1 : 0);
        }
        if (favorite_count > 0) {
            favoriteCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), favorite_count));
        } else {
            favoriteCountView.setText(null);
        }
    }

    public CardView getCardView() {
        return (CardView) itemView.findViewById(R.id.card);
    }

    public ShapedImageView getProfileImageView() {
        return profileImageView;
    }

    public ImageView getProfileTypeView() {
        return profileTypeView;
    }

    public void displaySampleStatus() {
        nameView.setText("Twittnuker Project");
        screenNameView.setText("@twittnuker");
        timeView.setTime(System.currentTimeMillis());
        textView.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam faucibus quis purus ac malesuada. Duis id vulputate magna, a eleifend amet.");
        mediaPreviewContainer.displayMedia(R.drawable.ic_profile_image_default,
                R.drawable.ic_profile_image_default);
		//TODO sample preview images
    }

    @Override
    public void onClick(View v) {
        final int position = getPosition();
        switch (v.getId()) {
            case R.id.item_content: {
                adapter.onStatusClick(this, position);
                break;
		    }
            case R.id.item_menu: {
                adapter.onItemMenuClick(this, position);
                break;
	        }
            case R.id.profile_image: {
                adapter.onUserProfileClick(this, position);
                break;
	        }
            case R.id.reply_count:
            case R.id.retweet_count:
            case R.id.favorite_count: {
                adapter.onItemActionClick(this, v.getId(), position);
                break;
	        }
        }
    }
}
