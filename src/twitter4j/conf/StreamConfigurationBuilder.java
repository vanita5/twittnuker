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

package twitter4j.conf;

import twitter4j.http.HostAddressResolver;
import twitter4j.http.HostAddressResolverFactory;

/**
 * A builder that can be used to construct a twitter4j configuration with
 * desired settings. This builder has sensible defaults such that
 * {@code new ConfigurationBuilder().build()} would create a usable
 * configuration. This configuration builder is useful for clients that wish to
 * configure twitter4j in unit tests or from command line flags for example.
 * 
 * @author John Sirois - john.sirois at gmail.com
 */
public final class StreamConfigurationBuilder {

	private StreamConfigurationBase configuration = new StreamConfigurationBase();

	public StreamConfiguration build() {
		checkNotBuilt();
		configuration.cacheInstance();
		try {
			return configuration;
		} finally {
			configuration = null;
		}
	}

	public StreamConfigurationBuilder setAsyncNumThreads(final int asyncNumThreads) {
		checkNotBuilt();
		configuration.setAsyncNumThreads(asyncNumThreads);
		return this;
	}

	public StreamConfigurationBuilder setClientName(final String clientName) {
		checkNotBuilt();
		configuration.setClientName(clientName);
		return this;
	}

	public StreamConfigurationBuilder setClientURL(final String clientURL) {
		checkNotBuilt();
		configuration.setClientURL(clientURL);
		return this;
	}

	public StreamConfigurationBuilder setClientVersion(final String clientVersion) {
		checkNotBuilt();
		configuration.setClientVersion(clientVersion);
		return this;
	}

	public StreamConfigurationBuilder setDebugEnabled(final boolean debugEnabled) {
		checkNotBuilt();
		configuration.setDebug(debugEnabled);
		return this;
	}

	public StreamConfigurationBuilder setDispatcherImpl(final String dispatcherImpl) {
		checkNotBuilt();
		configuration.setDispatcherImpl(dispatcherImpl);
		return this;
	}

	public StreamConfigurationBuilder setGZIPEnabled(final boolean gzipEnabled) {
		checkNotBuilt();
		configuration.setGZIPEnabled(gzipEnabled);
		return this;
	}

	public StreamConfigurationBuilder setHostAddressResolverFactory(final HostAddressResolverFactory factory) {
		checkNotBuilt();
		configuration.setHostAddressResolverFactory(factory);
		return this;
	}

	public StreamConfigurationBuilder setHttpConnectionTimeout(final int httpConnectionTimeout) {
		checkNotBuilt();
		configuration.setHttpConnectionTimeout(httpConnectionTimeout);
		return this;
	}

	public StreamConfigurationBuilder setHttpDefaultMaxPerRoute(final int httpDefaultMaxPerRoute) {
		checkNotBuilt();
		configuration.setHttpDefaultMaxPerRoute(httpDefaultMaxPerRoute);
		return this;
	}

	public StreamConfigurationBuilder setHttpMaxTotalConnections(final int httpMaxConnections) {
		checkNotBuilt();
		configuration.setHttpMaxTotalConnections(httpMaxConnections);
		return this;
	}

	public StreamConfigurationBuilder setHttpProxyHost(final String httpProxyHost) {
		checkNotBuilt();
		configuration.setHttpProxyHost(httpProxyHost);
		return this;
	}

	public StreamConfigurationBuilder setHttpProxyPassword(final String httpProxyPassword) {
		checkNotBuilt();
		configuration.setHttpProxyPassword(httpProxyPassword);
		return this;
	}

	public StreamConfigurationBuilder setHttpProxyPort(final int httpProxyPort) {
		checkNotBuilt();
		configuration.setHttpProxyPort(httpProxyPort);
		return this;
	}

	public StreamConfigurationBuilder setHttpProxyUser(final String httpProxyUser) {
		checkNotBuilt();
		configuration.setHttpProxyUser(httpProxyUser);
		return this;
	}

	public StreamConfigurationBuilder setHttpReadTimeout(final int httpReadTimeout) {
		checkNotBuilt();
		configuration.setHttpReadTimeout(httpReadTimeout);
		return this;
	}

	public StreamConfigurationBuilder setHttpRetryCount(final int httpRetryCount) {
		checkNotBuilt();
		configuration.setHttpRetryCount(httpRetryCount);
		return this;
	}

	public StreamConfigurationBuilder setHttpRetryIntervalSeconds(final int httpRetryIntervalSeconds) {
		checkNotBuilt();
		configuration.setHttpRetryIntervalSeconds(httpRetryIntervalSeconds);
		return this;
	}

	public StreamConfigurationBuilder setHttpStreamingReadTimeout(final int httpStreamingReadTimeout) {
		checkNotBuilt();
		configuration.setHttpStreamingReadTimeout(httpStreamingReadTimeout);
		return this;
	}

	public StreamConfigurationBuilder setIgnoreSSLError(final boolean ignoreSSLError) {
		checkNotBuilt();
		configuration.setIgnoreSSLError(ignoreSSLError);
		return this;
	}

	public StreamConfigurationBuilder setIncludeEntitiesEnabled(final boolean enabled) {
		checkNotBuilt();
		configuration.setIncludeEntitiesEnabled(enabled);
		return this;
	}

	public StreamConfigurationBuilder setIncludeRTsEnabled(final boolean enabled) {
		checkNotBuilt();
		configuration.setIncludeRTsEnabled(enabled);
		return this;
	}

	public StreamConfigurationBuilder setJSONStoreEnabled(final boolean enabled) {
		checkNotBuilt();
		configuration.setJSONStoreEnabled(enabled);
		return this;
	}

	public StreamConfigurationBuilder setOAuthAccessToken(final String oAuthAccessToken) {
		checkNotBuilt();
		configuration.setOAuthAccessToken(oAuthAccessToken);
		return this;
	}

	public StreamConfigurationBuilder setOAuthAccessTokenSecret(final String oAuthAccessTokenSecret) {
		checkNotBuilt();
		configuration.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
		return this;
	}

	public StreamConfigurationBuilder setOAuthBaseURL(final String oAuthBaseURL) {
		checkNotBuilt();
		configuration.setOAuthBaseURL(oAuthBaseURL);
		return this;
	}

	public StreamConfigurationBuilder setOAuthConsumerKey(final String oAuthConsumerKey) {
		checkNotBuilt();
		configuration.setOAuthConsumerKey(oAuthConsumerKey);
		return this;
	}

	public StreamConfigurationBuilder setOAuthConsumerSecret(final String oAuthConsumerSecret) {
		checkNotBuilt();
		configuration.setOAuthConsumerSecret(oAuthConsumerSecret);
		return this;
	}

	public StreamConfigurationBuilder setPassword(final String password) {
		checkNotBuilt();
		configuration.setPassword(password);
		return this;
	}

	public StreamConfigurationBuilder setPrettyDebugEnabled(final boolean prettyDebugEnabled) {
		checkNotBuilt();
		configuration.setPrettyDebugEnabled(prettyDebugEnabled);
		return this;
	}

	public StreamConfigurationBuilder setRestBaseURL(final String restBaseURL) {
		checkNotBuilt();
		configuration.setRestBaseURL(restBaseURL);
		return this;
	}

	public StreamConfigurationBuilder setSigningOAuthBaseURL(final String signingOAuthBaseURL) {
		checkNotBuilt();
		configuration.setSigningOAuthBaseURL(signingOAuthBaseURL);
		return this;
	}

	public StreamConfigurationBuilder setSigningRestBaseURL(final String signingRestBaseURL) {
		checkNotBuilt();
		configuration.setSigningRestBaseURL(signingRestBaseURL);
		return this;
	}

	public StreamConfigurationBuilder setSiteStreamBaseURL(final String siteStreamBaseURL) {
		checkNotBuilt();
		configuration.setSiteStreamBaseURL(siteStreamBaseURL);
		return this;
	}

	public StreamConfigurationBuilder setStreamBaseURL(final String streamBaseURL) {
		checkNotBuilt();
		configuration.setStreamBaseURL(streamBaseURL);
		return this;
	}

	public StreamConfigurationBuilder setUser(final String user) {
		checkNotBuilt();
		configuration.setUser(user);
		return this;
	}

	public StreamConfigurationBuilder setUserStreamBaseURL(final String siteStreamBaseURL) {
		checkNotBuilt();
		configuration.setUserStreamBaseURL(siteStreamBaseURL);
		return this;
	}

	public StreamConfigurationBuilder setUserStreamRepliesAllEnabled(final boolean enabled) {
		checkNotBuilt();
		configuration.setUserStreamRepliesAllEnabled(enabled);
		return this;
	}

	public StreamConfigurationBuilder setUseSSL(final boolean useSSL) {
		checkNotBuilt();
		configuration.setUseSSL(useSSL);
		return this;
	}

	private void checkNotBuilt() {
		if (configuration == null)
			throw new IllegalStateException("Cannot use this builder any longer, build() has already been called");
	}
}
