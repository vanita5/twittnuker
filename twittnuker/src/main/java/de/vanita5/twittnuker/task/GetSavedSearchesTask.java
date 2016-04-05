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

package de.vanita5.twittnuker.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.sqliteqb.library.Expression;
import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.SavedSearch;
import de.vanita5.twittnuker.model.SingleResponse;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.provider.TwidereDataStore.SavedSearches;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.content.ContentResolverUtils;

public class GetSavedSearchesTask extends AbstractTask<UserKey[], SingleResponse<Object>, Object>
        implements Constants {

    private final Context mContext;

    public GetSavedSearchesTask(Context context) {
        this.mContext = context;
    }

    @Override
    public SingleResponse<Object> doLongOperation(UserKey[] params) {
        final ContentResolver cr = mContext.getContentResolver();
        for (UserKey accountKey : params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountKey, true);
            if (twitter == null) continue;
            try {
                final ResponseList<SavedSearch> searches = twitter.getSavedSearches();
                final ContentValues[] values = ContentValuesCreator.createSavedSearches(searches,
                        accountKey);
                final Expression where = Expression.equalsArgs(SavedSearches.ACCOUNT_KEY);
                final String[] whereArgs = {accountKey.toString()};
                cr.delete(SavedSearches.CONTENT_URI, where.getSQL(), whereArgs);
                ContentResolverUtils.bulkInsert(cr, SavedSearches.CONTENT_URI, values);
            } catch (TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
            }
        }
        return SingleResponse.getInstance();
    }
}