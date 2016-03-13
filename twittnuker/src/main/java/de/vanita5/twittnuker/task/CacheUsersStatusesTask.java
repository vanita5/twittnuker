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

import com.twitter.Extractor;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedHashtags;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedStatuses;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedUsers;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.InternalTwitterContentUtils;
import de.vanita5.twittnuker.util.TwitterWrapper.TwitterListResponse;
import de.vanita5.twittnuker.util.content.ContentResolverUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CacheUsersStatusesTask extends AbstractTask<TwitterListResponse<Status>, Object, Object> implements Constants {

    private final Context context;

    public CacheUsersStatusesTask(final Context context) {
        this.context = context;
    }

    @Override
    public final Object doLongOperation(final TwitterListResponse<Status> params) {
        final ContentResolver resolver = context.getContentResolver();
        final Extractor extractor = new Extractor();

        if (params == null || !params.hasData()) {
            return null;
        }
        final List<Status> list = params.getData();
        for (int bulkIdx = 0, totalSize = list.size(); bulkIdx < totalSize; bulkIdx += 100) {
            for (int idx = bulkIdx, end = Math.min(totalSize, bulkIdx + ContentResolverUtils.MAX_BULK_COUNT); idx < end; idx++) {
                final Status status = list.get(idx);

                final Set<ContentValues> usersValues = new HashSet<>();
                final Set<ContentValues> statusesValues = new HashSet<>();
                final Set<ContentValues> hashTagValues = new HashSet<>();

                final UserKey accountKey = params.mAccountKey;
                statusesValues.add(ContentValuesCreator.createStatus(status, accountKey));
                final String text = InternalTwitterContentUtils.unescapeTwitterStatusText(status.getText());
                for (final String hashtag : extractor.extractHashtags(text)) {
                    final ContentValues values = new ContentValues();
                    values.put(CachedHashtags.NAME, hashtag);
                    hashTagValues.add(values);
                }
                final ContentValues cachedUser = ContentValuesCreator.createCachedUser(status.getUser());
                cachedUser.put(CachedUsers.LAST_SEEN, System.currentTimeMillis());
                usersValues.add(cachedUser);
                if (status.isRetweet()) {
                    final ContentValues cachedRetweetedUser = ContentValuesCreator.createCachedUser(status.getRetweetedStatus().getUser());
                    cachedRetweetedUser.put(CachedUsers.LAST_SEEN, System.currentTimeMillis());
                    usersValues.add(cachedRetweetedUser);
                }

                ContentResolverUtils.bulkInsert(resolver, CachedStatuses.CONTENT_URI, statusesValues);
                ContentResolverUtils.bulkInsert(resolver, CachedHashtags.CONTENT_URI, hashTagValues);
                ContentResolverUtils.bulkInsert(resolver, CachedUsers.CONTENT_URI, usersValues);
            }
        }
        return null;
    }

}