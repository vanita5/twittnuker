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

import de.vanita5.twittnuker.api.twitter.util.TwitterDateConverter;

import java.util.Date;

@JsonObject
public class Group {

    @JsonField(name = "modified", typeConverter = TwitterDateConverter.class)
    Date modified;
    @JsonField(name = "nickname")
    String nickname;
    @JsonField(name = "admin_count")
    int adminCount;
    @JsonField(name = "created", typeConverter = TwitterDateConverter.class)
    Date created;
    @JsonField(name = "id")
    long id;
    @JsonField(name = "homepage")
    String homepage;
    @JsonField(name = "fullname")
    String fullname;
    @JsonField(name = "homepage_logo")
    String homepageLogo;
    @JsonField(name = "mini_logo")
    String miniLogo;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "member_count")
    int memberCount;
    @JsonField(name = "blocked")
    boolean blocked;
    @JsonField(name = "stream_logo")
    String streamLogo;
    @JsonField(name = "member")
    boolean member;
    @JsonField(name = "description")
    String description;
    @JsonField(name = "original_logo")
    String originalLogo;
    @JsonField(name = "location")
    String location;

    public Date getModified() {
        return modified;
    }

    public String getNickname() {
        return nickname;
    }

    public int getAdminCount() {
        return adminCount;
    }

    public Date getCreated() {
        return created;
    }

    public long getId() {
        return id;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getFullname() {
        return fullname;
    }

    public String getHomepageLogo() {
        return homepageLogo;
    }

    public String getMiniLogo() {
        return miniLogo;
    }

    public String getUrl() {
        return url;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getStreamLogo() {
        return streamLogo;
    }

    public boolean isMember() {
        return member;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginalLogo() {
        return originalLogo;
    }

    public String getLocation() {
        return location;
    }
}