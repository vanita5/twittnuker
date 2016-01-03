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

package de.vanita5.twittnuker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.apache.commons.lang3.math.NumberUtils;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.model.StringLongPair;
import de.vanita5.twittnuker.util.ReadStateManager;
import de.vanita5.twittnuker.util.UriExtraUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.dagger.DependencyHolder;

public class NotificationReceiver extends BroadcastReceiver implements Constants {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            case BROADCAST_NOTIFICATION_DELETED: {
                final Uri uri = intent.getData();
                if (uri == null) return;
                DependencyHolder holder = DependencyHolder.get(context);
                final String type = uri.getQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE);
                final long accountId = NumberUtils.toLong(uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID), -1);
                final long itemId = NumberUtils.toLong(UriExtraUtils.getExtra(uri, "item_id"), -1);
                final long itemUserId = NumberUtils.toLong(UriExtraUtils.getExtra(uri, "item_user_id"), -1);
                final boolean itemUserFollowing = Boolean.parseBoolean(UriExtraUtils.getExtra(uri, "item_user_following"));
                final long timestamp = NumberUtils.toLong(uri.getQueryParameter(QUERY_PARAM_TIMESTAMP), -1);
                final ReadStateManager manager = holder.getReadStateManager();
                final String paramReadPosition, paramReadPositions;
                final String tag = getPositionTag(type);
                if (tag != null && !TextUtils.isEmpty(paramReadPosition = uri.getQueryParameter(QUERY_PARAM_READ_POSITION))) {
                    final long def = -1;
                    manager.setPosition(Utils.getReadPositionTagWithAccounts(tag, accountId),
                            NumberUtils.toLong(paramReadPosition, def));
                } else if (!TextUtils.isEmpty(paramReadPositions = uri.getQueryParameter(QUERY_PARAM_READ_POSITIONS))) {
                    try {
                        final StringLongPair[] pairs = StringLongPair.valuesOf(paramReadPositions);
                        for (StringLongPair pair : pairs) {
                            manager.setPosition(tag, pair.getKey(), pair.getValue());
                        }
                    } catch (NumberFormatException ignore) {

                    }
                }
                break;
            }
        }
    }

    private static String getPositionTag(@NonNull String type) {
        switch (type) {
            case AUTHORITY_HOME: {
                return TAB_TYPE_HOME_TIMELINE;
            }
            case AUTHORITY_MENTIONS: {
                return TAB_TYPE_NOTIFICATIONS_TIMELINE;
            }
            case AUTHORITY_DIRECT_MESSAGES: {
                return TAB_TYPE_DIRECT_MESSAGES;
            }
            default: {
                return TAB_TYPE_HOME_TIMELINE;
            }
        }
    }
}