/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vanita5.twittnuker.util.net;

import static android.text.TextUtils.isEmpty;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.TextUtils;
import de.vanita5.twittnuker.util.net.ssl.ApacheTrustAllSSLSocketFactoryHC4;

import twitter4j.TwitterException;
import twitter4j.auth.Authorization;
import twitter4j.http.FactoryUtils;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HttpClientConfiguration;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpResponseCode;
import twitter4j.http.RequestMethod;
import twitter4j.internal.logging.Logger;
import twitter4j.internal.util.InternalStringUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * HttpClient implementation for Apache HttpClient 4.0.x
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public class HttpClientImpl implements twitter4j.http.HttpClient, HttpResponseCode {
	private static final Logger logger = Logger.getLogger(HttpClientImpl.class);
	private final HttpClientConfiguration conf;
	private final HttpClient client;

	public HttpClientImpl(final HttpClientConfiguration conf) {
		this.conf = conf;
		final HttpClientBuilder clientBuilder = HttpClients.custom();
		final LayeredConnectionSocketFactory factory;
		if (conf.isSSLErrorIgnored()) {
			factory = ApacheTrustAllSSLSocketFactoryHC4.getSocketFactory();
		} else {
			factory = SSLConnectionSocketFactory.getSocketFactory();
		}
		clientBuilder.setSSLSocketFactory(factory);
		final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder.setConnectionRequestTimeout(conf.getHttpConnectionTimeout());
		requestConfigBuilder.setConnectTimeout(conf.getHttpConnectionTimeout());
		requestConfigBuilder.setSocketTimeout(conf.getHttpReadTimeout());
		clientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
		if (conf.isProxyConfigured()) {
			final HttpHost proxy = new HttpHost(conf.getHttpProxyHost(), conf.getHttpProxyPort());
			clientBuilder.setProxy(proxy);
			if (!TextUtils.isEmpty(conf.getHttpProxyUser())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Proxy AuthUser: " + conf.getHttpProxyUser());
					logger.debug("Proxy AuthPassword: " + InternalStringUtil.maskString(conf.getHttpProxyPassword()));
				}
				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(new AuthScope(conf.getHttpProxyHost(), conf.getHttpProxyPort()),
						new UsernamePasswordCredentials(conf.getHttpProxyUser(), conf.getHttpProxyPassword()));
				clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			}
		}
		client = clientBuilder.build();
	}

	@Override
	public twitter4j.http.HttpResponse request(final twitter4j.http.HttpRequest req) throws TwitterException {
		try {
			HttpRequestBase commonsRequest;

			final HostAddressResolver resolver = FactoryUtils.getHostAddressResolver(conf);
			final String urlString = req.getURL();
			final URI urlOrig;
			try {
				urlOrig = new URI(urlString);
			} catch (final URISyntaxException e) {
				throw new TwitterException(e);
			}
			final String host = urlOrig.getHost(), authority = urlOrig.getAuthority();
			final String resolvedHost = resolver != null ? resolver.resolve(host) : null;
			final String resolvedUrl = !isEmpty(resolvedHost) ? urlString.replace("://" + host, "://" + resolvedHost)
					: urlString;

            final RequestMethod method = req.getMethod();
            if (method == RequestMethod.GET) {
				commonsRequest = new HttpGet(resolvedUrl);
			} else if (method == RequestMethod.POST) {
				final HttpPost post = new HttpPost(resolvedUrl);
				// parameter has a file?
				final HttpParameter[] params = req.getParameters();
				if (params != null) {
					if (HttpParameter.containsFile(params)) {
						final MultipartEntityBuilder me = MultipartEntityBuilder.create();
						for (final HttpParameter param : params) {
							if (param.isFile()) {
								final ContentType contentType = ContentType.create(param.getContentType());
								final ContentBody body;
								if (param.getFile() != null) {
									body = new FileBody(param.getFile(), ContentType.create(param.getContentType()));
								} else {
									body = new InputStreamBody(param.getFileBody(), contentType, param.getFileName());
								}
								me.addPart(param.getName(), body);
							} else {
								final ContentType contentType = ContentType.TEXT_PLAIN.withCharset(Consts.UTF_8);
								final ContentBody body = new StringBody(param.getValue(), contentType);
                                me.addPart(param.getName(), body);
							}
						}
						post.setEntity(me.build());
					} else {
						if (params.length > 0) {
							post.setEntity(new UrlEncodedFormEntity(params));
						}
					}
				}
				post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
				commonsRequest = post;
			} else if (method == RequestMethod.DELETE) {
				commonsRequest = new HttpDelete(resolvedUrl);
			} else if (method == RequestMethod.HEAD) {
				commonsRequest = new HttpHead(resolvedUrl);
			} else if (method == RequestMethod.PUT) {
				commonsRequest = new HttpPut(resolvedUrl);
			} else
				throw new TwitterException("Unsupported request method " + method);
			final Map<String, String> headers = req.getRequestHeaders();
			for (final String headerName : headers.keySet()) {
				commonsRequest.addHeader(headerName, headers.get(headerName));
			}
            final Authorization authorization = req.getAuthorization();
            final String authorizationHeader = authorization != null ? authorization.getAuthorizationHeader(req) : null;
            if (authorizationHeader != null) {
				commonsRequest.addHeader("Authorization", authorizationHeader);
			}
            if (resolvedHost != null && !resolvedHost.isEmpty() && !resolvedHost.equals(host)) {
				commonsRequest.addHeader("Host", authority);
			}

			final ApacheHttpClientHttpResponseImpl res;
			try {
				res = new ApacheHttpClientHttpResponseImpl(client.execute(commonsRequest), conf);
			} catch (final IllegalStateException e) {
				throw new TwitterException("Please check your API settings.", e);
			} catch (final NullPointerException e) {
				// Bug http://code.google.com/p/android/issues/detail?id=5255
				throw new TwitterException("Please check your APN settings, make sure not to use WAP APNs.", e);
			} catch (final OutOfMemoryError e) {
				// I don't know why OOM thown, but it should be catched.
				System.gc();
				throw new TwitterException("Unknown error", e);
			}
			final int statusCode = res.getStatusCode();
			if (statusCode < OK || statusCode > ACCEPTED) throw new TwitterException(res.asString(), req, res);
			return res;
		} catch (final IOException e) {
			throw new TwitterException(e);
		}
	}

	@Override
	public void shutdown() {
		client.getConnectionManager().shutdown();
	}
}
