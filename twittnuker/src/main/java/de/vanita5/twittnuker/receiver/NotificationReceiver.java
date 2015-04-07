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

package de.vanita5.twittnuker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.fragment.support.DirectMessagesFragment;
import de.vanita5.twittnuker.fragment.support.HomeTimelineFragment;
import de.vanita5.twittnuker.fragment.support.MentionsTimelineFragment;
import de.vanita5.twittnuker.model.StringLongPair;
import de.vanita5.twittnuker.util.ParseUtils;
import de.vanita5.twittnuker.util.ReadStateManager;
import de.vanita5.twittnuker.util.Utils;

public class NotificationReceiver extends BroadcastReceiver implements Constants {
	@Override
	public void onReceive(Context context, Intent intent) {
		switch (intent.getAction()) {
			case BROADCAST_NOTIFICATION_DELETED: {
				final Uri uri = intent.getData();
				final String tag = getPositionTag(uri.getLastPathSegment());
				if (tag == null) return;
				final long accountId = ParseUtils.parseLong(uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID), -1);
				final TwittnukerApplication app = TwittnukerApplication.getInstance(context);
				final ReadStateManager manager = app.getReadStateManager();
				final String paramReadPosition, paramReadPositions;
				if (!TextUtils.isEmpty(paramReadPosition = uri.getQueryParameter(QUERY_PARAM_READ_POSITION))) {
					manager.setPosition(Utils.getReadPositionTagWithAccounts(tag, accountId),
							ParseUtils.parseLong(paramReadPosition, -1));
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

	private String getPositionTag(String type) {
		switch (type) {
			case AUTHORITY_HOME: {
				return HomeTimelineFragment.KEY_READ_POSITION_TAG;
			}
			case AUTHORITY_MENTIONS: {
				return MentionsTimelineFragment.KEY_READ_POSITION_TAG;
			}
			case AUTHORITY_DIRECT_MESSAGES: {
				return DirectMessagesFragment.KEY_READ_POSITION_TAG;
			}
		}
		return null;
	}
}