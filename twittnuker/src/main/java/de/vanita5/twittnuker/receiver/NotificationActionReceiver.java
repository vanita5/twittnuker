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

package de.vanita5.twittnuker.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.NotificationHelper;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.dagger.ApplicationModule;
import de.vanita5.twittnuker.util.dagger.DependencyHolder;

public class NotificationActionReceiver extends BroadcastReceiver implements Constants {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case INTENT_ACTION_PUSH_NOTIFICATION_CLEARED: {
                NotificationHelper notificationHelper = new NotificationHelper(context);
                final UserKey accountKey = intent.getParcelableExtra(EXTRA_ACCOUNT_KEY);
                notificationHelper.deleteCachedNotifications(accountKey, null);
                break;
            }
            case INTENT_ACTION_RETWEET: {
                DependencyHolder holder = DependencyHolder.Companion.get(context);
                AsyncTwitterWrapper twitter = holder.getAsyncTwitterWrapper();

                final ParcelableStatus status = intent.getParcelableExtra(EXTRA_STATUS);
                if (twitter == null || status == null) return;
                Utils.retweet(status, twitter);

                cancelNotificationById(context, intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1));
                break;
            }
            case INTENT_ACTION_FAVORITE: {
                DependencyHolder holder = DependencyHolder.Companion.get(context);
                AsyncTwitterWrapper twitter = holder.getAsyncTwitterWrapper();
                final ParcelableStatus status = intent.getParcelableExtra(EXTRA_STATUS);
                if (twitter == null || status == null) return;
                Utils.favorite(status, twitter, null);

                cancelNotificationById(context, intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1));
                break;
            }
            default:
                break;
        }
    }

    private void cancelNotificationById(final Context context, final int notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }
    }
}
