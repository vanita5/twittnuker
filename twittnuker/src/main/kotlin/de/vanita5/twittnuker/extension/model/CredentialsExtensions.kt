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

package de.vanita5.twittnuker.extension.model

import android.content.Context
import android.text.TextUtils
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.*
import de.vanita5.twittnuker.library.twitter.auth.BasicAuthorization
import de.vanita5.twittnuker.library.twitter.auth.EmptyAuthorization
import de.vanita5.twittnuker.library.twitter.util.TwitterConverterFactory
import org.mariotaku.restfu.RestAPIFactory
import org.mariotaku.restfu.http.Authorization
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.SimpleValueMap
import org.mariotaku.restfu.oauth.OAuthAuthorization
import org.mariotaku.restfu.oauth.OAuthEndpoint
import org.mariotaku.restfu.oauth.OAuthToken
import de.vanita5.twittnuker.TwittnukerConstants.DEFAULT_TWITTER_API_URL_FORMAT
import de.vanita5.twittnuker.model.ConsumerKeyType
import de.vanita5.twittnuker.model.account.cred.BasicCredentials
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.account.cred.EmptyCredentials
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import de.vanita5.twittnuker.util.MicroBlogAPIFactory.sTwitterConstantPool
import de.vanita5.twittnuker.util.TwitterContentUtils
import de.vanita5.twittnuker.util.dagger.DependencyHolder

fun Credentials.getAuthorization(): Authorization {
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
    val domain: String
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
        else -> throw TwitterConverterFactory.UnsupportedTypeException(cls)
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

fun <T> Credentials.newMicroBlogInstance(context: Context,
                                         twitterExtraQueries: Boolean = true,
                                         extraRequestParams: Map<String, String>? = null,
                                         cls: Class<T>): T {
    return newMicroBlogInstance(context, getEndpoint(cls), getAuthorization(),
            twitterExtraQueries, extraRequestParams, cls)
}

fun <T> newMicroBlogInstance(context: Context,
                             endpoint: Endpoint,
                             auth: Authorization,
                             twitterExtraQueries: Boolean = true,
                             extraRequestParams: Map<String, String>? = null,
                             cls: Class<T>): T {
    val factory = RestAPIFactory<MicroBlogException>()
    val userAgent: String
    if (auth is OAuthAuthorization) {
        val officialKeyType = TwitterContentUtils.getOfficialKeyType(context,
                auth.consumerKey, auth.consumerSecret)
        if (officialKeyType != ConsumerKeyType.UNKNOWN) {
            userAgent = MicroBlogAPIFactory.getUserAgentName(context, officialKeyType)
        } else {
            userAgent = MicroBlogAPIFactory.getTwidereUserAgent(context)
        }
    } else {
        userAgent = MicroBlogAPIFactory.getTwidereUserAgent(context)
    }
    val holder = DependencyHolder.get(context)
    factory.setHttpClient(holder.restHttpClient)
    factory.setAuthorization(auth)
    factory.setEndpoint(endpoint)
    if (twitterExtraQueries) {
        factory.setConstantPool(sTwitterConstantPool)
    } else {
        factory.setConstantPool(SimpleValueMap())
    }
    val converterFactory = TwitterConverterFactory()
    factory.setRestConverterFactory(converterFactory)
    factory.setRestRequestFactory(MicroBlogAPIFactory.TwidereRestRequestFactory(extraRequestParams))
    factory.setHttpRequestFactory(MicroBlogAPIFactory.TwidereHttpRequestFactory(userAgent))
    factory.setExceptionFactory(MicroBlogAPIFactory.TwidereExceptionFactory(converterFactory))
    return factory.build<T>(cls)
}