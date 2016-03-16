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

package de.vanita5.twittnuker.loader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.util.Pair;

import org.mariotaku.sqliteqb.library.Expression;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.ParcelableUserCursorIndices;
import de.vanita5.twittnuker.model.ParcelableUserValuesCreator;
import de.vanita5.twittnuker.model.SingleResponse;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.util.ParcelableCredentialsUtils;
import de.vanita5.twittnuker.model.util.ParcelableUserUtils;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedUsers;
import de.vanita5.twittnuker.task.UpdateAccountInfoTask;
import de.vanita5.twittnuker.task.util.TaskStarter;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.TwitterWrapper;

import static de.vanita5.twittnuker.util.ContentValuesCreator.createCachedUser;

public final class ParcelableUserLoader extends AsyncTaskLoader<SingleResponse<ParcelableUser>> implements Constants {

    private final boolean mOmitIntentExtra, mLoadFromCache;
    private final Bundle mExtras;
    private final UserKey mAccountKey;
    private final String mUserId;
    private final String mScreenName;

    public ParcelableUserLoader(final Context context, final UserKey accountKey, final String userId,
                                final String screenName, final Bundle extras, final boolean omitIntentExtra,
                                final boolean loadFromCache) {
        super(context);
        this.mOmitIntentExtra = omitIntentExtra;
        this.mLoadFromCache = loadFromCache;
        this.mExtras = extras;
        this.mAccountKey = accountKey;
        this.mUserId = userId;
        this.mScreenName = screenName;
    }

    @Override
    public SingleResponse<ParcelableUser> loadInBackground() {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final UserKey accountKey = mAccountKey;
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context,
                mAccountKey);
        if (credentials == null) return SingleResponse.getInstance();
        if (!mOmitIntentExtra && mExtras != null) {
            final ParcelableUser user = mExtras.getParcelable(EXTRA_USER);
            if (user != null) {
                final ContentValues values = ParcelableUserValuesCreator.create(user);
                resolver.insert(CachedUsers.CONTENT_URI, values);
                user.account_color = credentials.color;
                return SingleResponse.getInstance(user);
            }
        }
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, credentials, true, true);
        if (twitter == null) return SingleResponse.getInstance();
        if (mLoadFromCache) {
            final Expression where;
            final String[] whereArgs;
            if (mUserId != null) {
                where = Expression.equalsArgs(CachedUsers.USER_KEY);
                whereArgs = new String[]{mUserId};
            } else {
                where = Expression.equalsArgs(CachedUsers.SCREEN_NAME);
                whereArgs = new String[]{mScreenName};
            }
            final Cursor cur = resolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS,
                    where.getSQL(), whereArgs, null);
            if (cur != null) {
                try {
                    if (cur.moveToFirst()) {
                        final ParcelableUserCursorIndices indices = new ParcelableUserCursorIndices(cur);
                        final ParcelableUser user = indices.newObject(cur);
                        user.account_key = accountKey;
                        user.account_color = credentials.color;
                        return SingleResponse.getInstance(user);
                    }
                } finally {
                    cur.close();
                }
            }
        }
        try {
            final User twitterUser = TwitterWrapper.tryShowUser(twitter, mUserId, mScreenName,
                    credentials.account_type);
            final ContentValues cachedUserValues = createCachedUser(twitterUser);
            resolver.insert(CachedUsers.CONTENT_URI, cachedUserValues);
            final ParcelableUser user = ParcelableUserUtils.fromUser(twitterUser, accountKey);
            user.account_color = credentials.color;
            return SingleResponse.getInstance(user);
        } catch (final TwitterException e) {
            Log.w(LOGTAG, e);
            return SingleResponse.getInstance(e);
        }
    }

    @Override
    protected void onStartLoading() {
        if (!mOmitIntentExtra && mExtras != null) {
            final ParcelableUser user = mExtras.getParcelable(EXTRA_USER);
            if (user != null) {
                deliverResult(SingleResponse.getInstance(user));
            }
        }
        forceLoad();
    }

    @Override
    public void deliverResult(SingleResponse<ParcelableUser> data) {
        super.deliverResult(data);
        if (data.hasData()) {
            final ParcelableUser user = data.getData();
            if (user.is_cache) return;
            final UpdateAccountInfoTask task = new UpdateAccountInfoTask(getContext());
            task.setParams(Pair.create(mAccountKey, user));
            TaskStarter.execute(task);
        }
    }
}