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

package de.vanita5.twittnuker.loader.statuses

import android.content.Context
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.official
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey

class UserMentionsLoader(
        context: Context,
        accountKey: UserKey?,
        screenName: String,
        data: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        makeGap: Boolean,
        local: Boolean,
        loadingMore: Boolean
) : TweetSearchLoader(context, accountKey, screenName, data, savedStatusesArgs, tabPosition,
        fromUser, makeGap, local, loadingMore) {

    override fun processQuery(details: AccountDetails, query: String): String {
        val screenName = query.substringAfter("@")
        if (details.type == AccountType.TWITTER) {
            if (details.extras?.official == true) {
                return smQuery("to:$screenName", pagination)
            }
            return "to:$screenName exclude:retweets"
        }
        return "@$screenName -RT"
    }

}