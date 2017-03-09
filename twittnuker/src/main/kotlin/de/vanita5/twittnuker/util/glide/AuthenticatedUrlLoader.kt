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

package de.vanita5.twittnuker.util.glide

import android.accounts.AccountManager
import android.content.Context
import android.net.Uri
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.*
import okhttp3.OkHttpClient
import de.vanita5.twittnuker.extension.model.authorizationHeader
import de.vanita5.twittnuker.extension.model.getCredentials
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.media.AuthenticatedUri
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.util.media.TwidereMediaDownloader
import java.io.InputStream

class AuthenticatedUrlLoader(
        val context: Context,
        val client: OkHttpClient
) : ModelLoader<AuthenticatedUri, InputStream> {

    override fun getResourceFetcher(model: AuthenticatedUri, width: Int, height: Int): DataFetcher<InputStream> {
        val headersBuilder = LazyHeaders.Builder()
        val credentials = model.accountKey?.credentials
        if (credentials != null) {
            if (TwidereMediaDownloader.isAuthRequired(credentials, model.uri)) {
                headersBuilder.addHeader("Authorization", AuthorizationHeaderFactory(model.uri, credentials))
            }
        }
        val glideUrl = GlideUrl(model.uri.toString(), headersBuilder.build())
        return OkHttpStreamFetcher(client, glideUrl)
    }

    val UserKey.credentials: Credentials? get() {
        val am = AccountManager.get(context)
        return AccountUtils.findByAccountKey(am, this)?.getCredentials(am)
    }

    internal class AuthorizationHeaderFactory(val uri: Uri, val credentials: Credentials) : LazyHeaderFactory {
        override fun buildHeader() = credentials.authorizationHeader(uri)
    }

    class Factory(val client: OkHttpClient) : ModelLoaderFactory<AuthenticatedUri, InputStream> {
        override fun build(context: Context, factories: GenericLoaderFactory) = AuthenticatedUrlLoader(context, client)

        override fun teardown() {}
    }

}