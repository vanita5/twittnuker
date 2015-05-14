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

package de.vanita5.twittnuker.api.twitter.model.impl;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import de.vanita5.twittnuker.api.twitter.model.Trends;
import de.vanita5.twittnuker.api.twitter.util.TwitterTrendsDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 15/5/10.
 */
@JsonObject
public class TrendsImpl extends TwitterResponseImpl implements Trends {

	@JsonField(name = "as_of", typeConverter = TwitterTrendsDateConverter.class)
	Date asOf;
	@JsonField(name = "trends")
	TrendImpl[] trends;
	@JsonField(name = "locations")
	LocationImpl[] locations;

	@Override
	public Date getAsOf() {
		return asOf;
	}

	@Override
	public TrendImpl[] getTrends() {
		return trends;
	}

	@Override
	public LocationImpl[] getLocations() {
		return locations;
	}

	@Override
	public int compareTo(@NonNull Trends another) {
		return asOf.compareTo(another.getAsOf());
	}
}