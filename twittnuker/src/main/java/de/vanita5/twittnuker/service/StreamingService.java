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
import android.preference.PreferenceManager;
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
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableAccount.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.provider.TwidereDataStore.DirectMessages;
import de.vanita5.twittnuker.provider.TwidereDataStore.Mentions;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.util.TwidereArrayUtils;
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
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
		if (!mPreferences.getBoolean(KEY_STREAMING_ENABLED, true)) return;

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
				if (!mPreferences.getBoolean(KEY_STREAMING_NOTIFICATION, true)) return;
				final Intent intent = new Intent(this, HomeActivity.class);
				NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
				builder.setOngoing(true)
						.setOnlyAlertOnce(true)
						.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
						.setSmallIcon(R.drawable.ic_stat_twittnuker)
						.setContentTitle(getString(R.string.app_name))
						.setContentText(getString(R.string.streaming_service_running))
						.setTicker(getString(R.string.streaming_service_running))
						.setPriority(NotificationCompat.PRIORITY_MIN)
						.setCategory(NotificationCompat.CATEGORY_SERVICE);
				mNotificationManager.notify(NOTIFICATION_ID_STREAMING, builder.build());
			} else {
				isStreaming = false;
				mNotificationManager.cancel(NOTIFICATION_ID_STREAMING);
			}
		}
	}

	private boolean setTwitterInstances(final SharedPreferencesWrapper prefs) {
		if (prefs == null) return false;
		final List<ParcelableCredentials> accountsList = ParcelableAccount.getCredentialsList(this, true);
		mAccountIds = new long[accountsList.size()];
		clearTwitterInstances();
		for (int i = 0, j = accountsList.size(); i < j; i++) {
			final ParcelableCredentials account = accountsList.get(i);
			final String token = account.oauth_token;
			final String secret = account.oauth_token_secret;
			final long account_id = account.account_id;
			mAccountIds[i] = account_id;
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
			final String consumer_key = account.consumer_key, consumer_secret = account.consumer_secret;
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
		}
		isStreaming = true;
		return true;
	}

	private void refreshBefore(final long[] mAccountId) {
		if (mPreferences.getBoolean(KEY_REFRESH_BEFORE_STREAMING, true)) {
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
			mNotificationHelper = new NotificationHelper(context);
			mPreferences = preferences;
		}

		private void createNotification(final String fromUser, final String type, final String msg,
										ParcelableStatus status, User sourceUser) {
			if (mPreferences.getBoolean(KEY_STREAMING_NOTIFICATIONS, true)
					&& !mPreferences.getBoolean(KEY_ENABLE_PUSH, false)) {
				AccountPreferences pref = new AccountPreferences(context, account_id);
				NotificationContent notification = new NotificationContent();
				notification.setAccountId(account_id);
				notification.setFromUser(fromUser);
				notification.setType(type);
				notification.setMessage(msg);
				notification.setTimestamp(status != null ? status.timestamp : System.currentTimeMillis());
				notification.setOriginalStatus(status);
				notification.setSourceUser(sourceUser);
				notification.setProfileImageUrl(sourceUser != null ? sourceUser.getProfileImageURL().toString()
						: (status != null ? status.user_profile_image_url : null));

				mNotificationHelper.cachePushNotification(notification);
				mNotificationHelper.buildNotificationByType(notification, pref, false);
			}
		}

		@Override
		public void onBlock(final User source, final User blockedUser) {
			final String message = String.format("%s blocked %s", source.getScreenName(), blockedUser.getScreenName());
			//Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			//TODO Notification?
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
				final String where = DirectMessages.ACCOUNT_ID + " = " + account_id + " AND "
						+ DirectMessages.MESSAGE_ID + " = " + directMessage.getId();
				resolver.delete(uri, where, null);
			}
			final User sender = directMessage.getSender(), recipient = directMessage.getRecipient();
			if (sender.getId() == account_id) {
				final ContentValues values = ContentValuesCreator.createDirectMessage(directMessage, account_id, true,
						large_profile_image);
				if (values != null) {
					resolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
				}
			}
			if (recipient.getId() == account_id) {
				final ContentValues values = ContentValuesCreator.createDirectMessage(directMessage, account_id, false,
						large_profile_image);
				final Uri.Builder builder = DirectMessages.Inbox.CONTENT_URI.buildUpon();
				builder.appendQueryParameter(QUERY_PARAM_NOTIFY, "true");
				if (values != null) {
					resolver.insert(builder.build(), values);
				}
			}

		}

		@Override
		public void onException(final Exception ex) {
			if (Utils.isDebugBuild()) {
				Log.w(LOGTAG, ex);
			}
		}

		@Override
		public void onFavorite(final User source, final User target, final Status favoritedStatus) {
			if (favoritedStatus.getUser().getId() == account_id) {
				createNotification(source.getScreenName(), NotificationContent.NOTIFICATION_TYPE_FAVORITE,
						Utils.parseURLEntities(favoritedStatus.getText(), favoritedStatus.getURLEntities()),
						new ParcelableStatus(favoritedStatus, account_id, false),
						source);
			}
		}

		@Override
		public void onFollow(final User source, final User followedUser) {
			if (followedUser.getId() == account_id) {
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
		public void onStallWarning(final StallWarning warn) {
			if ("420".equals(warn.getCode())) {
				createNotification(null, NotificationContent.NOTIFICATION_TYPE_ERROR_420, null, null, null);
			}
		}

		@Override
		public void onStatus(final Status status) {
			final ContentValues values = ContentValuesCreator.createStatus(status, account_id, large_profile_image);
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
					createNotification(status.getUser().getScreenName(),
							NotificationContent.NOTIFICATION_TYPE_RETWEET,
							Utils.parseURLEntities(rt.getText(), rt.getURLEntities()),
							new ParcelableStatus(status, account_id, false), status.getUser());
				}
			}
		}

		@Override
		public void onTrackLimitationNotice(final int numberOfLimitedStatuses) {

		}

		@Override
		public void onUnblock(final User source, final User unblockedUser) {
			final String message = String.format("%s unblocked %s", source.getScreenName(),
					unblockedUser.getScreenName());
			//Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			//TODO Notification?
		}

		@Override
		public void onUnfavorite(final User source, final User target, final Status unfavoritedStatus) {
			final String message = String.format("%s unfavorited %s's tweet: %s", source.getScreenName(),
					target.getScreenName(), unfavoritedStatus.getText());
			//Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            //TODO notification?
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
