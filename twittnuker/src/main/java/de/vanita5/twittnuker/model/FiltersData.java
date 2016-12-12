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

package de.vanita5.twittnuker.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;

import de.vanita5.twittnuker.model.util.UserKeyConverter;
import de.vanita5.twittnuker.model.util.UserKeyCursorFieldConverter;
import de.vanita5.twittnuker.provider.TwidereDataStore.Filters;

import java.util.List;

@JsonObject
public class FiltersData {

    @JsonField(name = "users")
    List<UserItem> users;
    @JsonField(name = "keywords")
    List<BaseItem> keywords;
    @JsonField(name = "sources")
    List<BaseItem> sources;
    @JsonField(name = "links")
    List<BaseItem> links;

    public List<UserItem> getUsers() {
        return users;
    }

    public List<BaseItem> getKeywords() {
        return keywords;
    }

    public List<BaseItem> getSources() {
        return sources;
    }

    public List<BaseItem> getLinks() {
        return links;
    }

    public void setUsers(List<UserItem> users) {
        this.users = users;
    }

    public void setKeywords(List<BaseItem> keywords) {
        this.keywords = keywords;
    }

    public void setSources(List<BaseItem> sources) {
        this.sources = sources;
    }

    public void setLinks(List<BaseItem> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "FiltersData{" +
                "users=" + users +
                ", keywords=" + keywords +
                ", sources=" + sources +
                ", links=" + links +
                '}';
    }

    @JsonObject
    @CursorObject(valuesCreator = true)
    public static class UserItem {
        @CursorField(value = Filters.Users.USER_KEY, converter = UserKeyCursorFieldConverter.class)
        @JsonField(name = "user_key", typeConverter = UserKeyConverter.class)
        UserKey userKey;
        @CursorField(Filters.Users.NAME)
        @JsonField(name = "name")
        String name;
        @CursorField(Filters.Users.SCREEN_NAME)
        @JsonField(name = "screen_name")
        String screenName;

        public UserKey getUserKey() {
            return userKey;
        }

        public String getName() {
            return name;
        }

        public String getScreenName() {
            return screenName;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setScreenName(String screenName) {
            this.screenName = screenName;
        }

        public void setUserKey(UserKey userKey) {
            this.userKey = userKey;
        }

        @Override
        public String toString() {
            return "UserItem{" +
                    "userKey='" + userKey + '\'' +
                    ", name='" + name + '\'' +
                    ", screenName='" + screenName + '\'' +
                    '}';
        }
    }

    @JsonObject
    @CursorObject(valuesCreator = true)
    public static class BaseItem {
        @CursorField(Filters.VALUE)
        @JsonField(name = "value")
        String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "BaseItem{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }
}