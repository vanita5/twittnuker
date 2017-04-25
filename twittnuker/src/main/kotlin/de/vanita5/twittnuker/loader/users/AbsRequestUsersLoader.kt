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

package de.vanita5.twittnuker.loader.users

import android.accounts.AccountManager
import android.content.ActivityNotFoundException
import android.content.Context
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.constant.loadItemLimitKey
import de.vanita5.twittnuker.loader.iface.IPaginationLoader
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ListResponse
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.pagination.PaginatedList
import de.vanita5.twittnuker.model.pagination.Pagination
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.dagger.DependencyHolder
import java.util.*

abstract class AbsRequestUsersLoader(
        context: Context,
        val accountKey: UserKey?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : ParcelableUsersLoader(context, data, fromUser), IPaginationLoader {

    protected val profileImageSize: String = context.getString(R.string.profile_image_size)
    override var pagination: Pagination? = null
    override var prevPagination: Pagination? = null
        protected set
    override var nextPagination: Pagination? = null
        protected set
    protected val loadItemLimit: Int

    init {
        val preferences = DependencyHolder.get(context).preferences
        loadItemLimit = preferences[loadItemLimitKey]
    }

    override fun loadInBackground(): List<ParcelableUser> {
        val data = data
        val details: AccountDetails
        val users: List<ParcelableUser>
        try {
            val am = AccountManager.get(context)
            details = accountKey?.let { AccountUtils.getAccountDetails(am, it, true) } ?:
                    throw ActivityNotFoundException()
            users = getUsers(details)
        } catch (e: MicroBlogException) {
            DebugLog.w(tr = e)
            return ListResponse.getListInstance(data, e)
        }

        var pos = data.size
        for (user in users) {
            if (hasId(user.key)) {
                continue
            }
            user.position = pos.toLong()
            processUser(details, user)
            pos++
        }
        data.addAll(users)
        processUsersData(details, data)
        return ListResponse.getListInstance(data)
    }

    protected open fun processUser(details: AccountDetails, user: ParcelableUser) {

    }

    @Throws(MicroBlogException::class)
    private fun getUsers(details: AccountDetails): List<ParcelableUser> {
        val paging = Paging()
        paging.applyItemLimit(details, loadItemLimit)
        pagination?.applyTo(paging)
        val users = getUsers(details, paging)
        prevPagination = users.previousPage
        nextPagination = users.nextPage
        return users
    }

    protected open fun processUsersData(details: AccountDetails, list: MutableList<ParcelableUser>) {
        Collections.sort(data)
    }

    protected open fun Paging.applyItemLimit(details: AccountDetails, limit: Int) {
        when (details.type) {
            AccountType.MASTODON -> {
                limit(limit)
            }
            else -> {
                count(limit)
            }
        }
    }

    @Throws(MicroBlogException::class)
    protected abstract fun getUsers(details: AccountDetails, paging: Paging):
            PaginatedList<ParcelableUser>
}