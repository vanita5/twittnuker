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

package de.vanita5.twittnuker.service;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.model.NotificationContent;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.util.UserKeyUtils;
import de.vanita5.twittnuker.util.NotificationHelper;

public class TwittnukerGcmListenerService extends GcmListenerService {

    private static final String TAG = "GcmListenerService";

    private NotificationHelper mNotificationHelper;

    public TwittnukerGcmListenerService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationHelper = new NotificationHelper(this);
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String type = data.getString("type");
        Log.d(TAG, "Push Notification " + type);

        //TODO show notification
        UserKey accountKey = null;
        try {
            accountKey = new UserKey(Long.parseLong(data.getString("account")), TwittnukerConstants.USER_TYPE_TWITTER_COM);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        AccountPreferences accountPreferences = new AccountPreferences(this, accountKey);

        NotificationContent content = new NotificationContent();
        content.setAccountKey(accountKey);
        content.setObjectId(data.getString("object_id"));
        content.setObjectUserId(data.getString("object_user_id"));
        content.setFromUser(data.getString("fromuser"));
        content.setType(data.getString("type"));
        content.setMessage(data.getString("msg"));
        content.setTimestamp(System.currentTimeMillis());
        content.setProfileImageUrl(data.getString("image"));

        mNotificationHelper.cachePushNotification(content);
        mNotificationHelper.buildNotificationByType(content, accountPreferences, false);
    }
}
