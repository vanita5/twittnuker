package de.vanita5.twittnuker.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.receiver.GCMReceiver;
import de.vanita5.twittnuker.util.Utils;

import static de.vanita5.twittnuker.util.Utils.getAccountIds;

public class PushService extends IntentService implements Constants {

	public static final String TAG = "PushService";

	private NotificationManager mNotificationManager;
	Builder builder;

	public PushService(String name) {
		super(name);
	}

	public PushService() {
		super("PushService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
				//TODO type of notification
				final String fromUser = extras.getString("fromuser");
				final String sAccountId = extras.getString("account");
				final String message = extras.getString("msg");
				long accountId = -1;
				try {
					accountId = Long.parseLong(sAccountId);
				} catch (NumberFormatException e) {
					//This should never happen
					//Either a wrong intent has been processed or the backend server
					//sends wrong messages!
					Log.e(TAG, e.getMessage());
					return;
				}

				final AccountPreferences[] prefs = AccountPreferences.getPushEnabledPreferences(this, getAccountIds(this));
				for (final AccountPreferences pref : prefs) {
					if (pref.getAccountId() == accountId) {
						//Push has been enabled for this account. Continue!
						//TODO type

						break;
					}
					//There is no such account with Push enabled...
					//TODO maybe we should remove the account/unregister?
				}
				//buildPushNotification(extras.getString("fromuser"), null, extras.getString("msg"));
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GCMReceiver.completeWakefulIntent(intent);
	}
}
