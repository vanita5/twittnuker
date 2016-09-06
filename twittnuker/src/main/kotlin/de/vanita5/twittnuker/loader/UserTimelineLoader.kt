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

package de.vanita5.twittnuker.loader

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.support.annotation.WorkerThread
import android.text.TextUtils

import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.library.twitter.model.ResponseList
import de.vanita5.twittnuker.library.twitter.model.Status
import de.vanita5.twittnuker.model.ParcelableCredentials
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.InternalTwitterContentUtils

class UserTimelineLoader(
        context: Context,
        accountId: UserKey?,
        private val userId: UserKey?,
        private val screenName: String?,
        sinceId: String?,
        maxId: String?,
        data: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean
) : MicroBlogAPIStatusesLoader(context, accountId, sinceId, maxId, -1, data, savedStatusesArgs, tabPosition, fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(microBlog: MicroBlog,
                             credentials: ParcelableCredentials,
                             paging: Paging): ResponseList<Status> {
        if (userId != null) {
            return microBlog.getUserTimeline(userId.id, paging)
        } else if (screenName != null) {
            return microBlog.getUserTimelineByScreenName(screenName, paging)
        } else {
            throw MicroBlogException("Invalid user")
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        val accountId = accountKey
        if (accountId != null && userId != null && TextUtils.equals(accountId.id, userId.id))
            return false
        val retweetUserId = if (status.is_retweet) status.user_key else null
        return InternalTwitterContentUtils.isFiltered(database, retweetUserId, status.text_plain,
                status.quoted_text_plain, status.spans, status.quoted_spans, status.source,
                status.quoted_source, null, status.quoted_user_key)
    }
}