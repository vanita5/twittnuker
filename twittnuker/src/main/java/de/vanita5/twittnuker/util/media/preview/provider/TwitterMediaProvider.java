/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util.media.preview.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.restfu.http.RestHttpClient;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.util.UriUtils;

import java.util.Locale;

/**
 * Created by darkwhite on 1/16/16.
 */
public class TwitterMediaProvider implements Provider {
    @Override
    public boolean supports(@NonNull String link) {
        return isSupported(link);
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String link) {
        final String path = UriUtils.getPath(link);
        if (path == null) return null;
        final ParcelableMedia media = new ParcelableMedia();
        media.url = link;
        if (path.startsWith("/media/")) {
            media.type = ParcelableMedia.Type.IMAGE;
            media.preview_url = String.format(Locale.ROOT, "%s:medium", link);
            media.media_url = String.format(Locale.ROOT, "%s:orig", link);
        } else if (path.startsWith("/tweet_video/")) {
            // Video is not supported yet
            return null;
        } else {
            // Don't display media that not supported yet
            return null;
        }

        return media;
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String link, @NonNull RestHttpClient client, @Nullable Object extra) {
        return from(link);
    }

    public static boolean isSupported(@NonNull String link) {
        final String authority = UriUtils.getAuthority(link);
        if (authority == null || !authority.endsWith(".twimg.com")) {
            return false;
        }
        final String path = UriUtils.getPath(link);
        if (path == null) return false;
        if (path.startsWith("/media/")) {
            return true;
        }
        return false;
    }

}