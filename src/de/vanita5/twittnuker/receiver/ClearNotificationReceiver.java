package de.vanita5.twittnuker.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.provider.TweetStore.PushNotifications;

public class ClearNotificationReceiver extends BroadcastReceiver implements Constants {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (INTENT_ACTION_PUSH_NOTIFICATION_CLEARED.equals(action)) {
			final long accountId = intent.getLongExtra(EXTRA_USER_ID, -1);
			final ContentResolver resolver = context.getContentResolver();
			resolver.delete(PushNotifications.CONTENT_URI, PushNotifications.ACCOUNT_ID + " = " + accountId, null);
		}
	}
}
