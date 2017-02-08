/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.task

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.support.v4.util.ArraySet
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
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
import de.vanita5.twittnuker.model.message.TrendsRefreshedEvent
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedHashtags
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject

class GetTrendsTask(
        private val context: Context,
        private val accountKey: UserKey,
        private val woeId: Int
) : AbstractTask<Any?, Unit, Any?>() {

    @Inject
    lateinit var bus: Bus

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun doLongOperation(param: Any?) {
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?: return
        val twitter = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        try {
            val trends = when {
                details.type == AccountType.FANFOU -> twitter.fanfouTrends
                else -> twitter.getLocationTrends(woeId).firstOrNull()
            } ?: return
            storeTrends(context.contentResolver, CachedTrends.Local.CONTENT_URI, trends)
        } catch (e: MicroBlogException) {
            DebugLog.w(LOGTAG, tr = e)
        }
    }

    override fun afterExecute(handler: Any?, result: Unit) {
        bus.post(TrendsRefreshedEvent())
    }

    private fun storeTrends(cr: ContentResolver, uri: Uri, trends: Trends) {
        val hashtags = ArraySet<String>()
        val deleteWhere = Expression.and(Expression.equalsArgs(CachedTrends.ACCOUNT_KEY),
                Expression.equalsArgs(CachedTrends.WOEID)).sql
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
        ContentResolverUtils.bulkInsert(cr, uri, allTrends.map(ParcelableTrendValuesCreator::create))
        ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, false,
                hashtags, null)
        ContentResolverUtils.bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtags.map {
            val values = ContentValues()
            values.put(CachedHashtags.NAME, it)
            return@map values
        })
    }
}