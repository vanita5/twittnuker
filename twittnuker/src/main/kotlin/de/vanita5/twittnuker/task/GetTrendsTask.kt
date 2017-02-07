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

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Trends
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.message.TrendsRefreshedEvent
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedHashtags
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends
import de.vanita5.twittnuker.util.ContentValuesCreator
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import org.mariotaku.sqliteqb.library.Expression
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
        val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?: return
        try {
            val trends = twitter.getLocationTrends(woeId)
            storeTrends(context.contentResolver, CachedTrends.Local.CONTENT_URI, trends)
            return
        } catch (e: MicroBlogException) {
            return
        }

    }

    override fun afterExecute(handler: Any?, result: Unit) {
        bus.post(TrendsRefreshedEvent())
    }

    private fun storeTrends(cr: ContentResolver, uri: Uri, trendsList: List<Trends>) {
        val hashtags = ArrayList<String>()
        val hashtagValues = ArrayList<ContentValues>()
        val deleteWhere = Expression.and(Expression.equalsArgs(CachedTrends.ACCOUNT_KEY),
                Expression.equalsArgs(CachedTrends.WOEID)).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), woeId.toString())
        cr.delete(CachedTrends.Local.CONTENT_URI, deleteWhere, deleteWhereArgs)
        trendsList.forEach {

        }
        if (trendsList.isNotEmpty()) {
            val valuesArray = ContentValuesCreator.createTrends(trendsList)
            for (values in valuesArray) {
                val hashtag = values.getAsString(CachedTrends.NAME).replaceFirst("#", "")
                if (hashtags.contains(hashtag)) {
                    continue
                }
                hashtags.add(hashtag)
                val hashtagValue = ContentValues()
                hashtagValue.put(CachedHashtags.NAME, hashtag)
                hashtagValues.add(hashtagValue)
            }
            cr.delete(uri, null, null)
            ContentResolverUtils.bulkInsert(cr, uri, valuesArray)
            ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, false, hashtags, null)
            ContentResolverUtils.bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtagValues.toTypedArray())
        }
    }
}