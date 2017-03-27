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
import android.content.ContentResolver
import android.content.Context
import android.support.v4.util.ArraySet
import org.mariotaku.ktextension.map
import org.mariotaku.ktextension.useCursor
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.twittnuker.library.twitter.model.User
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.model.ParcelableRelationship
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.ParcelableRelationshipUtils
import de.vanita5.twittnuker.model.util.ParcelableUserUtils
import de.vanita5.twittnuker.model.util.UserKeyUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedRelationships
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedUsers
import de.vanita5.twittnuker.task.BaseAbstractTask
import de.vanita5.twittnuker.util.content.ContentResolverUtils

class CacheUserRelationshipTask(
        context: Context,
        val accountKey: UserKey,
        val accountType: String,
        val users: Collection<User>
) : BaseAbstractTask<Any?, Unit, Any?>(context) {

    override fun doLongOperation(param: Any?) {
        cacheUserRelationships(context.contentResolver, accountKey, accountType, users)
    }

    companion object {
        fun cacheUserRelationships(cr: ContentResolver, accountKey: UserKey, accountType: String,
                users: Collection<User>) {

            val parcelableUsers = users.map { ParcelableUserUtils.fromUser(it, accountKey, accountType) }

            val userValuesCreator = ObjectCursor.valuesCreatorFrom(ParcelableUser::class.java)
            ContentResolverUtils.bulkInsert(cr, CachedUsers.CONTENT_URI, parcelableUsers.map(userValuesCreator::create))

            val selectionArgsList = parcelableUsers.mapTo(mutableListOf(accountKey.toString())) {
                it.key.toString()
            }
            @SuppressLint("Recycle")
            val localRelationships = cr.query(CachedRelationships.CONTENT_URI, CachedRelationships.COLUMNS,
                    Expression.and(Expression.equalsArgs(CachedRelationships.ACCOUNT_KEY),
                            Expression.inArgs(CachedRelationships.USER_KEY, users.size)).sql,
                    selectionArgsList.toTypedArray(), null).useCursor { cur ->
                return@useCursor cur.map(ObjectCursor.indicesFrom(cur, ParcelableRelationship::class.java))
            }
            val relationships = users.mapTo(ArraySet<ParcelableRelationship>()) { user ->
                val userKey = UserKeyUtils.fromUser(user)
                return@mapTo localRelationships.find {
                    it.user_key == userKey
                }?.apply {
                    user.isFollowing?.let { this.following = it }
                    user.isFollowedBy?.let { this.followed_by = it }
                    user.isBlocking?.let { this.blocking = it }
                    user.isBlockedBy?.let { this.blocked_by = it }
                    user.isMuting?.let { this.muting = it }
                    user.isNotificationsEnabled?.let { this.notifications_enabled = it }
                } ?: ParcelableRelationshipUtils.create(accountKey, userKey, user)
            }
            ParcelableRelationshipUtils.insert(cr, relationships)
        }
    }

}