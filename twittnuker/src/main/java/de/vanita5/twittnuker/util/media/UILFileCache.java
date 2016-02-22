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

package de.vanita5.twittnuker.util.media;

import android.net.Uri;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.mariotaku.mediaviewer.library.FileCache;
import de.vanita5.twittnuker.provider.CacheProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class UILFileCache implements FileCache {
    private final DiskCache cache;

    public UILFileCache(final DiskCache cache) {
        this.cache = cache;
    }

    @Override
    public File get(final String key) {
        return cache.get(key);
    }

    @Override
    public void remove(final String key) {
        cache.remove(key);
    }

    @Override
    public void save(final String key, final InputStream is, final CopyListener listener) throws IOException {
        cache.save(key, is, new IoUtils.CopyListener() {
            @Override
            public boolean onBytesCopied(final int current, final int total) {
                return listener == null || listener.onCopied(current);
            }
        });
    }

    @Override
    public Uri toUri(final String key) {
        return CacheProvider.getCacheUri(key, null);
    }

    @Override
    public String fromUri(final Uri uri) {
        return CacheProvider.getCacheKey(uri);
    }
}