/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
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

package de.vanita5.twittnuker.api.twitter.model.impl;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import de.vanita5.twittnuker.api.twitter.model.AccountSettings;
import de.vanita5.twittnuker.api.twitter.model.Location;
import de.vanita5.twittnuker.api.twitter.model.TimeZone;

@JsonObject
public class AccountSettingsImpl extends TwitterResponseImpl implements AccountSettings {

    @JsonField(name = "geo_enabled")
    boolean geoEnabled;
    @JsonField(name = "trend_location")
    LocationImpl[] trendLocations;
    @JsonField(name = "language")
    String language;
    @JsonField(name = "always_use_https")
    boolean alwaysUseHttps;
    @JsonField(name = "time_zone")
    TimeZoneImpl timezone;

    @Override
    public boolean isAlwaysUseHttps() {
        return alwaysUseHttps;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public TimeZone getTimeZone() {
        return timezone;
    }

    @Override
    public Location[] getTrendLocations() {
        return trendLocations;
    }

    @Override
    public boolean isGeoEnabled() {
        return geoEnabled;
    }

}