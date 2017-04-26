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

package de.vanita5.twittnuker.task

import android.content.Context
import android.widget.Toast
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.mastodon.Mastodon
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.extension.model.api.mastodon.toParcelable
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.event.FriendshipTaskEvent
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.util.Utils

class DestroyFriendshipTask(context: Context) : AbsFriendshipOperationTask(context, FriendshipTaskEvent.Action.UNFOLLOW) {

    @Throws(MicroBlogException::class)
    override fun perform(details: AccountDetails, args: Arguments): ParcelableUser {
        when (details.type) {
            AccountType.FANFOU -> {
                val fanfou = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return fanfou.destroyFanfouFriendship(args.userKey.id).toParcelable(details,
                        profileImageSize = profileImageSize)
            }
            AccountType.MASTODON -> {
                val mastodon = details.newMicroBlogInstance(context, Mastodon::class.java)
                if (details.key.host != args.userKey.host) {
                    throw MicroBlogException("Unfollow remote user is not supported yet")
            }
                mastodon.unfollowUser(args.userKey.id)
                return mastodon.getAccount(args.userKey.id).toParcelable(details)
            }
            else -> {
                val twitter = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return twitter.destroyFriendship(args.accountKey.id).toParcelable(details,
                        profileImageSize = profileImageSize)
            }
        }
    }

    override fun succeededWorker(details: AccountDetails, args: Arguments, user: ParcelableUser) {
        user.is_following = false
        Utils.setLastSeen(context, user.key, -1)
        val where = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                Expression.or(Expression.equalsArgs(Statuses.USER_KEY),
                        Expression.equalsArgs(Statuses.RETWEETED_BY_USER_KEY)))
        val whereArgs = arrayOf(args.accountKey.toString(), args.userKey.toString(), args.userKey.toString())
        val resolver = context.contentResolver
        resolver.delete(Statuses.CONTENT_URI, where.sql, whereArgs)
    }

    override fun showSucceededMessage(params: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        val nameFirst = kPreferences[nameFirstKey]
        val message = context.getString(R.string.unfollowed_user, manager.getDisplayName(user, nameFirst))
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}