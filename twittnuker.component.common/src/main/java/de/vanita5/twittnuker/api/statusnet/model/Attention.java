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

package de.vanita5.twittnuker.api.statusnet.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class Attention {

    @JsonField(name = "fullname")
    String fullName;
    @JsonField(name = "id")
    long id;
    @JsonField(name = "ostatus_uri")
    String ostatusUri;
    @JsonField(name = "profileurl")
    String profileUrl;
    @JsonField(name = "screen_name")
    String screenName;

    public String getFullName() {
        return fullName;
    }

    public long getId() {
        return id;
    }

    public String getOstatusUri() {
        return ostatusUri;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getScreenName() {
        return screenName;
    }

    @Override
    public String toString() {
        return "Attention{" +
                "fullName='" + fullName + '\'' +
                ", id='" + id + '\'' +
                ", ostatusUri='" + ostatusUri + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                ", screenName='" + screenName + '\'' +
                '}';
    }
}