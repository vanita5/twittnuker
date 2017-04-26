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
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.mastodon.Mastodon
import de.vanita5.microblog.library.twitter.model.Paging
import de.vanita5.microblog.library.twitter.model.SearchQuery
import de.vanita5.microblog.library.twitter.model.Status
import de.vanita5.microblog.library.twitter.model.UniversalSearchQuery
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.api.mastodon.mapToPaginated
import de.vanita5.twittnuker.extension.model.api.mastodon.toParcelable
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.extension.model.official
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.pagination.PaginatedList
import de.vanita5.twittnuker.model.pagination.Pagination
import de.vanita5.twittnuker.model.pagination.SinceMaxPagination
import de.vanita5.twittnuker.util.InternalTwitterContentUtils

open class TweetSearchLoader(
        context: Context,
        accountKey: UserKey?,
        private val query: String?,
        adapterData: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        override val isGapEnabled: Boolean,
        val local: Boolean,
        loadingMore: Boolean
) : AbsRequestStatusesLoader(context, accountKey, adapterData, savedStatusesArgs, tabPosition,
        fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        when (account.type) {
            AccountType.MASTODON -> return getMastodonStatuses(account, paging)
            else -> return getMicroBlogStatuses(account, paging).mapMicroBlogToPaginated {
                it.toParcelable(account, profileImageSize)
            }
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        return InternalTwitterContentUtils.isFiltered(database, status, true)
    }

    protected open fun processQuery(details: AccountDetails, query: String): String {
        if (details.type == AccountType.TWITTER) {
            if (details.extras?.official ?: false) {
                return smQuery(query, pagination)
            }
            return "$query exclude:retweets"
        }
        return query
    }

    override fun processPaging(paging: Paging, details: AccountDetails, loadItemLimit: Int) {
        if (details.type == AccountType.STATUSNET) {
            paging.rpp(loadItemLimit)
            pagination?.applyTo(paging)
        } else {
            super.processPaging(paging, details, loadItemLimit)
        }
    }

    private fun getMastodonStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
        if (query == null) throw MicroBlogException("Empty query")
        val tagQuery = if (query.startsWith("#")) query.substringAfter("#") else query
        return mastodon.getHashtagTimeline(tagQuery, paging, local).mapToPaginated {
            it.toParcelable(account)
        }
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): List<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        if (query == null) throw MicroBlogException("Empty query")
        val queryText = processQuery(account, query)
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
            AccountType.STATUSNET -> {
                return microBlog.searchStatuses(queryText, paging)
            }
            AccountType.FANFOU -> {
                return microBlog.searchPublicTimeline(queryText, paging)
            }
        }
        throw MicroBlogException("Not implemented")
    }

    companion object {

        fun smQuery(query: String, pagination: Pagination?): String {
            var universalQueryText = query

            if (pagination !is SinceMaxPagination) return universalQueryText
            pagination.maxId?.let { maxId ->
                universalQueryText += " max_id:$maxId"
            }
            pagination.sinceId?.let { sinceId ->
                universalQueryText += " since_id:$sinceId"
            }

            return universalQueryText
        }
    }

}