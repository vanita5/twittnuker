package de.vanita5.twittnuker.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.HomeActivity;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.receiver.GCMReceiver;
import de.vanita5.twittnuker.util.HtmlEscapeHelper;
import de.vanita5.twittnuker.util.Utils;

import static de.vanita5.twittnuker.util.Utils.getAccountIds;
import static de.vanita5.twittnuker.util.Utils.isNotificationsSilent;

public class PushService extends IntentService implements Constants {

	public static final String TAG = "PushService";

	public static final String TYPE_MENTION = "type_mention";

	private NotificationManager mNotificationManager;

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
				final String type = extras.getString("type");
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
						if (TYPE_MENTION.equals(type)) {
							updateNotificationWithMention(message, fromUser, pref);
						}
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

	private void updateNotificationWithMention(final String message, final String fromuser, final AccountPreferences pref) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		final int notificationType = pref.getMentionsNotificationType();

		builder.setContentTitle("@" + fromuser); //TODO Should be modified if there already is a notification
		builder.setContentText(message); //TODO Update if there are multiple messages
		builder.setTicker(message);
		builder.setSmallIcon(R.drawable.ic_stat_twittnuker);
		builder.setAutoCancel(true);

		//TODO This looks interesting. We should delete all notification stuff by this intent
		//setDeleteIntent(PendingIntent intent)

		//TODO Get the notification count from somewhere...
		int notificationCount = 1;
		builder.setContentInfo(String.valueOf(notificationCount));

		if (notificationCount > 1) {
			NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			inboxStyle.setBigContentTitle(notificationCount + "new interactions");

			//TODO for every notification:
			final String nameEscaped = HtmlEscapeHelper.escape("@" + fromuser);
			final String textEscaped = HtmlEscapeHelper.escape(message);
			inboxStyle.addLine(Html.fromHtml(String.format("<b>%s</b>: %s", nameEscaped, textEscaped)));

			//TODO add summary? Test

			builder.setStyle(inboxStyle);
		} else {
			//TODO Add big style? We need a status for this!
		}

		//TODO NotificationType for Follower Notifications
		int defaults = 0;
		if (isNotificationAudible()) {
			if (AccountPreferences.isNotificationHasRingtone(notificationType)) {
				final Uri ringtone = pref.getNotificationRingtone();
				builder.setSound(ringtone, Notification.STREAM_DEFAULT);
			}
			if (AccountPreferences.isNotificationHasVibration(notificationType)) {
				defaults |= Notification.DEFAULT_VIBRATE;
			} else {
				defaults &= ~Notification.DEFAULT_VIBRATE;
			}
			if (AccountPreferences.isNotificationHasLight(notificationType)) {
				final int color = pref.getNotificationLightColor();
				builder.setLights(color, 1000, 2000);
			}
			builder.setDefaults(defaults);
		}

		Intent result = new Intent(this, HomeActivity.class);
		result.setAction(Intent.ACTION_MAIN);
		result.addCategory(Intent.CATEGORY_LAUNCHER);
		result.putExtra(EXTRA_TAB_TYPE, TAB_TYPE_MENTIONS_TIMELINE);
		if (notificationCount == 1) {
			//TODO Go directly to the mention (see TwidereDataProvider)? We need a status for this
		}

		//TaskStackBuilder: This ensures that navigating backward from the Activity leads out of your application
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(HomeActivity.class);
		stackBuilder.addNextIntent(result);
		PendingIntent resultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setContentIntent(resultIntent);

		mNotificationManager = getNotificationManager();
		mNotificationManager.notify(NOTIFICATION_ID_PUSH, builder.build());
	}

	private NotificationManager getNotificationManager() {
		if (mNotificationManager != null) return mNotificationManager;
		return mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}
}
