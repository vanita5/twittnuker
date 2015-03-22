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

package twitter4j.http;

import java.util.HashMap;
import java.util.Map;

import twitter4j.TwitterException;
import twitter4j.conf.ConfigurationContext;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public class HttpClientImpl extends HttpClientBase implements HttpClient, HttpResponseCode {

    private static final Map<HttpClientConfiguration, HttpClient> instanceMap = new HashMap<>(
			1);

	public HttpClientImpl() {
		super(ConfigurationContext.getInstance());
    }

	public HttpClientImpl(final HttpClientConfiguration conf) {
		super(conf);
	}

	public HttpResponse get(final String url, final String sign_url) throws TwitterException {
		return request(new HttpRequest(RequestMethod.GET, url, sign_url, null, null, null));
	}

	public HttpResponse post(final String url, final String sign_url, final HttpParameter[] params)
			throws TwitterException {
		return request(new HttpRequest(RequestMethod.POST, url, sign_url, params, null, null));
	}

	@Override
	public HttpResponse request(final HttpRequest req) throws TwitterException {
        throw new UnsupportedOperationException();
	}

	public static HttpClient getInstance(final HttpClientConfiguration conf) {
		HttpClient client = instanceMap.get(conf);
		if (null == client) {
			client = new HttpClientImpl(conf);
			instanceMap.put(conf, client);
		}
		return client;
	}

}