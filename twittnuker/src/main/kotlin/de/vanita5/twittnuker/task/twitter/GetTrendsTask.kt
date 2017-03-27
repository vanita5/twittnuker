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

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.support.v4.util.ArraySet
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Trends
import org.mariotaku.sqliteqb.library.Expression.and
import org.mariotaku.sqliteqb.library.Expression.equalsArgs
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.annotation.AccountType.FANFOU
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.ParcelableTrend
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.TrendsRefreshedEvent
import de.vanita5.twittnuker.model.util.AccountUtils.getAccountDetails
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedHashtags
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends
import de.vanita5.twittnuker.task.BaseAbstractTask
import de.vanita5.twittnuker.util.DebugLog.w
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import de.vanita5.twittnuker.util.content.ContentResolverUtils.bulkInsert
import java.util.*

class GetTrendsTask(
        context: Context,
        private val accountKey: UserKey,
        private val woeId: Int
) : BaseAbstractTask<Any?, Unit, Any?>(context) {

    override fun doLongOperation(param: Any?) {
        val details = getAccountDetails(AccountManager.get(context), accountKey, true) ?: return
        val twitter = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        try {
            val trends = when (details.type) {
                FANFOU -> twitter.fanfouTrends
                else -> twitter.getLocationTrends(woeId).firstOrNull()
            } ?: return
            storeTrends(context.contentResolver, CachedTrends.Local.CONTENT_URI, trends)
        } catch (e: MicroBlogException) {
            w(LOGTAG, tr = e)
        }
    }

    override fun afterExecute(callback: Any?, result: Unit) {
        bus.post(TrendsRefreshedEvent())
    }

    private fun storeTrends(cr: ContentResolver, uri: Uri, trends: Trends) {
        val hashtags = ArraySet<String>()
        val deleteWhere = and(equalsArgs(CachedTrends.ACCOUNT_KEY), equalsArgs(CachedTrends.WOEID)).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), woeId.toString())
        cr.delete(CachedTrends.Local.CONTENT_URI, deleteWhere, deleteWhereArgs)

        val allTrends = ArrayList<ParcelableTrend>()

        trends.trends.forEachIndexed { idx, trend ->
            val hashtag = trend.name.replaceFirst("#", "")
            hashtags.add(hashtag)
            allTrends.add(ParcelableTrend().apply {
                this.account_key = accountKey
                this.woe_id = woeId
                this.name = trend.name
                this.timestamp = System.currentTimeMillis()
                this.trend_order = idx
            })
        }
        val creator = ObjectCursor.valuesCreatorFrom(ParcelableTrend::class.java)
        bulkInsert(cr, uri, allTrends.map(creator::create))
        ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, false,
                hashtags, null, null)
        bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtags.map {
            val values = ContentValues()
            values.put(CachedHashtags.NAME, it)
            return@map values
        })
    }
}