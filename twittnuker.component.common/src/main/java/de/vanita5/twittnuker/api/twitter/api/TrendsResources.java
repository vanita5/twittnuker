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

package de.vanita5.twittnuker.api.twitter.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.GeoLocation;
import de.vanita5.twittnuker.api.twitter.model.Location;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.Trends;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
@SuppressWarnings("RedundantThrows")
public interface TrendsResources {

	@GET("/trends/available.json")
	ResponseList<Location> getAvailableTrends() throws TwitterException;

    @GET("/trends/available.json")
    ResponseList<Location> getAvailableTrends(@Query GeoLocation location) throws TwitterException;

    @GET("/trends/closest.json")
    ResponseList<Location> getClosestTrends(@Query GeoLocation location) throws TwitterException;

    @GET("/trends/place.json")
    ResponseList<Trends> getLocationTrends(@Query("id") int woeid) throws TwitterException;

    @GET("/trends/place.json")
    Trends getPlaceTrends(@Query("id") int woeid) throws TwitterException;
}