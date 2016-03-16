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

import java.util.Arrays;

import de.vanita5.twittnuker.model.ParcelableMedia;

public class GenericProvider implements Provider {

    private static final String[] EXTENSIONS = new String[] {"png", "jpg", "jpeg", "gif", "bmp"};

    @Override
    public boolean supports(@NonNull String link) {
        final int idx = link.lastIndexOf('.');
        if (idx < 0) return false;
        final String extension = link.substring(idx + 1);
        return Arrays.asList(EXTENSIONS).contains(extension);
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String url) {
        ParcelableMedia media = new ParcelableMedia();
        media.type = ParcelableMedia.Type.IMAGE;
        media.url = url;
        media.preview_url = url;
        media.media_url = url;
        return media;
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String link, @NonNull RestHttpClient client, @Nullable Object extra) {
        return from(link);
    }
}
