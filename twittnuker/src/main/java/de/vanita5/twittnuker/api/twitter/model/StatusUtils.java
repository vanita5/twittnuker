/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
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

package de.vanita5.twittnuker.api.twitter.model;

import android.support.annotation.NonNull;

import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.TwitterContentUtils;

import java.util.Date;

public class StatusUtils {

    public static Status fromParcelableStatus(@NonNull ParcelableStatus parcelable) {
        Status status = new Status();
        status.id = parcelable.id;
        status.text = TwitterContentUtils.escapeTwitterStatusText(parcelable.text_plain);
        status.createdAt = new Date(parcelable.timestamp);
        status.inReplyToStatusId = parcelable.in_reply_to_status_id;
        status.inReplyToUserId = parcelable.in_reply_to_user_id;
        status.inReplyToScreenName = parcelable.in_reply_to_screen_name;
        if (parcelable.is_retweet) {
            Status retweet = status.retweetedStatus = new Status();
            retweet.id = parcelable.retweet_id;
            retweet.text = TwitterContentUtils.escapeTwitterStatusText(parcelable.text_plain);
            retweet.createdAt = new Date(parcelable.retweet_timestamp);
            User retweetUser = retweet.user = new User();
            retweetUser.id = parcelable.user_id;
            retweetUser.screenName = parcelable.user_screen_name;
            retweetUser.name = parcelable.user_name;
            retweetUser.profileBackgroundImageUrl = parcelable.user_profile_image_url;

            User user = status.user = new User();
            user.id = parcelable.retweeted_by_user_id;
            user.name = parcelable.retweeted_by_user_name;
            user.screenName = parcelable.retweeted_by_user_screen_name;
            user.profileImageUrl = parcelable.retweeted_by_user_profile_image;
        } else if (parcelable.is_quote) {
            Status quote = status.quotedStatus = new Status();
            quote.id = parcelable.quoted_id;
            quote.text = TwitterContentUtils.escapeTwitterStatusText(parcelable.quoted_text_plain);
            quote.createdAt = new Date(parcelable.quoted_timestamp);
            User quotedUser = quote.user = new User();
            quotedUser.id = parcelable.quoted_user_id;
            quotedUser.name = parcelable.quoted_user_name;
            quotedUser.screenName = parcelable.quoted_user_screen_name;
            quotedUser.profileImageUrl = parcelable.quoted_user_profile_image;

            User user = status.user = new User();
            user.id = parcelable.user_id;
            user.screenName = parcelable.user_screen_name;
            user.name = parcelable.user_name;
            user.profileBackgroundImageUrl = parcelable.user_profile_image_url;
        } else {
            User user = status.user = new User();
            user.id = parcelable.user_id;
            user.screenName = parcelable.user_screen_name;
            user.name = parcelable.user_name;
            user.profileBackgroundImageUrl = parcelable.user_profile_image_url;
        }
        return status;
    }

}