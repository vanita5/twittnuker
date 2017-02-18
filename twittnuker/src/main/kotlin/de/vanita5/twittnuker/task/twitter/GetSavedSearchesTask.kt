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
import org.mariotaku.abstask.library.AbstractTask
import de.vanita5.twittnuker.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.model.SingleResponse
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.provider.TwidereDataStore.SavedSearches
import de.vanita5.twittnuker.util.ContentValuesCreator
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import de.vanita5.twittnuker.util.content.ContentResolverUtils

class GetSavedSearchesTask(
        private val context: Context
) : AbstractTask<Array<UserKey>, SingleResponse<Unit>, Any?>() {

    override fun doLongOperation(params: Array<UserKey>): SingleResponse<Unit> {
        val cr = context.contentResolver
        for (accountKey in params) {
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?: continue
            try {
                val searches = twitter.savedSearches
                val values = ContentValuesCreator.createSavedSearches(searches,
                        accountKey)
                val where = Expression.equalsArgs(SavedSearches.ACCOUNT_KEY)
                val whereArgs = arrayOf(accountKey.toString())
                cr.delete(SavedSearches.CONTENT_URI, where.sql, whereArgs)
                ContentResolverUtils.bulkInsert(cr, SavedSearches.CONTENT_URI, values)
            } catch (e: MicroBlogException) {
                DebugLog.w(LOGTAG, tr = e)
            }
        }
        return SingleResponse(Unit)
    }
}