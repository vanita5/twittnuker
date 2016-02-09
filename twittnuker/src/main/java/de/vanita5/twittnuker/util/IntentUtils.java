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

package de.vanita5.twittnuker.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.constant.IntentConstants;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.ParcelableUser;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class IntentUtils {
    public static String getStatusShareText(@NonNull final Context context, @NonNull final ParcelableStatus status) {
        final Uri link = LinkCreator.getTwitterStatusLink(status);
        return context.getString(R.string.status_share_text_format_with_link,
                status.text_plain, link.toString());
    }

    public static String getStatusShareSubject(@NonNull final Context context, @NonNull final ParcelableStatus status) {
        final String timeString = Utils.formatToLongTimeString(context, status.timestamp);
        return context.getString(R.string.status_share_subject_format_with_time,
                status.user_name, status.user_screen_name, timeString);
    }

    public static void openUserProfile(final Context context, final ParcelableUser user,
                                       final Bundle activityOptions, final boolean newDocument) {
        if (context == null || user == null) return;
        final Bundle extras = new Bundle();
        extras.putParcelable(IntentConstants.EXTRA_USER, user);
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(TwittnukerConstants.SCHEME_TWITTNUKER);
        builder.authority(TwittnukerConstants.AUTHORITY_USER);
        builder.appendQueryParameter(TwittnukerConstants.QUERY_PARAM_ACCOUNT_ID, String.valueOf(user.account_id));
        if (user.id > 0) {
            builder.appendQueryParameter(TwittnukerConstants.QUERY_PARAM_USER_ID, String.valueOf(user.id));
        }
        if (user.screen_name != null) {
            builder.appendQueryParameter(TwittnukerConstants.QUERY_PARAM_SCREEN_NAME, user.screen_name);
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        intent.setExtrasClassLoader(context.getClassLoader());
        intent.putExtras(extras);
        if (BuildConfig.NEW_DOCUMENT_INTENT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        if (context instanceof Activity) {
            ActivityCompat.startActivity((Activity) context, intent, activityOptions);
        } else {
            context.startActivity(intent);
        }
    }

    public static void openUserProfile(final Context context, final long accountId, final long userId,
                                       final String screenName, final Bundle activityOptions,
                                       final boolean newDocument) {
        if (context == null || accountId <= 0 || userId <= 0 && isEmpty(screenName)) return;
        final Uri uri = LinkCreator.getTwidereUserLink(accountId, userId, screenName);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (BuildConfig.NEW_DOCUMENT_INTENT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        if (context instanceof Activity) {
            ActivityCompat.startActivity((Activity) context, intent, activityOptions);
        } else {
            context.startActivity(intent);
        }
    }

    public static void openUsers(final Context context, final List<ParcelableUser> users) {
        if (context == null || users == null) return;
        final Bundle extras = new Bundle();
        extras.putParcelableArrayList(IntentConstants.EXTRA_USERS, new ArrayList<>(users));
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(TwittnukerConstants.SCHEME_TWITTNUKER);
        builder.authority(TwittnukerConstants.AUTHORITY_USERS);
        final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    public static void openUserTimeline(final Context context, final long accountId,
                                        final long userId, final String screenName) {
        if (context == null) return;
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(TwittnukerConstants.SCHEME_TWITTNUKER);
        builder.authority(TwittnukerConstants.AUTHORITY_USER_TIMELINE);
        builder.appendQueryParameter(TwittnukerConstants.QUERY_PARAM_ACCOUNT_ID, String.valueOf(accountId));
        if (userId > 0) {
            builder.appendQueryParameter(TwittnukerConstants.QUERY_PARAM_USER_ID, String.valueOf(userId));
        }
        if (screenName != null) {
            builder.appendQueryParameter(TwittnukerConstants.QUERY_PARAM_SCREEN_NAME, screenName);
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        context.startActivity(intent);
    }

    public static void openUserMentions(final Context context, final long accountId, final String screenName) {
        if (context == null) return;
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(TwittnukerConstants.SCHEME_TWITTNUKER);
        builder.authority(TwittnukerConstants.AUTHORITY_USER_MENTIONS);
        builder.appendQueryParameter(TwittnukerConstants.QUERY_PARAM_ACCOUNT_ID, String.valueOf(accountId));
        if (screenName != null) {
            builder.appendQueryParameter(TwittnukerConstants.QUERY_PARAM_SCREEN_NAME, screenName);
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        context.startActivity(intent);
    }
}