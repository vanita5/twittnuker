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
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Trends
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.ParcelableTrend
import de.vanita5.twittnuker.model.ParcelableTrendValuesCreator
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.TrendsRefreshedEvent
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedHashtags
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import java.util.*

class GetTrendsTask(
        context: android.content.Context,
        private val accountKey: de.vanita5.twittnuker.model.UserKey,
        private val woeId: Int
) : de.vanita5.twittnuker.task.BaseAbstractTask<Any?, Unit, Any?>(context) {

    override fun doLongOperation(param: Any?) {
        val details = de.vanita5.twittnuker.model.util.AccountUtils.getAccountDetails(android.accounts.AccountManager.get(context), accountKey, true) ?: return
        val twitter = details.newMicroBlogInstance(context, cls = de.vanita5.twittnuker.library.MicroBlog::class.java)
        try {
            val trends = when {
                details.type == de.vanita5.twittnuker.annotation.AccountType.FANFOU -> twitter.fanfouTrends
                else -> twitter.getLocationTrends(woeId).firstOrNull()
            } ?: return
            storeTrends(context.contentResolver, de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends.Local.CONTENT_URI, trends)
        } catch (e: de.vanita5.twittnuker.library.MicroBlogException) {
            de.vanita5.twittnuker.util.DebugLog.w(LOGTAG, tr = e)
        }
    }

    override fun afterExecute(handler: Any?, result: Unit) {
        bus.post(de.vanita5.twittnuker.model.event.TrendsRefreshedEvent())
    }

    private fun storeTrends(cr: android.content.ContentResolver, uri: android.net.Uri, trends: de.vanita5.twittnuker.library.twitter.model.Trends) {
        val hashtags = android.support.v4.util.ArraySet<String>()
        val deleteWhere = org.mariotaku.sqliteqb.library.Expression.and(org.mariotaku.sqliteqb.library.Expression.equalsArgs(de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends.ACCOUNT_KEY),
                org.mariotaku.sqliteqb.library.Expression.equalsArgs(de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends.WOEID)).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), woeId.toString())
        cr.delete(de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends.Local.CONTENT_URI, deleteWhere, deleteWhereArgs)

        val allTrends = java.util.ArrayList<de.vanita5.twittnuker.model.ParcelableTrend>()

        trends.trends.forEachIndexed { idx, trend ->
            val hashtag = trend.name.replaceFirst("#", "")
            hashtags.add(hashtag)
            allTrends.add(de.vanita5.twittnuker.model.ParcelableTrend().apply {
                this.account_key = accountKey
                this.woe_id = woeId
                this.name = trend.name
                this.timestamp = System.currentTimeMillis()
                this.trend_order = idx
            })
        }
        de.vanita5.twittnuker.util.content.ContentResolverUtils.bulkInsert(cr, uri, allTrends.map(de.vanita5.twittnuker.model.ParcelableTrendValuesCreator::create))
        ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, false,
                hashtags, null, null)
        ContentResolverUtils.bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtags.map {
            val values = ContentValues()
            values.put(CachedHashtags.NAME, it)
            return@map values
        })
    }
}