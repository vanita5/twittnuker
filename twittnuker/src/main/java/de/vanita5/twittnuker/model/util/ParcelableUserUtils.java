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

package de.vanita5.twittnuker.model.util;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.api.twitter.model.UrlEntity;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.provider.TwidereDataStore.DirectMessages;
import de.vanita5.twittnuker.util.HtmlEscapeHelper;
import de.vanita5.twittnuker.util.InternalTwitterContentUtils;
import de.vanita5.twittnuker.util.ParseUtils;
import de.vanita5.twittnuker.util.TwitterContentUtils;
import de.vanita5.twittnuker.util.media.preview.PreviewMediaExtractor;

public class ParcelableUserUtils implements TwittnukerConstants{

    public static ParcelableUser fromUser(@NonNull User user, @Nullable AccountKey accountKey) {
        return fromUser(user, accountKey, 0);
    }

    public static ParcelableUser fromUser(@NonNull User user, @Nullable AccountKey accountKey, long position) {
        final UrlEntity[] urlEntities = user.getUrlEntities();
        final ParcelableUser obj = new ParcelableUser();
        obj.position = position;
        if (accountKey != null) {
            obj.account_id = accountKey.getId();
            obj.account_host = accountKey.getHost();
        }
        obj.id = user.getId();
        obj.created_at = user.getCreatedAt().getTime();
        obj.is_protected = user.isProtected();
        obj.is_verified = user.isVerified();
        obj.name = user.getName();
        obj.screen_name = user.getScreenName();
        obj.description_plain = user.getDescription();
        obj.description_html = InternalTwitterContentUtils.formatUserDescription(user);
        obj.description_expanded = InternalTwitterContentUtils.formatExpandedUserDescription(user);
        obj.description_unescaped = HtmlEscapeHelper.toPlainText(obj.description_html);
        obj.location = user.getLocation();
        obj.profile_image_url = TwitterContentUtils.getProfileImageUrl(user);
        obj.profile_banner_url = user.getProfileBannerImageUrl();
        obj.url = user.getUrl();
        if (obj.url != null && urlEntities != null && urlEntities.length > 0) {
            obj.url_expanded = urlEntities[0].getExpandedUrl();
        }
        obj.is_follow_request_sent = user.isFollowRequestSent();
        obj.followers_count = user.getFollowersCount();
        obj.friends_count = user.getFriendsCount();
        obj.statuses_count = user.getStatusesCount();
        obj.favorites_count = user.getFavouritesCount();
        obj.listed_count = user.getListedCount();
        obj.media_count = user.getMediaCount();
        obj.is_following = user.isFollowing();
        obj.background_color = ParseUtils.parseColor("#" + user.getProfileBackgroundColor(), 0);
        obj.link_color = ParseUtils.parseColor("#" + user.getProfileLinkColor(), 0);
        obj.text_color = ParseUtils.parseColor("#" + user.getProfileTextColor(), 0);
        obj.is_cache = false;
        obj.is_basic = false;

        ParcelableUser.Extras extras = new ParcelableUser.Extras();
        extras.ostatus_uri = user.getOstatusUri();
        extras.statusnet_profile_url = user.getStatusnetProfileUrl();
        extras.profile_image_url_original = user.getProfileImageUrlOriginal();
        extras.profile_image_url_profile_size = user.getProfileImageUrlProfileSize();
        extras.groups_count = user.getGroupsCount();
        obj.extras = extras;
        obj.user_host = getUserHost(extras.ostatus_uri);
        return obj;
    }

    public static String getUserHost(@Nullable String uri) {
        if (uri == null) return USER_TYPE_TWITTER_COM;
        final String authority = PreviewMediaExtractor.getAuthority(uri);
        if (authority == null) return null;
        return authority.replaceAll("[^\\w\\d]", "-");
    }

    public static String getUserHost(ParcelableUser user) {
        if (user.extras == null) return USER_TYPE_TWITTER_COM;
        return getUserHost(user.extras.ostatus_uri);
    }

    public static ParcelableUser fromDirectMessageConversationEntry(final Cursor cursor) {
        final long accountId = cursor.getLong(DirectMessages.ConversationEntries.IDX_ACCOUNT_ID);
        final long id = cursor.getLong(DirectMessages.ConversationEntries.IDX_CONVERSATION_ID);
        final String name = cursor.getString(DirectMessages.ConversationEntries.IDX_NAME);
        final String screenName = cursor.getString(DirectMessages.ConversationEntries.IDX_SCREEN_NAME);
        final String profileImageUrl = cursor.getString(DirectMessages.ConversationEntries.IDX_PROFILE_IMAGE_URL);
        return new ParcelableUser(accountId, id, name, screenName, profileImageUrl);
    }

    public static ParcelableUser[] fromUsers(final User[] users, AccountKey accountKey) {
        if (users == null) return null;
        int size = users.length;
        final ParcelableUser[] result = new ParcelableUser[size];
        for (int i = 0; i < size; i++) {
            result[i] = fromUser(users[i], accountKey);
        }
        return result;
    }
}