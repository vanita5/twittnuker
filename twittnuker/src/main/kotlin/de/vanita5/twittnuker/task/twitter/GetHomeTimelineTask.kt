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

package de.vanita5.twittnuker.task.twitter

import android.content.Context
import android.net.Uri
import org.mariotaku.ktextension.addTo
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.mastodon.Mastodon
import de.vanita5.microblog.library.twitter.model.Paging
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.annotation.FilterScope
import de.vanita5.twittnuker.annotation.ReadPositionTag
import de.vanita5.twittnuker.extension.model.api.mastodon.toParcelable
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.extractFanfouHashtags
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.fragment.HomeTimelineFragment
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.task.GetTimelineResult
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.util.ErrorInfoStore
import de.vanita5.twittnuker.util.sync.TimelineSyncManager

class GetHomeTimelineTask(context: Context) : GetStatusesTask(context) {

    override val contentUri: Uri = Statuses.CONTENT_URI

    override val filterScopes: Int = FilterScope.HOME

    override val errorInfoKey: String = ErrorInfoStore.KEY_HOME_TIMELINE

    private val profileImageSize = context.getString(R.string.profile_image_size)

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): GetTimelineResult<ParcelableStatus> {
        when (account.type) {
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                val timeline = mastodon.getHomeTimeline(paging)
                return GetTimelineResult(account, timeline.map {
                    it.toParcelable(account)
                }, timeline.flatMap { status ->
                    val mapResult = mutableListOf(status.account.toParcelable(account))
                    status.reblog?.account?.toParcelable(account)?.addTo(mapResult)
                    return@flatMap mapResult
                }, timeline.flatMap { status ->
                    status.tags?.map { it.name }.orEmpty()
                })
            }
            else -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                val timeline = microBlog.getHomeTimeline(paging)
                val statuses = timeline.map {
                    it.toParcelable(account, profileImageSize)
                }
                val hashtags = if (account.type == AccountType.FANFOU) statuses.flatMap { status ->
                    return@flatMap status.extractFanfouHashtags()
                } else timeline.flatMap { status ->
                    status.entities?.hashtags?.map { it.text }.orEmpty()
                }
                return GetTimelineResult(account, statuses, timeline.flatMap { status ->
                    val mapResult = mutableListOf(status.user.toParcelable(account,
                            profileImageSize = profileImageSize))
                    status.retweetedStatus?.user?.toParcelable(account,
                            profileImageSize = profileImageSize)?.addTo(mapResult)
                    status.quotedStatus?.user?.toParcelable(account,
                            profileImageSize = profileImageSize)?.addTo(mapResult)
                    return@flatMap mapResult
                }, hashtags)
            }
        }
    }

    override fun syncFetchReadPosition(manager: TimelineSyncManager, accountKeys: Array<UserKey>) {
        val tag = HomeTimelineFragment.getTimelineSyncTag(accountKeys)
        manager.fetchSingle(ReadPositionTag.HOME_TIMELINE, tag)
    }
}