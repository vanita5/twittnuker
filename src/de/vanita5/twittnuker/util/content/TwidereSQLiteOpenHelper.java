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

package de.vanita5.twittnuker.util.content;

import static de.vanita5.twittnuker.util.content.DatabaseUpgradeHelper.safeUpgrade;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.mariotaku.querybuilder.NewColumn;
import org.mariotaku.querybuilder.SQLQueryBuilder;
import org.mariotaku.querybuilder.query.SQLCreateTableQuery;
import org.mariotaku.querybuilder.query.SQLCreateViewQuery;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.provider.TweetStore.Accounts;
import de.vanita5.twittnuker.provider.TweetStore.CachedHashtags;
import de.vanita5.twittnuker.provider.TweetStore.CachedStatuses;
import de.vanita5.twittnuker.provider.TweetStore.CachedTrends;
import de.vanita5.twittnuker.provider.TweetStore.CachedUsers;
import de.vanita5.twittnuker.provider.TweetStore.DirectMessages;
import de.vanita5.twittnuker.provider.TweetStore.Drafts;
import de.vanita5.twittnuker.provider.TweetStore.Filters;
import de.vanita5.twittnuker.provider.TweetStore.Mentions;
import de.vanita5.twittnuker.provider.TweetStore.PushNotifications;
import de.vanita5.twittnuker.provider.TweetStore.Statuses;
import de.vanita5.twittnuker.provider.TweetStore.Tabs;
import de.vanita5.twittnuker.util.TwidereQueryBuilder.ConversationsEntryQueryBuilder;
import de.vanita5.twittnuker.util.TwidereQueryBuilder.DirectMessagesQueryBuilder;

import java.util.HashMap;

public final class TwidereSQLiteOpenHelper extends SQLiteOpenHelper implements Constants {

	private final Context mContext;

	public TwidereSQLiteOpenHelper(final Context context, final String name, final int version) {
		super(context, name, null, version);
		mContext = context;
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.beginTransaction();
		db.execSQL(createTable(Accounts.TABLE_NAME, Accounts.COLUMNS, Accounts.TYPES, true));
		db.execSQL(createTable(Statuses.TABLE_NAME, Statuses.COLUMNS, Statuses.TYPES, true));
		db.execSQL(createTable(Mentions.TABLE_NAME, Mentions.COLUMNS, Mentions.TYPES, true));
		db.execSQL(createTable(Drafts.TABLE_NAME, Drafts.COLUMNS, Drafts.TYPES, true));
		db.execSQL(createTable(CachedUsers.TABLE_NAME, CachedUsers.COLUMNS, CachedUsers.TYPES, true));
		db.execSQL(createTable(CachedStatuses.TABLE_NAME, CachedStatuses.COLUMNS, CachedStatuses.TYPES, true));
		db.execSQL(createTable(CachedHashtags.TABLE_NAME, CachedHashtags.COLUMNS, CachedHashtags.TYPES, true));
		db.execSQL(createTable(Filters.Users.TABLE_NAME, Filters.Users.COLUMNS, Filters.Users.TYPES, true));
		db.execSQL(createTable(Filters.Keywords.TABLE_NAME, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true));
		db.execSQL(createTable(Filters.Sources.TABLE_NAME, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true));
		db.execSQL(createTable(Filters.Links.TABLE_NAME, Filters.Links.COLUMNS, Filters.Links.TYPES, true));
		db.execSQL(createTable(DirectMessages.Inbox.TABLE_NAME, DirectMessages.Inbox.COLUMNS,
				DirectMessages.Inbox.TYPES, true));
		db.execSQL(createTable(DirectMessages.Outbox.TABLE_NAME, DirectMessages.Outbox.COLUMNS,
				DirectMessages.Outbox.TYPES, true));
		db.execSQL(createTable(CachedTrends.Local.TABLE_NAME, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES,
				true));
		db.execSQL(createTable(Tabs.TABLE_NAME, Tabs.COLUMNS, Tabs.TYPES, true));
		db.execSQL(createTable(PushNotifications.TABLE_NAME, PushNotifications.COLUMNS, PushNotifications.TYPES, true));
		db.execSQL(createDirectMessagesView().getSQL());
		db.execSQL(createDirectMessageConversationEntriesView().getSQL());
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		handleVersionChange(db, oldVersion, newVersion);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		handleVersionChange(db, oldVersion, newVersion);
	}

	private SQLCreateViewQuery createDirectMessageConversationEntriesView() {
		final SQLCreateViewQuery.Builder qb = SQLQueryBuilder.createView(true,
				DirectMessages.ConversationEntries.TABLE_NAME);
		qb.as(ConversationsEntryQueryBuilder.build());
		return qb.build();
	}

	private SQLCreateViewQuery createDirectMessagesView() {
		final SQLCreateViewQuery.Builder qb = SQLQueryBuilder.createView(true, DirectMessages.TABLE_NAME);
		qb.as(DirectMessagesQueryBuilder.build());
		return qb.build();
	}

	private void handleVersionChange(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        final HashMap<String, String> accountsAlias = new HashMap<>();
        final HashMap<String, String> filtersAlias = new HashMap<>();
        final HashMap<String, String> draftsAlias = new HashMap<>();
		accountsAlias.put(Accounts.SCREEN_NAME, "username");
		accountsAlias.put(Accounts.NAME, "username");
		accountsAlias.put(Accounts.ACCOUNT_ID, "user_id");
		accountsAlias.put(Accounts.COLOR, "user_color");
		accountsAlias.put(Accounts.OAUTH_TOKEN_SECRET, "token_secret");
        draftsAlias.put(Drafts.MEDIAS, "medias");
		safeUpgrade(db, Accounts.TABLE_NAME, Accounts.COLUMNS, Accounts.TYPES, false, accountsAlias);
		safeUpgrade(db, Statuses.TABLE_NAME, Statuses.COLUMNS, Statuses.TYPES, true, null);
		safeUpgrade(db, Mentions.TABLE_NAME, Mentions.COLUMNS, Mentions.TYPES, true, null);
        safeUpgrade(db, Drafts.TABLE_NAME, Drafts.COLUMNS, Drafts.TYPES, false, draftsAlias);
		safeUpgrade(db, CachedUsers.TABLE_NAME, CachedUsers.COLUMNS, CachedUsers.TYPES, true, null);
		safeUpgrade(db, CachedStatuses.TABLE_NAME, CachedStatuses.COLUMNS, CachedStatuses.TYPES, false, null);
		safeUpgrade(db, CachedHashtags.TABLE_NAME, CachedHashtags.COLUMNS, CachedHashtags.TYPES, false, null);
		safeUpgrade(db, Filters.Users.TABLE_NAME, Filters.Users.COLUMNS, Filters.Users.TYPES, oldVersion < 49, null);
		safeUpgrade(db, Filters.Keywords.TABLE_NAME, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, oldVersion < 49,
				filtersAlias);
		safeUpgrade(db, Filters.Sources.TABLE_NAME, Filters.Sources.COLUMNS, Filters.Sources.TYPES, oldVersion < 49,
				filtersAlias);
		safeUpgrade(db, Filters.Links.TABLE_NAME, Filters.Links.COLUMNS, Filters.Links.TYPES, oldVersion < 49,
				filtersAlias);
		safeUpgrade(db, DirectMessages.Inbox.TABLE_NAME, DirectMessages.Inbox.COLUMNS, DirectMessages.Inbox.TYPES,
				true, null);
		safeUpgrade(db, DirectMessages.Outbox.TABLE_NAME, DirectMessages.Outbox.COLUMNS, DirectMessages.Outbox.TYPES,
				true, null);
		safeUpgrade(db, CachedTrends.Local.TABLE_NAME, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES, true, null);
		safeUpgrade(db, Tabs.TABLE_NAME, Tabs.COLUMNS, Tabs.TYPES, false, null);
		safeUpgrade(db, PushNotifications.TABLE_NAME, PushNotifications.COLUMNS, PushNotifications.TYPES, false, null);
        db.beginTransaction();
		db.execSQL(createDirectMessagesView().getSQL());
		db.execSQL(createDirectMessageConversationEntriesView().getSQL());
        db.setTransactionSuccessful();
        db.endTransaction();
	}

	private static String createTable(final String tableName, final String[] columns, final String[] types,
			final boolean createIfNotExists) {
		final SQLCreateTableQuery.Builder qb = SQLQueryBuilder.createTable(createIfNotExists, tableName);
		qb.columns(NewColumn.createNewColumns(columns, types));
		return qb.buildSQL();
	}

}
