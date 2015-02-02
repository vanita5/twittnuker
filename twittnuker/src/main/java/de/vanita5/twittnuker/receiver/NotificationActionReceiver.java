package de.vanita5.twittnuker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.NotificationHelper;
import de.vanita5.twittnuker.util.Utils;

public class NotificationActionReceiver extends BroadcastReceiver implements Constants {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		switch (action) {
			case INTENT_ACTION_PUSH_NOTIFICATION_CLEARED: {
				NotificationHelper notificationHelper = new NotificationHelper(context);
				final long accountId = intent.getLongExtra(EXTRA_USER_ID, -1);
				notificationHelper.deleteCachedNotifications(accountId, null);
				break;
			}
			case INTENT_ACTION_RETWEET: {
				AsyncTwitterWrapper twitter = AsyncTwitterWrapper.getInstance(context);
				ParcelableStatus status = intent.getParcelableExtra(EXTRA_STATUS);
				if (twitter == null || status == null) return;
				Utils.retweet(status, twitter);
				break;
			}
			case INTENT_ACTION_FAVORITE: {
				AsyncTwitterWrapper twitter = AsyncTwitterWrapper.getInstance(context);
				ParcelableStatus status = intent.getParcelableExtra(EXTRA_STATUS);
				if (twitter == null || status == null) return;
				Utils.favorite(status, twitter);
				break;
			}
			default:
				break;
		}
	}
}
