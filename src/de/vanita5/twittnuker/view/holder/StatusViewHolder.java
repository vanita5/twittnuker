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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.iface.IStatusesAdapter;
import de.vanita5.twittnuker.constant.IntentConstants;
import de.vanita5.twittnuker.fragment.support.StatusMenuDialogFragment;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.ImageLoaderWrapper;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.CircularImageView;
import de.vanita5.twittnuker.view.ShortTimeView;

import java.util.Locale;

import static de.vanita5.twittnuker.util.Utils.getUserTypeIconRes;

public class StatusViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final IStatusesAdapter adapter;

    private final ImageView retweetProfileImageView;
    private final CircularImageView profileImageView;
    private final ImageView profileTypeView;
    private final ImageView mediaPreviewView;
    private final TextView textView;
    private final TextView nameView, screenNameView;
    private final TextView replyRetweetView;
    private final ShortTimeView timeView;
    private final View mediaPreviewContainer;
    private final TextView replyCountView, retweetCountView, favoriteCountView;

    public StatusViewHolder(IStatusesAdapter adapter, View itemView) {
        super(itemView);
        this.adapter = adapter;
        itemView.findViewById(R.id.item_content).setOnClickListener(this);
        itemView.findViewById(R.id.menu).setOnClickListener(this);
        profileImageView = (CircularImageView) itemView.findViewById(R.id.profile_image);
        profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
        textView = (TextView) itemView.findViewById(R.id.text);
        nameView = (TextView) itemView.findViewById(R.id.name);
        screenNameView = (TextView) itemView.findViewById(R.id.screen_name);
        retweetProfileImageView = (ImageView) itemView.findViewById(R.id.retweet_profile_image);
        replyRetweetView = (TextView) itemView.findViewById(R.id.reply_retweet_status);
        timeView = (ShortTimeView) itemView.findViewById(R.id.time);

        mediaPreviewContainer = itemView.findViewById(R.id.media_preview_container);
        mediaPreviewView = (ImageView) itemView.findViewById(R.id.media_preview);

        replyCountView = (TextView) itemView.findViewById(R.id.reply_count);
        retweetCountView = (TextView) itemView.findViewById(R.id.retweet_count);
        favoriteCountView = (TextView) itemView.findViewById(R.id.favorite_count);
//TODO
//        profileImageView.setSelectorColor(ThemeUtils.getUserHighlightColor(itemView.getContext()));

        itemView.setOnClickListener(this);
        profileImageView.setOnClickListener(this);
        mediaPreviewContainer.setOnClickListener(this);
        retweetCountView.setOnClickListener(this);
        retweetCountView.setOnClickListener(this);
        favoriteCountView.setOnClickListener(this);
	}

    public void displayStatus(ParcelableStatus status) {
        final ImageLoaderWrapper loader = adapter.getImageLoader();
        final Context context = adapter.getContext();
        final ParcelableMedia[] media = status.media;

        if (status.retweet_id > 0) {
            if (status.retweet_count == 2) {
                replyRetweetView.setText(context.getString(R.string.name_and_another_retweeted,
                        status.retweeted_by_name));
            } else if (status.retweet_count > 2) {
                replyRetweetView.setText(context.getString(R.string.name_and_count_retweeted,
                        status.retweeted_by_name, status.retweet_count - 1));
            } else {
                replyRetweetView.setText(context.getString(R.string.name_retweeted, status.retweeted_by_name));
            }
            replyRetweetView.setVisibility(View.VISIBLE);
//            replyRetweetView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_retweet, 0, 0, 0);
            retweetProfileImageView.setVisibility(View.GONE);
        } else if (status.in_reply_to_status_id > 0 && status.in_reply_to_user_id > 0) {
            replyRetweetView.setText(context.getString(R.string.in_reply_to_name, status.in_reply_to_name));
            replyRetweetView.setVisibility(View.VISIBLE);
//            replyRetweetView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_reply, 0, 0, 0);
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

//            final int userColor = UserColorNicknameUtils.getUserColor(context, status.user_id);
//            profileImageView.setBorderColor(userColor);

        loader.displayProfileImage(profileImageView, status.user_profile_image_url);

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

        if (status.reply_count > 0) {
            replyCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.reply_count));
        } else {
            replyCountView.setText(null);
	    }
        if (status.retweet_count > 0) {
            retweetCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.retweet_count));
        } else {
            retweetCountView.setText(null);
	    }
        if (status.favorite_count > 0) {
            favoriteCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.favorite_count));
        } else {
            favoriteCountView.setText(null);
        }

        retweetCountView.setEnabled(!status.user_is_protected);

        retweetCountView.setActivated(Utils.isMyRetweet(status));
        favoriteCountView.setActivated(status.is_favorite);
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
            case R.id.menu: {
                if (context instanceof FragmentActivity) {
                    final Bundle args = new Bundle();
                    args.putParcelable(IntentConstants.EXTRA_STATUS, status);
                    final StatusMenuDialogFragment f = new StatusMenuDialogFragment();
                    f.setArguments(args);
                    f.show(((FragmentActivity) context).getSupportFragmentManager(), "status_menu");
	            }
                break;
	        }
            case R.id.profile_image: {
                Utils.openUserProfile(context, status.account_id, status.user_id, status.user_screen_name);
                break;
	        }
            case R.id.reply_count: {
                final Intent intent = new Intent(IntentConstants.INTENT_ACTION_REPLY);
                intent.setPackage(context.getPackageName());
                intent.putExtra(IntentConstants.EXTRA_STATUS, status);
                context.startActivity(intent);
                break;
	        }
        }
    }
}
