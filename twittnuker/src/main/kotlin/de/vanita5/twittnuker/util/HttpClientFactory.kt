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

package de.vanita5.twittnuker.util

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.text.TextUtils.isEmpty
import okhttp3.ConnectionPool
import okhttp3.Credentials
import okhttp3.Dns
import okhttp3.OkHttpClient
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.restfu.okhttp3.OkHttpRestClient
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.*
import de.vanita5.twittnuker.util.dagger.DependencyHolder
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object HttpClientFactory {

    fun createRestHttpClient(context: Context,
                             prefs: SharedPreferencesWrapper, dns: Dns,
                             connectionPool: ConnectionPool): RestHttpClient {
        val builder = OkHttpClient.Builder()
        initOkHttpClient(context, prefs, builder, dns, connectionPool)
        return OkHttpRestClient(builder.build())
    }

    fun initOkHttpClient(context: Context, prefs: SharedPreferencesWrapper,
                         builder: OkHttpClient.Builder, dns: Dns,
                         connectionPool: ConnectionPool) {
        updateHttpClientConfiguration(context, builder, prefs, dns, connectionPool)
        DebugModeUtils.initForOkHttpClient(builder)
    }

    @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
    fun updateHttpClientConfiguration(context: Context,
                                      builder: OkHttpClient.Builder,
                                      prefs: SharedPreferencesWrapper, dns: Dns,
                                      connectionPool: ConnectionPool) {
        val enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false)
        builder.connectTimeout(prefs.getInt(KEY_CONNECTION_TIMEOUT, 10).toLong(), TimeUnit.SECONDS)
        builder.connectionPool(connectionPool)
        if (enableProxy) {
            val proxyType = prefs.getString(KEY_PROXY_TYPE, null)
            val proxyHost = prefs.getString(KEY_PROXY_HOST, null)
            val proxyPort = NumberUtils.toInt(prefs.getString(KEY_PROXY_PORT, null), -1)
            if (!isEmpty(proxyHost) && TwidereMathUtils.inRange(proxyPort, 0, 65535,
                    TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE)) {
                val type = getProxyType(proxyType)
                if (type != Proxy.Type.DIRECT) {
                    builder.proxy(Proxy(type, InetSocketAddress.createUnresolved(proxyHost, proxyPort)))
                }
            }
            val username = prefs.getString(KEY_PROXY_USERNAME, null)
            val password = prefs.getString(KEY_PROXY_PASSWORD, null)
            builder.authenticator { route, response ->
                val b = response.request().newBuilder()
                if (response.code() == 407) {
                    if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                        val credential = Credentials.basic(username, password)
                        b.header("Proxy-Authorization", credential)
                    }
                }
                b.build()
            }
        }
        builder.dns(dns)
    }

    private fun getProxyType(proxyType: String?): Proxy.Type {
        if (proxyType == null) return Proxy.Type.DIRECT
        when (proxyType.toLowerCase()) {
        //            case "socks": {
        //                return Proxy.Type.SOCKS;
        //            }
            "http" -> {
                return Proxy.Type.HTTP
            }
        }
        return Proxy.Type.DIRECT
    }

    fun reloadConnectivitySettings(context: Context) {
        val holder = DependencyHolder.get(context)
        val client = holder.restHttpClient
        if (client is OkHttpRestClient) {
            val builder = OkHttpClient.Builder()
            initOkHttpClient(context, holder.preferences, builder,
                    holder.dns, holder.connectionPoll)
            client.client = builder.build()
        }
    }
}