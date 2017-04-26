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

package de.vanita5.twittnuker.loader

import android.accounts.AccountManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.support.v4.content.FixedAsyncTaskLoader
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.mastodon.Mastodon
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.extension.model.api.mastodon.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.ListResponse
import de.vanita5.twittnuker.model.ParcelableHashtag
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.AccountUtils

class MastodonSearchLoader(
        context: Context,
        private val accountKey: UserKey?,
        private val query: String
) : FixedAsyncTaskLoader<List<Any>>(context), Constants {

    override fun loadInBackground(): List<Any> {
        try {
            val am = AccountManager.get(context)
            val account = accountKey?.let {
                AccountUtils.getAccountDetails(am, it, true)
            } ?: throw ActivityNotFoundException()
            val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
            val searchResult = mastodon.search(query, true, null)
            return ListResponse(ArrayList<Any>().apply {
                searchResult.accounts?.mapTo(this) {
                    it.toParcelable(account)
                }
                searchResult.hashtags?.mapTo(this) { hashtag ->
                    ParcelableHashtag().also { it.hashtag = hashtag }
                }
                searchResult.statuses?.mapTo(this) {
                    it.toParcelable(account)
                }
            })
        } catch (e: MicroBlogException) {
            return ListResponse(e)
        }
    }

    override fun onStartLoading() {
        forceLoad()
    }

}