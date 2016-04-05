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
import android.support.annotation.WorkerThread;

import org.mariotaku.restfu.http.RestHttpClient;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.util.UriUtils;

import java.util.Locale;

public class InstagramProvider implements Provider {
    @Override
    public boolean supports(@NonNull String link) {
        final String authority = UriUtils.getAuthority(link);
        if (authority == null) return false;
        switch (authority) {
            //noinspection SpellCheckingInspection
            case "instagr.am":
            case "instagram.com":
            case "www.instagram.com": {
                final String path = UriUtils.getPath(link);
                return path != null && path.startsWith("/p/");
            }
        }
        return false;
    }

    @Override
    @Nullable
    public ParcelableMedia from(@NonNull String link) {
        final String path = UriUtils.getPath(link);
        final String prefix = "/p/";
        if (path == null || !path.startsWith(prefix)) {
            return null;
        }
        String lastPath = path.substring(prefix.length());
        if (lastPath.isEmpty()) return null;
        int end = lastPath.indexOf('/');
        if (end < 0) {
            end = lastPath.length();
        }
        final String id = lastPath.substring(0, end);
        final ParcelableMedia media = new ParcelableMedia();
        media.type = ParcelableMedia.Type.IMAGE;
        media.url = link;
        media.preview_url = String.format(Locale.ROOT, "https://instagram.com/p/%s/media/?size=m", id);
        media.media_url = String.format(Locale.ROOT, "https://instagram.com/p/%s/media/?size=l", id);
        media.open_browser = true;
        return media;
    }

    @Override
    @Nullable
    @WorkerThread
    public ParcelableMedia from(@NonNull String link, @NonNull RestHttpClient client, @Nullable Object extra) {
        return from(link);
    }
}