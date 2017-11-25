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
import de.vanita5.microblog.library.twitter.model.Activity
import de.vanita5.microblog.library.twitter.model.InternalActivityCreator
import de.vanita5.microblog.library.twitter.model.Paging
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.annotation.FilterScope
import de.vanita5.twittnuker.annotation.ReadPositionTag
import de.vanita5.twittnuker.extension.api.batchGetRelationships
import de.vanita5.twittnuker.extension.model.api.mastodon.toParcelable
import de.vanita5.twittnuker.extension.model.api.microblog.toParcelable
import de.vanita5.twittnuker.extension.model.extractFanfouHashtags
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.fragment.InteractionsTimelineFragment
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableActivity
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.task.GetTimelineResult
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities
import de.vanita5.twittnuker.util.ErrorInfoStore
import de.vanita5.twittnuker.util.sync.TimelineSyncManager

class GetActivitiesAboutMeTask(context: Context) : GetActivitiesTask(context) {

    override val errorInfoKey: String = ErrorInfoStore.KEY_INTERACTIONS
    override val filterScopes: Int = FilterScope.INTERACTIONS
    override val contentUri: Uri = Activities.AboutMe.CONTENT_URI

    private val profileImageSize = context.getString(R.string.profile_image_size)

    @Throws(MicroBlogException::class)
    override fun getActivities(account: AccountDetails, paging: Paging): GetTimelineResult<ParcelableActivity> {
        when (account.type) {
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                val notifications = mastodon.getNotifications(paging)
                val userIds = notifications.flatMapTo(HashSet()) {
                    val mapResult = mutableSetOf<String>()
                    it?.account?.id?.addTo(mapResult)
                    it.status?.account?.id?.addTo(mapResult)
                    return@flatMapTo mapResult
                }
                val relationships = mastodon.batchGetRelationships(userIds)
                val activities = notifications.mapNotNull {
                    val activity = it.toParcelable(account, relationships)
                    if (activity.action == Activity.Action.INVALID) return@mapNotNull null
                    return@mapNotNull activity
                }
                return GetTimelineResult(account, activities, activities.flatMap {
                    it.sources?.toList().orEmpty()
                }, notifications.flatMapTo(HashSet()) { notification ->
                    notification.status?.tags?.map { it.name }.orEmpty()
                })
            }
            AccountType.TWITTER -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                if (account.isOfficial(context)) {
                    val timeline = microBlog.getActivitiesAboutMe(paging)
                    val activities = timeline.map {
                        it.toParcelable(account, profileImageSize = profileImageSize)
                    }

                    return GetTimelineResult(account, activities, activities.flatMap {
                        it.sources?.toList().orEmpty()
                    }, timeline.flatMapTo(HashSet()) { activity ->
                        val mapResult = mutableSetOf<String>()
                        activity.targetStatuses?.flatMapTo(mapResult) { status ->
                            status.entities?.hashtags?.map { it.text }.orEmpty()
                        }
                        activity.targetObjectStatuses?.flatMapTo(mapResult) { status ->
                            status.entities?.hashtags?.map { it.text }.orEmpty()
                        }
                        return@flatMapTo mapResult
                    })
                }
            }
            AccountType.FANFOU -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                val activities = microBlog.getMentions(paging).map {
                    InternalActivityCreator.status(it, account.key.id).toParcelable(account,
                            profileImageSize = profileImageSize)
                }
                return GetTimelineResult(account, activities, activities.flatMap {
                    it.sources?.toList().orEmpty()
                }, activities.flatMap { it.extractFanfouHashtags() })
            }
        }
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        val timeline = microBlog.getMentionsTimeline(paging)
        val activities = timeline.map {
            InternalActivityCreator.status(it, account.key.id).toParcelable(account,
                    profileImageSize = profileImageSize)
        }
        return GetTimelineResult(account, activities, activities.flatMap {
            it.sources?.toList().orEmpty()
        }, timeline.flatMap {
            it.entities?.hashtags?.map { it.text }.orEmpty()
        })
    }

    override fun syncFetchReadPosition(manager: TimelineSyncManager, accountKeys: Array<UserKey>) {
        val tag = InteractionsTimelineFragment.getTimelineSyncTag(accountKeys)
        manager.fetchSingle(ReadPositionTag.ACTIVITIES_ABOUT_ME, tag)
    }
}