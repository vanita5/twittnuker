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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.otto.Bus;

import org.mariotaku.abstask.library.AbstractTask;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.library.MicroBlog;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.SingleResponse;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.message.FriendshipTaskEvent;
import de.vanita5.twittnuker.model.util.ParcelableCredentialsUtils;
import de.vanita5.twittnuker.model.util.ParcelableUserUtils;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.MicroBlogAPIFactory;
import de.vanita5.twittnuker.util.UserColorNameManager;
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

public abstract class AbsFriendshipOperationTask extends AbstractTask<AbsFriendshipOperationTask.Arguments,
        SingleResponse<ParcelableUser>, Object> implements Constants {

    protected final Context context;
    @FriendshipTaskEvent.Action
    protected final int action;
    @Inject
    protected Bus bus;
    @Inject
    protected AsyncTwitterWrapper twitter;
    @Inject
    protected SharedPreferencesWrapper preferences;
    @Inject
    protected UserColorNameManager manager;

    public AbsFriendshipOperationTask(Context context, @FriendshipTaskEvent.Action int action) {
        this.context = context;
        this.action = action;
        GeneralComponentHelper.build(context).inject(this);
    }


    @Override
    protected final void beforeExecute() {
        Arguments params = getParams();
        twitter.addUpdatingRelationshipId(params.accountKey, params.userKey);
        final FriendshipTaskEvent event = new FriendshipTaskEvent(action, params.accountKey,
                params.userKey);
        event.setFinished(false);
        bus.post(event);
    }

    @Override
    protected final void afterExecute(SingleResponse<ParcelableUser> result) {
        final Arguments params = getParams();
        twitter.removeUpdatingRelationshipId(params.accountKey, params.userKey);
        final FriendshipTaskEvent event = new FriendshipTaskEvent(action, params.accountKey,
                params.userKey);
        event.setFinished(true);
        if (result.hasData()) {
            final ParcelableUser user = result.getData();
            showSucceededMessage(params, user);
            event.setSucceeded(true);
            event.setUser(result.getData());
        } else {
            showErrorMessage(params, result.getException());
        }
        bus.post(event);
    }

    @Override
    public final SingleResponse<ParcelableUser> doLongOperation(final Arguments args) {
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context,
                args.accountKey);
        if (credentials == null) return SingleResponse.getInstance();
        final MicroBlog twitter = MicroBlogAPIFactory.getTwitterInstance(context, credentials, false, false);
        if (twitter == null) return SingleResponse.getInstance();
        try {
            final User user = perform(twitter, credentials, args);
            final ParcelableUser parcelableUser = ParcelableUserUtils.fromUser(user, args.accountKey);
            succeededWorker(twitter, credentials, args, parcelableUser);
            return SingleResponse.getInstance(parcelableUser, null);
        } catch (final MicroBlogException e) {
            return SingleResponse.getInstance(null, e);
        }
    }

    @NonNull
    protected abstract User perform(@NonNull MicroBlog twitter,
                                    @NonNull ParcelableCredentials credentials,
                                    @NonNull Arguments args) throws MicroBlogException;

    protected abstract void succeededWorker(@NonNull MicroBlog twitter,
                                            @NonNull ParcelableCredentials credentials,
                                            @NonNull Arguments args,
                                            @NonNull ParcelableUser user);

    protected abstract void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user);

    protected abstract void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception);

    public final void setup(@NonNull UserKey accountKey, @NonNull UserKey userKey) {
        setParams(new Arguments(accountKey, userKey));
    }

    protected static class Arguments {
        @NonNull
        protected final UserKey accountKey;
        @NonNull
        protected final UserKey userKey;

        protected Arguments(@NonNull UserKey accountKey, @NonNull UserKey userKey) {
            this.accountKey = accountKey;
            this.userKey = userKey;
        }
    }

}