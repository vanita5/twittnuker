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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.sqliteqb.library.Expression;
import de.vanita5.twittnuker.adapter.ParcelableActivitiesAdapter;
import de.vanita5.twittnuker.annotation.ReadPositionTag;
import de.vanita5.twittnuker.api.twitter.model.Activity;
import de.vanita5.twittnuker.model.RefreshTaskParam;
import de.vanita5.twittnuker.model.tab.extra.InteractionsTabExtras;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
import de.vanita5.twittnuker.util.ErrorInfoStore;

public class InteractionsTimelineFragment extends CursorActivitiesFragment {

    @Override
    public boolean getActivities(RefreshTaskParam param) {
        mTwitterWrapper.getActivitiesAboutMeAsync(param);
        return true;
    }

    @NonNull
    @Override
    protected String getErrorInfoKey() {
        return ErrorInfoStore.KEY_INTERACTIONS;
    }

    @Override
    public Uri getContentUri() {
        return Activities.AboutMe.CONTENT_URI;
    }

    @Override
    protected int getNotificationType() {
        return NOTIFICATION_ID_INTERACTIONS_TIMELINE;
    }

    @Override
    protected boolean isFilterEnabled() {
        return true;
    }

    @Override
    protected void updateRefreshState() {
    }

    @Override
    @NonNull
    protected Where processWhere(@NonNull Expression where, @NonNull String[] whereArgs) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final InteractionsTabExtras extras = arguments.getParcelable(EXTRA_EXTRAS);
            if (extras != null && extras.isMentionsOnly()) {
                final Expression expression = Expression.and(where, Expression.inArgs(Activities.ACTION, 3));
                return new Where(expression, ArrayUtils.addAll(whereArgs, Activity.Action.MENTION,
                        Activity.Action.REPLY, Activity.Action.QUOTE));
            }
        }
        return super.processWhere(where, whereArgs);
    }

    @NonNull
    @Override
    protected ParcelableActivitiesAdapter onCreateAdapter(Context context, boolean compact) {
        final ParcelableActivitiesAdapter adapter = new ParcelableActivitiesAdapter(context, compact, false);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final InteractionsTabExtras extras = arguments.getParcelable(EXTRA_EXTRAS);
            if (extras != null) {
                adapter.setFollowingOnly(extras.isMyFollowingOnly());
                adapter.setMentionsOnly(extras.isMentionsOnly());
            }
        }
        return adapter;
    }

    @Nullable
    @Override
    @ReadPositionTag
    protected String getReadPositionTag() {
        return ReadPositionTag.ACTIVITIES_ABOUT_ME;
    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

}