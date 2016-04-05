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

package de.vanita5.twittnuker.api.gnusocial.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class Attachment {
    @JsonField(name = "width")
    int width;
    @JsonField(name = "height")
    int height;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "thumb_url")
    String thumbUrl;
    @JsonField(name = "large_thumb_url")
    String largeThumbUrl;
    @JsonField(name = "mimetype")
    String mimetype;
    @JsonField(name = "id")
    long id;
    @JsonField(name = "oembed")
    boolean oembed;
    @JsonField(name = "size")
    long size;
    @JsonField(name = "version")
    String version;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getLargeThumbUrl() {
        return largeThumbUrl;
    }

    public String getMimetype() {
        return mimetype;
    }

    public long getId() {
        return id;
    }

    public boolean isOembed() {
        return oembed;
    }

    public long getSize() {
        return size;
    }

    public String getVersion() {
        return version;
    }
}