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

package de.vanita5.twittnuker.task.status

import android.content.Context
import android.widget.Toast
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.twitter.model.PinTweetResult
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.StatusPinEvent
import de.vanita5.twittnuker.task.AbsAccountRequestTask


class PinStatusTask(context: Context, accountKey: UserKey, val id: String) : AbsAccountRequestTask<Any?,
        PinTweetResult, Any?>(context, accountKey) {

    override fun onExecute(account: AccountDetails, params: Any?): PinTweetResult {
        val twitter = account.newMicroBlogInstance(context, MicroBlog::class.java)
        return twitter.pinTweet(id)
    }

    override fun onSucceed(callback: Any?, result: PinTweetResult) {
        super.onSucceed(callback, result)
        Toast.makeText(context, R.string.message_toast_status_pinned, Toast.LENGTH_SHORT).show()
        if (accountKey != null) {
            bus.post(StatusPinEvent(accountKey, true))
        }
    }
}