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

import org.mariotaku.simplerestapi.http.Authorization;
import de.vanita5.twittnuker.api.twitter.TwitterAPIFactory;
import de.vanita5.twittnuker.api.twitter.auth.OAuthAuthorization;
import de.vanita5.twittnuker.api.twitter.auth.OAuthToken;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

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

	/**
	 * Returns a OAuth Authenticated instance.<br>
	 * consumer key and consumer Secret must be provided by
	 * twitter4j.properties, or system properties.<br>
	 * Unlike {@link twitter4j.Twitter#setOAuthAccessToken(twitter4j.auth.AccessToken)} ,
	 * this factory method potentially returns a cached instance.
	 * 
	 * @param accessToken access token
	 * @return an instance
	 * @since Twitter4J 2.1.9
	 */
    public Twitter getInstance(final OAuthToken accessToken) {
		final String consumerKey = conf.getOAuthConsumerKey();
		final String consumerSecret = conf.getOAuthConsumerSecret();
		if (null == consumerKey && null == consumerSecret)
			throw new IllegalStateException("Consumer key and Consumer secret not supplied.");
        final OAuthAuthorization oauth = new OAuthAuthorization(conf.getOAuthConsumerKey(), conf.getOAuthConsumerSecret(), accessToken);
		return getInstance(oauth);
	}

	public Twitter getInstance(final Authorization auth) {
        return TwitterAPIFactory.getInstance(auth);
	}

}