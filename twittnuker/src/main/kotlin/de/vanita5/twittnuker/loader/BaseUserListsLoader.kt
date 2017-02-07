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
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.CursorSupport
import de.vanita5.twittnuker.library.twitter.model.PageableResponseList
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.library.twitter.model.UserList
import de.vanita5.twittnuker.loader.iface.ICursorSupportLoader
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.ParcelableUserListUtils
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import de.vanita5.twittnuker.util.collection.NoDuplicatesArrayList
import java.util.*


abstract class BaseUserListsLoader(
        context: Context,
        protected val accountId: UserKey,
        override val cursor: Long,
        data: List<ParcelableUserList>?
) : AsyncTaskLoader<List<ParcelableUserList>>(context), ICursorSupportLoader {

    protected val data = NoDuplicatesArrayList<ParcelableUserList>()

    override var nextCursor: Long = 0
    override var prevCursor: Long = 0

    init {
        if (data != null) {
            this.data.addAll(data)
        }
    }

    @Throws(MicroBlogException::class)
    abstract fun getUserLists(twitter: MicroBlog, paging: Paging): List<UserList>

    override fun loadInBackground(): List<ParcelableUserList> {
        val twitter = MicroBlogAPIFactory.getInstance(context, accountId) ?: return data
        var listLoaded: List<UserList>? = null
        try {
            val paging = Paging()
            if (cursor > 0) {
                paging.cursor(cursor)
            }
            listLoaded = getUserLists(twitter, paging)
        } catch (e: MicroBlogException) {
            Log.w(LOGTAG, e)
        }

        if (listLoaded != null) {
            val listSize = listLoaded.size
            if (listLoaded is PageableResponseList<*>) {
                nextCursor = (listLoaded as CursorSupport).nextCursor
                prevCursor = listLoaded.previousCursor
                val dataSize = data.size
                for (i in 0..listSize - 1) {
                    val list = listLoaded[i]
                    data.add(ParcelableUserListUtils.from(list, accountId, (dataSize + i).toLong(), isFollowing(list)))
                }
            } else {
                for (i in 0..listSize - 1) {
                    val list = listLoaded[i]
                    data.add(ParcelableUserListUtils.from(listLoaded[i], accountId, i.toLong(), isFollowing(list)))
                }
            }
        }
        Collections.sort(data)
        return data
    }

    override fun onStartLoading() {
        forceLoad()
    }

    protected open fun isFollowing(list: UserList): Boolean {
        return list.isFollowing
    }
}