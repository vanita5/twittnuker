/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.account.AccountExtras
import de.vanita5.twittnuker.model.account.TwitterAccountExtras
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import de.vanita5.twittnuker.util.TwitterContentUtils

fun AccountDetails.isOfficial(context: Context): Boolean {
    val extra = this.extras
    if (extra is TwitterAccountExtras) {
        return extra.isOfficialCredentials
    }
    val credentials = this.credentials
    if (credentials is OAuthCredentials) {
        return TwitterContentUtils.isOfficialKey(context,
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


@JvmOverloads
fun <T> AccountDetails.newMicroBlogInstance(context: Context, includeEntities: Boolean = true, includeRetweets: Boolean = true,
                                            extraRequestParams: Map<String, String>? =
                                            MicroBlogAPIFactory.getExtraParams(type, includeEntities, includeRetweets),
                                            cls: Class<T>): T {
    return credentials.newMicroBlogInstance(context, type == AccountType.TWITTER, extraRequestParams, cls)
}

val AccountDetails.is_oauth: Boolean
    get() = credentials_type == Credentials.Type.OAUTH || credentials_type == Credentials.Type.XAUTH