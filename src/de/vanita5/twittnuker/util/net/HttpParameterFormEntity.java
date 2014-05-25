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

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntityHC4;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.http.HttpParameter;

public class HttpParameterFormEntity extends UrlEncodedFormEntityHC4 {

	public HttpParameterFormEntity(final HttpParameter[] params) throws UnsupportedEncodingException {
		super(generateKeyValuePairs(params), Consts.UTF_8);
	}

	private static List<NameValuePair> generateKeyValuePairs(final HttpParameter[] params) {
		final List<NameValuePair> result = new ArrayList<NameValuePair>();
		for (final HttpParameter param : params) {
			if (!param.isFile()) {
				result.add(new BasicNameValuePair(param.getName(), param.getValue()));
			}
		}
		return result;
	}

}