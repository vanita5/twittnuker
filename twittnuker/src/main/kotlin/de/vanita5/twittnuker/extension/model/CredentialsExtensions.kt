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

package de.vanita5.twittnuker.extension.model

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.fanfou.FanfouStream
import de.vanita5.twittnuker.library.twitter.*
import de.vanita5.twittnuker.library.twitter.auth.BasicAuthorization
import de.vanita5.twittnuker.library.twitter.auth.EmptyAuthorization
import org.mariotaku.restfu.RestAPIFactory
import org.mariotaku.restfu.RestRequest
import org.mariotaku.restfu.http.Authorization
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.MultiValueMap
import org.mariotaku.restfu.oauth.OAuthAuthorization
import org.mariotaku.restfu.oauth.OAuthEndpoint
import org.mariotaku.restfu.oauth.OAuthToken
import de.vanita5.twittnuker.TwittnukerConstants.DEFAULT_TWITTER_API_URL_FORMAT
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.model.account.cred.BasicCredentials
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.account.cred.EmptyCredentials
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials
import de.vanita5.twittnuker.util.HttpClientFactory
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import de.vanita5.twittnuker.util.MicroBlogAPIFactory.sFanfouConstantPool
import de.vanita5.twittnuker.util.MicroBlogAPIFactory.sTwitterConstantPool
import de.vanita5.twittnuker.util.TwitterContentUtils
import de.vanita5.twittnuker.util.api.*
import de.vanita5.twittnuker.util.dagger.DependencyHolder
import de.vanita5.twittnuker.util.media.TwidereMediaDownloader

/**
 * Creates [MicroBlog] instances
 */
fun Credentials.getAuthorization(cls: Class<*>?): Authorization {
    if (cls != null) {
        when {
            TwitterWeb::class.java.isAssignableFrom(cls) -> {
                return EmptyAuthorization()
            }
        }
    }
    when (this) {
        is OAuthCredentials -> {
            return OAuthAuthorization(consumer_key, consumer_secret, OAuthToken(access_token,
                    access_token_secret))
        }
        is BasicCredentials -> {
            return BasicAuthorization(username, password)
        }
        is EmptyCredentials -> {
            return EmptyAuthorization()
        }
    }
    throw UnsupportedOperationException()
}

fun Credentials.getEndpoint(cls: Class<*>): Endpoint {
    val apiUrlFormat: String
    val noVersionSuffix = this.no_version_suffix
    if (!TextUtils.isEmpty(this.api_url_format)) {
        apiUrlFormat = this.api_url_format
    } else {
        apiUrlFormat = DEFAULT_TWITTER_API_URL_FORMAT
    }
    val domain: String?
    val versionSuffix: String?
    when {
        MicroBlog::class.java.isAssignableFrom(cls) -> {
            domain = "api"
            versionSuffix = if (noVersionSuffix) null else "/1.1/"
        }
        Twitter::class.java.isAssignableFrom(cls) -> {
            domain = "api"
            versionSuffix = if (noVersionSuffix) null else "/1.1/"
        }
        TwitterUpload::class.java.isAssignableFrom(cls) -> {
            domain = "upload"
            versionSuffix = if (noVersionSuffix) null else "/1.1/"
        }
        TwitterOAuth::class.java.isAssignableFrom(cls) -> {
            domain = "api"
            versionSuffix = null
        }
        TwitterOAuth2::class.java.isAssignableFrom(cls) -> {
            domain = "api"
            versionSuffix = null
        }
        TwitterUserStream::class.java.isAssignableFrom(cls) -> {
            domain = "userstream"
            versionSuffix = if (noVersionSuffix) null else "/1.1/"
        }
        TwitterCaps::class.java.isAssignableFrom(cls) -> {
            domain = "caps"
            versionSuffix = null
        }
        FanfouStream::class.java.isAssignableFrom(cls) -> {
            domain = "stream"
            versionSuffix = null
        }
        TwitterWeb::class.java.isAssignableFrom(cls) -> {
            domain = null
            versionSuffix = null
        }
        else -> throw UnsupportedOperationException("Unsupported class $cls")
    }
    val endpointUrl = MicroBlogAPIFactory.getApiUrl(apiUrlFormat, domain, versionSuffix)
    if (this is OAuthCredentials) {
        val signEndpointUrl: String
        if (same_oauth_signing_url) {
            signEndpointUrl = endpointUrl
        } else {
            signEndpointUrl = MicroBlogAPIFactory.getApiUrl(DEFAULT_TWITTER_API_URL_FORMAT, domain, versionSuffix)
        }
        return OAuthEndpoint(endpointUrl, signEndpointUrl)
    }
    return Endpoint(endpointUrl)
}

fun <T> Credentials.newMicroBlogInstance(context: Context, @AccountType accountType: String? = null,
        cls: Class<T>): T {
    return newMicroBlogInstance(context, getEndpoint(cls), getAuthorization(cls), accountType,
            cls)
}

fun <T> newMicroBlogInstance(context: Context, endpoint: Endpoint, auth: Authorization,
        @AccountType accountType: String? = null, cls: Class<T>): T {
    val factory = RestAPIFactory<MicroBlogException>()
    val extraHeaders = run {
        if (auth !is OAuthAuthorization) return@run null
        val officialKeyType = TwitterContentUtils.getOfficialKeyType(context,
                auth.consumerKey, auth.consumerSecret)
        return@run MicroBlogAPIFactory.getExtraHeaders(context, officialKeyType)
    } ?: UserAgentExtraHeaders(MicroBlogAPIFactory.getTwidereUserAgent(context))
    val holder = DependencyHolder.get(context)
    var extraRequestParams: Map<String, String>? = null
    when (cls) {
        TwitterUpload::class.java -> {
            val conf = HttpClientFactory.HttpClientConfiguration(holder.preferences)
            // Use longer timeout for uploading
            conf.readTimeoutSecs = 30
            conf.writeTimeoutSecs = 30
            conf.connectionTimeoutSecs = 60
            val uploadHttpClient = HttpClientFactory.createRestHttpClient(conf, holder.dns,
                    holder.connectionPool, holder.cache)
            factory.setHttpClient(uploadHttpClient)
        }
        TwitterUserStream::class.java, FanfouStream::class.java -> {
            val conf = HttpClientFactory.HttpClientConfiguration(holder.preferences)
            // Use longer read timeout for streaming
            conf.readTimeoutSecs = 300
            val streamHttpClient = HttpClientFactory.createRestHttpClient(conf, holder.dns,
                    holder.connectionPool, holder.cache)
            factory.setHttpClient(streamHttpClient)
        }
        else -> {
            factory.setHttpClient(holder.restHttpClient)
        }
    }
    factory.setAuthorization(auth)
    factory.setEndpoint(endpoint)
    when (accountType) {
        AccountType.TWITTER -> {
            factory.setConstantPool(sTwitterConstantPool)
        }
        AccountType.FANFOU -> {
            factory.setConstantPool(sFanfouConstantPool)
            if (cls != FanfouStream::class.java) {
                extraRequestParams = mapOf("format" to "html")
            }
        }
    }
    factory.setRestConverterFactory(TwitterConverterFactory)
    factory.setExceptionFactory(TwidereExceptionFactory)
    factory.setRestRequestFactory(TwidereRestRequestFactory(extraRequestParams))
    factory.setHttpRequestFactory(TwidereHttpRequestFactory(extraHeaders))
    return factory.build<T>(cls)
}

internal fun Credentials.authorizationHeader(
        uri: Uri,
        modifiedUri: Uri = TwidereMediaDownloader.getReplacedUri(uri, api_url_format) ?: uri,
        cls: Class<*>? = null
): String {
    val auth = getAuthorization(cls)
    val endpoint: Endpoint
    if (auth is OAuthAuthorization) {
        endpoint = OAuthEndpoint(TwidereMediaDownloader.getEndpoint(modifiedUri),
                TwidereMediaDownloader.getEndpoint(uri))
    } else {
        endpoint = Endpoint(TwidereMediaDownloader.getEndpoint(modifiedUri))
    }
    val queries = MultiValueMap<String>()
    for (name in uri.queryParameterNames) {
        for (value in uri.getQueryParameters(name)) {
            queries.add(name, value)
        }
    }
    val info = RestRequest("GET", false, uri.path, null, queries, null, null, null, null)
    return auth.getHeader(endpoint, info)
}