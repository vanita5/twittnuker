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
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.library.twitter.model.ResponseList
import de.vanita5.twittnuker.library.twitter.model.Status
import de.vanita5.twittnuker.annotation.ReadPositionTag
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.util.ErrorInfoStore
import de.vanita5.twittnuker.util.Utils
import java.io.IOException

class GetHomeTimelineTask(context: Context) : GetStatusesTask(context) {

    override val contentUri: Uri
        get() = Statuses.CONTENT_URI

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_HOME_TIMELINE

    @Throws(MicroBlogException::class)
    override fun getStatuses(twitter: MicroBlog, paging: Paging): ResponseList<Status> {
        return twitter.getHomeTimeline(paging)
    }

    override fun setLocalReadPosition(accountKey: UserKey, details: AccountDetails, twitter: MicroBlog) {
        val syncManager = timelineSyncManagerFactory.get() ?: return
        try {
            val tag = Utils.getReadPositionTagWithAccount(ReadPositionTag.HOME_TIMELINE, accountKey)
            syncManager.blockingGetPosition(ReadPositionTag.HOME_TIMELINE, tag)
        }catch (e: IOException) {

        }
    }
}