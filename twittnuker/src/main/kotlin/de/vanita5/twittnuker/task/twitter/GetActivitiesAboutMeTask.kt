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
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.*
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.annotation.ReadPositionTag
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities
import de.vanita5.twittnuker.task.twitter.GetActivitiesTask
import de.vanita5.twittnuker.util.ErrorInfoStore
import de.vanita5.twittnuker.util.Utils

class GetActivitiesAboutMeTask(context: Context) : GetActivitiesTask(context) {

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_INTERACTIONS

    override fun saveReadPosition(accountKey: UserKey, details: AccountDetails, twitter: MicroBlog) {
        if (AccountType.TWITTER == details.type && details.isOfficial(context)) {
            try {
                val response = twitter.getActivitiesAboutMeUnread(true)
                val tag = Utils.getReadPositionTagWithAccount(ReadPositionTag.ACTIVITIES_ABOUT_ME,
                        accountKey)
                readStateManager.setPosition(tag, response.cursor, false)
            } catch (e: MicroBlogException) {
                // Ignore
            }
        }
    }

    @Throws(MicroBlogException::class)
    override fun getActivities(twitter: MicroBlog, details: AccountDetails, paging: Paging): ResponseList<Activity> {
        if (details.isOfficial(context)) {
            return twitter.getActivitiesAboutMe(paging)
        }
        val activities = ResponseList<Activity>()
        val statuses: ResponseList<Status>
        when (details.type) {
            AccountType.FANFOU -> {
                statuses = twitter.getMentions(paging)
            }
            else -> {
                statuses = twitter.getMentionsTimeline(paging)
            }
        }
        statuses.mapTo(activities) { InternalActivityCreator.status(details.key.id, it) }
        return activities
    }

    override val contentUri: Uri
        get() = Activities.AboutMe.CONTENT_URI
}