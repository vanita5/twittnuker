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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.mariotaku.restfu.http.RestHttpClient;

import java.util.List;
import java.util.Locale;

import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.util.media.preview.PreviewMediaExtractor;

public class InstagramProvider implements Provider {
    @Override
    public boolean supports(@NonNull String link) {
        final String authority = PreviewMediaExtractor.getAuthority(link);
        return "instagr.am".equals(authority) || "instagram.com".equals(authority) ||
                "www.instagram.com".equals(authority);
    }

    @Override
    @Nullable
    public ParcelableMedia from(@NonNull String link) {
        final Uri uri = Uri.parse(link);
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() < 2 || !"p".equals(pathSegments.get(0))) return null;
        final String id = pathSegments.get(1);
        final ParcelableMedia media = new ParcelableMedia();
        media.type = ParcelableMedia.Type.IMAGE;
        media.url = link;
        media.preview_url = String.format(Locale.ROOT, "https://instagram.com/p/%s/media/?size=m", id);
        media.media_url = String.format(Locale.ROOT, "https://instagram.com/p/%s/media/?size=l", id);
        return media;
    }

    @Override
    @Nullable
    @WorkerThread
    public ParcelableMedia from(@NonNull String link, @NonNull RestHttpClient client, @Nullable Object extra) {
        return from(link);
    }
}