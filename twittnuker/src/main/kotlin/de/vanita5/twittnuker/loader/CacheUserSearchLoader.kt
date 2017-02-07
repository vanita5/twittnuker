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

import android.content.Context
import android.text.TextUtils
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.ParcelableUserCursorIndices
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.provider.TwidereDataStore
import de.vanita5.twittnuker.util.UserColorNameManager
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject

class CacheUserSearchLoader(
        context: Context,
        accountKey: UserKey,
        query: String,
        private val fromCache: Boolean,
        fromUser: Boolean
) : UserSearchLoader(context, accountKey, query, 0, null, fromUser) {
    @Inject
    internal lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun loadInBackground(): List<ParcelableUser> {
        if (TextUtils.isEmpty(query)) return emptyList()
        if (fromCache) {
            val cachedList = ArrayList<ParcelableUser>()
            val queryEscaped = query.replace("_", "^_")
            val selection = Expression.or(Expression.likeRaw(Columns.Column(TwidereDataStore.CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                    Expression.likeRaw(Columns.Column(TwidereDataStore.CachedUsers.NAME), "?||'%'", "^"))
            val selectionArgs = arrayOf(queryEscaped, queryEscaped)
            val order = arrayOf(TwidereDataStore.CachedUsers.LAST_SEEN, TwidereDataStore.CachedUsers.SCREEN_NAME, TwidereDataStore.CachedUsers.NAME)
            val ascending = booleanArrayOf(false, true, true)
            val orderBy = OrderBy(order, ascending)
            val c = context.contentResolver.query(TwidereDataStore.CachedUsers.CONTENT_URI,
                    TwidereDataStore.CachedUsers.BASIC_COLUMNS, selection?.sql,
                    selectionArgs, orderBy.sql)!!
            val i = ParcelableUserCursorIndices(c)
            c.moveToFirst()
            while (!c.isAfterLast) {
                cachedList.add(i.newObject(c))
                c.moveToNext()
            }
            c.close()
            return cachedList
        }
        return super.loadInBackground()
    }
}