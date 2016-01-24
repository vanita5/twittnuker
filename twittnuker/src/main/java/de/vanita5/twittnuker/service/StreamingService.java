/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.widget.Toast;

import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.sqliteqb.library.Expression;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.HomeActivity;
import de.vanita5.twittnuker.api.twitter.model.Warning;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.TwitterUserStream;
import de.vanita5.twittnuker.api.twitter.UserStreamCallback;
import de.vanita5.twittnuker.api.twitter.model.DirectMessage;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.api.twitter.model.StatusDeletionNotice;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.api.twitter.model.UserList;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.model.NotificationContent;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
import de.vanita5.twittnuker.provider.TwidereDataStore.DirectMessages;
import de.vanita5.twittnuker.provider.TwidereDataStore.Mentions;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.DebugModeUtils;
import de.vanita5.twittnuker.util.NotificationHelper;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.TwidereArrayUtils;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.dagger.DependencyHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class StreamingService extends Service implements Constants {

    private final LongSparseArray<UserStreamCallback> mCallbacks = new LongSparseArray<>();
    private ContentResolver mResolver;

    private SharedPreferences mPreferences;
    private NotificationManager mNotificationManager;

    private AsyncTwitterWrapper mTwitterWrapper;

    private long[] mAccountIds;
    private boolean mNetworkOK;

    private static final Uri[] STATUSES_URIS = new Uri[]{Statuses.CONTENT_URI, Mentions.CONTENT_URI};
    private static final Uri[] MESSAGES_URIS = new Uri[]{DirectMessages.Inbox.CONTENT_URI,
            DirectMessages.Outbox.CONTENT_URI};

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

//            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
//                if (BuildConfig.DEBUG) Log.d(Constants.LOGTAG, "StreamingService: Received NETWORK_STATE_CHANGED_ACTION");
//                NetworkInfo wifi = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//
//                if ((wifi.isConnected() && !mNetworkOK) || mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false)) {
//                    mNetworkOK = true;
//                    Log.d(LOGTAG, "Wifi OK");
//                    initStreaming();
//                } else {
//                    mNetworkOK = false;
//                    Log.d(LOGTAG, "Wifi BAD");
//                    clearTwitterInstances();
//                }
//            } else
            if (BROADCAST_REFRESH_STREAMING_SERVICE.equals(action)) {
                Log.d(LOGTAG, "Refresh Streaming Service");
                clearTwitterInstances();
                initStreaming();
            }
        }
    };

    private final ContentObserver mAccountChangeObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(final boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(final boolean selfChange, final Uri uri) {
            if (!TwidereArrayUtils.contentMatch(mAccountIds, DataStoreUtils.getActivatedAccountIds(StreamingService.this))) {
                clearTwitterInstances();
                initStreaming();
            }
        }

    };

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = SharedPreferencesWrapper.getInstance(this, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mResolver = getContentResolver();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOGTAG, "Stream service started.");
        }
        DependencyHolder holder = DependencyHolder.get(this);
        mTwitterWrapper = holder.getAsyncTwitterWrapper();

//        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        mNetworkOK = wifi.isConnected();

        IntentFilter filter = new IntentFilter();
//        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(BROADCAST_REFRESH_STREAMING_SERVICE);
        registerReceiver(mStateReceiver, filter);

        initStreaming();
        mResolver.registerContentObserver(Accounts.CONTENT_URI, true, mAccountChangeObserver);
    }

    @Override
    public void onDestroy() {
        clearTwitterInstances();
        unregisterReceiver(mStateReceiver);
        mResolver.unregisterContentObserver(mAccountChangeObserver);
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOGTAG, "Stream service stopped.");
        }
        super.onDestroy();
        DebugModeUtils.watchReferenceLeak(this);
    }

    private void clearTwitterInstances() {
        for (int i = 0, j = mCallbacks.size(); i < j; i++) {
            new Thread(new ShutdownStreamTwitterRunnable(mCallbacks.valueAt(i))).start();
        }
        mCallbacks.clear();
        updateStreamState();
    }

    @SuppressWarnings("deprecation")
    private void initStreaming() {
        if (!mPreferences.getBoolean(KEY_STREAMING_ENABLED, true)) return;

//        if (mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false)
//                || mNetworkOK) {
            setTwitterInstances();
//        }
    }

    private boolean setTwitterInstances() {
        final List<ParcelableCredentials> accountsList = ParcelableAccount.getCredentialsList(this, true);
        final long[] accountIds = new long[accountsList.size()];
        for (int i = 0, j = accountIds.length; i < j; i++) {
            accountIds[i] = accountsList.get(i).account_id;
        }
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOGTAG, "Setting up twitter stream instances");
        }
        mAccountIds = accountIds;
//        clearTwitterInstances();
        boolean result = false;
        for (int i = 0, j = accountsList.size(); i < j; i++) {
            final ParcelableCredentials account = accountsList.get(i);

            if (mCallbacks.indexOfKey(account.account_id) >= 0) {
                Log.d(Constants.LOGTAG, String.format("Stream Callback %d already exists!!!", account.account_id));
                return false;
            }

            final Endpoint endpoint = TwitterAPIFactory.getEndpoint(account, TwitterUserStream.class);
            final Authorization authorization = TwitterAPIFactory.getAuthorization(account);
            final TwitterUserStream twitter = TwitterAPIFactory.getInstance(this, endpoint, authorization, TwitterUserStream.class);
            final TwidereUserStreamCallback callback = new TwidereUserStreamCallback(this, account, mPreferences);
            refreshBefore(new long[]{account.account_id});
            mCallbacks.put(account.account_id, callback);
            Log.d(Constants.LOGTAG, String.format("Stream %d starts...", account.account_id));
            new Thread() {
                @Override
                public void run() {
                    twitter.getUserStream(callback);
                }
            }.start();
            result |= true;
        }
        updateStreamState();
        return result;
    }

    private void refreshBefore(final long[] mAccountId) {
        if (mPreferences.getBoolean(KEY_REFRESH_BEFORE_STREAMING, true)) {
            mTwitterWrapper.refreshAll(mAccountId);
        }
    }

    private void updateStreamState() {
        Log.d(LOGTAG, "updateStreamState()");
        if (!mPreferences.getBoolean(KEY_STREAMING_NOTIFICATION, true)) {
            mNotificationManager.cancel(NOTIFICATION_ID_STREAMING);
            return;
        }
        if (mCallbacks.size() > 0) {
            final Intent intent = new Intent(this, HomeActivity.class);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_stat_twittnuker)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.streaming_service_running))
                    .setTicker(getString(R.string.streaming_service_running))
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            mNotificationManager.notify(NOTIFICATION_ID_STREAMING, builder.build());
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID_STREAMING);
        }
    }

    static class ShutdownStreamTwitterRunnable implements Runnable {
        private final UserStreamCallback callback;

        ShutdownStreamTwitterRunnable(final UserStreamCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            if (callback == null) return;
            Log.d(Constants.LOGTAG, "Disconnecting stream");
            callback.disconnect();
        }

    }

    static class TwidereUserStreamCallback extends UserStreamCallback {

        private final ParcelableAccount account;
        private final ContentResolver resolver;

        private boolean statusStreamStarted, mentionsStreamStarted;

        private NotificationHelper mNotificationHelper;
        private SharedPreferences mPreferences;

        public TwidereUserStreamCallback(final Context context, final ParcelableAccount account,
                                         SharedPreferences preferences) {
            super(context);
            this.account = account;
            resolver = context.getContentResolver();
            mNotificationHelper = new NotificationHelper(context);
            mPreferences = preferences;
        }

        private void createNotification(final String fromUser, final String type, final String msg,
                                        ParcelableStatus status, User sourceUser) {
            if (mPreferences.getBoolean(KEY_STREAMING_NOTIFICATIONS, true)) {
                AccountPreferences pref = new AccountPreferences(mContext, account.account_id);
                NotificationContent notification = new NotificationContent();
                notification.setAccountId(account.account_id);
                notification.setObjectId(status != null ? String.valueOf(status.id) : null);
                notification.setObjectUserId(status != null ? String.valueOf(status.user_id) : null);
                notification.setFromUser(fromUser);
                notification.setType(type);
                notification.setMessage(msg);
                notification.setTimestamp(status != null ? status.timestamp : System.currentTimeMillis());
                notification.setOriginalStatus(status);
                notification.setSourceUser(sourceUser);
                notification.setProfileImageUrl(sourceUser != null ? sourceUser.getProfileImageUrl()
                        : (status != null ? status.user_profile_image_url : null));

                mNotificationHelper.cachePushNotification(notification);
                mNotificationHelper.buildNotificationByType(notification, pref, false);
            }
        }

        @Override
        public void onConnected() {

        }

        @Override
        public void onBlock(final User source, final User blockedUser) {
            final String message = String.format("%s blocked %s", source.getScreenName(), blockedUser.getScreenName());
            Log.d(LOGTAG, message);
        }

        @Override
        public void onDeletionNotice(final long directMessageId, final long userId) {
            final String where = DirectMessages.MESSAGE_ID + " = " + directMessageId;
            for (final Uri uri : MESSAGES_URIS) {
                resolver.delete(uri, where, null);
            }
        }

        @Override
        public void onDeletionNotice(final StatusDeletionNotice statusDeletionNotice) {
            final long statusId = statusDeletionNotice.getStatusId();
            resolver.delete(Statuses.CONTENT_URI, Expression.equals(Statuses.STATUS_ID, statusId).getSQL(), null);
            resolver.delete(Activities.AboutMe.CONTENT_URI, Expression.equals(Activities.AboutMe.STATUS_ID, statusId).getSQL(), null);
        }

        @Override
        public void onDirectMessage(final DirectMessage directMessage) {
            if (directMessage == null || directMessage.getId() <= 0) return;
            for (final Uri uri : MESSAGES_URIS) {
                final String where = DirectMessages.ACCOUNT_ID + " = " + account.account_id + " AND "
                        + DirectMessages.MESSAGE_ID + " = " + directMessage.getId();
                resolver.delete(uri, where, null);
            }
            final User sender = directMessage.getSender(), recipient = directMessage.getRecipient();
            if (sender.getId() == account.account_id) {
                final ContentValues values = ContentValuesCreator.createDirectMessage(directMessage,
                        account.account_id, true);
                if (values != null) {
                    resolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
                }
            }
            if (recipient.getId() == account.account_id) {
                final ContentValues values = ContentValuesCreator.createDirectMessage(directMessage,
                        account.account_id, false);
                final Uri.Builder builder = DirectMessages.Inbox.CONTENT_URI.buildUpon();
                builder.appendQueryParameter(QUERY_PARAM_NOTIFY, "true");
                if (values != null) {
                    resolver.insert(builder.build(), values);
                }
            }

        }

        @Override
        public void onException(final Throwable ex) {
            if (ex instanceof TwitterException) {
                Log.w(LOGTAG, String.format("Error %d", ((TwitterException) ex).getStatusCode()), ex);
                final HttpResponse response = ((TwitterException) ex).getHttpResponse();
                if (response != null) {
                    try {
                        final Body body = response.getBody();
                        if (body != null) {
                            final ByteArrayOutputStream os = new ByteArrayOutputStream();
                            body.writeTo(os);
                            final String charsetName;
                            final ContentType contentType = body.contentType();
                            if (contentType != null) {
                                final Charset charset = contentType.getCharset();
                                if (charset != null) {
                                    charsetName = charset.name();
                                } else {
                                    charsetName = Charset.defaultCharset().name();
                                }
                            } else {
                                charsetName = Charset.defaultCharset().name();
                            }
                            Log.w(LOGTAG, os.toString(charsetName));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.w(Constants.LOGTAG, ex);
            }
        }

        @Override
        public void onFavorite(final User source, final User target, final Status favoritedStatus) {
//            ParcelableActivity activity = new ParcelableActivity();
//            activity.account_id = account.account_id;
//            activity.timestamp = favoritedStatus.getCreatedAt() != null ? favoritedStatus.getCreatedAt().getTime() : System.currentTimeMillis();
//            activity.action = Activity.Action.FAVORITE.literal;
//            activity.sources = ParcelableUser.fromUsers(new User[]{source}, source.getId());
//            activity.target_users = ParcelableUser.fromUsers(new User[]{target}, target.getId());
//            activity.target_statuses = ParcelableStatus.fromStatuses(new Status[]{favoritedStatus}, favoritedStatus.getId());
//            activity.source_ids = new long[] { source.getId() };
//            activity.is_gap = false;
//
//            ContentValues values = ParcelableActivityValuesCreator.create(activity);
//            resolver.insert(Activities.AboutMe.CONTENT_URI, values);

            if (favoritedStatus.getUser().getId() == account.account_id) {
                createNotification(source.getScreenName(), NotificationContent.NOTIFICATION_TYPE_FAVORITE,
                        Utils.parseURLEntities(favoritedStatus.getText(), favoritedStatus.getUrlEntities()),
                        new ParcelableStatus(favoritedStatus, account.account_id, false),
                        source);
            }
        }

        @Override
        public void onFollow(final User source, final User followedUser) {
            if (followedUser.getId() == account.account_id) {
                createNotification(source.getScreenName(), NotificationContent.NOTIFICATION_TYPE_FOLLOWER,
                        null, null, source);
            }
        }

        @Override
        public void onFriendList(final long[] friendIds) {

        }

        @Override
        public void onScrubGeo(final long userId, final long upToStatusId) {
            final String where = Statuses.USER_ID + " = " + userId + " AND " + Statuses.STATUS_ID + " >= "
                    + upToStatusId;
            final ContentValues values = new ContentValues();
            values.putNull(Statuses.LOCATION);
            resolver.update(Statuses.CONTENT_URI, values, where, null);
        }

        @Override
        public void onStallWarning(final Warning warn) {

        }

        @Override
        public void onStatus(final Status status) {
            final ContentValues values = ContentValuesCreator.createStatus(status, account.account_id);
            if (!statusStreamStarted && !mPreferences.getBoolean(KEY_REFRESH_BEFORE_STREAMING, true)) {
                statusStreamStarted = true;
                values.put(Statuses.IS_GAP, true);
            }
            final String where = Statuses.ACCOUNT_ID + " = " + account.account_id + " AND " + Statuses.STATUS_ID + " = "
                    + status.getId();
            resolver.delete(Statuses.CONTENT_URI, where, null);
//            resolver.delete(Activities.AboutMe.CONTENT_URI, where, null);
            resolver.insert(Statuses.CONTENT_URI, values);
            final Status rt = status.getRetweetedStatus();
            if (rt != null && rt.getText().contains("@" + account.screen_name) || rt == null
                    && status.getText().contains("@" + account.screen_name)) {

//                Activity activity = Activity.fromMention(account.account_id, status);
//                final ParcelableActivity parcelableActivity = new ParcelableActivity(activity, account.account_id, false);
//                parcelableActivity.timestamp = status.getCreatedAt() != null ? status.getCreatedAt().getTime(): System.currentTimeMillis();
//                final ContentValues activityValues = ParcelableActivityValuesCreator.create(parcelableActivity);
//                resolver.insert(Activities.AboutMe.CONTENT_URI, activityValues);
            }
            if (rt != null && rt.getUser().getId() == account.account_id) {
                createNotification(status.getUser().getScreenName(),
                        NotificationContent.NOTIFICATION_TYPE_RETWEET,
                        Utils.parseURLEntities(rt.getText(), rt.getUrlEntities()),
                        new ParcelableStatus(status, account.account_id, false), status.getUser());
                //TODO insert retweet activity
            }
        }

        @Override
        public void onTrackLimitationNotice(final int numberOfLimitedStatuses) {

        }

        @Override
        public void onUnblock(final User source, final User unblockedUser) {
            final String message = String.format("%s unblocked %s", source.getScreenName(),
                    unblockedUser.getScreenName());
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUnfavorite(final User source, final User target, final Status unfavoritedStatus) {
            final String message = String.format("%s unfavorited %s's tweet: %s", source.getScreenName(),
                    target.getScreenName(), unfavoritedStatus.getText());
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onUserListCreation(final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListDeletion(final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListMemberAddition(final User addedMember, final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListMemberDeletion(final User deletedMember, final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListSubscription(final User subscriber, final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListUnsubscription(final User subscriber, final User listOwner, final UserList list) {

        }

        @Override
        public void onUserListUpdate(final User listOwner, final UserList list) {

        }

        @Override
        public void onUserProfileUpdate(final User updatedUser) {

        }
    }

}