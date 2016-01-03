/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
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

package de.vanita5.twittnuker.util;

import android.content.ContentResolver;
import android.net.Uri;

import de.vanita5.twittnuker.TwittnukerConstants;
import okio.ByteString;

public class SimpleDiskCacheUtils implements TwittnukerConstants {

    public static Uri getCacheUri(String key) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY_TWITTNUKER_CACHE)
                .appendPath(ByteString.encodeUtf8(key).base64Url())
                .build();
    }

    public static String getCacheKey(Uri uri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()))
            throw new IllegalArgumentException(uri.toString());
        if (!AUTHORITY_TWITTNUKER_CACHE.equals(uri.getAuthority()))
            throw new IllegalArgumentException(uri.toString());
        return ByteString.decodeBase64(uri.getLastPathSegment()).utf8();
    }
}