package de.vanita5.twittnuker.service;

import android.app.Notification;
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
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.HomeActivity;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.model.NotificationContent;
import de.vanita5.twittnuker.provider.TweetStore.Accounts;
import de.vanita5.twittnuker.provider.TweetStore.DirectMessages;
import de.vanita5.twittnuker.provider.TweetStore.Mentions;
import de.vanita5.twittnuker.provider.TweetStore.Statuses;
import de.vanita5.twittnuker.util.ArrayUtils;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.NotificationHelper;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.net.TwidereHostResolverFactory;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.auth.AccessToken;
import twitter4j.conf.StreamConfigurationBuilder;

import static android.text.TextUtils.isEmpty;

public class StreamingService extends Service implements Constants {

	private final List<WeakReference<TwitterStream>> mTwitterInstances = new ArrayList<WeakReference<TwitterStream>>();
	private ContentResolver mResolver;

	private SharedPreferences mPreferences;
	private NotificationManager mNotificationManager;

	private AsyncTwitterWrapper mTwitterWrapper;

	private long[] mAccountIds;
	private boolean isStreaming = false;

	private static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI };
	private static final Uri[] MESSAGES_URIS = new Uri[] { DirectMessages.Inbox.CONTENT_URI, DirectMessages.Outbox.CONTENT_URI };

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				NetworkInfo wifi = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

				if (wifi.isConnected()) {
					initStreaming();
				} else {
					if (!mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false)) {
						clearTwitterInstances();
					}
				}
			} else if (BROADCAST_REFRESH_STREAMING_SERVICE.equals(action)) {
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
			if (!ArrayUtils.contentMatch(mAccountIds, Utils.getActivatedAccountIds(StreamingService.this))) {
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
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
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
		for(final WeakReference<TwitterStream> reference : mTwitterInstances) {
			final TwitterStream stream = reference.get();
			new Thread(new ShutdownStreamTwitterRunnable(stream)).start();
		}
		mTwitterInstances.clear();
		isStreaming = false;
		mNotificationManager.cancel(NOTIFICATION_ID_STREAMING);
	}

	private void initStreaming() {
		if (!mPreferences.getBoolean(KEY_STREAMING_ENABLED, false)) return;

		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (isStreaming) {
			if (!(mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false)
					|| wifi.isConnected())) {
				clearTwitterInstances();
			}
			return;
		}

		if (mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false)
				|| wifi.isConnected()) {

			final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(this, SHARED_PREFERENCES_NAME, MODE_PRIVATE);
			if (setTwitterInstances(prefs)) {
				if (!mPreferences.getBoolean(KEY_STREAMING_NOTIFICATION, false)) return;
				final Intent intent = new Intent(this, HomeActivity.class);
				final Notification.Builder builder = new Notification.Builder(this);
				builder.setOngoing(true);
				builder.setOnlyAlertOnce(true);
				builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
				builder.setSmallIcon(R.drawable.ic_stat_twittnuker);
				builder.setContentTitle(getString(R.string.app_name));
				builder.setContentText(getString(R.string.streaming_service_running));
				builder.setTicker(getString(R.string.streaming_service_running));
				builder.setPriority(NotificationCompat.PRIORITY_MIN);
				mNotificationManager.notify(NOTIFICATION_ID_STREAMING, builder.build());
			} else {
				isStreaming = false;
				mNotificationManager.cancel(NOTIFICATION_ID_STREAMING);
			}
		}
	}

	private boolean setTwitterInstances(final SharedPreferencesWrapper prefs) {
		if (prefs == null) return false;
		final String[] cols = new String[] { Accounts.OAUTH_TOKEN, Accounts.OAUTH_TOKEN_SECRET, Accounts.ACCOUNT_ID,
				Accounts.CONSUMER_KEY, Accounts.CONSUMER_SECRET };
		final String where = Accounts.IS_ACTIVATED + " = 1 AND " + Accounts.AUTH_TYPE + " = " + Accounts.AUTH_TYPE_OAUTH;
		final Cursor cur = mResolver.query(Accounts.CONTENT_URI, cols, where, null, null);
		if (cur == null) return false;
		final int count = cur.getCount();
		if (count == 0) {
			cur.close();
			return false;
		}
		mAccountIds = new long[count];
		cur.moveToFirst();
		clearTwitterInstances();
		final int token_idx = cur.getColumnIndex(Accounts.OAUTH_TOKEN);
		final int secret_idx = cur.getColumnIndex(Accounts.OAUTH_TOKEN_SECRET);
		final int account_id_idx = cur.getColumnIndex(Accounts.ACCOUNT_ID);
		final int consumer_key_idx = cur.getColumnIndex(Accounts.CONSUMER_KEY);
		final int consumer_secret_idx = cur.getColumnIndex(Accounts.CONSUMER_SECRET);
		while(!cur.isAfterLast()) {
			final String token = cur.getString(token_idx);
			final String secret = cur.getString(secret_idx);
			final long account_id = cur.getLong(account_id_idx);
			mAccountIds[cur.getPosition()] = account_id;
			final StreamConfigurationBuilder cb = new StreamConfigurationBuilder();
			cb.setGZIPEnabled(prefs.getBoolean(KEY_GZIP_COMPRESSING, true));
			cb.setIncludeEntitiesEnabled(true);
			if (prefs.getBoolean(KEY_IGNORE_SSL_ERROR, false)) {
				final TwittnukerApplication app = TwittnukerApplication.getInstance(this);
				cb.setHostAddressResolverFactory(new TwidereHostResolverFactory(app));
				cb.setIgnoreSSLError(true);
			}
			final String default_consumer_key = Utils.getNonEmptyString(prefs.getSharedPreferences(),
					KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY_2);
			final String default_consumer_secret = Utils.getNonEmptyString(prefs.getSharedPreferences(),
					KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET_2);
			final String consumer_key = cur.getString(consumer_key_idx), consumer_secret = cur
					.getString(consumer_secret_idx);
			if (!isEmpty(consumer_key) && !isEmpty(consumer_secret)) {
				cb.setOAuthConsumerKey(consumer_key);
				cb.setOAuthConsumerSecret(consumer_secret);
			} else {
				cb.setOAuthConsumerKey(default_consumer_key);
				cb.setOAuthConsumerSecret(default_consumer_secret);
			}
			final TwitterStream stream = new TwitterStreamFactory(cb.build()).getInstance(new AccessToken(token, secret));
			stream.addListener(new UserStreamListenerImpl(this, mPreferences, account_id));
			refreshBefore(new long[]{ account_id });
			stream.user();
			mTwitterInstances.add(new WeakReference<TwitterStream>(stream));
			cur.moveToNext();
		}
		cur.close();
		isStreaming = true;
		return true;
	}

	private void refreshBefore(final long[] mAccountId) {
		if (mPreferences.getBoolean(KEY_REFRESH_BEFORE_STREAMING, false)) {
			mTwitterWrapper.refreshAll(mAccountId);
		}
	}

	static class ShutdownStreamTwitterRunnable implements Runnable {

		private final TwitterStream stream;

		ShutdownStreamTwitterRunnable(final TwitterStream stream) {
			this.stream = stream;
		}

		@Override
		public void run() {
			if (stream == null) return;
			stream.shutdown();
		}
	}

	static class UserStreamListenerImpl implements UserStreamListener {

		private final long account_id;
		private final String screen_name;
		private final ContentResolver resolver;
		private final boolean large_profile_image;

		private NotificationHelper mNotificationHelper;
		private SharedPreferences mPreferences;
		private Context context;

		public UserStreamListenerImpl(final Context context, final SharedPreferences preferences, final long account_id) {
			this.context = context;
			this.account_id = account_id;
			large_profile_image = context.getResources().getBoolean(R.bool.hires_profile_image);
			resolver = context.getContentResolver();
			screen_name = Utils.getAccountScreenName(context, account_id);
			mNotificationHelper = new NotificationHelper();
			mPreferences = preferences;
		}

		private void createNotification(final String fromUser, final String type, final String msg) {
			if (mPreferences.getBoolean(KEY_STREAMING_NOTIFICATIONS, false)
					&& !mPreferences.getBoolean(KEY_ENABLE_PUSH, false)) {
				AccountPreferences pref = new AccountPreferences(context, account_id);
				NotificationContent notification = new NotificationContent();
				notification.setAccountId(account_id);
				notification.setFromUser(fromUser);
				notification.setType(type);
				notification.setMessage(msg);
				notification.setTimestamp(System.currentTimeMillis());

				mNotificationHelper.cachePushNotification(context, notification);
				mNotificationHelper.buildNotificationByType(context, notification, pref);
			}
		}

		@Override
		public void onBlock(User source, User blockedUser) {

		}

		@Override
		public void onDeletionNotice(long directMessageId, long userId) {
			final String where = DirectMessages.MESSAGE_ID + " = " + directMessageId;
			for (final Uri uri : MESSAGES_URIS) {
				resolver.delete(uri, where, null);
			}
		}

		@Override
		public void onDirectMessage(DirectMessage directMessage) {
			if (directMessage == null || directMessage.getId() <= 0) return;
			for (final Uri uri : MESSAGES_URIS) {
				final String where = DirectMessages.ACCOUNT_ID + " = " + account_id + " AND "
						+ DirectMessages.MESSAGE_ID + " = " + directMessage.getId();
				resolver.delete(uri, where, null);
			}
			final User sender = directMessage.getSender(), recipient = directMessage.getRecipient();
			if (sender.getId() == account_id) {
				final ContentValues values = ContentValuesCreator.makeDirectMessageContentValues(directMessage, account_id, true,
						large_profile_image);
				if (values != null) {
					resolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
				}
			}
			if (recipient.getId() == account_id) {
				final ContentValues values = ContentValuesCreator.makeDirectMessageContentValues(directMessage, account_id, false,
						large_profile_image);
				final Uri.Builder builder = DirectMessages.Inbox.CONTENT_URI.buildUpon();
				builder.appendQueryParameter(QUERY_PARAM_NOTIFY, "true");
				if (values != null) {
					resolver.insert(builder.build(), values);
				}
			}
		}

		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			if (favoritedStatus.getUser().getId() == account_id) {
				createNotification(source.getScreenName(), NotificationContent.NOTIFICATION_TYPE_FAVORITE,
						favoritedStatus.getText());
			}
		}

		@Override
		public void onFollow(User source, User followedUser) {
			createNotification(source.getScreenName(), NotificationContent.NOTIFICATION_TYPE_FOLLOWER, null);
		}

		@Override
		public void onFriendList(long[] friendIds) {

		}

		@Override
		public void onUnblock(User source, User unblockedUser) {

		}

		@Override
		public void onUnfavorite(User source, User target, Status unfavoritedStatus) {

		}

		@Override
		public void onUserListCreation(User listOwner, UserList list) {

		}

		@Override
		public void onUserListDeletion(User listOwner, UserList list) {

		}

		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {

		}

		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {

		}

		@Override
		public void onUserListSubscription(User subscriber, User listOwner, UserList list) {

		}

		@Override
		public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {

		}

		@Override
		public void onUserListUpdate(User listOwner, UserList list) {

		}

		@Override
		public void onUserProfileUpdate(User updatedUser) {

		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			final long status_id = statusDeletionNotice.getStatusId();
			final String where = Statuses.STATUS_ID + " = " + status_id;
			for (final Uri uri : STATUSES_URIS) {
				resolver.delete(uri, where, null);
			}
		}

		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
			final String where = Statuses.USER_ID + " = " + userId + " AND " + Statuses.STATUS_ID + " >= "
					+ upToStatusId;
			final ContentValues values = new ContentValues();
			values.putNull(Statuses.LOCATION);
			for (final Uri uri : STATUSES_URIS) {
				resolver.update(uri, values, where, null);
			}
		}

		@Override
		public void onStallWarning(StallWarning warning) {
			if ("420".equals(warning.getCode())) {
				createNotification(null, NotificationContent.NOTIFICATION_TYPE_ERROR_420, null);
			}
		}

		@Override
		public void onStatus(Status status) {
			final ContentValues values = ContentValuesCreator.makeStatusContentValues(status, account_id, large_profile_image);
			if (values != null) {
				final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.STATUS_ID + " = "
						+ status.getId();
				resolver.delete(Statuses.CONTENT_URI, where, null);
				resolver.delete(Mentions.CONTENT_URI, where, null);
				final Uri.Builder status_uri_builder = Statuses.CONTENT_URI.buildUpon();
				status_uri_builder.appendQueryParameter(QUERY_PARAM_NOTIFY, "true");
				final Uri status_uri = status_uri_builder.build();
				resolver.insert(status_uri, values);
				final Status rt = status.getRetweetedStatus();
				if (rt != null && rt.getText().contains("@" + screen_name) || rt == null
						&& status.getText().contains("@" + screen_name)) {
					final Uri.Builder mention_uri_builder = Mentions.CONTENT_URI.buildUpon();
					mention_uri_builder.appendQueryParameter(QUERY_PARAM_NOTIFY, "true");
					final Uri mention_uri = mention_uri_builder.build();
					resolver.insert(mention_uri, values);
				}
				if (rt != null && rt.getUser().getId() == account_id) {
					createNotification(status.getUser().getScreenName(), NotificationContent.NOTIFICATION_TYPE_RETWEET,
							rt.getText());
				}
			}
		}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

		}

		@Override
		public void onException(Exception ex) {
			if (Utils.isDebugBuild()) {
				Log.w(LOGTAG, ex);
			}
		}
	}
}
