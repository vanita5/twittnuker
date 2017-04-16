/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.net.ConnectivityManagerCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mariotaku.sqliteqb.library.Expression;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.activity.HomeActivity;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.model.NotificationContent;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.provider.TwidereDataStore.PushNotifications;
import de.vanita5.twittnuker.receiver.NotificationActionReceiver;
import de.vanita5.twittnuker.util.dagger.GeneralComponent;

import static de.vanita5.twittnuker.util.Utils.getAccountNotificationId;

public class NotificationHelper implements Constants {

    private Context mContext;
    @Inject
    ActivityTracker mActivityTracker;
    private SharedPreferencesWrapper mSharedPreferences;

    public NotificationHelper(final Context context) {
        this.mContext = context;
        GeneralComponent.Companion.get(context).inject(this);
        mSharedPreferences = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    private List<NotificationContent> getCachedNotifications(final UserKey userKey) {
        if (userKey == null) return null;
        Cursor c = null;
        List<NotificationContent> results = new ArrayList<>();
        try {
            final ContentResolver resolver = mContext.getContentResolver();

            final String where = Expression.equalsArgs(PushNotifications.ACCOUNT_KEY).getSQL();
            final String[] whereArgs = {userKey.toString()};

            c = resolver.query(PushNotifications.CONTENT_URI, PushNotifications.MATRIX_COLUMNS,
                    where, whereArgs, PushNotifications.DEFAULT_SORT_ORDER);

            if (c == null || c.getCount() == 0) return null;
            c.moveToFirst();
            final int idxAccountKey = c.getColumnIndex(PushNotifications.ACCOUNT_KEY);
            final int idxObjectId = c.getColumnIndex(PushNotifications.OBJECT_ID);
            final int idxObjectUserId = c.getColumnIndex(PushNotifications.OBJECT_USER_ID);
            final int idxMessage = c.getColumnIndex(PushNotifications.MESSAGE);
            final int idxTimestamp = c.getColumnIndex(PushNotifications.TIMESTAMP);
            final int idxFromUser = c.getColumnIndex(PushNotifications.FROM_USER);
            final int idxType = c.getColumnIndex(PushNotifications.NOTIFICATION_TYPE);

            while (!c.isAfterLast()) {
                NotificationContent notification = new NotificationContent();
                notification.setAccountKey(UserKey.valueOf(c.getString(idxAccountKey)));
                notification.setObjectId(c.getString(idxObjectId));
                notification.setObjectUserKey(new UserKey(c.getString(idxObjectUserId), TwittnukerConstants.USER_TYPE_TWITTER_COM));
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
        List<NotificationContent> cache = getCachedNotifications(notification.getAccountKey());
        if (cache != null && !cache.isEmpty() && cache.contains(notification)) return;
        final ContentResolver resolver = mContext.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(PushNotifications.ACCOUNT_KEY, String.valueOf(notification.getAccountKey()));
        values.put(PushNotifications.OBJECT_ID, notification.getObjectId());
        values.put(PushNotifications.OBJECT_USER_ID, notification.getObjectUserKey().getId());
        values.put(PushNotifications.FROM_USER, notification.getFromUser());
        values.put(PushNotifications.MESSAGE, notification.getMessage());
        values.put(PushNotifications.NOTIFICATION_TYPE, notification.getType());
        values.put(PushNotifications.TIMESTAMP, notification.getTimestamp());
        resolver.insert(PushNotifications.CONTENT_URI, values);
    }

    public void deleteCachedNotifications(final UserKey userKey, final String type) {
        final ContentResolver resolver = mContext.getContentResolver();

        String where = Expression.equalsArgs(PushNotifications.ACCOUNT_KEY).getSQL();
        final String[] whereArgs = {userKey.toString()};

        if (type != null && !type.isEmpty()) {
            where += " AND " + PushNotifications.NOTIFICATION_TYPE + " = '" + type + "'";
        }
        Cursor c = resolver.query(PushNotifications.CONTENT_URI, PushNotifications.MATRIX_COLUMNS,
                where, whereArgs, PushNotifications.DEFAULT_SORT_ORDER);

        // Only rebuild notifications if there are entries that will be removed
        if (c == null) return;
        if (c.getCount() > 0) {
            resolver.delete(PushNotifications.CONTENT_URI, where, whereArgs);
            rebuildNotification(userKey);
        }
        c.close();
    }

    private void rebuildNotification(final UserKey userKey) {
        NotificationManager notificationManager = getNotificationManager();
        notificationManager.cancel(getAccountNotificationId(NOTIFICATION_ID_PUSH, userKey.getId()));

        List<NotificationContent> pendingNotifications = getCachedNotifications(userKey);
        UserKey[] userKeys = {userKey};

        //we trigger rebuilding the notification by just calling buildNotificationByType()
        //with the last notification from the db
        if (pendingNotifications != null && !pendingNotifications.isEmpty()) {
            NotificationContent notification = pendingNotifications.get(0);
            AccountPreferences[] prefs = AccountPreferences.Companion.getAccountPreferences(mContext, userKeys);

            //Should always contains just one pref
            if (prefs.length == 1) {
                buildNotificationByType(notification, prefs[0], true);
            }
        }
    }

    private ParcelableStatus getParcelableStatusDummy(NotificationContent notification,
                                                      boolean is_retweet) {
        ParcelableStatus status = new ParcelableStatus();
        status.id = notification.getObjectId();
        status.user_key = notification.getObjectUserKey();
        status.account_key = notification.getAccountKey();
        status.user_screen_name = notification.getFromUser();
        status.user_profile_image_url = notification.getProfileImageUrl();
        status.is_retweet = is_retweet;
        status.is_favorite = false;
        status.text_plain = notification.getMessage();
        status.text_unescaped = notification.getMessage();
        status.timestamp = notification.getTimestamp();

        if (is_retweet) {
            status.retweeted_by_user_key = notification.getObjectUserKey();
            status.retweeted_by_user_screen_name = notification.getFromUser();
        }

        return status;
    }

    public void buildNotificationByType(final NotificationContent notification, final AccountPreferences pref,
                                        final boolean rebuild) {
        final String type = notification.getType();
        final int notificationType = pref.getMentionsNotificationType();
        List<NotificationContent> pendingNotifications = getCachedNotifications(notification.getAccountKey());
        if (pendingNotifications == null) return;
        final int notificationCount = pendingNotifications.size();

        ParcelableStatus status = notification.getOriginalStatus();

        Intent mainActionIntent = null;
        String contentText = null;
        String ticker = null;
        int smallicon = R.drawable.ic_stat_twittnuker;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setCategory(NotificationCompat.CATEGORY_SOCIAL);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setColor(pref.getNotificationLightColor());

        switch (type) {
            case NotificationContent.NOTIFICATION_TYPE_MENTION: {
                contentText = notification.getMessage();
                ticker = notification.getMessage();
                smallicon = R.drawable.ic_stat_mention;

                if (notificationCount == 1) {
                    if (status == null) {
                        status = getParcelableStatusDummy(notification, false);
                    }
                    final Uri.Builder uriBuilder = new Uri.Builder();
                    uriBuilder.scheme(SCHEME_TWITTNUKER);
                    uriBuilder.authority(AUTHORITY_STATUS);
                    uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(status.account_key.getId()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status.id));
                    UriExtraUtils.addExtra(uriBuilder, "item_id", String.valueOf(status.id));
                    UriExtraUtils.addExtra(uriBuilder, "item_user_id", String.valueOf(status.user_key.getId()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_FROM_NOTIFICATION, String.valueOf(true));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE, AUTHORITY_STATUS);
                    mainActionIntent = new Intent(Intent.ACTION_VIEW);
                    mainActionIntent.setPackage(BuildConfig.APPLICATION_ID);
                    mainActionIntent.setData(uriBuilder.build());

                    //Reply Intent
                    final Intent replyIntent = new Intent(INTENT_ACTION_REPLY);
                    replyIntent.setExtrasClassLoader(mContext.getClassLoader());
                    replyIntent.putExtra(EXTRA_NOTIFICATION_ID, getAccountNotificationId(NOTIFICATION_ID_PUSH,
                            notification.getAccountKey().getId()));
                    replyIntent.putExtra(EXTRA_STATUS, status);
                    replyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    builder.addAction(R.drawable.ic_action_reply, mContext.getString(R.string.action_reply),
                            PendingIntent.getActivity(mContext, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT));

                    //Retweet Intent
                    builder.addAction(R.drawable.ic_action_retweet, mContext.getString(R.string.action_retweet),
                            getRetweetIntent(status));

                    //Like Intent
                    if (mSharedPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK, false)) {
                        builder.addAction(R.drawable.ic_action_star, mContext.getString(R.string.action_favorite),
                                getFavoriteIntent(status));
                    } else {
                        builder.addAction(R.drawable.ic_action_heart, mContext.getString(R.string.action_like),
                                getFavoriteIntent(status));
                    }
                }
                break;
            }
            case NotificationContent.NOTIFICATION_TYPE_RETWEET: {
                contentText = mContext.getString(R.string.notification_new_retweet_single)
                        + ": " + notification.getMessage();
                ticker = contentText;
                smallicon = R.drawable.ic_stat_retweet;

                if (notificationCount == 1) {
                    if (status == null) {
                        status = getParcelableStatusDummy(notification, true);
                    }
                    final Uri.Builder uriBuilder = new Uri.Builder();
                    uriBuilder.scheme(SCHEME_TWITTNUKER);
                    uriBuilder.authority(AUTHORITY_STATUS);
                    uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(status.account_key.getId()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status.id));
                    UriExtraUtils.addExtra(uriBuilder, "item_id", String.valueOf(status.id));
                    UriExtraUtils.addExtra(uriBuilder, "item_user_id", status.retweeted_by_user_key != null ? status.retweeted_by_user_key.getId() : null);
                    uriBuilder.appendQueryParameter(QUERY_PARAM_FROM_NOTIFICATION, String.valueOf(true));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE, AUTHORITY_STATUS);
                    mainActionIntent = new Intent(Intent.ACTION_VIEW);
                    mainActionIntent.setPackage(BuildConfig.APPLICATION_ID);
                    mainActionIntent.setData(uriBuilder.build());

                    //Profile Intent
                    final Uri.Builder viewProfileBuilder = new Uri.Builder();
                    viewProfileBuilder.scheme(SCHEME_TWITTNUKER);
                    viewProfileBuilder.authority(AUTHORITY_USER);
                    viewProfileBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, status.account_key.toString());
                    viewProfileBuilder.appendQueryParameter(QUERY_PARAM_USER_KEY, status.retweeted_by_user_key != null ? status.retweeted_by_user_key.toString() : null);
                    final Intent viewProfileIntent = new Intent(Intent.ACTION_VIEW, viewProfileBuilder.build());
                    viewProfileIntent.setPackage(TWITTNUKER_PACKAGE_NAME);
                    builder.addAction(R.drawable.ic_action_profile, mContext.getString(R.string.view_user_profile),
                            PendingIntent.getActivity(mContext, 0, viewProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                }
                break;
            }
            case NotificationContent.NOTIFICATION_TYPE_QUOTE: {
                contentText = mContext.getString(R.string.notification_new_quote_single)
                        + ": " + notification.getMessage();
                ticker = contentText;
                smallicon = R.drawable.ic_stat_quote;

                if (notificationCount == 1) {
                    if (status == null) {
                        status = getParcelableStatusDummy(notification, false);
                    }
                    final Uri.Builder uriBuilder = new Uri.Builder();
                    uriBuilder.scheme(SCHEME_TWITTNUKER);
                    uriBuilder.authority(AUTHORITY_QUOTE);
                    uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(status.account_key.getId()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status.id));
                    UriExtraUtils.addExtra(uriBuilder, "item_id", String.valueOf(status.id));
                    UriExtraUtils.addExtra(uriBuilder, "item_user_id", String.valueOf(status.user_key.getId()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_FROM_NOTIFICATION, String.valueOf(true));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE, AUTHORITY_QUOTE);
                    mainActionIntent = new Intent(Intent.ACTION_VIEW);
                    mainActionIntent.setPackage(BuildConfig.APPLICATION_ID);
                    mainActionIntent.setData(uriBuilder.build());

                    //Reply Intent
                    final Intent replyIntent = new Intent(INTENT_ACTION_REPLY);
                    replyIntent.setExtrasClassLoader(mContext.getClassLoader());
                    replyIntent.putExtra(EXTRA_NOTIFICATION_ID, getAccountNotificationId(NOTIFICATION_ID_PUSH,
                            notification.getAccountKey().getId()));
                    replyIntent.putExtra(EXTRA_STATUS, status);
                    replyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    builder.addAction(R.drawable.ic_action_reply, mContext.getString(R.string.action_reply),
                            PendingIntent.getActivity(mContext, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT));

                    //Retweet Intent
                    builder.addAction(R.drawable.ic_action_retweet, mContext.getString(R.string.action_retweet),
                            getRetweetIntent(status));

                    //Like Intent
                    if (mSharedPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK, false)) {
                        builder.addAction(R.drawable.ic_action_star, mContext.getString(R.string.action_favorite),
                                getFavoriteIntent(status));
                    } else {
                        builder.addAction(R.drawable.ic_action_heart, mContext.getString(R.string.action_like),
                                getFavoriteIntent(status));
                    }
                }
                break;
            }
            case NotificationContent.NOTIFICATION_TYPE_FAVORITE: {
                if (mSharedPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK, false)) {
                    contentText = mContext.getString(R.string.notification_new_favorite_single)
                            + ": " + notification.getMessage();
                    smallicon = R.drawable.ic_stat_favorite;
                } else {
                    contentText = mContext.getString(R.string.notification_new_like_single)
                            + ": " + notification.getMessage();
                    smallicon = R.drawable.ic_stat_heart;
                }
                ticker = contentText;

                if (notificationCount == 1) {
                    if (status == null) {
                        status = getParcelableStatusDummy(notification, false);
                    }
                    final Uri.Builder uriBuilder = new Uri.Builder();
                    uriBuilder.scheme(SCHEME_TWITTNUKER);
                    uriBuilder.authority(AUTHORITY_STATUS);
                    uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(status.account_key.getId()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status.id));
                    UriExtraUtils.addExtra(uriBuilder, "item_id", String.valueOf(status.id));
                    UriExtraUtils.addExtra(uriBuilder, "item_user_id", String.valueOf(status.user_key.getId()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_FROM_NOTIFICATION, String.valueOf(true));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE, AUTHORITY_STATUS);
                    mainActionIntent = new Intent(Intent.ACTION_VIEW);
                    mainActionIntent.setPackage(BuildConfig.APPLICATION_ID);
                    mainActionIntent.setData(uriBuilder.build());

                    //Profile Intent
                    if (notification.getSourceUser() != null) {
                        final Uri.Builder viewProfileBuilder = new Uri.Builder();
                        viewProfileBuilder.scheme(SCHEME_TWITTNUKER);
                        viewProfileBuilder.authority(AUTHORITY_USER);
                        viewProfileBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, notification.getAccountKey().toString());
                        viewProfileBuilder.appendQueryParameter(QUERY_PARAM_USER_KEY, notification.getObjectUserKey().toString());
                        final Intent viewProfileIntent = new Intent(Intent.ACTION_VIEW, viewProfileBuilder.build());
                        viewProfileIntent.setPackage(TWITTNUKER_PACKAGE_NAME);
                        builder.addAction(R.drawable.ic_action_profile, mContext.getString(R.string.view_user_profile),
                                PendingIntent.getActivity(mContext, 0, viewProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                    }
                }
                break;
            }
            case NotificationContent.NOTIFICATION_TYPE_FOLLOWER: {
                contentText = "@" + notification.getFromUser() + " " + mContext.getString(R.string.notification_new_follower);
                ticker = contentText;
                smallicon = R.drawable.ic_stat_follower;

                if (notificationCount == 1) {
                    final Uri.Builder uriBuilder = new Uri.Builder();
                    uriBuilder.scheme(SCHEME_TWITTNUKER);
                    uriBuilder.authority(AUTHORITY_USER);
                    uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, notification.getAccountKey().toString());
                    uriBuilder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, notification.getFromUser());
                    uriBuilder.appendQueryParameter(QUERY_PARAM_USER_KEY, notification.getObjectUserKey().toString());
//                    UriExtraUtils.addExtra(uriBuilder, "item_id", String.valueOf(notification.getFromUser()));
                    UriExtraUtils.addExtra(uriBuilder, "item_user_id", notification.getObjectUserKey().getId());
                    uriBuilder.appendQueryParameter(QUERY_PARAM_FROM_NOTIFICATION, String.valueOf(true));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE, AUTHORITY_USER);
                    mainActionIntent = new Intent(Intent.ACTION_VIEW);
                    mainActionIntent.setPackage(BuildConfig.APPLICATION_ID);
                    mainActionIntent.setData(uriBuilder.build());
                }
                break;
            }
            case NotificationContent.NOTIFICATION_TYPE_DIRECT_MESSAGE: {
                contentText = notification.getMessage();
                ticker = contentText;
                smallicon = R.drawable.ic_stat_direct_message;

                if (notificationCount == 1) {
                    final Uri.Builder uriBuilder = new Uri.Builder();
                    uriBuilder.scheme(SCHEME_TWITTNUKER);
                    uriBuilder.authority(AUTHORITY_MESSAGES);
                    uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(notification.getAccountKey()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_CONVERSATION_ID, notification.getObjectId());
//                    UriExtraUtils.addExtra(uriBuilder, "item_id", String.valueOf(notification.getFromUser()));
                    UriExtraUtils.addExtra(uriBuilder, "item_user_id", notification.getObjectUserKey().getId());
                    uriBuilder.appendQueryParameter(QUERY_PARAM_FROM_NOTIFICATION, String.valueOf(true));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                    uriBuilder.appendQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE, AUTHORITY_MESSAGES);
                    mainActionIntent = new Intent(Intent.ACTION_VIEW);
                    mainActionIntent.setPackage(BuildConfig.APPLICATION_ID);
                    mainActionIntent.setData(uriBuilder.build());
                }
                break;
            }
            case NotificationContent.NOTIFICATION_TYPE_ERROR_420: {
                buildErrorNotification(420, pref);
                break;
            }
            default:
                return;
        }
        if (contentText == null) return;

        if (mainActionIntent == null) {
            mainActionIntent = new Intent(mContext, HomeActivity.class);
            mainActionIntent.setAction(Intent.ACTION_MAIN);
            mainActionIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        }
        builder.setContentIntent(PendingIntent.getActivity(mContext, 0, mainActionIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        buildNotification(notification, pref, notificationType, notificationCount,
                pendingNotifications, contentText, ticker, smallicon, rebuild, builder);
        sendPebbleNotification(ticker);
    }

    private void buildNotification(final NotificationContent notification, final AccountPreferences pref,
                                   final int notificationType, final int notificationCount,
                                   List<NotificationContent> pendingNotifications, final String contentText,
                                   final String ticker, final int smallicon, final boolean rebuild,
                                   NotificationCompat.Builder builder) {

        builder.setContentTitle("@" + notification.getFromUser());
        builder.setContentText(contentText);
        if (!rebuild) builder.setTicker(ticker);
        builder.setDeleteIntent(getDeleteIntent(notification.getAccountKey()));
        builder.setAutoCancel(true);
        builder.setWhen(notification.getTimestamp());

        if (notificationCount > 1) {
            builder.setSmallIcon(R.drawable.ic_stat_twittnuker);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(notificationCount + " " + mContext.getString(R.string.notification_new_interactions));

            for (NotificationContent pendingNotification : pendingNotifications) {
                Spanned line = getInboxLineByType(pendingNotification);
                if (line != null) inboxStyle.addLine(line);
            }
            inboxStyle.setSummaryText("@" + DataStoreUtils.INSTANCE.getAccountScreenName(mContext, notification.getAccountKey()));
            builder.setNumber(notificationCount);
            builder.setStyle(inboxStyle);
        } else if (notificationCount == 1) {
            final Bitmap profileImage = getProfileImageForNotification(notification.getProfileImageUrl());

            builder.setLargeIcon(profileImage);
            builder.setSmallIcon(smallicon);
            final NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(builder);
            bigTextStyle.bigText(contentText);
            builder.setStyle(bigTextStyle);
        }

        int defaults = 0;
        if (isNotificationAudible()) {
            if (AccountPreferences.Companion.isNotificationHasRingtone(notificationType)) {
                final Uri ringtone = pref.getNotificationRingtone();
                builder.setSound(ringtone);
            }
            if (AccountPreferences.Companion.isNotificationHasVibration(notificationType)) {
                defaults |= Notification.DEFAULT_VIBRATE;
            } else {
                defaults &= ~Notification.DEFAULT_VIBRATE;
            }
            if (AccountPreferences.Companion.isNotificationHasLight(notificationType)) {
                final int color = pref.getNotificationLightColor();
                builder.setLights(color, 1000, 2000);
            }
            builder.setDefaults(defaults);
        }
        NotificationManager notificationManager = getNotificationManager();
        notificationManager.notify(getAccountNotificationId(NOTIFICATION_ID_PUSH,
                notification.getAccountKey().getId()), builder.build());
    }

    private void buildErrorNotification(final int type, final AccountPreferences pref) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
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
        if (isNotificationAudible()) {
            if (AccountPreferences.Companion.isNotificationHasRingtone(pref.getMentionsNotificationType())) { //TODO Settings for error messages
                final Uri ringtone = pref.getNotificationRingtone();
                builder.setSound(ringtone, Notification.STREAM_DEFAULT);
            }
            if (AccountPreferences.Companion.isNotificationHasVibration(pref.getMentionsNotificationType())) {
                defaults |= Notification.DEFAULT_VIBRATE;
            } else {
                defaults &= ~Notification.DEFAULT_VIBRATE;
            }
            if (AccountPreferences.Companion.isNotificationHasLight(pref.getMentionsNotificationType())) {
                final int color = pref.getNotificationLightColor();
                builder.setLights(color, 1000, 2000);
            }
            builder.setDefaults(defaults);
        }
        NotificationManager notificationManager = getNotificationManager();
        notificationManager.notify(NOTIFICATION_ID_PUSH_ERROR, builder.build());
    }

    /**
     * Send notifications to Pebble smartwatches
     */
    private void sendPebbleNotification(final String message) {
        if (mSharedPreferences.getBoolean(KEY_PEBBLE_NOTIFICATIONS, false)
                && !TextUtils.isEmpty(message)) {

            final String app_name = mContext.getString(R.string.app_name);

            final HashMap<String, String> data = new HashMap<>();
            data.put("title", app_name);
            data.put("body", message);

            final JSONObject jsonData = new JSONObject(data);

            final String notificationData = new JSONArray().put(jsonData).toString();

            final Intent intent = new Intent(INTENT_ACTION_PEBBLE_NOTIFICATION);
            intent.putExtra("messageType", "PEBBLE_ALERT");
            intent.putExtra("sender", app_name);
            intent.putExtra("notificationData", notificationData);

            mContext.getApplicationContext().sendBroadcast(intent);
        }
    }

    private Spanned getInboxLineByType(final NotificationContent pendingNotification) {
        final String type = pendingNotification.getType();
        final String nameEscaped = HtmlEscapeHelper.escape("@" + pendingNotification.getFromUser());
        final String textEscaped = HtmlEscapeHelper.escape(pendingNotification.getMessage());
        if (NotificationContent.NOTIFICATION_TYPE_MENTION.equals(type)) {
            return Html.fromHtml(String.format("<b>%s:</b> %s", nameEscaped, textEscaped));
        } else if (NotificationContent.NOTIFICATION_TYPE_RETWEET.equals(type)) {
            return Html.fromHtml(String.format("<b>%s %s:</b> %s",
                    nameEscaped, mContext.getString(R.string.notification_new_retweet), textEscaped));
        } else if (NotificationContent.NOTIFICATION_TYPE_FAVORITE.equals(type)) {
            if (mSharedPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK, false)) {
                return Html.fromHtml(String.format("<b>%s %s:</b> %s",
                        nameEscaped, mContext.getString(R.string.notification_new_favorite),textEscaped));
            } else {
                return Html.fromHtml(String.format("<b>%s %s:</b> %s",
                        nameEscaped, mContext.getString(R.string.notification_new_like), textEscaped));
            }
        } else if (NotificationContent.NOTIFICATION_TYPE_FOLLOWER.equals(type)) {
            return Html.fromHtml(String.format("<b>%s</b> %s",
                    nameEscaped, mContext.getString(R.string.notification_new_follower)));
        } else if (NotificationContent.NOTIFICATION_TYPE_DIRECT_MESSAGE.equals(type)) {
            return Html.fromHtml(String.format("<b>%s:</b> %s", mContext.getString(R.string.notification_new_direct_message), textEscaped));
        } else if (NotificationContent.NOTIFICATION_TYPE_QUOTE.equals(type)) {
            return Html.fromHtml(String.format("<b>%s %s:</b> %s",
                    nameEscaped, mContext.getString(R.string.notification_new_quote), textEscaped));
        }
        return null;
    }

    private static String stripMentionText(final String text, final String my_screen_name) {
        if (text == null || my_screen_name == null) return text;
        final String temp = "@" + my_screen_name + " ";
        if (text.startsWith(temp)) return text.substring(temp.length());
        return text;
    }

    private PendingIntent getDeleteIntent(final UserKey accountKey) {
        Intent intent = new Intent(mContext, NotificationActionReceiver.class);
        intent.setAction(INTENT_ACTION_PUSH_NOTIFICATION_CLEARED);
        intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent getRetweetIntent(final ParcelableStatus status) {
        Intent intent = new Intent(mContext, NotificationActionReceiver.class);
        intent.setAction(INTENT_ACTION_RETWEET);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_NOTIFICATION_ID, getAccountNotificationId(NOTIFICATION_ID_PUSH,
                status.account_key.getId()));
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent getFavoriteIntent(final ParcelableStatus status) {
        Intent intent = new Intent(mContext, NotificationActionReceiver.class);
        intent.setAction(INTENT_ACTION_FAVORITE);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_NOTIFICATION_ID, getAccountNotificationId(NOTIFICATION_ID_PUSH,
                status.account_key.getId()));
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private Bitmap getProfileImageForNotification(final String profileImageUrl) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        final Resources res = mContext.getResources();
        final int w = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        final int h = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

        Bitmap profileImage = null;
        if (ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager)) {
            try {
                profileImage = Glide.with(mContext).load(profileImageUrl).asBitmap().into(w, h).get();
            } catch (InterruptedException e) {
                DebugLog.d(LOGTAG, "getProfileImageForNotification", e);
            } catch (ExecutionException e) {
                DebugLog.d(LOGTAG, "getProfileImageForNotification", e);
            }
        }
        return (profileImage != null) ? Bitmap.createScaledBitmap(profileImage, w, h, true) : null;
    }

    private boolean isNotificationAudible() {
        return !mActivityTracker.isHomeActivityStarted();
    }
}
