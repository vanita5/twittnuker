/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.provider;

import static de.vanita5.twittnuker.util.Utils.clearAccountColor;
import static de.vanita5.twittnuker.util.Utils.clearAccountName;
import static de.vanita5.twittnuker.util.Utils.getAccountIds;
import static de.vanita5.twittnuker.util.Utils.getActivatedAccountIds;
import static de.vanita5.twittnuker.util.Utils.getNotificationUri;
import static de.vanita5.twittnuker.util.Utils.getTableId;
import static de.vanita5.twittnuker.util.Utils.getTableNameById;
import static de.vanita5.twittnuker.util.Utils.isFiltered;

import android.app.NotificationManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.mariotaku.jsonserializer.JSONFileIO;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.model.AccountPreferences;
import de.vanita5.twittnuker.model.NotificationContent;
import de.vanita5.twittnuker.model.ParcelableDirectMessage;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.SupportTabSpec;
import de.vanita5.twittnuker.model.UnreadItem;
import de.vanita5.twittnuker.provider.TweetStore.DirectMessages;
import de.vanita5.twittnuker.provider.TweetStore.Preferences;
import de.vanita5.twittnuker.provider.TweetStore.Statuses;
import de.vanita5.twittnuker.provider.TweetStore.UnreadCounts;
import de.vanita5.twittnuker.util.ArrayUtils;
import de.vanita5.twittnuker.util.CustomTabUtils;
import de.vanita5.twittnuker.util.ImagePreloader;
import de.vanita5.twittnuker.util.MediaPreviewUtils;
import de.vanita5.twittnuker.util.NotificationHelper;
import de.vanita5.twittnuker.util.SQLiteDatabaseWrapper;
import de.vanita5.twittnuker.util.SQLiteDatabaseWrapper.LazyLoadCallback;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.collection.NoDuplicatesCopyOnWriteArrayList;
import de.vanita5.twittnuker.util.ParseUtils;
import de.vanita5.twittnuker.util.TwidereQueryBuilder;
import de.vanita5.twittnuker.util.Utils;

import twitter4j.http.HostAddressResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TwidereDataProvider extends ContentProvider implements Constants, LazyLoadCallback {

	private static final String UNREAD_STATUSES_FILE_NAME = "unread_statuses";
	private static final String UNREAD_MENTIONS_FILE_NAME = "unread_mentions";
	private static final String UNREAD_MESSAGES_FILE_NAME = "unread_messages";

	private ContentResolver mContentResolver;
	private SQLiteDatabaseWrapper mDatabaseWrapper;
	private NotificationManager mNotificationManager;
	private SharedPreferencesWrapper mPreferences;
	private ImagePreloader mImagePreloader;
	private HostAddressResolver mHostAddressResolver;
	private NotificationHelper mNotificationHelper;

	private final List<ParcelableStatus> mNewStatuses = new CopyOnWriteArrayList<ParcelableStatus>();
	private final List<ParcelableStatus> mNewMentions = new CopyOnWriteArrayList<ParcelableStatus>();
	private final List<ParcelableDirectMessage> mNewMessages = new CopyOnWriteArrayList<ParcelableDirectMessage>();

	private final List<UnreadItem> mUnreadStatuses = new NoDuplicatesCopyOnWriteArrayList<UnreadItem>();
	private final List<UnreadItem> mUnreadMentions = new NoDuplicatesCopyOnWriteArrayList<UnreadItem>();
	private final List<UnreadItem> mUnreadMessages = new NoDuplicatesCopyOnWriteArrayList<UnreadItem>();

	@Override
	public int bulkInsert(final Uri uri, final ContentValues[] values) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			switch (tableId) {
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
				case TABLE_ID_DIRECT_MESSAGES:
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
					return 0;
			}
			int result = 0;
			if (table != null && values != null) {
				mDatabaseWrapper.beginTransaction();
				final boolean replaceOnConflict = shouldReplaceOnConflict(tableId);
				for (final ContentValues contentValues : values) {
					if (replaceOnConflict) {
						mDatabaseWrapper.insertWithOnConflict(table, null, contentValues,
								SQLiteDatabase.CONFLICT_REPLACE);
					} else {
						mDatabaseWrapper.insert(table, null, contentValues);
					}
					result++;
				}
				mDatabaseWrapper.setTransactionSuccessful();
				mDatabaseWrapper.endTransaction();
			}
			if (result > 0) {
				onDatabaseUpdated(tableId, uri);
			}
			onNewItemsInserted(uri, values);
			return result;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			switch (tableId) {
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
				case TABLE_ID_DIRECT_MESSAGES:
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
					return 0;
				case VIRTUAL_TABLE_ID_NOTIFICATIONS: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() == 1) {
						clearNotification();
					} else if (segments.size() == 2) {
						final int notificationType = ParseUtils.parseInt(segments.get(1));
						clearNotification(notificationType, 0);
					} else if (segments.size() == 3) {
						final int notificationType = ParseUtils.parseInt(segments.get(1));
						final long accountId = ParseUtils.parseLong(segments.get(2));
						clearNotification(notificationType, accountId);
					}
					return 1;
				}
				case VIRTUAL_TABLE_ID_UNREAD_COUNTS: {
					final List<String> segments = uri.getPathSegments();
					final int segmentsSize = segments.size();
					if (segmentsSize == 1)
						return clearUnreadCount();
					else if (segmentsSize == 2)
						return clearUnreadCount(ParseUtils.parseInt(segments.get(1)));
					else if (segmentsSize == 4)
						return removeUnreadItems(ParseUtils.parseInt(segments.get(1)),
								ParseUtils.parseLong(segments.get(2)), ArrayUtils.parseLongArray(segments.get(3), ','));
					return 0;
				}
			}
			if (table == null) return 0;
			final int result = mDatabaseWrapper.delete(table, selection, selectionArgs);
			if (result > 0) {
				onDatabaseUpdated(tableId, uri);
			}
			return result;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getType(final Uri uri) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			switch (tableId) {
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
				case TABLE_ID_DIRECT_MESSAGES:
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
					return null;
			}
			if (table == null) return null;
			final boolean replaceOnConflict = shouldReplaceOnConflict(tableId);
			final long rowId;
			if (replaceOnConflict) {
				rowId = mDatabaseWrapper.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			} else {
				rowId = mDatabaseWrapper.insert(table, null, values);
			}
			onDatabaseUpdated(tableId, uri);
			onNewItemsInserted(uri, values);
			return Uri.withAppendedPath(uri, String.valueOf(rowId));
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		final TwittnukerApplication app = TwittnukerApplication.getInstance(context);
        mDatabaseWrapper = new SQLiteDatabaseWrapper(this);
		mHostAddressResolver = app.getHostAddressResolver();
        mPreferences = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mImagePreloader = new ImagePreloader(context, app.getImageLoader());
		restoreUnreadItems();
		mNotificationHelper = new NotificationHelper(context);
		// final GetWritableDatabaseTask task = new
		// GetWritableDatabaseTask(context, helper, mDatabaseWrapper);
		// task.execute();
		return true;
	}

    @Override
    public SQLiteDatabase onCreateSQLiteDatabase() {
        final TwittnukerApplication app = TwittnukerApplication.getInstance(getContext());
        final SQLiteOpenHelper helper = app.getSQLiteOpenHelper();
        return helper.getWritableDatabase();
    }

	@Override
	public ParcelFileDescriptor openFile(final Uri uri, final String mode) throws FileNotFoundException {
		if (uri == null || mode == null) throw new IllegalArgumentException();
		final int table_id = getTableId(uri);
		final String table = getTableNameById(table_id);
		final int mode_code;
		if ("r".equals(mode)) {
			mode_code = ParcelFileDescriptor.MODE_READ_ONLY;
		} else if ("rw".equals(mode)) {
			mode_code = ParcelFileDescriptor.MODE_READ_WRITE;
		} else if ("rwt".equals(mode)) {
			mode_code = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_TRUNCATE;
		} else
			throw new IllegalArgumentException();
		if (mode_code == ParcelFileDescriptor.MODE_READ_ONLY) {
			//
		} else if ((mode_code & ParcelFileDescriptor.MODE_READ_WRITE) != 0) {
			//
		}
		switch (table_id) {
			case VIRTUAL_TABLE_ID_CACHED_IMAGES: {
				return getCachedImageFd(uri.getQueryParameter(QUERY_PARAM_URL));
			}
			case VIRTUAL_TABLE_ID_CACHE_FILES: {
				return getCacheFileFd(uri.getLastPathSegment());
			}
		}
		return null;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			switch (tableId) {
				case VIRTUAL_TABLE_ID_DATABASE_READY: {
					if (mDatabaseWrapper.isReady())
						return new MatrixCursor(projection != null ? projection : new String[0]);
					return null;
				}
				case VIRTUAL_TABLE_ID_ALL_PREFERENCES: {
					return getPreferencesCursor(mPreferences, null);
				}
				case VIRTUAL_TABLE_ID_PREFERENCES: {
					return getPreferencesCursor(mPreferences, uri.getLastPathSegment());
				}
				case VIRTUAL_TABLE_ID_DNS: {
					return getDNSCursor(uri.getLastPathSegment());
				}
				case VIRTUAL_TABLE_ID_CACHED_IMAGES: {
					return getCachedImageCursor(uri.getQueryParameter(QUERY_PARAM_URL));
				}
				case VIRTUAL_TABLE_ID_NOTIFICATIONS: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() == 2)
						return getNotificationsCursor(ParseUtils.parseInt(segments.get(1), -1));
					else
						return getNotificationsCursor();
				}
				case VIRTUAL_TABLE_ID_UNREAD_COUNTS: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() == 2)
						return getUnreadCountsCursor(ParseUtils.parseInt(segments.get(1), -1));
					else
						return getUnreadCountsCursor();
				}
				case VIRTUAL_TABLE_ID_UNREAD_COUNTS_BY_TYPE: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() != 3) return null;
					return getUnreadCountsCursorByType(segments.get(2));
				}
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() != 4) return null;
					final long accountId = ParseUtils.parseLong(segments.get(2));
					final long conversationId = ParseUtils.parseLong(segments.get(3));
					final String query = TwidereQueryBuilder.ConversationQueryBuilder.buildByConversationId(projection,
							accountId, conversationId, selection, sortOrder);
					final Cursor c = mDatabaseWrapper.rawQuery(query, selectionArgs);
					setNotificationUri(c, DirectMessages.CONTENT_URI);
					return c;
				}
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() != 4) return null;
					final long accountId = ParseUtils.parseLong(segments.get(2));
					final String screenName = segments.get(3);
					final String query = TwidereQueryBuilder.ConversationQueryBuilder.buildByScreenName(projection,
							accountId, screenName, selection, sortOrder);
					final Cursor c = mDatabaseWrapper.rawQuery(query, selectionArgs);
					setNotificationUri(c, DirectMessages.CONTENT_URI);
					return c;
				}
			}
			if (table == null) return null;
			final Cursor c = mDatabaseWrapper.query(table, projection, selection, selectionArgs, null, null, sortOrder);
			setNotificationUri(c, getNotificationUri(tableId, uri));
			return c;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			int result = 0;
			if (table != null) {
				switch (tableId) {
					case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
					case TABLE_ID_DIRECT_MESSAGES:
					case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
						return 0;
				}
				result = mDatabaseWrapper.update(table, values, selection, selectionArgs);
			}
			if (result > 0) {
				onDatabaseUpdated(tableId, uri);
			}
			return result;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private void clearNotification() {
		mNewStatuses.clear();
		mNewMentions.clear();
		mNewMessages.clear();
	}

	private void clearNotification(final int notificationType, final long accountId) {
		switch (notificationType) {
			case NOTIFICATION_ID_HOME_TIMELINE: {
				mNewStatuses.clear();
				break;
			}
			case NOTIFICATION_ID_MENTIONS: {
				mNewMentions.clear();
				break;
			}
			case NOTIFICATION_ID_DIRECT_MESSAGES: {
				mNewMessages.clear();
				break;
			}
			default: {
			}
		}
	}

	private int clearUnreadCount() {
		int result = 0;
		result += mUnreadStatuses.size();
		result += mUnreadMentions.size();
		result += mUnreadMentions.size();
		mUnreadStatuses.clear();
		mUnreadMentions.clear();
		mUnreadMessages.clear();
		saveUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
		saveUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
		saveUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
		notifyContentObserver(UnreadCounts.CONTENT_URI);
		return result;
	}

	private int clearUnreadCount(final int position) {
		final Context context = getContext();
		final int result;
		final SupportTabSpec tab = CustomTabUtils.getAddedTabAt(context, position);
		final String type = tab.type;
		if (TAB_TYPE_HOME_TIMELINE.equals(type) || TAB_TYPE_STAGGERED_HOME_TIMELINE.equals(type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			result = clearUnreadCount(mUnreadStatuses, account_ids);
			saveUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
		} else if (TAB_TYPE_MENTIONS_TIMELINE.equals(type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			result = clearUnreadCount(mUnreadMentions, account_ids);
			mUnreadMentions.clear();
			saveUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
		} else if (TAB_TYPE_DIRECT_MESSAGES.equals(type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			result = clearUnreadCount(mUnreadMessages, account_ids);
			mUnreadMessages.clear();
			saveUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
		} else
			return 0;
		if (result > 0) {
			notifyUnreadCountChanged(position);
		}
		return result;
	}

	/**
	 * Creates notifications for mentions and DMs
	 * @param pref
	 * @param type
	 * @param build
	 */
	private void createNotifications(final AccountPreferences pref, final String type,
									 final Object o_status, final boolean build) {
		NotificationContent notification = null;

		if (o_status instanceof ParcelableStatus) {
			ParcelableStatus status = (ParcelableStatus) o_status;
			notification = new NotificationContent();
			notification.setAccountId(status.account_id);
			notification.setFromUser(status.user_screen_name);
			notification.setType(type);
			notification.setMessage(status.text_unescaped);
			notification.setTimestamp(status.timestamp);
			notification.setProfileImageUrl(status.user_profile_image_url);
			notification.setOriginalStatus(status);
		} else if (o_status instanceof ParcelableDirectMessage) {
			ParcelableDirectMessage dm = (ParcelableDirectMessage) o_status;
			notification = new NotificationContent();
			notification.setAccountId(dm.account_id);
			notification.setFromUser(dm.sender_screen_name);
			notification.setType(type);
			notification.setMessage(dm.text_unescaped);
			notification.setTimestamp(dm.timestamp);
			notification.setProfileImageUrl(dm.sender_profile_image_url);
			notification.setOriginalMessage(dm);
		}
		if (notification != null) {
			mNotificationHelper.cachePushNotification(notification);
		}
		if (build) mNotificationHelper.buildNotificationByType(notification, pref, false);
	}

	private Cursor getCachedImageCursor(final String url) {
		if (Utils.isDebugBuild()) {
			Log.d(LOGTAG, String.format("getCachedImageCursor(%s)", url));
		}
		final MatrixCursor c = new MatrixCursor(TweetStore.CachedImages.MATRIX_COLUMNS);
		final File file = mImagePreloader.getCachedImageFile(url);
		if (url != null && file != null) {
			c.addRow(new String[] { url, file.getPath() });
		}
		return c;
	}

	private ParcelFileDescriptor getCachedImageFd(final String url) throws FileNotFoundException {
		if (Utils.isDebugBuild()) {
			Log.d(LOGTAG, String.format("getCachedImageFd(%s)", url));
		}
		final File file = mImagePreloader.getCachedImageFile(url);
		if (file == null) return null;
		return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
	}

	private ParcelFileDescriptor getCacheFileFd(final String name) throws FileNotFoundException {
		if (name == null) return null;
		final Context mContext = getContext();
		final File cacheDir = mContext.getCacheDir();
		final File file = new File(cacheDir, name);
		if (!file.exists()) return null;
		return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
	}

    private ContentResolver getContentResolver() {
        if (mContentResolver != null) return mContentResolver;
        final Context context = getContext();
        return mContentResolver = context.getContentResolver();
    }

	private Cursor getDNSCursor(final String host) {
		final MatrixCursor c = new MatrixCursor(TweetStore.DNS.MATRIX_COLUMNS);
		try {
			final String address = mHostAddressResolver.resolve(host);
			if (host != null && address != null) {
				c.addRow(new String[] { host, address });
			}
		} catch (final IOException e) {

		}
		return c;
	}

    private NotificationManager getNotificationManager() {
        if (mNotificationManager != null) return mNotificationManager;
        final Context context = getContext();
        return mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

	private Cursor getNotificationsCursor() {
		final MatrixCursor c = new MatrixCursor(TweetStore.Notifications.MATRIX_COLUMNS);
		c.addRow(new Integer[] { NOTIFICATION_ID_HOME_TIMELINE, mUnreadStatuses.size() });
		c.addRow(new Integer[] { NOTIFICATION_ID_MENTIONS, mNewMentions.size() });
		c.addRow(new Integer[] { NOTIFICATION_ID_DIRECT_MESSAGES, mNewMessages.size() });
		return c;
	}

	private Cursor getNotificationsCursor(final int id) {
		final MatrixCursor c = new MatrixCursor(TweetStore.Notifications.MATRIX_COLUMNS);
		if (id == NOTIFICATION_ID_HOME_TIMELINE) {
			c.addRow(new Integer[] { id, mNewStatuses.size() });
		} else if (id == NOTIFICATION_ID_MENTIONS) {
			c.addRow(new Integer[] { id, mNewMentions.size() });
		} else if (id == NOTIFICATION_ID_DIRECT_MESSAGES) {
			c.addRow(new Integer[] { id, mNewMessages.size() });
		}
		return c;
	}

	private Cursor getUnreadCountsCursor() {
		final MatrixCursor c = new MatrixCursor(TweetStore.UnreadCounts.MATRIX_COLUMNS);
		return c;
	}

	private Cursor getUnreadCountsCursor(final int position) {
		final MatrixCursor c = new MatrixCursor(TweetStore.UnreadCounts.MATRIX_COLUMNS);
		final Context context = getContext();
		final SupportTabSpec tab = CustomTabUtils.getAddedTabAt(context, position);
        if (tab == null) return c;
		final int count;
        if (TAB_TYPE_HOME_TIMELINE.equals(tab.type) || TAB_TYPE_STAGGERED_HOME_TIMELINE.equals(tab.type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			count = getUnreadCount(mUnreadStatuses, account_ids);
        } else if (TAB_TYPE_MENTIONS_TIMELINE.equals(tab.type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			count = getUnreadCount(mUnreadMentions, account_ids);
        } else if (TAB_TYPE_DIRECT_MESSAGES.equals(tab.type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			count = getUnreadCount(mUnreadMessages, account_ids);
		} else {
			count = 0;
		}
        if (tab.type != null) {
            c.addRow(new Object[] { position, tab.type, count });
		}
		return c;
	}

	private Cursor getUnreadCountsCursorByType(final String type) {
		final MatrixCursor c = new MatrixCursor(TweetStore.UnreadCounts.MATRIX_COLUMNS);
		final int count;
		if (TAB_TYPE_HOME_TIMELINE.equals(type) || TAB_TYPE_STAGGERED_HOME_TIMELINE.equals(type)) {
			count = mUnreadStatuses.size();
		} else if (TAB_TYPE_MENTIONS_TIMELINE.equals(type)) {
			count = mUnreadMentions.size();
		} else if (TAB_TYPE_DIRECT_MESSAGES.equals(type)) {
			count = mUnreadMessages.size();
		} else {
			count = 0;
		}
		if (type != null) {
			c.addRow(new Object[] { -1, type, count });
		}
		return c;
	}

	private void notifyContentObserver(final Uri uri) {
        final ContentResolver cr = getContentResolver();
        if (uri == null || cr == null) return;
        cr.notifyChange(uri, null);
	}

	private int notifyIncomingMessagesInserted(final ContentValues... values) {
		if (values == null || values.length == 0) return 0;
		// Add statuses that not filtered to list for future use.
		int result = 0;
		int i = 1;
		for (final ContentValues value : values) {
			final ParcelableDirectMessage message = new ParcelableDirectMessage(value);
			mNewMessages.add(message);
			if (mUnreadMessages.add(new UnreadItem(message.sender_id, message.account_id))) { //we got a new dm
				//DM Notification
				final AccountPreferences[] prefs = AccountPreferences.getNotificationEnabledPreferences(getContext(),
						getAccountIds(getContext()));
				final AccountPreferences pref = AccountPreferences.getAccountPreferences(prefs, message.account_id);
				if (pref != null && pref.isDirectMessagesNotificationEnabled()) {
					createNotifications(pref, NotificationContent.NOTIFICATION_TYPE_MENTION,
							message, i >= values.length);
				}
				result++;
			}
		}
		if (result > 0) {
			saveUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
		}
		return result;
	}

    private int notifyMentionsInserted(final AccountPreferences[] prefs, final ContentValues... values) {
		if (values == null || values.length == 0) return 0;
		// Add statuses that not filtered to list for future use.
		int result = 0;
		final boolean enabled = mPreferences.getBoolean(KEY_FILTERS_IN_MENTIONS, true);
		final boolean filtersForRts = mPreferences.getBoolean(KEY_FILTERS_FOR_RTS, true);
		int i = 1;
		for (final ContentValues value : values) {
			final ParcelableStatus status = new ParcelableStatus(value);
			if (!enabled || !isFiltered(mDatabaseWrapper.getSQLiteDatabase(), status, filtersForRts)) {
                final AccountPreferences pref = AccountPreferences.getAccountPreferences(prefs, status.account_id);
				if (pref == null || status.user_is_following || !pref.isNotificationFollowingOnly()) {
                    mNewMentions.add(status);
                }
				if (mUnreadMentions.add(new UnreadItem(status.id, status.account_id))) { //we got a new mention
					//Mention Notification
					if (pref != null && pref.isMentionsNotificationEnabled()) {
						createNotifications(pref, NotificationContent.NOTIFICATION_TYPE_MENTION,
								status, i >= values.length);
					}
					result++;
				}
			}
			i++;
		}
		if (result > 0) {
			saveUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
		}
		return result;
	}

	private int notifyStatusesInserted(final ContentValues... values) {
		if (values == null || values.length == 0) return 0;
		// Add statuses that not filtered to list for future use.
		int result = 0;
		final boolean enabled = mPreferences.getBoolean(KEY_FILTERS_IN_HOME_TIMELINE, true);
		final boolean filtersForRts = mPreferences.getBoolean(KEY_FILTERS_FOR_RTS, true);
		for (final ContentValues value : values) {
			final ParcelableStatus status = new ParcelableStatus(value);
			if (!enabled || !isFiltered(mDatabaseWrapper.getSQLiteDatabase(), status, filtersForRts)) {
				mNewStatuses.add(status);
				if (mUnreadStatuses.add(new UnreadItem(status.id, status.account_id))) {
					result++;
				}
			}
		}
		if (result > 0) {
			saveUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
		}
		return result;
	}

	private void notifyUnreadCountChanged(final int position) {
		final Intent intent = new Intent(BROADCAST_UNREAD_COUNT_UPDATED);
		intent.putExtra(EXTRA_TAB_POSITION, position);
		final Context context = getContext();
		context.sendBroadcast(intent);
		notifyContentObserver(UnreadCounts.CONTENT_URI);
	}

	private void onDatabaseUpdated(final int tableId, final Uri uri) {
		if (uri == null) return;
		switch (tableId) {
			case TABLE_ID_ACCOUNTS: {
				clearAccountColor();
				clearAccountName();
				break;
			}
		}
		notifyContentObserver(getNotificationUri(tableId, uri));
	}

	private void onNewItemsInserted(final Uri uri, final ContentValues... values) {
		if (uri == null || values == null || values.length == 0) return;
		preloadImages(values);
		if (!uri.getBooleanQueryParameter(QUERY_PARAM_NOTIFY, true)) return;
		switch (getTableId(uri)) {
			case TABLE_ID_STATUSES: {
				notifyStatusesInserted(values);
				final List<ParcelableStatus> items = new ArrayList<ParcelableStatus>(mNewStatuses);
				Collections.sort(items);
				//TODO Notifications for new tweets in timeline
				notifyUnreadCountChanged(NOTIFICATION_ID_HOME_TIMELINE);
				break;
			}
			case TABLE_ID_MENTIONS: {
				final AccountPreferences[] prefs = AccountPreferences.getNotificationEnabledPreferences(getContext(),
						getAccountIds(getContext()));
                notifyMentionsInserted(prefs, values);
				notifyUnreadCountChanged(NOTIFICATION_ID_MENTIONS);
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES_INBOX: {
				notifyIncomingMessagesInserted(values);
				notifyUnreadCountChanged(NOTIFICATION_ID_DIRECT_MESSAGES);
				break;
			}
		}
	}

	private void preloadImages(final ContentValues... values) {
		if (values == null) return;
		for (final ContentValues v : values) {
			if (mPreferences.getBoolean(KEY_PRELOAD_PROFILE_IMAGES, false)) {
				mImagePreloader.preloadImage(v.getAsString(Statuses.USER_PROFILE_IMAGE_URL));
				mImagePreloader.preloadImage(v.getAsString(DirectMessages.SENDER_PROFILE_IMAGE_URL));
				mImagePreloader.preloadImage(v.getAsString(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL));
			}
			if (mPreferences.getBoolean(KEY_PRELOAD_PREVIEW_IMAGES, false)) {
				final String textHtml = v.getAsString(Statuses.TEXT_HTML);
				for (final String link : MediaPreviewUtils.getSupportedLinksInStatus(textHtml)) {
					mImagePreloader.preloadImage(link);
				}
			}
		}
	}

	private int removeUnreadItems(final int tab_position, final long account_id, final long... ids) {
		if (tab_position < 0 || account_id == 0 || ids == null || ids.length == 0) return 0;
		final UnreadItem[] items = new UnreadItem[ids.length];
		for (int i = 0, j = ids.length; i < j; i++) {
			items[i] = new UnreadItem(ids[i], account_id);
		}
		return removeUnreadItems(tab_position, items);
	}

	private synchronized int removeUnreadItems(final int tab_position, final UnreadItem... items) {
		if (tab_position < 0 || items == null || items.length == 0) return 0;
		final int result;
		String notificationType = null;
		final String type = CustomTabUtils.getAddedTabTypeAt(getContext(), tab_position);
		final List<UnreadItem> arrItems = Arrays.asList(items);
		if (TAB_TYPE_HOME_TIMELINE.equals(type)) {
			final int size = mUnreadStatuses.size();
			mUnreadStatuses.removeAll(arrItems);
			result = size - mUnreadStatuses.size();
			if (result != 0) {
				saveUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
			}
		} else if (TAB_TYPE_MENTIONS_TIMELINE.equals(type)) {
			notificationType = NotificationContent.NOTIFICATION_TYPE_MENTION;
			final int size = mUnreadMentions.size();
			mUnreadMentions.removeAll(arrItems);
			result = size - mUnreadMentions.size();
			if (result != 0) {
				saveUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
			}
		} else if (TAB_TYPE_DIRECT_MESSAGES.equals(type)) {
			notificationType = NotificationContent.NOTIFICATION_TYPE_DIRECT_MESSAGE;
			final int size = mUnreadMessages.size();
			mUnreadMessages.removeAll(arrItems);
			result = size - mUnreadMessages.size();
			if (result != 0) {
				saveUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
			}
		} else
			return 0;
		if (result != 0) {
			notifyUnreadCountChanged(tab_position);
		}

		if (notificationType != null) {
			//get unique account ids from removed items
			List<Long> accountIds = new ArrayList<>();
			for (UnreadItem unreadItem : arrItems) {
				if (!accountIds.contains(unreadItem.account_id) && unreadItem.account_id > 0) {
					accountIds.add(unreadItem.account_id);
				}
			}

			if (!accountIds.isEmpty()) {
				for (Long accountId : accountIds) {
					mNotificationHelper.deleteCachedNotifications(accountId, notificationType);
				}
			} else { //remove for all accounts
				for (final long id : getAccountIds(getContext())) {
					mNotificationHelper.deleteCachedNotifications(id, notificationType);
				}
			}
		}

		return result;
	}

	private void restoreUnreadItems() {
		restoreUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
		restoreUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
		restoreUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
	}

	private void restoreUnreadItemsFile(final Collection<UnreadItem> items, final String name) {
		if (items == null || name == null) return;
		try {
			final File file = JSONFileIO.getSerializationFile(getContext(), name);
			final List<UnreadItem> restored = JSONFileIO.readArrayList(file);
			if (restored != null) {
				items.addAll(restored);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void saveUnreadItemsFile(final Collection<UnreadItem> items, final String name) {
		if (items == null || name == null) return;
		try {
			final File file = JSONFileIO.getSerializationFile(getContext(), name);
			JSONFileIO.writeArray(file, items.toArray(new UnreadItem[items.size()]));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void setNotificationUri(final Cursor c, final Uri uri) {
        final ContentResolver cr = getContentResolver();
        if (cr == null || c == null || uri == null) return;
        c.setNotificationUri(cr, uri);
	}

	private static int clearUnreadCount(final List<UnreadItem> set, final long[] accountIds) {
		if (accountIds == null) return 0;
		int count = 0;
		for (final UnreadItem item : set.toArray(new UnreadItem[set.size()])) {
            if (item != null && ArrayUtils.contains(accountIds, item.account_id) && set.remove(item)) {
				count++;
			}
		}
		return count;
	}

	private static List<ParcelableDirectMessage> getMessagesForAccounts(final List<ParcelableDirectMessage> items,
			final long accountId) {
		if (items == null) return Collections.emptyList();
		final List<ParcelableDirectMessage> result = new ArrayList<ParcelableDirectMessage>();
		for (final ParcelableDirectMessage item : items.toArray(new ParcelableDirectMessage[items.size()])) {
			if (item.account_id == accountId) {
				result.add(item);
			}
		}
		return result;
	}

    private static Cursor getPreferencesCursor(final SharedPreferencesWrapper preferences, final String key) {
		final MatrixCursor c = new MatrixCursor(TweetStore.Preferences.MATRIX_COLUMNS);
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, ?> all = preferences.getAll();
		if (key == null) {
			map.putAll(all);
		} else {
			map.put(key, all.get(key));
		}
		for (final Map.Entry<String, ?> item : map.entrySet()) {
			final Object value = item.getValue();
			final int type = getPreferenceType(value);
			c.addRow(new Object[] { item.getKey(), ParseUtils.parseString(value), type });
		}
		return c;
	}

	private static int getPreferenceType(final Object object) {
		if (object == null)
			return Preferences.TYPE_NULL;
		else if (object instanceof Boolean)
			return Preferences.TYPE_BOOLEAN;
		else if (object instanceof Integer)
			return Preferences.TYPE_INTEGER;
		else if (object instanceof Long)
			return Preferences.TYPE_LONG;
		else if (object instanceof Float)
			return Preferences.TYPE_FLOAT;
		else if (object instanceof String) return Preferences.TYPE_STRING;
		return Preferences.TYPE_INVALID;
	}

	private static List<ParcelableStatus> getStatusesForAccounts(final List<ParcelableStatus> items,
			final long accountId) {
		if (items == null) return Collections.emptyList();
		final List<ParcelableStatus> result = new ArrayList<ParcelableStatus>();
		for (final ParcelableStatus item : items.toArray(new ParcelableStatus[items.size()])) {
			if (item.account_id == accountId) {
				result.add(item);
			}
		}
		return result;
	}

	private static int getUnreadCount(final List<UnreadItem> set, final long... accountIds) {
		if (set == null || set.isEmpty()) return 0;
		int count = 0;
		for (final UnreadItem item : set.toArray(new UnreadItem[set.size()])) {
			if (item != null && ArrayUtils.contains(accountIds, item.account_id)) {
				count++;
			}
		}
		return count;
	}

	private static <T> T safeGet(final List<T> list, final int index) {
		return index >= 0 && index < list.size() ? list.get(index) : null;
	}

	private static boolean shouldReplaceOnConflict(final int table_id) {
		switch (table_id) {
			case TABLE_ID_CACHED_HASHTAGS:
			case TABLE_ID_CACHED_STATUSES:
			case TABLE_ID_CACHED_USERS:
			case TABLE_ID_FILTERED_USERS:
			case TABLE_ID_FILTERED_KEYWORDS:
			case TABLE_ID_FILTERED_SOURCES:
			case TABLE_ID_FILTERED_LINKS:
				return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private static class GetWritableDatabaseTask extends AsyncTask<Void, Void, SQLiteDatabase> {
		private final Context mContext;
		private final SQLiteOpenHelper mHelper;
		private final SQLiteDatabaseWrapper mWrapper;

		GetWritableDatabaseTask(final Context context, final SQLiteOpenHelper helper,
				final SQLiteDatabaseWrapper wrapper) {
			mContext = context;
			mHelper = helper;
			mWrapper = wrapper;
		}

		@Override
		protected SQLiteDatabase doInBackground(final Void... params) {
			return mHelper.getWritableDatabase();
		}

		@Override
		protected void onPostExecute(final SQLiteDatabase result) {
			mWrapper.setSQLiteDatabase(result);
			if (result != null) {
				mContext.sendBroadcast(new Intent(BROADCAST_DATABASE_READY));
			}
		}
	}

}
