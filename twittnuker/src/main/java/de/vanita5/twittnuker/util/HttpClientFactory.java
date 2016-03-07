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

package de.vanita5.twittnuker.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.okhttp3.OkHttpRestClient;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.util.dagger.DependencyHolder;
import de.vanita5.twittnuker.util.net.TwidereDns;
import de.vanita5.twittnuker.util.net.TwidereProxySelector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import static android.text.TextUtils.isEmpty;

public class HttpClientFactory implements Constants {

    public static RestHttpClient createRestHttpClient(final Context context,
                                                      final SharedPreferencesWrapper prefs, final Dns dns,
                                                      final ConnectionPool connectionPool) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        initOkHttpClient(context, prefs, builder, dns, connectionPool);
        return new OkHttpRestClient(builder.build());
    }

    public static void initOkHttpClient(final Context context, final SharedPreferencesWrapper prefs,
                                        final OkHttpClient.Builder builder, final Dns dns,
                                        final ConnectionPool connectionPool) {
        updateHttpClientConfiguration(context, builder, prefs, dns, connectionPool);
        DebugModeUtils.initForOkHttpClient(builder);
    }

    @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
    public static void updateHttpClientConfiguration(final Context context,
                                                     final OkHttpClient.Builder builder,
                                                     final SharedPreferencesWrapper prefs, final Dns dns,
                                                     final ConnectionPool connectionPool) {
        final boolean enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false);
        builder.connectTimeout(prefs.getInt(KEY_CONNECTION_TIMEOUT, 10), TimeUnit.SECONDS);
        final boolean retryOnConnectionFailure = prefs.getBoolean(KEY_RETRY_ON_NETWORK_ISSUE);
        builder.retryOnConnectionFailure(retryOnConnectionFailure);
        builder.connectionPool(connectionPool);
        if (enableProxy) {
            final String proxyType = prefs.getString(KEY_PROXY_TYPE, null);
            final String proxyHost = prefs.getString(KEY_PROXY_HOST, null);
            final int proxyPort = NumberUtils.toInt(prefs.getString(KEY_PROXY_PORT, null), -1);
            if (!isEmpty(proxyHost) && TwidereMathUtils.inRange(proxyPort, 0, 65535,
                    TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE)) {
                final Proxy.Type type = getProxyType(proxyType);
                if (type != Proxy.Type.DIRECT) {
                    if (TwidereDns.isValidIpAddress(proxyHost) && !retryOnConnectionFailure) {
                        builder.proxy(new Proxy(type, InetSocketAddress.createUnresolved(proxyHost, proxyPort)));
                    } else {
                        builder.proxySelector(new TwidereProxySelector(context, type, proxyHost, proxyPort));
                    }
                }
            }
            final String username = prefs.getString(KEY_PROXY_USERNAME, null);
            final String password = prefs.getString(KEY_PROXY_PASSWORD, null);
            builder.authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    final Request.Builder builder = response.request().newBuilder();
                    if (response.code() == 407) {
                        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                            final String credential = Credentials.basic(username, password);
                            builder.header("Proxy-Authorization", credential);
                        }
                    }
                    return builder.build();
                }

            });
        }
        builder.dns(dns);
    }

    private static Proxy.Type getProxyType(String proxyType) {
        if (proxyType == null) return Proxy.Type.DIRECT;
        switch (proxyType.toLowerCase()) {
//            case "socks": {
//                return Proxy.Type.SOCKS;
//            }
            case "http": {
                return Proxy.Type.HTTP;
            }
        }
        return Proxy.Type.DIRECT;
    }

    public static void reloadConnectivitySettings(Context context) {
        final DependencyHolder holder = DependencyHolder.get(context);
        final RestHttpClient client = holder.getRestHttpClient();
        if (client instanceof OkHttpRestClient) {
            final OkHttpClient.Builder builder = new OkHttpClient.Builder();
            initOkHttpClient(context, holder.getPreferences(), builder,
                    holder.getDns(), holder.getConnectionPoll());
            final OkHttpRestClient restClient = (OkHttpRestClient) client;
            restClient.setClient(builder.build());
        }
    }
}