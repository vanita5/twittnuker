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

package de.vanita5.twittnuker.api.twitter.model;

import org.mariotaku.library.logansquare.extension.annotation.Implementation;

import de.vanita5.twittnuker.api.twitter.model.impl.MediaUploadResponseImpl;

@Implementation(MediaUploadResponseImpl.class)
public interface MediaUploadResponse extends TwitterResponse {

    long getId();

    Image getImage();

    long getSize();

    @Implementation(MediaUploadResponseImpl.ImageImpl.class)
    interface Image {

        int getHeight();

        String getImageType();

        int getWidth();
    }
}