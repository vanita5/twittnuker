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
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.HomeActivity;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.model.PushNotificationContent;
import de.vanita5.twittnuker.provider.TweetStore.PushNotifications;
import de.vanita5.twittnuker.receiver.ClearNotificationReceiver;
import de.vanita5.twittnuker.receiver.GCMReceiver;
import de.vanita5.twittnuker.util.HtmlEscapeHelper;
import de.vanita5.twittnuker.util.ImagePreloader;
import de.vanita5.twittnuker.util.Utils;

import static de.vanita5.twittnuker.util.Utils.getAccountIds;
import static de.vanita5.twittnuker.util.Utils.getAccountNotificationId;
import static de.vanita5.twittnuker.util.Utils.getAccountScreenName;
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
					Log.e(TAG, e.getMessage());
					return;
				}

				final AccountPreferences[] prefs = AccountPreferences.getPushEnabledPreferences(this, getAccountIds(this));
				for (final AccountPreferences pref : prefs) {
					if (pref.getAccountId() == accountId) {
						//Push has been enabled for this account. Continue!
						PushNotificationContent notification = new PushNotificationContent();
						notification.setAccountId(accountId);
						notification.setFromUser(extras.getString("fromuser"));
						notification.setType(extras.getString("type"));
						notification.setMessage(extras.getString("msg"));
						notification.setTimestamp(System.currentTimeMillis());

						cachePushNotification(notification);
						buildNotificationByType(notification, pref);
						break;
					}
					//There is no such account with Push enabled...
					//TODO maybe we should remove the account/unregister?
				}
			}
		}
		GCMReceiver.completeWakefulIntent(intent);
	}

	private void buildNotificationByType(final PushNotificationContent notification, final AccountPreferences pref) {
		final String type = notification.getType();
		final int notificationType = pref.getMentionsNotificationType();
		List<PushNotificationContent> pendingNotifications = getCachedPushNotifications(notification.getAccountId());
		final int notificationCount = pendingNotifications.size();

		String contentText = null;
		String ticker = null;
		//Bitmap icon; TODO

		if (PushNotificationContent.PUSH_NOTIFICATION_TYPE_MENTION.equals(type)) {
			contentText = stripMentionText(notification.getMessage(),
					getAccountScreenName(this, notification.getAccountId()));
			ticker = notification.getMessage();
		} else if (PushNotificationContent.PUSH_NOTIFICATION_TYPE_RETWEET.equals(type)) {
			contentText = getString(R.string.push_new_retweet_single)
					+ ": " + notification.getMessage();
			ticker = contentText; //TODO Should we really add the message to the ticker? We could
		} else if (PushNotificationContent.PUSH_NOTIFICATION_TYPE_FAVORITE.equals(type)) {
			contentText = getString(R.string.push_new_favorite_single)
					+ ": " + notification.getMessage();
			ticker = contentText; //TODO Should we really add the message to the ticker?
		} else if (PushNotificationContent.PUSH_NOTIFICATION_TYPE_FOLLOWER.equals(type)) {
			contentText = "@" + notification.getFromUser() + " " + getString(R.string.push_new_follower);
			ticker = contentText;
		}
		if (contentText == null && ticker == null) return;
		buildNotification(notification, pref, notificationType, notificationCount,
				pendingNotifications, contentText, ticker, null);
	}

	private Spanned getInboxLineByType(final PushNotificationContent pendingNotification) {
		final String type = pendingNotification.getType();
		final String nameEscaped = HtmlEscapeHelper.escape("@" + pendingNotification.getFromUser());
		final String textEscaped = HtmlEscapeHelper.escape(pendingNotification.getMessage());
		if (PushNotificationContent.PUSH_NOTIFICATION_TYPE_MENTION.equals(type)) {
			return Html.fromHtml(String.format("<b>%s</b>: %s", nameEscaped, textEscaped));
		} else if (PushNotificationContent.PUSH_NOTIFICATION_TYPE_RETWEET.equals(type)) {
			return Html.fromHtml(String.format("<b>%s " + getString(R.string.push_new_retweet) + "</b>: %s",
					nameEscaped, textEscaped));
		} else if (PushNotificationContent.PUSH_NOTIFICATION_TYPE_FAVORITE.equals(type)) {
			return Html.fromHtml(String.format("<b>%s " + getString(R.string.push_new_favorite) + "</b>: %s",
					nameEscaped, textEscaped));
		} else if (PushNotificationContent.PUSH_NOTIFICATION_TYPE_FOLLOWER.equals(type)) {
			return Html.fromHtml(String.format("<b>%s</b> " + getString(R.string.push_new_follower),
					nameEscaped));
		}
		return null;
	}

	private void buildNotification(final PushNotificationContent notification, final AccountPreferences pref,
								   final int notificationType, final int notificationCount,
								   List<PushNotificationContent> pendingNotifications, final String contentText,
								   final String ticker, final Bitmap icon) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		builder.setContentTitle("@" + notification.getFromUser());
		builder.setContentText(contentText);
		builder.setTicker(ticker);
		builder.setSmallIcon(R.drawable.ic_stat_twittnuker);
		if (icon != null) builder.setLargeIcon(icon);
		builder.setDeleteIntent(getDeleteIntent(notification.getAccountId()));
		builder.setAutoCancel(true);
		builder.setWhen(notification.getTimestamp());

		if (notificationCount > 1) {
			NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			inboxStyle.setBigContentTitle(notificationCount + " " + getString(R.string.push_new_interactions));

			for (PushNotificationContent pendingNotification : pendingNotifications) {
				Spanned line = getInboxLineByType(pendingNotification);
				if (line != null) inboxStyle.addLine(line);
			}
			inboxStyle.setSummaryText("@" + getAccountScreenName(this, notification.getAccountId()));
			builder.setNumber(notificationCount);
			builder.setStyle(inboxStyle);
		} else {
			//final Intent replyIntent = new Intent(INTENT_ACTION_REPLY);
			//replyIntent.setExtrasClassLoader(getClassLoader());
			//replyIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationType);
			//replyIntent.putExtra(EXTRA_NOTIFICATION_ACCOUNT, notification.getAccountId());
			////replyIntent.putExtra(EXTRA_STATUS, firstItem); TODO
			//replyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//builder.addAction(R.drawable.ic_action_reply, getString(R.string.reply),
			//		PendingIntent.getActivity(this, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			//final NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(builder);
			//bigTextStyle.bigText(stripMentionText(notification.getMessage(),
			//		getAccountScreenName(this, notification.getAccountId())));
			//bigTextStyle.setSummaryText("@" + getAccountScreenName(this, notification.getAccountId()));
			//builder.setStyle(bigTextStyle);
		}

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
			//TODO Go directly to the mention (see TwidereDataProvider)? We need a ParcelableStatus for this...
			//Load the status?
		}

		//TaskStackBuilder: This ensures that navigating backward from the Activity leads out of your application
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(HomeActivity.class);
		stackBuilder.addNextIntent(result);
		PendingIntent resultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setContentIntent(resultIntent);

		mNotificationManager = getNotificationManager();
		mNotificationManager.notify(getAccountNotificationId(NOTIFICATION_ID_PUSH,
				notification.getAccountId()), builder.build());
	}

	private NotificationManager getNotificationManager() {
		if (mNotificationManager != null) return mNotificationManager;
		return mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private PendingIntent getDeleteIntent(final long accountId) {
		Intent intent = new Intent(this, ClearNotificationReceiver.class);
		intent.setAction(INTENT_ACTION_PUSH_NOTIFICATION_CLEARED);
		intent.putExtra(EXTRA_USER_ID, accountId);
		return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	private Bitmap getProfileImageForNotification(final String profileImageUrl) {
		final TwittnukerApplication app = TwittnukerApplication.getInstance(this);
		ImagePreloader imagePreloader = new ImagePreloader(this, app.getImageLoader());
		final Resources res = getResources();
		final int w = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
		final int h = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
		final File profileImageFile = imagePreloader.getCachedImageFile(profileImageUrl);
		final Bitmap profile_image = profileImageFile != null && profileImageFile.isFile() ? BitmapFactory
				.decodeFile(profileImageFile.getPath()) : null;
		if (profile_image != null) return Bitmap.createScaledBitmap(profile_image, w, h, true);
				else return null;
	}

	private static String stripMentionText(final String text, final String my_screen_name) {
		if (text == null || my_screen_name == null) return text;
		final String temp = "@" + my_screen_name + " ";
		if (text.startsWith(temp)) return text.substring(temp.length());
		return text;
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
