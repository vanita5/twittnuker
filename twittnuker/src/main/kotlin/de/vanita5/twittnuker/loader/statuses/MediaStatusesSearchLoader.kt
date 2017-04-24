/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.loader.statuses

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.support.annotation.WorkerThread
import org.mariotaku.ktextension.isNullOrEmpty
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.library.twitter.model.SearchQuery
import de.vanita5.twittnuker.library.twitter.model.Status
import de.vanita5.twittnuker.library.twitter.model.UniversalSearchQuery
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.extension.model.official
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.InternalTwitterContentUtils

open class MediaStatusesSearchLoader(
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
) : AbsRequestStatusesLoader(context, accountKey, sinceId, maxId, page, adapterData, savedStatusesArgs,
        tabPosition, fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): List<ParcelableStatus> {
        return getMicroBlogStatuses(account, paging).map {
            it.toParcelable(account.key, account.type, profileImageSize)
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        if (status.media.isNullOrEmpty()) return true
        return InternalTwitterContentUtils.isFiltered(database, status, true)
    }

    override fun processPaging(details: AccountDetails, loadItemLimit: Int, paging: Paging) {
        if (details.type == AccountType.STATUSNET) {
            paging.rpp(loadItemLimit)
            val page = page
            if (page > 0) {
                paging.page(page)
        }
        } else {
            super.processPaging(details, loadItemLimit, paging)
        }
    }

    protected open fun processQuery(details: AccountDetails, query: String): String {
        if (details.type == AccountType.TWITTER) {
            if (details.extras?.official ?: false) {
                return smQuery(query)
            }
            return "$query exclude:retweets filter:media"
        }
        return query
    }

    protected fun smQuery(query: String): String {
        var universalQueryText = "$query filter:media"
        if (maxId != null) {
            universalQueryText += " max_id:$maxId"
        }
        if (sinceId != null) {
            universalQueryText += " since_id:$sinceId"
        }
        return universalQueryText
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): List<Status> {
        if (query == null) throw MicroBlogException("Empty query")
        val queryText = processQuery(account, query)
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.extras?.official ?: false) {
                    val universalQuery = UniversalSearchQuery(queryText)
                    universalQuery.setModules(UniversalSearchQuery.Module.TWEET)
                    universalQuery.setResultType(UniversalSearchQuery.ResultType.RECENT)
                    universalQuery.setPaging(paging)
                    val searchResult = microBlog.universalSearch(universalQuery)
                    return searchResult.modules.mapNotNull { it.status?.data }
                }

                val searchQuery = SearchQuery(queryText)
                searchQuery.paging(paging)
                return microBlog.search(searchQuery)
            }
        }
        throw MicroBlogException("Not implemented")
    }

}