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

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.annotation.ReadPositionTag;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Activity;
import de.vanita5.twittnuker.api.twitter.model.CursorTimestampResponse;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.task.twitter.GetActivitiesTask;
import de.vanita5.twittnuker.util.ErrorInfoStore;
import de.vanita5.twittnuker.util.Utils;

public class GetActivitiesAboutMeTask extends GetActivitiesTask {

    public GetActivitiesAboutMeTask(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected String getErrorInfoKey() {
        return ErrorInfoStore.KEY_INTERACTIONS;
    }

    @Override
    protected void saveReadPosition(long accountId, Twitter twitter) {
        try {
            CursorTimestampResponse response = twitter.getActivitiesAboutMeUnread(true);
            final String tag = Utils.getReadPositionTagWithAccounts(ReadPositionTag.ACTIVITIES_ABOUT_ME, accountId);
            readStateManager.setPosition(tag, response.getCursor(), false);
        } catch (TwitterException e) {
            // Ignore
        }
    }

    @Override
    protected ResponseList<Activity> getActivities(@NonNull final Twitter twitter, final long accountId, final Paging paging) throws TwitterException {
        if (Utils.shouldUsePrivateAPIs(context, accountId)) {
            return twitter.getActivitiesAboutMe(paging);
        }
        final ResponseList<Activity> activities = new ResponseList<>();
        for (Status status : twitter.getMentionsTimeline(paging)) {
            activities.add(Activity.fromMention(accountId, status));
        }
        return activities;
    }

    @Override
    protected Uri getContentUri() {
        return TwidereDataStore.Activities.AboutMe.CONTENT_URI;
    }
}