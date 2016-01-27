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
import android.content.SharedPreferences;
import android.net.SSLCertificateSocketFactory;
import android.net.SSLSessionCache;
import android.text.TextUtils;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.okhttp.OkHttpRestClient;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.util.net.TwidereProxySelector;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import static android.text.TextUtils.isEmpty;

public class HttpClientFactory implements Constants {
    public static RestHttpClient getDefaultHttpClient(final Context context) {
        if (context == null) return null;
        final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(context,
                TwittnukerConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return createHttpClient(context, prefs);
    }

    public static RestHttpClient createHttpClient(final Context context, final SharedPreferences prefs) {
        final OkHttpClient client = new OkHttpClient();
        initDefaultHttpClient(context, prefs, client);
        return new OkHttpRestClient(client);
    }

    public static void initDefaultHttpClient(Context context, SharedPreferences prefs, OkHttpClient client) {
        updateHttpClientConfiguration(context, prefs, client);
        DebugModeUtils.initForHttpClient(client);
    }

    @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
    public static void updateHttpClientConfiguration(final Context context,
                                                     final SharedPreferences prefs,
                                                     final OkHttpClient client) {
        final int connectionTimeoutSeconds = prefs.getInt(KEY_CONNECTION_TIMEOUT, 10);
        final boolean ignoreSslError = prefs.getBoolean(KEY_IGNORE_SSL_ERROR, false);
        final boolean enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false);

        client.setConnectTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS);
        if (ignoreSslError) {
            // We use insecure connections intentionally
            client.setSslSocketFactory(SSLCertificateSocketFactory.getInsecure((int)
                            TimeUnit.SECONDS.toMillis(connectionTimeoutSeconds),
                    new SSLSessionCache(context)));
        } else {
            client.setSslSocketFactory(null);
        }
        if (enableProxy) {
            final String proxyType = prefs.getString(KEY_PROXY_TYPE, null);
            final String proxyHost = prefs.getString(KEY_PROXY_HOST, null);
            final int proxyPort = NumberUtils.toInt(prefs.getString(KEY_PROXY_PORT, null), -1);
            if (!isEmpty(proxyHost) && TwidereMathUtils.inRange(proxyPort, 0, 65535,
                    TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE)) {
                client.setProxy(null);
                client.setProxySelector(new TwidereProxySelector(context, getProxyType(proxyType),
                        proxyHost, proxyPort));
            }
            final String username = prefs.getString(KEY_PROXY_USERNAME, null);
            final String password = prefs.getString(KEY_PROXY_PASSWORD, null);
            client.setAuthenticator(new Authenticator() {
                @Override
                public Request authenticate(Proxy proxy, Response response) throws IOException {
                    return null;
                }

                @Override
                public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                    final Request.Builder builder = response.request().newBuilder();
                    if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                        final String credential = Credentials.basic(username, password);
                        builder.header("Proxy-Authorization", credential);
                    }
                    return builder.build();
                }
            });
        } else {
            client.setProxy(null);
            client.setProxySelector(null);
            client.setAuthenticator(null);
        }
    }

    private static Proxy.Type getProxyType(String proxyType) {
        if ("socks".equalsIgnoreCase(proxyType)) return Proxy.Type.SOCKS;
        return Proxy.Type.HTTP;
    }
}