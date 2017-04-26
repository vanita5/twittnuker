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

package de.vanita5.twittnuker.model.util;

import android.location.Location;
import android.support.annotation.Nullable;

import de.vanita5.microblog.library.twitter.model.GeoLocation;
import de.vanita5.twittnuker.model.ParcelableLocation;
import de.vanita5.twittnuker.util.InternalParseUtils;

public class ParcelableLocationUtils {
    private ParcelableLocationUtils() {
    }

    public static String getHumanReadableString(ParcelableLocation obj, int decimalDigits) {
        return String.format("%s,%s", InternalParseUtils.parsePrettyDecimal(obj.latitude, decimalDigits),
                InternalParseUtils.parsePrettyDecimal(obj.longitude, decimalDigits));
    }

    @Nullable
    public static ParcelableLocation fromGeoLocation(@Nullable GeoLocation geoLocation) {
        if (geoLocation == null) return null;
        final ParcelableLocation result = new ParcelableLocation();
        result.latitude = geoLocation.getLatitude();
        result.longitude = geoLocation.getLongitude();
        return result;
    }

    @Nullable
    public static ParcelableLocation fromLocation(@Nullable Location location) {
        if (location == null) return null;
        final ParcelableLocation result = new ParcelableLocation();
        result.latitude = location.getLatitude();
        result.longitude = location.getLongitude();
        return result;
    }

    public static boolean isValidLocation(final ParcelableLocation location) {
        return location != null && !Double.isNaN(location.latitude) && !Double.isNaN(location.longitude);
    }

    public static GeoLocation toGeoLocation(final ParcelableLocation location) {
        return isValidLocation(location) ? new GeoLocation(location.latitude, location.longitude) : null;
    }

    public static boolean isValidLocation(double latitude, double longitude) {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude);
    }
}