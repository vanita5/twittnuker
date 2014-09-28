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
import android.support.v4.app.NotificationCompat.Builder;
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
import static de.vanita5.twittnuker.util.Utils.getAccountScreenName;
import static de.vanita5.twittnuker.util.Utils.getReasonablySmallTwitterProfileImage;
import static de.vanita5.twittnuker.util.Utils.isNotificationsSilent;

public class NotificationHelper implements Constants {

	private Context context;
	private ImagePreloader mImagePreloader;

	public NotificationHelper(final Context context) {
		this.context = context;
		final TwittnukerApplication app = TwittnukerApplication.getInstance(context);
		final DisplayImageOptions.Builder profileOptsBuilder = new DisplayImageOptions.Builder();
		profileOptsBuilder.cacheInMemory(true);
		profileOptsBuilder.cacheOnDisk(true);
		final DisplayImageOptions displayImageOptions = profileOptsBuilder.build();
		mImagePreloader = new ImagePreloader(context, app.getImageLoader(), displayImageOptions);
	}

	private List<NotificationContent> getCachedNotifications(final long argAccountId) {
		Cursor c = null;
		List<NotificationContent> results = new ArrayList<NotificationContent>();
		try {
			if (argAccountId <= 0) return null;
			final ContentResolver resolver = context.getContentResolver();
			final String where = PushNotifications.ACCOUNT_ID + " = " + argAccountId;
			c = resolver.query(PushNotifications.CONTENT_URI, PushNotifications.MATRIX_COLUMNS,
					where, null, PushNotifications.DEFAULT_SORT_ORDER);

			if (c == null || c.getCount() == 0) return null;
			c.moveToFirst();
			final int idxAccountId = c.getColumnIndex(PushNotifications.ACCOUNT_ID);
			final int idxMessage = c.getColumnIndex(PushNotifications.MESSAGE);
			final int idxTimestamp = c.getColumnIndex(PushNotifications.TIMESTAMP);
			final int idxFromUser = c.getColumnIndex(PushNotifications.FROM_USER);
			final int idxType = c.getColumnIndex(PushNotifications.NOTIFICATION_TYPE);

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
		} finally {
			if (c != null) c.close();
		}
		return results;
	}

	public void cachePushNotification(final NotificationContent notification) {
		List<NotificationContent> cache = getCachedNotifications(notification.getAccountId());
		if (cache != null && !cache.isEmpty() && cache.contains(notification)) return;
		final ContentResolver resolver = context.getContentResolver();
		final ContentValues values = new ContentValues();
		values.put(PushNotifications.ACCOUNT_ID, notification.getAccountId());
		values.put(PushNotifications.FROM_USER, notification.getFromUser());
		values.put(PushNotifications.MESSAGE, notification.getMessage());
		values.put(PushNotifications.NOTIFICATION_TYPE, notification.getType());
		values.put(PushNotifications.TIMESTAMP, notification.getTimestamp());
		resolver.insert(PushNotifications.CONTENT_URI, values);
	}

	public void deleteCachedNotifications(final long accountId, final String type) {
		final ContentResolver resolver = context.getContentResolver();
		final String where = PushNotifications.ACCOUNT_ID + " = " + accountId
				+ " AND " + PushNotifications.NOTIFICATION_TYPE + " = '" + type + "'";
		Cursor c = resolver.query(PushNotifications.CONTENT_URI, PushNotifications.MATRIX_COLUMNS,
				where, null, PushNotifications.DEFAULT_SORT_ORDER);

		// Only rebuild notifications if there are entries that will be removed
		if (c == null) {
			return;
		} else if (c.getCount() == 0) {
			c.close();
			return;
		} else {
			resolver.delete(PushNotifications.CONTENT_URI, where, null);
			rebuildNotification(accountId);

		}
	}

	private void rebuildNotification(final long accountId) {
		NotificationManager notificationManager = getNotificationManager();
		notificationManager.cancel(getAccountNotificationId(NOTIFICATION_ID_PUSH, accountId));

		List<NotificationContent> pendingNotifications = getCachedNotifications(accountId);
		long[] accountIdArray = { accountId };

		//we trigger rebuilding the notification by just calling buildNotificationByType()
		//with the last notification from the db
		if (pendingNotifications != null && !pendingNotifications.isEmpty()) {
			NotificationContent notification = pendingNotifications.get(0);
			AccountPreferences[] prefs = AccountPreferences.getAccountPreferences(context, accountIdArray);

			//Should always contains just one pref
			if (prefs.length == 1) {
				buildNotificationByType(notification, prefs[0], true);
			}
		}
	}

	public void buildNotificationByType(final NotificationContent notification, final AccountPreferences pref,
										final boolean rebuild) {
		final String type = notification.getType();
		final int notificationType = pref.getMentionsNotificationType();
		List<NotificationContent> pendingNotifications = getCachedNotifications(notification.getAccountId());
		final int notificationCount = pendingNotifications.size();

		Intent mainActionIntent = null;
		String contentText = null;
		String ticker = null;
		int smallicon = R.drawable.ic_stat_twittnuker;

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

		if (NotificationContent.NOTIFICATION_TYPE_MENTION.equals(type)) {
			contentText = stripMentionText(notification.getMessage(),
					getAccountScreenName(context, notification.getAccountId()));
			ticker = notification.getMessage();
			smallicon = R.drawable.ic_stat_mention;

			if (notificationCount == 1 && notification.getOriginalStatus() != null) {
				final Uri.Builder uriBuilder = new Uri.Builder();
				uriBuilder.scheme(SCHEME_TWITTNUKER);
				uriBuilder.authority(AUTHORITY_STATUS);
				uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(notification.getOriginalStatus().account_id));
				uriBuilder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(notification.getOriginalStatus().id));
				mainActionIntent = new Intent(Intent.ACTION_VIEW, uriBuilder.build());
				mainActionIntent.setExtrasClassLoader(context.getClassLoader());
				mainActionIntent.putExtra(EXTRA_STATUS, notification.getOriginalStatus());
				mainActionIntent.putExtra(EXTRA_TAB_TYPE, TAB_TYPE_MENTIONS_TIMELINE);

				//Reply Intent
				final Intent replyIntent = new Intent(INTENT_ACTION_REPLY);
				replyIntent.setExtrasClassLoader(context.getClassLoader());
				replyIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationType);
				replyIntent.putExtra(EXTRA_NOTIFICATION_ACCOUNT, pref.getAccountId());
				replyIntent.putExtra(EXTRA_STATUS, notification.getOriginalStatus());
				replyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				builder.addAction(R.drawable.ic_action_reply, context.getString(R.string.reply),
						PendingIntent.getActivity(context, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT));

				//TODO
				//Retweet Intent
				//Favorite Intent
			}
		} else if (NotificationContent.NOTIFICATION_TYPE_RETWEET.equals(type)) {
			contentText = context.getString(R.string.notification_new_retweet_single)
					+ ": " + notification.getMessage();
			ticker = contentText;
			smallicon = R.drawable.ic_stat_retweet;

			if (notificationCount == 1 && notification.getOriginalStatus() != null) {
				final Uri.Builder uriBuilder = new Uri.Builder();
				uriBuilder.scheme(SCHEME_TWITTNUKER);
				uriBuilder.authority(AUTHORITY_STATUS);
				uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(notification.getOriginalStatus().account_id));
				uriBuilder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(notification.getOriginalStatus().id));
				mainActionIntent = new Intent(Intent.ACTION_VIEW, uriBuilder.build());
				mainActionIntent.setExtrasClassLoader(context.getClassLoader());
				mainActionIntent.putExtra(EXTRA_STATUS, notification.getOriginalStatus());

				//Profile Intent
				final Uri.Builder viewProfileBuilder = new Uri.Builder();
				viewProfileBuilder.scheme(SCHEME_TWITTNUKER);
				viewProfileBuilder.authority(AUTHORITY_USER);
				viewProfileBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(notification.getOriginalStatus().account_id));
				viewProfileBuilder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(notification.getOriginalStatus().retweeted_by_id));
				final Intent viewProfileIntent = new Intent(Intent.ACTION_VIEW, viewProfileBuilder.build());
				viewProfileIntent.setPackage(APP_PACKAGE_NAME);
				builder.addAction(R.drawable.ic_action_profile, context.getString(R.string.view_user_profile),
						PendingIntent.getActivity(context, 0, viewProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			}
		} else if (NotificationContent.NOTIFICATION_TYPE_FAVORITE.equals(type)) {
			contentText = context.getString(R.string.notification_new_favorite_single)
					+ ": " + notification.getMessage();
			ticker = contentText;
			smallicon = R.drawable.ic_stat_favorite;

			if (notificationCount == 1 && notification.getOriginalStatus() != null) {
				final Uri.Builder uriBuilder = new Uri.Builder();
				uriBuilder.scheme(SCHEME_TWITTNUKER);
				uriBuilder.authority(AUTHORITY_STATUS);
				uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(notification.getOriginalStatus().account_id));
				uriBuilder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(notification.getOriginalStatus().id));
				mainActionIntent = new Intent(Intent.ACTION_VIEW, uriBuilder.build());
				mainActionIntent.setExtrasClassLoader(context.getClassLoader());
				mainActionIntent.putExtra(EXTRA_STATUS, notification.getOriginalStatus());

				//Profile Intent
				if (notification.getSourceUser() != null) {
					final Uri.Builder viewProfileBuilder = new Uri.Builder();
					viewProfileBuilder.scheme(SCHEME_TWITTNUKER);
					viewProfileBuilder.authority(AUTHORITY_USER);
					viewProfileBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(notification.getOriginalStatus().account_id));
					viewProfileBuilder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(notification.getSourceUser().getId()));
					final Intent viewProfileIntent = new Intent(Intent.ACTION_VIEW, viewProfileBuilder.build());
					viewProfileIntent.setPackage(APP_PACKAGE_NAME);
					builder.addAction(R.drawable.ic_action_profile, context.getString(R.string.view_user_profile),
							PendingIntent.getActivity(context, 0, viewProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT));
				}
			}
		} else if (NotificationContent.NOTIFICATION_TYPE_FOLLOWER.equals(type)) {
			contentText = "@" + notification.getFromUser() + " " + context.getString(R.string.notification_new_follower);
			ticker = contentText;
			smallicon = R.drawable.ic_stat_follower;

			if (notificationCount == 1 && notification.getSourceUser() != null) {
				final Uri.Builder uriBuilder = new Uri.Builder();
				uriBuilder.scheme(SCHEME_TWITTNUKER);
				uriBuilder.authority(AUTHORITY_STATUS);
				uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(pref.getAccountId()));
				uriBuilder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(notification.getSourceUser().getId()));
				mainActionIntent = new Intent(Intent.ACTION_VIEW, uriBuilder.build());
				mainActionIntent.setExtrasClassLoader(context.getClassLoader());
				mainActionIntent.putExtra(EXTRA_STATUS, notification.getOriginalStatus());
			}
		} else if (NotificationContent.NOTIFICATION_TYPE_DIRECT_MESSAGE.equals(type)) {
			contentText = notification.getMessage();
			ticker = contentText;
			smallicon = R.drawable.ic_stat_direct_message;

			if (notificationCount == 1 && notification.getOriginalMessage() != null) {
				final Uri.Builder uriBuilder = new Uri.Builder();
				uriBuilder.scheme(SCHEME_TWITTNUKER);
				uriBuilder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
				uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(notification.getOriginalMessage().account_id));
				uriBuilder.appendQueryParameter(QUERY_PARAM_RECIPIENT_ID, String.valueOf(notification.getOriginalMessage().sender_id));
				mainActionIntent = new Intent(Intent.ACTION_VIEW, uriBuilder.build());
				mainActionIntent.putExtra(EXTRA_TAB_TYPE, TAB_TYPE_DIRECT_MESSAGES);
				mainActionIntent.setExtrasClassLoader(context.getClassLoader());
			}
		} else if (NotificationContent.NOTIFICATION_TYPE_ERROR_420.equals(type)) {
			buildErrorNotification(420, pref);
		}
		if (contentText == null && ticker == null) return;

		if (mainActionIntent == null && notificationCount > 1) {
			mainActionIntent = new Intent(context, HomeActivity.class);
			mainActionIntent.setAction(Intent.ACTION_MAIN);
			mainActionIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		}
		builder.setContentIntent(PendingIntent.getActivity(context, 0, mainActionIntent,
				PendingIntent.FLAG_UPDATE_CURRENT));

		buildNotification(notification, pref, notificationType, notificationCount,
				pendingNotifications, contentText, ticker, smallicon, rebuild, builder);
	}

	private void buildNotification(final NotificationContent notification, final AccountPreferences pref,
								   final int notificationType, final int notificationCount,
								   List<NotificationContent> pendingNotifications, final String contentText,
								   final String ticker, final int smallicon, final boolean rebuild,
								   NotificationCompat.Builder builder) {

		builder.setContentTitle("@" + notification.getFromUser());
		builder.setContentText(contentText);
		if (!rebuild) builder.setTicker(ticker);
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
		} else if (notificationCount == 1) {
			final Bitmap profileImage = getProfileImageForNotification(
					getReasonablySmallTwitterProfileImage(notification.getProfileImageUrl()));

			builder.setLargeIcon(profileImage);
			builder.setSmallIcon(smallicon);
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
			return Html.fromHtml(String.format("<b>%s:</b> %s", nameEscaped, textEscaped));
		} else if (NotificationContent.NOTIFICATION_TYPE_RETWEET.equals(type)) {
			return Html.fromHtml(String.format("<b>%s " + context.getString(R.string.notification_new_retweet) + ":</b> %s",
					nameEscaped, textEscaped));
		} else if (NotificationContent.NOTIFICATION_TYPE_FAVORITE.equals(type)) {
			return Html.fromHtml(String.format("<b>%s " + context.getString(R.string.notification_new_favorite) + ":</b> %s",
					nameEscaped, textEscaped));
		} else if (NotificationContent.NOTIFICATION_TYPE_FOLLOWER.equals(type)) {
			return Html.fromHtml(String.format("<b>%s</b> " + context.getString(R.string.notification_new_follower),
					nameEscaped));
		} else if (NotificationContent.NOTIFICATION_TYPE_DIRECT_MESSAGE.equals(type)) {
			return Html.fromHtml(String.format("<b>%s:</b>", context.getString(R.string.notification_new_direct_message)) + " " + textEscaped);
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

	//TODO Fix this method. Cache is always empty?
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
