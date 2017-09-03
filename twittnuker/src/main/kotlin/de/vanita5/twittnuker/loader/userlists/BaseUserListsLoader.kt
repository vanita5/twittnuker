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

package de.vanita5.twittnuker.loader.userlists

import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.support.v4.content.FixedAsyncTaskLoader
import android.util.Log
import org.mariotaku.kpreferences.get
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.twitter.model.PageableResponseList
import de.vanita5.microblog.library.twitter.model.Paging
import de.vanita5.microblog.library.twitter.model.UserList
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.constant.loadItemLimitKey
import de.vanita5.twittnuker.extension.model.api.microblog.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.loader.iface.IPaginationLoader
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.pagination.CursorPagination
import de.vanita5.twittnuker.model.pagination.Pagination
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.util.collection.NoDuplicatesArrayList
import de.vanita5.twittnuker.util.dagger.GeneralComponent
import java.util.*
import javax.inject.Inject


abstract class BaseUserListsLoader(
        context: Context,
        protected val accountKey: UserKey?,
        data: List<ParcelableUserList>?
) : FixedAsyncTaskLoader<List<ParcelableUserList>>(context), IPaginationLoader {
    @Inject
    lateinit var preferences: SharedPreferences

    protected val data = NoDuplicatesArrayList<ParcelableUserList>()

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override var pagination: Pagination? = null

    override var nextPagination: Pagination? = null
        protected set
    override var prevPagination: Pagination? = null
        protected set

    init {
        GeneralComponent.get(context).inject(this)
        if (data != null) {
            this.data.addAll(data)
        }
    }

    @Throws(MicroBlogException::class)
    abstract fun getUserLists(twitter: MicroBlog, paging: Paging): List<UserList>

    override fun loadInBackground(): List<ParcelableUserList> {
        if (accountKey == null) return emptyList()
        var listLoaded: List<UserList>? = null
        try {
            val am = AccountManager.get(context)
            val details = AccountUtils.getAccountDetails(am, accountKey, true) ?: return data
            val twitter = details.newMicroBlogInstance(context, MicroBlog::class.java)
            val paging = Paging()
            paging.count(preferences[loadItemLimitKey].coerceIn(0, 100))
            pagination?.applyTo(paging)
            listLoaded = getUserLists(twitter, paging)
        } catch (e: MicroBlogException) {
            Log.w(LOGTAG, e)
        }

        if (listLoaded != null) {
            val listSize = listLoaded.size
            if (listLoaded is PageableResponseList<*>) {
                nextPagination = CursorPagination.valueOf(listLoaded.nextCursor)
                prevPagination = CursorPagination.valueOf(listLoaded.previousCursor)
                val dataSize = data.size
                for (i in 0 until listSize) {
                    val list = listLoaded[i]
                    data.add(list.toParcelable(accountKey, (dataSize + i).toLong(),
                            isFollowing(list), profileImageSize))
                }
            } else {
                for (i in 0 until listSize) {
                    val list = listLoaded[i]
                    data.add(listLoaded[i].toParcelable(accountKey, i.toLong(),
                            isFollowing(list), profileImageSize))
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