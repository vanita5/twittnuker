/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util.media.preview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.mariotaku.restfu.http.RestHttpClient;

import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.util.media.preview.provider.InstagramProvider;
import de.vanita5.twittnuker.util.media.preview.provider.Provider;
import de.vanita5.twittnuker.util.media.preview.provider.TwitterMediaProvider;

import java.io.IOException;

public class PreviewMediaExtractor {

    private static final Provider[] sProviders = {
            new InstagramProvider(),
            new TwitterMediaProvider()
    };

    private PreviewMediaExtractor() {
    }

    @Nullable
    public static ParcelableMedia fromLink(@NonNull String link) {
        final Provider provider = providerFor(link);
        if (provider == null) return null;
        return provider.from(link);
    }

    @Nullable
    @WorkerThread
    public static ParcelableMedia fromLink(@NonNull String link, RestHttpClient client, Object extra) throws IOException {
        final Provider provider = providerFor(link);
        if (provider == null) return null;
        return provider.from(link, client, extra);
    }

    @Nullable
    private static Provider providerFor(String link) {
        if (TextUtils.isEmpty(link)) return null;
        for (Provider provider : sProviders) {
            if (provider.supports(link)) {
                return provider;
            }
        }
        return null;
    }


    public static boolean isSupported(@Nullable String link) {
        return providerFor(link) != null;
    }

}