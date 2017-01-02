/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.task

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.support.v4.util.LongSparseArray
import android.text.TextUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_TYPE
import de.vanita5.twittnuker.extension.model.setAccountKey
import de.vanita5.twittnuker.extension.model.setAccountUser
import de.vanita5.twittnuker.model.*
import de.vanita5.twittnuker.provider.TwidereDataStore.*
import java.io.IOException

class UpdateAccountInfoTask(
        private val context: Context
) : AbstractTask<Pair<AccountDetails, ParcelableUser>, Unit, Unit?>() {

    override fun doLongOperation(params: Pair<AccountDetails, ParcelableUser>) {
        val resolver = context.contentResolver
        val details = params.first
        val user = params.second
        if (user.is_cache) {
            return
        }
        if (!user.key.maybeEquals(user.account_key)) {
            return
        }

        val am = AccountManager.get(context)
        val account = Account(details.account.name, ACCOUNT_TYPE)
        account.setAccountUser(am, user)
        account.setAccountKey(am, user.key)

        val accountKeyValues = ContentValues().apply {
            put(AccountSupportColumns.ACCOUNT_KEY, user.key.toString())
        }
        val accountKeyWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).sql
        val accountKeyWhereArgs = arrayOf(details.key.toString())

        resolver.update(Statuses.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(Activities.AboutMe.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(DirectMessages.Inbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(DirectMessages.Outbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(CachedRelationships.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)

        updateTabs(resolver, user.key)
    }

    private fun updateTabs(resolver: ContentResolver, accountKey: UserKey) {
        val tabsCursor = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, null) ?: return
        try {
            val indices = TabCursorIndices(tabsCursor)
            tabsCursor.moveToFirst()
            val values = LongSparseArray<ContentValues>()
            while (!tabsCursor.isAfterLast) {
                val tab = indices.newObject(tabsCursor)
                val arguments = tab.arguments
                if (arguments != null) {
                    val accountId = arguments.accountId
                    val keys = arguments.accountKeys
                    if (TextUtils.equals(accountKey.id, accountId) && keys == null) {
                        arguments.accountKeys = arrayOf(accountKey)
                        values.put(tab.id, TabValuesCreator.create(tab))
                    }
                }
                tabsCursor.moveToNext()
            }
            val where = Expression.equalsArgs(Tabs._ID).sql
            for (i in 0 until values.size()) {
                val whereArgs = arrayOf(values.keyAt(i).toString())
                resolver.update(Tabs.CONTENT_URI, values.valueAt(i), where, whereArgs)
            }
        } catch (e: IOException) {
            // Ignore
        } finally {
            tabsCursor.close()
        }
    }
}