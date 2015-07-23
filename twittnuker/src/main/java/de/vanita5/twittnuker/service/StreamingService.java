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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.widget.Toast;

import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.restfu.http.mime.TypedData;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.HomeActivity;
import de.vanita5.twittnuker.api.twitter.model.Warning;
import de.vanita5.twittnuker.app.TwittnukerApplication;
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
import de.vanita5.twittnuker.provider.TwidereDataStore.DirectMessages;
import de.vanita5.twittnuker.provider.TwidereDataStore.Mentions;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.NotificationHelper;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.TwidereArrayUtils;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.Utils;

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
	private boolean isStreaming = false;

	private static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI };
	private static final Uri[] MESSAGES_URIS = new Uri[] { DirectMessages.Inbox.CONTENT_URI,
			DirectMessages.Outbox.CONTENT_URI };

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				NetworkInfo wifi = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

				if (wifi.isConnected() || mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false)) {
					initStreaming();
				} else {
					clearTwitterInstances();
				}
			} else if (BROADCAST_REFRESH_STREAMING_SERVICE.equals(action)) {
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
			if (!TwidereArrayUtils.contentMatch(mAccountIds, Utils.getActivatedAccountIds(StreamingService.this))) {
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
		mTwitterWrapper = TwittnukerApplication.getInstance(this).getTwitterWrapper();

		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
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
		super.onDestroy();
	}

	private void clearTwitterInstances() {
        for (int i = 0, j = mCallbacks.size(); i < j; i++) {
            new Thread(new ShutdownStreamTwitterRunnable(mCallbacks.valueAt(i))).start();
		}
        mCallbacks.clear();
		isStreaming = false;
		mNotificationManager.cancel(NOTIFICATION_ID_STREAMING);
	}

	@SuppressWarnings("deprecation")
	private void initStreaming() {
		if (!mPreferences.getBoolean(KEY_STREAMING_ENABLED, true)) return;

		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false)
				|| wifi.isConnected()) {
			setTwitterInstances();
			updateStreamState();
		}
	}

    private boolean setTwitterInstances() {
		final List<ParcelableCredentials> accountsList = ParcelableAccount.getCredentialsList(this, true);
		mAccountIds = new long[accountsList.size()];
		clearTwitterInstances();
        boolean result = false;
		for (int i = 0, j = accountsList.size(); i < j; i++) {
			final ParcelableCredentials account = accountsList.get(i);
            final Endpoint endpoint = TwitterAPIFactory.getEndpoint(account, TwitterUserStream.class);
            final Authorization authorization = TwitterAPIFactory.getAuthorization(account);
            final TwitterUserStream twitter = TwitterAPIFactory.getInstance(this, endpoint, authorization, TwitterUserStream.class);
            final TwidereUserStreamCallback callback = new TwidereUserStreamCallback(this, account, mPreferences);
			refreshBefore(new long[]{ account.account_id });
			mCallbacks.put(account.account_id, callback);
            new Thread() {
                @Override
                public void run() {
                    twitter.getUserStream(callback);
                    Log.d(Constants.LOGTAG, String.format("Stream %d disconnected", account.account_id));
                    mCallbacks.remove(account.account_id);
                    updateStreamState();
				}
            }.start();
            result |= true;
		}
        return result;
	}

	private void refreshBefore(final long[] mAccountId) {
		if (mPreferences.getBoolean(KEY_REFRESH_BEFORE_STREAMING, true)) {
			mTwitterWrapper.refreshAll(mAccountId);
		}
	}

	private void updateStreamState() {
		if (!mPreferences.getBoolean(KEY_STREAMING_NOTIFICATION, true)) return;
		if (mCallbacks.size() > 0) {
			isStreaming = true;
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
					.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
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

        private final Context context;
        private final ParcelableAccount account;
		private final ContentResolver resolver;

		private boolean statusStreamStarted, mentionsStreamStarted;

		private NotificationHelper mNotificationHelper;
		private SharedPreferences mPreferences;

        public TwidereUserStreamCallback(final Context context, final ParcelableAccount account,
										 SharedPreferences preferences) {
			this.context = context;
            this.account = account;
			resolver = context.getContentResolver();
			mNotificationHelper = new NotificationHelper(context);
			mPreferences = preferences;
		}

		private void createNotification(final String fromUser, final String type, final String msg,
										ParcelableStatus status, User sourceUser) {
			if (mPreferences.getBoolean(KEY_STREAMING_NOTIFICATIONS, true)
					&& !mPreferences.getBoolean(KEY_ENABLE_PUSH, false)) {
				AccountPreferences pref = new AccountPreferences(context, account.account_id);
				NotificationContent notification = new NotificationContent();
				notification.setAccountId(account.account_id);
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
			final long status_id = statusDeletionNotice.getStatusId();
			final String where = Statuses.STATUS_ID + " = " + status_id;
			for (final Uri uri : STATUSES_URIS) {
				resolver.delete(uri, where, null);
			}
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
                final RestHttpResponse response = ((TwitterException) ex).getHttpResponse();
                if (response != null) {
                    try {
                        final TypedData body = response.getBody();
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
			for (final Uri uri : STATUSES_URIS) {
				resolver.update(uri, values, where, null);
			}
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
			resolver.delete(Mentions.CONTENT_URI, where, null);
            resolver.insert(Statuses.CONTENT_URI, values);
				final Status rt = status.getRetweetedStatus();
            if (rt != null && rt.getText().contains("@" + account.screen_name) || rt == null
                    && status.getText().contains("@" + account.screen_name)) {
                resolver.insert(Mentions.CONTENT_URI, values);
			}
			if (rt != null && rt.getUser().getId() == account.account_id) {
				createNotification(status.getUser().getScreenName(),
						NotificationContent.NOTIFICATION_TYPE_RETWEET,
						Utils.parseURLEntities(rt.getText(), rt.getUrlEntities()),
						new ParcelableStatus(status, account.account_id, false), status.getUser());
			}
		}

		@Override
		public void onTrackLimitationNotice(final int numberOfLimitedStatuses) {

		}

		@Override
		public void onUnblock(final User source, final User unblockedUser) {
			final String message = String.format("%s unblocked %s", source.getScreenName(),
					unblockedUser.getScreenName());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onUnfavorite(final User source, final User target, final Status unfavoritedStatus) {
			final String message = String.format("%s unfavorited %s's tweet: %s", source.getScreenName(),
					target.getScreenName(), unfavoritedStatus.getText());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

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