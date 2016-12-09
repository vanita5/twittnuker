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

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.otto.Bus;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.abstask.library.AbstractTask;
import de.vanita5.twittnuker.library.MicroBlog;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.DirectMessage;
import de.vanita5.twittnuker.library.twitter.model.ErrorInfo;
import de.vanita5.twittnuker.library.twitter.model.Paging;
import de.vanita5.twittnuker.library.twitter.model.ResponseList;
import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.model.RefreshTaskParam;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.message.GetMessagesTaskEvent;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.ErrorInfoStore;
import de.vanita5.twittnuker.util.MicroBlogAPIFactory;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.TwitterWrapper;
import de.vanita5.twittnuker.util.UriUtils;
import de.vanita5.twittnuker.util.content.ContentResolverUtils;
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public abstract class GetDirectMessagesTask extends AbstractTask<RefreshTaskParam,
        List<TwitterWrapper.MessageListResponse>, Object> implements Constants {

    protected final Context context;
    @Inject
    protected ErrorInfoStore errorInfoStore;
    @Inject
    protected SharedPreferencesWrapper preferences;
    @Inject
    protected Bus bus;

    public GetDirectMessagesTask(Context context) {
        this.context = context;
        GeneralComponentHelper.build(context).inject(this);
    }

    public abstract ResponseList<DirectMessage> getDirectMessages(MicroBlog twitter, Paging paging)
            throws MicroBlogException;

    protected abstract Uri getDatabaseUri();

    protected abstract boolean isOutgoing();

    @Override
    public List<TwitterWrapper.MessageListResponse> doLongOperation(final RefreshTaskParam param) {
        final UserKey[] accountKeys = param.getAccountKeys();
        final String[] sinceIds = param.getSinceIds(), maxIds = param.getMaxIds();
        final List<TwitterWrapper.MessageListResponse> result = new ArrayList<>();
        int idx = 0;
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        for (final UserKey accountKey : accountKeys) {
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(context, accountKey);
            if (twitter == null) continue;
            try {
                final Paging paging = new Paging();
                paging.setCount(loadItemLimit);
                String maxId = null, sinceId = null;
                if (maxIds != null && maxIds[idx] != null) {
                    maxId = maxIds[idx];
                    paging.setMaxId(maxId);
                }
                if (sinceIds != null && sinceIds[idx] != null) {
                    sinceId = sinceIds[idx];
                    long sinceIdLong = NumberUtils.toLong(sinceId, -1);
                    //TODO handle non-twitter case
                    if (sinceIdLong != -1) {
                        paging.sinceId(String.valueOf(sinceIdLong - 1));
                    } else {
                        paging.sinceId(sinceId);
                    }
                    if (maxIds == null || sinceIds[idx] == null) {
                        paging.setLatestResults(true);
                    }
                }
                final List<DirectMessage> messages = getDirectMessages(twitter, paging);
                result.add(new TwitterWrapper.MessageListResponse(accountKey, maxId, sinceId, messages));
                storeMessages(accountKey, messages, isOutgoing(), true);
                errorInfoStore.remove(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey);
            } catch (final MicroBlogException e) {
                if (e.getErrorCode() == ErrorInfo.NO_DIRECT_MESSAGE_PERMISSION) {
                    errorInfoStore.put(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey,
                            ErrorInfoStore.CODE_NO_DM_PERMISSION);
                } else if (e.isCausedByNetworkIssue()) {
                    errorInfoStore.put(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey,
                            ErrorInfoStore.CODE_NETWORK_ERROR);
                }
                if (BuildConfig.DEBUG) {
                    Log.w(TwittnukerConstants.LOGTAG, e);
                }
                result.add(new TwitterWrapper.MessageListResponse(accountKey, e));
            }
            idx++;
        }
        return result;

    }

    private boolean storeMessages(UserKey accountKey, List<DirectMessage> messages, boolean isOutgoing, boolean notify) {
        if (messages == null) return true;
        final Uri uri = getDatabaseUri();
        final ContentValues[] valuesArray = new ContentValues[messages.size()];

        for (int i = 0, j = messages.size(); i < j; i++) {
            final DirectMessage message = messages.get(i);
            try {
                valuesArray[i] = ContentValuesCreator.createDirectMessage(message, accountKey, isOutgoing);
            } catch (IOException e) {
                return false;
            }
        }

        // Delete all rows conflicting before new data inserted.
//            final Expression deleteWhere = Expression.and(Expression.equals(DirectMessages.ACCOUNT_ID, accountKey),
//                    Expression.in(new Column(DirectMessages.MESSAGE_ID), new RawItemArray(messageIds)));
//            final Uri deleteUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, false);
//            mResolver.delete(deleteUri, deleteWhere.getSQL(), null);


        // Insert previously fetched items.
        final Uri insertUri = UriUtils.appendQueryParameters(uri, TwittnukerConstants.QUERY_PARAM_NOTIFY, notify);
        ContentResolverUtils.bulkInsert(context.getContentResolver(), insertUri, valuesArray);
        return false;
    }


    public void beforeExecute(RefreshTaskParam params) {
        bus.post(new GetMessagesTaskEvent(getDatabaseUri(), true, null));
    }

    @Override
    protected void afterExecute(Object handler, List<TwitterWrapper.MessageListResponse> result) {
        bus.post(new GetMessagesTaskEvent(getDatabaseUri(), false, AsyncTwitterWrapper.getException(result)));
    }
}