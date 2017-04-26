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

package de.vanita5.twittnuker.loader

import android.annotation.SuppressLint
import android.content.Context
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.microblog.library.twitter.model.Paging
import de.vanita5.twittnuker.loader.users.UserSearchLoader
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.pagination.PaginatedArrayList
import de.vanita5.twittnuker.model.pagination.PaginatedList
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedUsers
import de.vanita5.twittnuker.util.UserColorNameManager
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.util.dagger.GeneralComponent
import java.text.Collator
import java.util.*
import javax.inject.Inject

class CacheUserSearchLoader(
        context: Context,
        accountKey: UserKey,
        query: String,
        private val fromNetwork: Boolean,
        private val fromCache: Boolean,
        fromUser: Boolean
) : UserSearchLoader(context, accountKey, query, null, fromUser) {
    @Inject
    internal lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponent.get(context).inject(this)
    }

    override fun getUsers(details: AccountDetails, paging: Paging): PaginatedList<ParcelableUser> {
        if (query.isEmpty() || !fromNetwork) return PaginatedArrayList()
        return super.getUsers(details, paging)
    }

    override fun processUsersData(details: AccountDetails, list: MutableList<ParcelableUser>) {
        if (query.isEmpty() || !fromCache) return
        val queryEscaped = query.replace("_", "^_")
        val selection = Expression.and(Expression.equalsArgs(Columns.Column(CachedUsers.USER_TYPE)),
                Expression.or(Expression.likeRaw(Columns.Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                Expression.likeRaw(Columns.Column(CachedUsers.NAME), "?||'%'", "^")))
        val selectionArgs = arrayOf(queryEscaped, queryEscaped)
        @SuppressLint("Recycle")
        val c = context.contentResolver.query(CachedUsers.CONTENT_URI, CachedUsers.BASIC_COLUMNS,
                selection.sql, selectionArgs, null)!!
        val i = ObjectCursor.indicesFrom(c, ParcelableUser::class.java)
        c.moveToFirst()
        while (!c.isAfterLast) {
            if (list.none { it.key.toString() == c.getString(i[CachedUsers.USER_KEY]) }) {
            list.add(i.newObject(c))
        }
            c.moveToNext()
        }
        c.close()
        val collator = Collator.getInstance()
        list.sortWith(Comparator { l, r ->
            val compare = collator.compare(r.name, l.name)
            if (compare != 0) return@Comparator compare
            return@Comparator r.screen_name.compareTo(l.screen_name)
        })
    }
}