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

import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.library.twitter.model.SearchQuery
import de.vanita5.twittnuker.library.twitter.model.Status
import de.vanita5.twittnuker.model.ParcelableAccount
import de.vanita5.twittnuker.model.ParcelableCredentials
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.ParcelableAccountUtils
import de.vanita5.twittnuker.util.InternalTwitterContentUtils
import de.vanita5.twittnuker.util.MicroBlogAPIFactory

open class TweetSearchLoader(
        context: Context,
        accountKey: UserKey?,
        private val query: String?,
        sinceId: String?,
        maxId: String?,
        page: Int,
        adapterData: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        override val isGapEnabled: Boolean,
        loadingMore: Boolean
) : MicroBlogAPIStatusesLoader(context, accountKey, sinceId, maxId, page, adapterData, savedStatusesArgs,
        tabPosition, fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    public override fun getStatuses(microBlog: MicroBlog,
                                    credentials: ParcelableCredentials,
                                    paging: Paging): List<Status> {
        if (query == null) throw MicroBlogException("Empty query")
        val processedQuery = processQuery(credentials, query)
        when (ParcelableAccountUtils.getAccountType(credentials)) {
            ParcelableAccount.Type.TWITTER -> {
                val query = SearchQuery(processedQuery)
                query.paging(paging)
                return microBlog.search(query)
            }
            ParcelableAccount.Type.STATUSNET -> {
                return microBlog.searchStatuses(processedQuery, paging)
            }
            ParcelableAccount.Type.FANFOU -> {
                return microBlog.searchPublicTimeline(processedQuery, paging)
            }
        }
        throw MicroBlogException("Not implemented")
    }

    protected open fun processQuery(credentials: ParcelableCredentials, query: String): String {
        if (MicroBlogAPIFactory.isTwitterCredentials(credentials)) {
            return String.format("%s exclude:retweets", query)
        }
        return query
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        return InternalTwitterContentUtils.isFiltered(database, status, true)
    }

    override fun processPaging(credentials: ParcelableCredentials, loadItemLimit: Int, paging: Paging) {
        if (MicroBlogAPIFactory.isStatusNetCredentials(credentials)) {
            paging.setRpp(loadItemLimit)
            val page = page
            if (page > 0) {
                paging.setPage(page)
            }
        } else {
            super.processPaging(credentials, loadItemLimit, paging)
        }
    }

}