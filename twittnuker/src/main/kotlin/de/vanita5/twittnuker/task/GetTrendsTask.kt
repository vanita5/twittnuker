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
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Trends
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.message.TrendsRefreshedEvent
import de.vanita5.twittnuker.provider.TwidereDataStore
import de.vanita5.twittnuker.util.ContentValuesCreator
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject

abstract class GetTrendsTask(
        private val context: Context,
        private val accountId: UserKey
) : AbstractTask<Any?, Unit, Any?>() {

    @Inject
    lateinit var bus: Bus

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    @Throws(MicroBlogException::class)
    abstract fun getTrends(twitter: MicroBlog): List<Trends>

    public override fun doLongOperation(param: Any?) {
        val twitter = MicroBlogAPIFactory.getInstance(context, accountId) ?: return
        try {
            val trends = getTrends(twitter)
            storeTrends(context.contentResolver, contentUri, trends)
            return
        } catch (e: MicroBlogException) {
            return
        }

    }

    override fun afterExecute(handler: Any?, result: Unit) {
        bus.post(TrendsRefreshedEvent())
    }

    protected abstract val contentUri: Uri

    private fun storeTrends(cr: ContentResolver, uri: Uri, trendsList: List<Trends>) {
        val hashtags = ArrayList<String>()
        val hashtagValues = ArrayList<ContentValues>()
        if (trendsList.isNotEmpty()) {
            val valuesArray = ContentValuesCreator.createTrends(trendsList)
            for (values in valuesArray) {
                val hashtag = values.getAsString(TwidereDataStore.CachedTrends.NAME).replaceFirst("#".toRegex(), "")
                if (hashtags.contains(hashtag)) {
                    continue
                }
                hashtags.add(hashtag)
                val hashtagValue = ContentValues()
                hashtagValue.put(TwidereDataStore.CachedHashtags.NAME, hashtag)
                hashtagValues.add(hashtagValue)
            }
            cr.delete(uri, null, null)
            ContentResolverUtils.bulkInsert(cr, uri, valuesArray)
            ContentResolverUtils.bulkDelete(cr, TwidereDataStore.CachedHashtags.CONTENT_URI, TwidereDataStore.CachedHashtags.NAME, hashtags, null)
            ContentResolverUtils.bulkInsert(cr, TwidereDataStore.CachedHashtags.CONTENT_URI,
                    hashtagValues.toTypedArray())
        }
    }
}