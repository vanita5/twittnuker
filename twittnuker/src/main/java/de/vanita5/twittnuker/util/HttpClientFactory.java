/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This code incorporates a modified class of Twittbomber.
 * Copyright (C) 2012 nilsding
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

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

public class HttpClientFactory {

	private static DefaultHttpClient client;

	public static synchronized DefaultHttpClient getThreadSafeClient() {
		if (client != null) {
			return client;
		}

		client = new DefaultHttpClient();

		ClientConnectionManager manager = client.getConnectionManager();

		HttpParams params = client.getParams();

		client = new DefaultHttpClient(
				new ThreadSafeClientConnManager(
						params,
						manager.getSchemeRegistry())
				, params);

		return client;
	}
}
