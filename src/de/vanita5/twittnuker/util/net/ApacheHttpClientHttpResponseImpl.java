/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util.net;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;

import twitter4j.http.HttpClientConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
final class ApacheHttpClientHttpResponseImpl extends twitter4j.http.HttpResponse {
	private final HttpResponse res;

	ApacheHttpClientHttpResponseImpl(final HttpResponse res, final HttpClientConfiguration conf) throws IOException {
		super(conf);
		this.res = res;
		is = res.getEntity().getContent();
		statusCode = res.getStatusLine().getStatusCode();
		if (is != null && "gzip".equals(getResponseHeader("Content-Encoding"))) {
			// the response is gzipped
			is = new GZIPInputStream(is);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() throws IOException {
		if (res != null) {
			res.getEntity().consumeContent();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getResponseHeader(final String name) {
		final Header[] headers = res.getHeaders(name);
		if (headers != null && headers.length > 0)
			return headers[0].getValue();
		else
			return null;
	}

	@Override
	public Map<String, List<String>> getResponseHeaderFields() {
		final Header[] headers = res.getAllHeaders();
		final Map<String, List<String>> maps = new HashMap<String, List<String>>();
		for (final Header header : headers) {
			final HeaderElement[] elements = header.getElements();
			final List<String> values = new ArrayList<String>(1);
			for (final HeaderElement element : elements) {
				values.add(element.getValue());
			}
			maps.put(header.getName(), values);
		}
		return maps;
	}
}
