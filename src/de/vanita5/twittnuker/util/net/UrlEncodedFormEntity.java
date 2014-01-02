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

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import twitter4j.http.HttpParameter;

import java.io.UnsupportedEncodingException;

/**
 * An entity composed of a list of url-encoded pairs. This is typically useful
 * while sending an HTTP POST request.
 * 
 * @since 4.0
 */
public class UrlEncodedFormEntity extends StringEntity {

	/**
	 * Constructs a new {@link UrlEncodedFormEntity} with the list of parameters
	 * in the specified encoding.
	 * 
	 * @param parameters list of name/value pairs
	 * @param encoding encoding the name/value pairs be encoded with
	 * @throws UnsupportedEncodingException if the encoding isn't supported
	 */
	public UrlEncodedFormEntity(final HttpParameter[] params) throws UnsupportedEncodingException {
		super(HttpParameter.encodeParameters(params), HTTP.DEFAULT_CONTENT_CHARSET);
		setContentType(URLEncodedUtils.CONTENT_TYPE + HTTP.CHARSET_PARAM + HTTP.DEFAULT_CONTENT_CHARSET);
	}

}
