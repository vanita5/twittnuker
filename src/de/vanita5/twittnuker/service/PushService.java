package de.vanita5.twittnuker.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.model.NotificationContent;
import de.vanita5.twittnuker.receiver.GCMReceiver;
import de.vanita5.twittnuker.util.NotificationHelper;
import de.vanita5.twittnuker.util.Utils;

import static de.vanita5.twittnuker.util.Utils.getAccountIds;

public class PushService extends IntentService implements Constants {

	public static final String TAG = "PushService";

	private NotificationHelper mNotificationHelper;

	public PushService(String name) {
		super(name);
	}

	public PushService() {
		super("PushService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationHelper = new NotificationHelper();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) return;
		Bundle extras = intent.getExtras();

		if (!extras.isEmpty()) {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
			final String messageType = gcm.getMessageType(intent);

			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				if (Utils.isDebugBuild()) Log.i("accounts", "Received: " + extras.toString());

				long accountId = -1;
				try {
					accountId = Long.parseLong(extras.getString("account"));
				} catch (NumberFormatException e) {
					//This should never happen
					Log.e(TAG, e.getMessage());
					return;
				}

				final AccountPreferences[] prefs = AccountPreferences.getPushEnabledPreferences(this, getAccountIds(this));
				for (final AccountPreferences pref : prefs) {
					if (pref.getAccountId() == accountId) {
						//Push has been enabled for this account. Continue!
						NotificationContent notification = new NotificationContent();
						notification.setAccountId(accountId);
						notification.setFromUser(extras.getString("fromuser"));
						notification.setType(extras.getString("type"));
						notification.setMessage(extras.getString("msg"));
						notification.setTimestamp(System.currentTimeMillis());

						mNotificationHelper.cachePushNotification(this, notification);
						mNotificationHelper.buildNotificationByType(this, notification, pref);
						break;
					}
					//There is no such account with Push enabled...
					//TODO maybe we should remove the account/unregister?
				}
			}
		}
		GCMReceiver.completeWakefulIntent(intent);
	}
}
