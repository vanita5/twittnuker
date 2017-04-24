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

package de.vanita5.twittnuker.loader.group

import android.content.Context
import android.support.v4.content.FixedAsyncTaskLoader
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.statusnet.model.Group
import de.vanita5.twittnuker.library.twitter.model.PageableResponseList
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.loader.iface.IPaginationLoader
import de.vanita5.twittnuker.model.ParcelableGroup
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.pagination.CursorPagination
import de.vanita5.twittnuker.model.pagination.Pagination
import de.vanita5.twittnuker.model.util.ParcelableGroupUtils
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import de.vanita5.twittnuker.util.collection.NoDuplicatesArrayList
import java.util.*


abstract class BaseGroupsLoader(
        context: Context,
        protected val accountKey: UserKey,
        data: List<ParcelableGroup>?
) : FixedAsyncTaskLoader<List<ParcelableGroup>>(context), IPaginationLoader {

    override var pagination: Pagination? = null

    override final var nextPagination: Pagination? = null
        private set

    override final var prevPagination: Pagination? = null
        private set

    protected val data = NoDuplicatesArrayList<ParcelableGroup>()

    init {
        if (data != null) {
            this.data.addAll(data)
        }
    }

    @Throws(MicroBlogException::class)
    abstract fun getGroups(twitter: MicroBlog): List<Group>

    override fun loadInBackground(): List<ParcelableGroup> {
        val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?: return emptyList()
        var listLoaded: List<Group>? = null
        try {
            listLoaded = getGroups(twitter)
        } catch (e: MicroBlogException) {
            DebugLog.w(LOGTAG, tr = e)
        }

        if (listLoaded != null) {
            val listSize = listLoaded.size
            if (listLoaded is PageableResponseList<*>) {
                nextPagination = CursorPagination.valueOf(listLoaded.nextCursor)
                prevPagination = CursorPagination.valueOf(listLoaded.previousCursor)
                val dataSize = data.size
                for (i in 0..listSize - 1) {
                    val group = listLoaded[i]
                    data.add(ParcelableGroupUtils.from(group, accountKey, dataSize + i, isMember(group)))
                }
            } else {
                for (i in 0..listSize - 1) {
                    val list = listLoaded[i]
                    data.add(ParcelableGroupUtils.from(listLoaded[i], accountKey, i, isMember(list)))
                }
            }
        }
        Collections.sort(data)
        return data
    }

    public override fun onStartLoading() {
        forceLoad()
    }

    protected open fun isMember(list: Group): Boolean {
        return list.isMember
    }
}