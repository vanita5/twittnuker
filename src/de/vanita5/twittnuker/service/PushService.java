package de.vanita5.twittnuker.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;
import java.util.List;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.HomeActivity;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.model.PushNotificationContent;
import de.vanita5.twittnuker.provider.TweetStore.PushNotifications;
import de.vanita5.twittnuker.receiver.GCMReceiver;
import de.vanita5.twittnuker.util.HtmlEscapeHelper;
import de.vanita5.twittnuker.util.Utils;

import static de.vanita5.twittnuker.util.Utils.getAccountIds;
import static de.vanita5.twittnuker.util.Utils.isNotificationsSilent;

public class PushService extends IntentService implements Constants {

	public static final String TAG = "PushService";

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

				long accountId = -1;
				try {
					accountId = Long.parseLong(extras.getString("account"));
				} catch (NumberFormatException e) {
					//This should never happen
					//Either a wrong intent has been processed or the backend server
					//sends wrong messages!
					Log.e(TAG, e.getMessage());
					return;
				}
				PushNotificationContent notification = new PushNotificationContent();
				notification.setAccountId(accountId);
				notification.setFromUser(extras.getString("fromuser"));
				notification.setType(extras.getString("type"));
				notification.setMessage(extras.getString("msg"));
				notification.setTimestamp(System.currentTimeMillis());

				final AccountPreferences[] prefs = AccountPreferences.getPushEnabledPreferences(this, getAccountIds(this));
				for (final AccountPreferences pref : prefs) {
					if (pref.getAccountId() == accountId) {
						//Push has been enabled for this account. Continue!
						cachePushNotification(notification);
						buildNotification(notification, pref);
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

	private void buildNotification(final PushNotificationContent notification, final AccountPreferences pref) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		final int notificationType = pref.getMentionsNotificationType();

		builder.setContentTitle("@" + notification.getFromUser()); //TODO Should be modified if there already is a notification
		builder.setContentText(notification.getMessage()); //TODO Update if there are multiple messages
		builder.setTicker(notification.getMessage());
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
			final String nameEscaped = HtmlEscapeHelper.escape("@" + notification.getFromUser());
			final String textEscaped = HtmlEscapeHelper.escape(notification.getMessage());
			inboxStyle.addLine(Html.fromHtml(String.format("<b>%s</b>: %s", nameEscaped, textEscaped)));

			//TODO add summary? Test

			builder.setStyle(inboxStyle);
		} else {
			//TODO Add big style? We need a status for this!
		}

		//TODO NotificationType for Follower Notifications
		int defaults = 0;
		if (!isNotificationsSilent(this)) {
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
		//TODO make notification id uniqe for every account!
		mNotificationManager.notify(NOTIFICATION_ID_PUSH, builder.build());
	}

	private NotificationManager getNotificationManager() {
		if (mNotificationManager != null) return mNotificationManager;
		return mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private void cachePushNotification(final PushNotificationContent notification) {
		final ContentResolver resolver = getContentResolver();
		final ContentValues values = new ContentValues();
		values.put(PushNotifications.ACCOUNT_ID, notification.getAccountId());
		values.put(PushNotifications.FROM_USER, notification.getFromUser());
		values.put(PushNotifications.MESSAGE, notification.getMessage());
		values.put(PushNotifications.NOTIFICATION_TYPE, notification.getType());
		values.put(PushNotifications.TIMESTAMP, notification.getTimestamp());
		resolver.insert(PushNotifications.CONTENT_URI, values);
	}

	private List<PushNotificationContent> getCachedPushNotifications(final long argAccountId) {
		if (argAccountId <= 0) return null;
		final ContentResolver resolver = getContentResolver();
		final String where = PushNotifications.ACCOUNT_ID + " = " + argAccountId;
		final Cursor c = resolver.query(PushNotifications.CONTENT_URI, PushNotifications.MATRIX_COLUMNS,
				where, null, PushNotifications.DEFAULT_SORT_ORDER);

		if (c == null || c.getCount() == 0) return null;
		c.moveToFirst();
		final int idxAccountId = c.getColumnIndex(PushNotifications.ACCOUNT_ID);
		final int idxMessage = c.getColumnIndex(PushNotifications.MESSAGE);
		final int idxTimestamp = c.getColumnIndex(PushNotifications.TIMESTAMP);
		final int idxFromUser = c.getColumnIndex(PushNotifications.FROM_USER);
		final int idxType = c.getColumnIndex(PushNotifications.NOTIFICATION_TYPE);

		List<PushNotificationContent> results = new ArrayList<PushNotificationContent>();
		while(!c.isAfterLast()) {
			PushNotificationContent notification = new PushNotificationContent();
			notification.setAccountId(c.getLong(idxAccountId));
			notification.setMessage(c.getString(idxMessage));
			notification.setTimestamp(c.getLong(idxTimestamp));
			notification.setFromUser(c.getString(idxFromUser));
			notification.setType(c.getString(idxType));
			results.add(notification);
			c.moveToNext();
		}
		c.close();
		return results;
	}
}
