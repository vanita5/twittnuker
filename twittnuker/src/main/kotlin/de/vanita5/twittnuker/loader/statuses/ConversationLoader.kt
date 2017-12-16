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
import android.support.annotation.WorkerThread
import org.attoparser.config.ParseConfiguration
import org.attoparser.dom.DOMMarkupParser
import org.mariotaku.commons.parcel.ParcelUtils
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.mastodon.Mastodon
import de.vanita5.microblog.library.twitter.TwitterWeb
import de.vanita5.microblog.library.twitter.model.Paging
import de.vanita5.microblog.library.twitter.model.SearchQuery
import de.vanita5.microblog.library.twitter.model.Status
import de.vanita5.twittnuker.alias.MastodonStatus
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.exception.APINotSupportedException
import de.vanita5.twittnuker.extension.atto.filter
import de.vanita5.twittnuker.extension.atto.firstElementOrNull
import de.vanita5.twittnuker.extension.model.api.mastodon.toParcelable
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.makeOriginal
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.pagination.PaginatedArrayList
import de.vanita5.twittnuker.model.pagination.PaginatedList
import de.vanita5.twittnuker.model.pagination.Pagination
import de.vanita5.twittnuker.model.pagination.SinceMaxPagination
import de.vanita5.twittnuker.util.database.ContentFiltersUtils
import java.text.ParseException
import java.util.*

class ConversationLoader(
        context: Context,
        status: ParcelableStatus,
        adapterData: List<ParcelableStatus>?,
        fromUser: Boolean,
        loadingMore: Boolean
) : AbsRequestStatusesLoader(context, status.account_key, adapterData, null, -1, fromUser, loadingMore) {

    override val comparator: Comparator<ParcelableStatus>? = null

    var canLoadAllReplies: Boolean = false
        private set

    private val status = ParcelUtils.clone(status).apply { makeOriginal() }

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        return when (account.type) {
            AccountType.MASTODON -> getMastodonStatuses(account, paging).mapTo(PaginatedArrayList()) {
                it.toParcelable(account)
            }
            else -> getMicroBlogStatuses(account, paging)
        }
    }

    private fun getMastodonStatuses(account: AccountDetails, paging: Paging): List<MastodonStatus> {
        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
        canLoadAllReplies = true
        val statusContext = mastodon.getStatusContext(status.id)
        return statusContext.ancestors + statusContext.descendants
    }

    @Throws(MicroBlogException::class)
    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        canLoadAllReplies = false
        when (account.type) {
            AccountType.TWITTER -> {
                val isOfficial = account.isOfficial(context)
                canLoadAllReplies = isOfficial
                if (isOfficial) {
                    return microBlog.showConversation(status.id, paging).mapMicroBlogToPaginated {
                        it.toParcelable(account, profileImageSize)
                    }
                }
                return showConversationCompat(microBlog, account, status, true)
            }
            AccountType.STATUSNET -> {
                canLoadAllReplies = true
                status.extras?.statusnet_conversation_id?.let {
                    return microBlog.getStatusNetConversation(it, paging).mapMicroBlogToPaginated {
                        it.toParcelable(account, profileImageSize)
                    }
                }
            }
            AccountType.FANFOU -> {
                canLoadAllReplies = true
                return microBlog.getContextTimeline(status.id, paging).mapMicroBlogToPaginated {
                    it.toParcelable(account, profileImageSize)
                }
            }
            else -> {
                throw APINotSupportedException(account.type)
            }
        }
        canLoadAllReplies = true
        return showConversationCompat(microBlog, account, status, true)
    }

    @WorkerThread
    override fun shouldFilterStatus(status: ParcelableStatus): Boolean {
        return ContentFiltersUtils.isFiltered(context.contentResolver, status, false, 0)
    }

    @Throws(MicroBlogException::class)
    private fun showConversationCompat(twitter: MicroBlog, details: AccountDetails,
            status: ParcelableStatus, loadReplies: Boolean): PaginatedList<ParcelableStatus> {
        val statuses = ArrayList<Status>()
        val pagination = this.pagination as? SinceMaxPagination
        val maxId = pagination?.maxId
        val sinceId = pagination?.sinceId
        val maxSortId = pagination?.maxSortId ?: -1
        val sinceSortId = pagination?.sinceSortId ?: -1
        val noSinceMaxId = maxId == null && sinceId == null

        var nextPagination: Pagination? = null

        // Load conversations
        if (maxId != null && maxSortId < status.sort_id || noSinceMaxId) {
            var inReplyToId: String? = maxId ?: status.in_reply_to_status_id
            var count = 0
            while (inReplyToId != null && count < 10) {
                val item = twitter.showStatus(inReplyToId)
                inReplyToId = item.inReplyToStatusId
                statuses.add(item)
                count++
            }
        }
        if (loadReplies || noSinceMaxId || sinceId != null && sinceSortId > status.sort_id) {
            // Load replies
            var repliesLoaded = false
            try {
                if (details.type == AccountType.TWITTER) {
                    if (noSinceMaxId) {
                        statuses.addAll(loadTwitterWebReplies(details, twitter))
                    }
                    repliesLoaded = true
                }
            } catch (e: MicroBlogException) {
                // Ignore
            }
            if (!repliesLoaded) {
                val query = SearchQuery()
                query.count(100)
                if (details.type == AccountType.TWITTER) {
                    query.query("to:${status.user_screen_name} since_id:${status.id}")
                } else {
                    query.query("@${status.user_screen_name}")
                }
                query.sinceId(sinceId ?: status.id)
                try {
                    val queryResult = twitter.search(query)
                    val firstId = queryResult.firstOrNull()?.id
                    if (firstId != null) {
                        nextPagination = SinceMaxPagination.sinceId(firstId, 0)
                    }
                    queryResult.filterTo(statuses) { it.inReplyToStatusId == status.id }
                } catch (e: MicroBlogException) {
                    // Ignore for now
                }
            }
        }
        return statuses.mapTo(PaginatedArrayList()) {
            it.toParcelable(details, profileImageSize)
        }.apply {
            this.nextPage = nextPagination
        }
    }

    private fun loadTwitterWebReplies(details: AccountDetails, twitter: MicroBlog): List<Status> {
        val web = details.newMicroBlogInstance(context, TwitterWeb::class.java)
        val page = web.getStatusPage(status.user_screen_name, status.id).page

        val parser = DOMMarkupParser(ParseConfiguration.htmlConfiguration())
        val statusIds = ArrayList<String>()

        try {
            val document = parser.parse(page)
            val repliesElement = document.firstElementOrNull { element ->
                element.getAttributeValue("data-component-context") == "replies"
            } ?: throw MicroBlogException("No replies data found")
            repliesElement.filter {
                it.getAttributeValue("data-item-type") == "tweet" && it.hasAttribute("data-item-id")
            }.mapTo(statusIds) { it.getAttributeValue("data-item-id") }
        } catch (e: ParseException) {
            throw MicroBlogException(e)
        }
        if (statusIds.isEmpty()) {
            throw MicroBlogException("Invalid response")
        }
        return twitter.lookupStatuses(statusIds.distinct().toTypedArray())
    }
}
