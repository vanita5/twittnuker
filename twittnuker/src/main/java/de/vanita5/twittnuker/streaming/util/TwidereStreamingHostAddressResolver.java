/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.streaming.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.provider.TwidereDataStore.DNS;
import twitter4j.http.HostAddressResolver;

public class TwidereStreamingHostAddressResolver implements HostAddressResolver {

	private static final String RESOLVER_LOGTAG = "Twidere.Streaming.Host";

	private final HostCache mHostCache = new HostCache(512);
	private final Context mContext;

	public TwidereStreamingHostAddressResolver(final Context context) {
		mContext = context;
	}

	@Override
	public InetAddress[] resolve(final String host) throws UnknownHostException {
		if (host == null) return null;
		// First, I'll try to load address cached.
		final InetAddress[] cached = mHostCache.get(host);
		if (cached != null) {
			if (BuildConfig.DEBUG) {
				Log.d(RESOLVER_LOGTAG, "Got cached " + Arrays.toString(cached));
			}
			return cached;
		}
		final InetAddress[] resolved = resolveHost(mContext, host);
		mHostCache.put(host, resolved);
		return resolved;
	}

	private static class HostCache extends LinkedHashMap<String, InetAddress[]> {

		private static final long serialVersionUID = -9216545511009449147L;

		HostCache(final int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		public InetAddress[] put(final String key, final InetAddress[] value) {
			if (value == null) return null;
			return super.put(key, value);
		}
	}

	private static InetAddress fromAddressString(String host, String address) throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getByName(address);
		if (inetAddress instanceof Inet4Address) {
			return Inet4Address.getByAddress(host, inetAddress.getAddress());
		} else if (inetAddress instanceof Inet6Address) {
			return Inet6Address.getByAddress(host, inetAddress.getAddress());
		}
		throw new UnknownHostException("Bad address " + host + " = " + address);
	}

	@NonNull
	public static InetAddress[] resolveHost(final Context context, final String host) throws UnknownHostException {
		if (context == null || host == null) return InetAddress.getAllByName(host);
		final ContentResolver resolver = context.getContentResolver();
		final Uri uri = Uri.withAppendedPath(DNS.CONTENT_URI, host);
		final Cursor cur = resolver.query(uri, DNS.MATRIX_COLUMNS, null, null, null);
		if (cur == null) return InetAddress.getAllByName(host);
		try {
			cur.moveToFirst();
			final ArrayList<InetAddress> addresses = new ArrayList<>();
			final int idxHost = cur.getColumnIndex(DNS.HOST), idxAddr = cur.getColumnIndex(DNS.ADDRESS);
			while (!cur.isAfterLast()) {
				addresses.add(fromAddressString(cur.getString(idxHost), cur.getString(idxAddr)));
				cur.moveToNext();
			}
			if (addresses.isEmpty()) {
				throw new UnknownHostException("Unknown host " + host);
			}
			return addresses.toArray(new InetAddress[addresses.size()]);
		} finally {
			cur.close();
		}
	}
}