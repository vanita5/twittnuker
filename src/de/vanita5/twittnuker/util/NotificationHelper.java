package de.vanita5.twittnuker.util;

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
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.HomeActivity;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.model.NotificationContent;
import de.vanita5.twittnuker.provider.TweetStore.PushNotifications;
import de.vanita5.twittnuker.receiver.ClearNotificationReceiver;

import static de.vanita5.twittnuker.util.Utils.getAccountNotificationId;
import static de.vanita5.twittnuker.util.Utils.getAccountProfileImage;
import static de.vanita5.twittnuker.util.Utils.getAccountScreenName;
import static de.vanita5.twittnuker.util.Utils.getReasonablySmallTwitterProfileImage;
import static de.vanita5.twittnuker.util.Utils.isNotificationsSilent;

public class NotificationHelper implements Constants {

	private Context context;
	private TwittnukerApplication app;
	private ImagePreloader mImagePreloader;

	public NotificationHelper(final Context context) {
		this.context = context;
		app = TwittnukerApplication.getInstance(context);
		final DisplayImageOptions.Builder profileOptsBuilder = new DisplayImageOptions.Builder();
		profileOptsBuilder.cacheInMemory(true);
		profileOptsBuilder.cacheOnDisk(true);
		final DisplayImageOptions displayImageOptions = profileOptsBuilder.build();
		mImagePreloader = new ImagePreloader(context, app.getImageLoader(), displayImageOptions);
	}

	public void cachePushNotification(final NotificationContent notification) {
		final ContentResolver resolver = context.getContentResolver();
		final ContentValues values = new ContentValues();
		values.put(PushNotifications.ACCOUNT_ID, notification.getAccountId());
		values.put(PushNotifications.FROM_USER, notification.getFromUser());
		values.put(PushNotifications.MESSAGE, notification.getMessage());
		values.put(PushNotifications.NOTIFICATION_TYPE, notification.getType());
		values.put(PushNotifications.TIMESTAMP, notification.getTimestamp());
		resolver.insert(PushNotifications.CONTENT_URI, values);
	}

	public void buildNotificationByType(final NotificationContent notification, final long fromUserId,
										final AccountPreferences pref) {
		final String type = notification.getType();
		final int notificationType = pref.getMentionsNotificationType();
		List<NotificationContent> pendingNotifications = getCachedNotifications(notification.getAccountId());
		final int notificationCount = pendingNotifications.size();

		String contentText = null;
		String ticker = null;
		int smallicon = R.drawable.ic_stat_twittnuker;

		if (NotificationContent.NOTIFICATION_TYPE_MENTION.equals(type)) {
			contentText = stripMentionText(notification.getMessage(),
					getAccountScreenName(context, notification.getAccountId()));
			ticker = notification.getMessage();
			smallicon = R.drawable.ic_stat_mention;
		} else if (NotificationContent.NOTIFICATION_TYPE_RETWEET.equals(type)) {
			contentText = context.getString(R.string.notification_new_retweet_single)
					+ ": " + notification.getMessage();
			ticker = contentText; //TODO Should we really add the message to the ticker? We could
			smallicon = R.drawable.ic_stat_retweet;
		} else if (NotificationContent.NOTIFICATION_TYPE_FAVORITE.equals(type)) {
			contentText = context.getString(R.string.notification_new_favorite_single)
					+ ": " + notification.getMessage();
			ticker = contentText; //TODO Should we really add the message to the ticker?
			smallicon = R.drawable.ic_stat_favorite;
		} else if (NotificationContent.NOTIFICATION_TYPE_FOLLOWER.equals(type)) {
			contentText = "@" + notification.getFromUser() + " " + context.getString(R.string.notification_new_follower);
			ticker = contentText;
			smallicon = R.drawable.ic_stat_follower;
		} else if (NotificationContent.NOTIFICATION_TYPE_ERROR_420.equals(type)) {
			buildErrorNotification(420, pref);
		}
		if (contentText == null && ticker == null) return;
		buildNotification(notification, pref, fromUserId, notificationType, notificationCount,
				pendingNotifications, contentText, ticker, smallicon);
	}

	private List<NotificationContent> getCachedNotifications(final long argAccountId) {
		if (argAccountId <= 0) return null;
		final ContentResolver resolver = context.getContentResolver();
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

		List<NotificationContent> results = new ArrayList<NotificationContent>();
		while(!c.isAfterLast()) {
			NotificationContent notification = new NotificationContent();
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

	private void buildNotification(final NotificationContent notification, final AccountPreferences pref,
								   final long fromUserId,
								   final int notificationType, final int notificationCount,
								   List<NotificationContent> pendingNotifications, final String contentText,
								   final String ticker, final int smallicon) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

		builder.setContentTitle("@" + notification.getFromUser());
		builder.setContentText(contentText);
		builder.setTicker(ticker);
		builder.setDeleteIntent(getDeleteIntent(notification.getAccountId()));
		builder.setAutoCancel(true);
		builder.setWhen(notification.getTimestamp());

		if (notificationCount > 1) {
			builder.setSmallIcon(R.drawable.ic_stat_twittnuker);
			NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			inboxStyle.setBigContentTitle(notificationCount + " " + context.getString(R.string.notification_new_interactions));

			for (NotificationContent pendingNotification : pendingNotifications) {
				Spanned line = getInboxLineByType(pendingNotification);
				if (line != null) inboxStyle.addLine(line);
			}
			inboxStyle.setSummaryText("@" + getAccountScreenName(context, notification.getAccountId()));
			builder.setNumber(notificationCount);
			builder.setStyle(inboxStyle);
		} else {
			String imageUrl = getAccountProfileImage(context, fromUserId);
			imageUrl = getReasonablySmallTwitterProfileImage(imageUrl);
			final Bitmap profileImage = getProfileImageForNotification(imageUrl);

			builder.setLargeIcon(profileImage);
			builder.setSmallIcon(smallicon);
			//TODO add actions for notification type
			//builder.addAction(R.drawable.ic_action_reply, getString(R.string.reply),
			//		PendingIntent.getActivity(this, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			final NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(builder);
			bigTextStyle.bigText(contentText);
			builder.setStyle(bigTextStyle);
		}

		int defaults = 0;
		if (!isNotificationsSilent(context)) {
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

		Intent result = new Intent(context, HomeActivity.class);
		result.setAction(Intent.ACTION_MAIN);
		result.addCategory(Intent.CATEGORY_LAUNCHER);
		result.putExtra(EXTRA_TAB_TYPE, TAB_TYPE_MENTIONS_TIMELINE);
		if (notificationCount == 1) {
			//TODO Go directly to the mention (see TwidereDataProvider)? We need a ParcelableStatus for this...
			//Load the status?
		}

		//TaskStackBuilder: This ensures that navigating backward from the Activity leads out of your application
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(HomeActivity.class);
		stackBuilder.addNextIntent(result);
		PendingIntent resultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setContentIntent(resultIntent);

		NotificationManager notificationManager = getNotificationManager();
		notificationManager.notify(getAccountNotificationId(NOTIFICATION_ID_PUSH,
				notification.getAccountId()), builder.build());
	}

	private void buildErrorNotification(final int type, final AccountPreferences pref) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle("Error!");
		switch (type) {
			case 420:
				builder.setContentText("Your account has been logging in too often.\nThe stream has been disconnected by Twitter, so you won't receive Push Notifications for some time.");
				builder.setTicker("Error: Push has been halted...");
				break;
			default:
				break;
		}
		builder.setSmallIcon(R.drawable.ic_stat_info);
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		int defaults = 0;
		if (!isNotificationsSilent(context)) {
			if (AccountPreferences.isNotificationHasRingtone(pref.getMentionsNotificationType())) { //TODO Settings for error messages
				final Uri ringtone = pref.getNotificationRingtone();
				builder.setSound(ringtone, Notification.STREAM_DEFAULT);
			}
			if (AccountPreferences.isNotificationHasVibration(pref.getMentionsNotificationType())) {
				defaults |= Notification.DEFAULT_VIBRATE;
			} else {
				defaults &= ~Notification.DEFAULT_VIBRATE;
			}
			if (AccountPreferences.isNotificationHasLight(pref.getMentionsNotificationType())) {
				final int color = pref.getNotificationLightColor();
				builder.setLights(color, 1000, 2000);
			}
			builder.setDefaults(defaults);
		}
		NotificationManager notificationManager = getNotificationManager();
		notificationManager.notify(NOTIFICATION_ID_PUSH_ERROR, builder.build());
	}

	private Spanned getInboxLineByType(final NotificationContent pendingNotification) {
		final String type = pendingNotification.getType();
		final String nameEscaped = HtmlEscapeHelper.escape("@" + pendingNotification.getFromUser());
		final String textEscaped = HtmlEscapeHelper.escape(pendingNotification.getMessage());
		if (NotificationContent.NOTIFICATION_TYPE_MENTION.equals(type)) {
			return Html.fromHtml(String.format("<b>%s</b>: %s", nameEscaped, textEscaped));
		} else if (NotificationContent.NOTIFICATION_TYPE_RETWEET.equals(type)) {
			return Html.fromHtml(String.format("<b>%s " + context.getString(R.string.notification_new_retweet) + "</b>: %s",
					nameEscaped, textEscaped));
		} else if (NotificationContent.NOTIFICATION_TYPE_FAVORITE.equals(type)) {
			return Html.fromHtml(String.format("<b>%s " + context.getString(R.string.notification_new_favorite) + "</b>: %s",
					nameEscaped, textEscaped));
		} else if (NotificationContent.NOTIFICATION_TYPE_FOLLOWER.equals(type)) {
			return Html.fromHtml(String.format("<b>%s</b> " + context.getString(R.string.notification_new_follower),
					nameEscaped));
		}
		return null;
	}

	private static String stripMentionText(final String text, final String my_screen_name) {
		if (text == null || my_screen_name == null) return text;
		final String temp = "@" + my_screen_name + " ";
		if (text.startsWith(temp)) return text.substring(temp.length());
		return text;
	}

	private PendingIntent getDeleteIntent(final long accountId) {
		Intent intent = new Intent(context, ClearNotificationReceiver.class);
		intent.setAction(INTENT_ACTION_PUSH_NOTIFICATION_CLEARED);
		intent.putExtra(EXTRA_USER_ID, accountId);
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	private NotificationManager getNotificationManager() {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private Bitmap getProfileImageForNotification(final String profileImageUrl) {
		final Resources res = context.getResources();
		final int w = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
		final int h = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
		final File profileImageFile = mImagePreloader.getCachedImageFile(profileImageUrl);

		final Bitmap profileImage = profileImageFile != null && profileImageFile.isFile() ? BitmapFactory
					.decodeFile(profileImageFile.getPath()) : null;
		return (profileImage != null) ? Bitmap.createScaledBitmap(profileImage, w, h, true) : null;
	}
}
