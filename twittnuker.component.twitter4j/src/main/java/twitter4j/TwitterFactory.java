/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import android.net.SSLCertificateSocketFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.Network;

import org.mariotaku.simplerestapi.RestAPIFactory;
import org.mariotaku.simplerestapi.RestMethod;
import org.mariotaku.simplerestapi.RestMethodInfo;
import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.simplerestapi.http.Endpoint;
import org.mariotaku.simplerestapi.http.RestRequest;
import de.vanita5.twittnuker.api.twitter.OkHttpRestClient;
import de.vanita5.twittnuker.api.twitter.TwitterConverter;
import de.vanita5.twittnuker.api.twitter.auth.OAuthAuthorization;
import de.vanita5.twittnuker.api.twitter.auth.OAuthEndpoint;
import de.vanita5.twittnuker.api.twitter.auth.OAuthToken;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import twitter4j.api.TwitterUpload;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HostAddressResolverFactory;
import twitter4j.http.HttpClientConfiguration;

/**
 * A factory class for Twitter. <br>
 * An instance of this class is completely thread safe and can be re-used and
 * used concurrently.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.0
 */
public final class TwitterFactory {
	/* AsyncTwitterFactory and TWitterStream will access this field */

	private final Configuration conf;

	/**
	 * Creates a TwitterFactory with the root configuration.
	 */
	public TwitterFactory() {
		this(ConfigurationContext.getInstance());
	}

	/**
	 * Creates a TwitterFactory with the given configuration.
	 * 
	 * @param conf the configuration to use
	 * @since Twitter4J 2.1.1
	 */
	public TwitterFactory(final Configuration conf) {
		if (conf == null) throw new NullPointerException("configuration cannot be null");
		this.conf = conf;
	}

    public Twitter getInstance(final OAuthToken accessToken) {
		final String consumerKey = conf.getOAuthConsumerKey();
		final String consumerSecret = conf.getOAuthConsumerSecret();
		if (null == consumerKey && null == consumerSecret)
			throw new IllegalStateException("Consumer key and Consumer secret not supplied.");
        final OAuthAuthorization oauth = new OAuthAuthorization(conf.getOAuthConsumerKey(), conf.getOAuthConsumerSecret(), accessToken);
		return getInstance(oauth);
	}

	public Twitter getInstance(final Authorization auth) {
        return getInstance(auth, Twitter.class);
    }

    public <T> T getInstance(final Authorization auth, Class<T> cls) {
        final OAuthEndpoint endpoint;
        if (TwitterOAuth.class.isAssignableFrom(cls)) {
            endpoint = new OAuthEndpoint(conf.getOAuthBaseURL(), conf.getSigningOAuthBaseURL());
        } else if (TwitterUpload.class.isAssignableFrom(cls)) {
            endpoint = new OAuthEndpoint(conf.getUploadBaseURL(), conf.getSigningUploadBaseURL());
        } else {
            endpoint = new OAuthEndpoint(conf.getRestBaseURL(), conf.getSigningRestBaseURL());
        }
        final RestAPIFactory factory = new RestAPIFactory();
        factory.setClient(new OkHttpRestClient(createHttpClient(conf)));
        factory.setConverter(new TwitterConverter());
        factory.setEndpoint(endpoint);
        factory.setAuthorization(auth);
        factory.setRequestFactory(new RestRequest.Factory() {

            @Override
            public RestRequest create(@NonNull Endpoint endpoint, @NonNull RestMethodInfo info, @Nullable Authorization authorization) {
                final RestMethod restMethod = info.getMethod();
                final String url = Endpoint.constructUrl(endpoint.getUrl(), info);
                final ArrayList<Pair<String, String>> headers = new ArrayList<>(info.getHeaders());

                if (authorization != null && authorization.hasAuthorization()) {
                    headers.add(Pair.create("Authorization", authorization.getHeader(endpoint, info)));
                }
                headers.add(Pair.create("User-Agent", conf.getHttpUserAgent()));
                return new RestRequest(restMethod.value(), url, headers, info.getBody(), null);
            }
        });
        return factory.build(cls);
	}

    public Twitter getInstance() {
        return getInstance(new OAuthToken(conf.getOAuthConsumerKey(), conf.getOAuthConsumerSecret()));
    }


    private static OkHttpClient createHttpClient(HttpClientConfiguration conf) {
        final OkHttpClient client = new OkHttpClient();
        final boolean ignoreSSLError = conf.isSSLErrorIgnored();
        final SSLCertificateSocketFactory sslSocketFactory;
        final HostAddressResolverFactory resolverFactory = conf.getHostAddressResolverFactory();
        if (ignoreSSLError) {
            sslSocketFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getInsecure(0, null);
        } else {
            sslSocketFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0, null);
        }
        client.setSslSocketFactory(sslSocketFactory);
        client.setSocketFactory(SocketFactory.getDefault());
        client.setConnectTimeout(conf.getHttpConnectionTimeout(), TimeUnit.MILLISECONDS);

        if (conf.isProxyConfigured()) {
            client.setProxy(new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(conf.getHttpProxyHost(),
                    conf.getHttpProxyPort())));
        }
        if (resolverFactory != null) {
            final HostAddressResolver resolver = resolverFactory.getInstance(conf);
            Internal.instance.setNetwork(client, new Network() {
                @Override
                public InetAddress[] resolveInetAddresses(String host) throws UnknownHostException {
                    try {
                        return resolver.resolve(host);
                    } catch (IOException e) {
                        if (e instanceof UnknownHostException) throw (UnknownHostException) e;
                        throw new UnknownHostException("Unable to resolve address " + e.getMessage());
                    }
                }
            });
        }
        return client;
    }
}