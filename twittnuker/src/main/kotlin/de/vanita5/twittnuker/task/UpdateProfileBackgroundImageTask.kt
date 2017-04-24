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
import android.net.Uri
import android.widget.Toast
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.ProfileUpdatedEvent
import de.vanita5.twittnuker.task.twitter.UpdateStatusTask
import de.vanita5.twittnuker.util.DebugLog
import java.io.IOException

open class UpdateProfileBackgroundImageTask<ResultHandler>(
        context: Context,
        accountKey: UserKey,
        private val imageUri: Uri,
        private val tile: Boolean,
        private val deleteImage: Boolean
) : AbsAccountRequestTask<Any?, ParcelableUser, ResultHandler>(context, accountKey) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableUser {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        try {
            UpdateStatusTask.getBodyFromMedia(context, imageUri, ParcelableMedia.Type.IMAGE, true,
                    true, null, false, null).use {
                microBlog.updateProfileBackgroundImage(it.body, tile)
            }
        } catch (e: IOException) {
            throw MicroBlogException(e)
        }
        // Wait for 5 seconds, see
        // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
        try {
            Thread.sleep(5000L)
        } catch (e: InterruptedException) {
            DebugLog.w(LOGTAG, tr = e)
        }
        val user = microBlog.verifyCredentials()
        return user.toParcelable(account.key, account.type, profileImageSize = profileImageSize)
    }

    override fun onSucceed(callback: ResultHandler?, result: ParcelableUser) {
        Toast.makeText(context, R.string.message_toast_profile_background_image_updated,
                Toast.LENGTH_SHORT).show()
        bus.post(ProfileUpdatedEvent(result))
    }


}