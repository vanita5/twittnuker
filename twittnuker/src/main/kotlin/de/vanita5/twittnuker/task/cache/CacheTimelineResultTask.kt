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

package de.vanita5.twittnuker.task.cache

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.util.ArraySet
import org.mariotaku.ktextension.ContentValues
import org.mariotaku.ktextension.set
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.extension.bulkInsert
import de.vanita5.twittnuker.extension.model.applyTo
import de.vanita5.twittnuker.extension.model.relationship
import de.vanita5.twittnuker.extension.queryAll
import de.vanita5.twittnuker.model.ParcelableRelationship
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.task.GetTimelineResult
import de.vanita5.twittnuker.provider.TwidereDataStore.*
import de.vanita5.twittnuker.task.BaseAbstractTask
import de.vanita5.twittnuker.util.content.ContentResolverUtils

class CacheTimelineResultTask(
        context: Context,
        val result: GetTimelineResult<*>,
        val cacheRelationship: Boolean
) : BaseAbstractTask<Any?, Unit, Any?>(context) {

    override fun doLongOperation(param: Any?) {
        val cr = context.contentResolver
        val account = result.account
        val users = result.users
        val hashtags = result.hashtags

        cr.bulkInsert(CachedUsers.CONTENT_URI, users, ParcelableUser::class.java)
        ContentResolverUtils.bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtags.map {
            ContentValues { this[CachedHashtags.NAME] = it.substringAfter("#") }
        })

        if (cacheRelationship) {
            val selectionArgsList = users.mapTo(mutableListOf(account.key.toString())) {
                it.key.toString()
            }
            @SuppressLint("Recycle")
            val localRelationships = cr.queryAll(CachedRelationships.CONTENT_URI, CachedRelationships.COLUMNS,
                    Expression.and(Expression.equalsArgs(CachedRelationships.ACCOUNT_KEY),
                            Expression.inArgs(CachedRelationships.USER_KEY, users.size)).sql,
                    selectionArgsList.toTypedArray(), null, ParcelableRelationship::class.java)
            val relationships = users.mapTo(ArraySet<ParcelableRelationship>()) { user ->
                val userKey = user.key
                return@mapTo localRelationships.find {
                    it.user_key == userKey
                }?.apply { user.applyTo(this) } ?: user.relationship
            }
            cr.bulkInsert(CachedRelationships.CONTENT_URI, relationships, ParcelableRelationship::class.java)
        }
    }

}