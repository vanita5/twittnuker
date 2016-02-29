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
import android.net.Uri;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Trends;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.content.ContentResolverUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class GetTrendsTask extends AbstractTask<Object, Object, Object> {

    private final Context mContext;
    private final long mAccountId;

    public GetTrendsTask(Context context, final long accountId) {
        this.mContext = context;
        this.mAccountId = accountId;
    }

    public abstract List<Trends> getTrends(@NonNull Twitter twitter) throws TwitterException;

    @Override
    public Object doLongOperation(final Object param) {
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
        if (twitter == null) return null;
        try {
            final List<Trends> trends = getTrends(twitter);
            storeTrends(mContext.getContentResolver(), getContentUri(), trends);
            return null;
        } catch (final TwitterException e) {
            return null;
        }
    }

    protected abstract Uri getContentUri();

    private static void storeTrends(ContentResolver cr, Uri uri, List<Trends> trendsList) {
        final ArrayList<String> hashtags = new ArrayList<>();
        final ArrayList<ContentValues> hashtagValues = new ArrayList<>();
        if (trendsList != null && trendsList.size() > 0) {
            final ContentValues[] valuesArray = ContentValuesCreator.createTrends(trendsList);
            for (final ContentValues values : valuesArray) {
                final String hashtag = values.getAsString(TwidereDataStore.CachedTrends.NAME).replaceFirst("#", "");
                if (hashtags.contains(hashtag)) {
                    continue;
                }
                hashtags.add(hashtag);
                final ContentValues hashtagValue = new ContentValues();
                hashtagValue.put(TwidereDataStore.CachedHashtags.NAME, hashtag);
                hashtagValues.add(hashtagValue);
            }
            cr.delete(uri, null, null);
            ContentResolverUtils.bulkInsert(cr, uri, valuesArray);
            ContentResolverUtils.bulkDelete(cr, TwidereDataStore.CachedHashtags.CONTENT_URI, TwidereDataStore.CachedHashtags.NAME, hashtags, null, true);
            ContentResolverUtils.bulkInsert(cr, TwidereDataStore.CachedHashtags.CONTENT_URI,
                    hashtagValues.toArray(new ContentValues[hashtagValues.size()]));
        }
    }
}