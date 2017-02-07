/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.task

import android.content.Context
import android.net.Uri
import com.squareup.otto.Bus
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.DirectMessage
import de.vanita5.twittnuker.library.twitter.model.ErrorInfo
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.library.twitter.model.ResponseList
import de.vanita5.twittnuker.TwittnukerConstants
import de.vanita5.twittnuker.constant.loadItemLimitKey
import de.vanita5.twittnuker.model.RefreshTaskParam
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.message.GetMessagesTaskEvent
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject

abstract class GetDirectMessagesTask(
        protected val context: Context
) : AbstractTask<RefreshTaskParam, List<TwitterWrapper.MessageListResponse>, (Boolean) -> Unit>() {
    @Inject
    protected lateinit var errorInfoStore: ErrorInfoStore
    @Inject
    protected lateinit var preferences: SharedPreferencesWrapper
    @Inject
    protected lateinit var bus: Bus

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    @Throws(MicroBlogException::class)
    abstract fun getDirectMessages(twitter: MicroBlog, paging: Paging): ResponseList<DirectMessage>

    protected abstract val databaseUri: Uri

    protected abstract val isOutgoing: Boolean

    override fun doLongOperation(param: RefreshTaskParam): List<TwitterWrapper.MessageListResponse> {
        val accountKeys = param.accountKeys
        val sinceIds = param.sinceIds
        val maxIds = param.maxIds
        val result = ArrayList<TwitterWrapper.MessageListResponse>()
        var idx = 0
        val loadItemLimit = preferences[loadItemLimitKey]
        for (accountKey in accountKeys) {
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?: continue
            try {
                val paging = Paging()
                paging.setCount(loadItemLimit)
                var maxId: String? = null
                var sinceId: String? = null
                if (maxIds != null && maxIds[idx] != null) {
                    maxId = maxIds[idx]
                    paging.setMaxId(maxId)
                }
                if (sinceIds != null && sinceIds[idx] != null) {
                    sinceId = sinceIds[idx]
                    val sinceIdLong = NumberUtils.toLong(sinceId, -1)
                    //TODO handle non-twitter case
                    if (sinceIdLong != -1L) {
                        paging.sinceId((sinceIdLong - 1).toString())
                    } else {
                        paging.sinceId(sinceId)
                    }
                    if (maxIds == null || sinceIds[idx] == null) {
                        paging.setLatestResults(true)
                    }
                }
                val messages = getDirectMessages(twitter, paging)
                result.add(TwitterWrapper.MessageListResponse(accountKey, maxId, sinceId, messages))
                storeMessages(accountKey, messages, isOutgoing, true)
                errorInfoStore.remove(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey)
            } catch (e: MicroBlogException) {
                if (e.errorCode == ErrorInfo.NO_DIRECT_MESSAGE_PERMISSION) {
                    errorInfoStore[ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey] = ErrorInfoStore.CODE_NO_DM_PERMISSION
                } else if (e.isCausedByNetworkIssue) {
                    errorInfoStore[ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey] = ErrorInfoStore.CODE_NETWORK_ERROR
                }
                DebugLog.w(TwittnukerConstants.LOGTAG, tr = e)
                result.add(TwitterWrapper.MessageListResponse(accountKey, e))
            }

            idx++
        }
        return result

    }

    private fun storeMessages(accountKey: UserKey, messages: List<DirectMessage>?, isOutgoing: Boolean, notify: Boolean): Boolean {
        if (messages == null) return true
        val uri = databaseUri
        val valuesArray = messages.map { ContentValuesCreator.createDirectMessage(it, accountKey, isOutgoing) }

        // Delete all rows conflicting before new data inserted.
        //            final Expression deleteWhere = Expression.and(Expression.equals(DirectMessages.ACCOUNT_ID, accountKey),
        //                    Expression.in(new Column(DirectMessages.MESSAGE_ID), new RawItemArray(messageIds)));
        //            final Uri deleteUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, false);
        //            mResolver.delete(deleteUri, deleteWhere.getSQL(), null);


        // Insert previously fetched items.
        val insertUri = UriUtils.appendQueryParameters(uri, TwittnukerConstants.QUERY_PARAM_NOTIFY, notify)
        ContentResolverUtils.bulkInsert(context.contentResolver, insertUri, valuesArray)
        return false
    }

    override fun beforeExecute() {
        bus.post(GetMessagesTaskEvent(databaseUri, true, null))
    }

    override fun afterExecute(handler: ((Boolean) -> Unit)?, result: List<TwitterWrapper.MessageListResponse>?) {
        bus.post(GetMessagesTaskEvent(databaseUri, false, AsyncTwitterWrapper.getException(result)))
        handler?.invoke(true)
    }
}