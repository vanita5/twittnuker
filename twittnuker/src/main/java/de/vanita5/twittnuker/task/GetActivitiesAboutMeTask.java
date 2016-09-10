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

import de.vanita5.twittnuker.library.MicroBlog;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.Activity;
import de.vanita5.twittnuker.library.twitter.model.CursorTimestampResponse;
import de.vanita5.twittnuker.library.twitter.model.Paging;
import de.vanita5.twittnuker.library.twitter.model.ResponseList;
import de.vanita5.twittnuker.library.twitter.model.Status;
import de.vanita5.twittnuker.annotation.ReadPositionTag;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.util.ParcelableAccountUtils;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
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
    protected void saveReadPosition(@NonNull UserKey accountKey, ParcelableCredentials credentials, @NonNull MicroBlog twitter) {
        if (ParcelableAccount.Type.TWITTER.equals(ParcelableAccountUtils.getAccountType(credentials))) {
            if (Utils.isOfficialCredentials(getContext(), credentials)) {
                try {
                    CursorTimestampResponse response = twitter.getActivitiesAboutMeUnread(true);
                    final String tag = Utils.getReadPositionTagWithAccount(ReadPositionTag.ACTIVITIES_ABOUT_ME,
                            accountKey);
                    getReadStateManager().setPosition(tag, response.getCursor(), false);
                } catch (MicroBlogException e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    protected ResponseList<Activity> getActivities(@NonNull final MicroBlog twitter,
                                                   @NonNull final ParcelableCredentials credentials,
                                                   @NonNull final Paging paging) throws MicroBlogException {
        if (Utils.isOfficialCredentials(getContext(), credentials)) {
            return twitter.getActivitiesAboutMe(paging);
        }
        final ResponseList<Activity> activities = new ResponseList<>();
        final ResponseList<Status> statuses;
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.FANFOU: {
                statuses = twitter.getMentions(paging);
                break;
            }
            default: {
                statuses = twitter.getMentionsTimeline(paging);
                break;
            }
        }
        for (Status status : statuses) {
            activities.add(Activity.fromMention(credentials.account_key.getId(), status));
        }
        return activities;
    }

    @Override
    protected Uri getContentUri() {
        return Activities.AboutMe.CONTENT_URI;
    }
}