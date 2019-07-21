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

package de.vanita5.twittnuker.extension.model

import android.content.Context
import com.twitter.Validator
import de.vanita5.microblog.library.twitter.annotation.MediaCategory
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.account.AccountExtras
import de.vanita5.twittnuker.model.account.MastodonAccountExtras
import de.vanita5.twittnuker.model.account.StatusNetAccountExtras
import de.vanita5.twittnuker.model.account.TwitterAccountExtras
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials
import de.vanita5.twittnuker.task.twitter.UpdateStatusTask
import de.vanita5.twittnuker.util.InternalTwitterContentUtils

fun AccountDetails.isOfficial(context: Context): Boolean {
    val extra = this.extras
    if (extra is TwitterAccountExtras) {
        return extra.isOfficialCredentials
    }
    val credentials = this.credentials
    if (credentials is OAuthCredentials) {
        return InternalTwitterContentUtils.isOfficialKey(context,
                credentials.consumer_key, credentials.consumer_secret)
    }
    return false
}

val AccountExtras.official: Boolean
    get() {
        if (this is TwitterAccountExtras) {
            return isOfficialCredentials
        }
        return false
    }

fun <T> AccountDetails.newMicroBlogInstance(context: Context, cls: Class<T>): T {
    return credentials.newMicroBlogInstance(context, type, cls)
}

val AccountDetails.isOAuth: Boolean
    get() = credentials_type == Credentials.Type.OAUTH || credentials_type == Credentials.Type.XAUTH

fun AccountDetails.getMediaSizeLimit(@MediaCategory mediaCategory: String? = null): UpdateStatusTask.SizeLimit? {
    when (type) {
        AccountType.TWITTER -> {
            val imageLimit = AccountExtras.ImageLimit.twitterDefault(mediaCategory)
            val videoLimit = AccountExtras.VideoLimit.twitterDefault()
            return UpdateStatusTask.SizeLimit(imageLimit, videoLimit)
        }
        AccountType.FANFOU -> {
            val imageLimit = AccountExtras.ImageLimit.ofSize(5 * 1024 * 1024)
            val videoLimit = AccountExtras.VideoLimit.unsupported()
            return UpdateStatusTask.SizeLimit(imageLimit, videoLimit)
        }
        AccountType.STATUSNET -> {
            val extras = extras as? StatusNetAccountExtras ?: return null
            val imageLimit = AccountExtras.ImageLimit().apply {
                maxSizeSync = extras.uploadLimit
                maxSizeAsync = extras.uploadLimit
            }
            val videoLimit = AccountExtras.VideoLimit().apply {
                maxSizeSync = extras.uploadLimit
                maxSizeAsync = extras.uploadLimit
            }
            return UpdateStatusTask.SizeLimit(imageLimit, videoLimit)
        }
        else -> return null
    }
}

/**
 * Text limit when composing a status, 0 for no limit
 */
val AccountDetails.textLimit: Int get() {
    if (type == null) {
        return Validator.MAX_TWEET_LENGTH*2
    }
    when (type) {
        AccountType.STATUSNET -> {
            val extras = this.extras as? StatusNetAccountExtras
            if (extras != null) {
                return extras.textLimit
            }
        }
        AccountType.MASTODON -> {
            val extras = this.extras as? MastodonAccountExtras
            if (extras != null) {
                return extras.statusTextLimit
            }
        }
    }
    return Validator.MAX_TWEET_LENGTH*2
}

val Array<AccountDetails>.textLimit: Int
    get() {
        var limit = -1
        forEach { details ->
            val currentLimit = details.textLimit
            if (currentLimit != 0) {
                if (limit <= 0) {
                    limit = currentLimit
                } else {
                    limit = Math.min(limit, currentLimit)
                }
            }
        }
        return limit
    }


val AccountDetails.isStreamingSupported: Boolean
    get() = type == AccountType.TWITTER