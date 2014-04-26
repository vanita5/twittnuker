/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.jsonserializer.JSONSerializer;

public class ParcelableMediaUpdate implements Parcelable, JSONParcelable {

	public static final Parcelable.Creator<ParcelableMediaUpdate> CREATOR = new Parcelable.Creator<ParcelableMediaUpdate>() {
		@Override
		public ParcelableMediaUpdate createFromParcel(final Parcel in) {
			return new ParcelableMediaUpdate(in);
		}

		@Override
		public ParcelableMediaUpdate[] newArray(final int size) {
			return new ParcelableMediaUpdate[size];
		}
	};

	public static final JSONParcelable.Creator<ParcelableMediaUpdate> JSON_CREATOR = new JSONParcelable.Creator<ParcelableMediaUpdate>() {
		@Override
		public ParcelableMediaUpdate createFromParcel(final JSONParcel in) {
			return new ParcelableMediaUpdate(in);
		}

		@Override
		public ParcelableMediaUpdate[] newArray(final int size) {
			return new ParcelableMediaUpdate[size];
		}
	};

	public final String uri;
	public final int type;

	public ParcelableMediaUpdate(final JSONParcel in) {
		uri = in.readString("uri");
		type = in.readInt("type");
	}

	public ParcelableMediaUpdate(final Parcel in) {
		uri = in.readString();
		type = in.readInt();
	}

	public ParcelableMediaUpdate(final String uri, final int type) {
		this.uri = uri;
		this.type = type;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "ParcelableMediaUpdate{uri=" + uri + ", type=" + type + "}";
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		out.writeString("uri", uri);
		out.writeInt("type", type);
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(uri);
		dest.writeInt(type);
	}

	public static ParcelableMediaUpdate[] fromJSONString(final String json) {
		if (TextUtils.isEmpty(json)) return null;
		try {
			return JSONSerializer.createArray(JSON_CREATOR, new JSONArray(json));
		} catch (final JSONException e) {
			return null;
		}
	}

}