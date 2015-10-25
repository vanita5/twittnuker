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

package de.vanita5.twittnuker.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class MediaUploadResult implements Parcelable {

	public static final Parcelable.Creator<MediaUploadResult> CREATOR = new Parcelable.Creator<MediaUploadResult>() {

		@Override
		public MediaUploadResult createFromParcel(final Parcel source) {
			return new MediaUploadResult(source);
		}

		@Override
		public MediaUploadResult[] newArray(final int size) {
			return new MediaUploadResult[size];
		}
	};

	public final String[] media_uris;
	public final int error_code;
	public final String error_message;

	public MediaUploadResult(final int errorCode, final String errorMessage) {
		if (errorCode == 0) throw new IllegalArgumentException("Error code must not be 0");
		media_uris = null;
		error_code = errorCode;
		error_message = errorMessage;
	}

	public MediaUploadResult(final Parcel src) {
		media_uris = src.createStringArray();
		error_code = src.readInt();
		error_message = src.readString();
	}

	public MediaUploadResult(final String[] mediaUris) {
		if (mediaUris == null) throw new IllegalArgumentException("Media uris must not be null");
		media_uris = mediaUris;
		error_code = 0;
		error_message = null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "MediaUploadResult{media_uris=" + Arrays.toString(media_uris) + ", error_code=" + error_code
				+ ", error_message=" + error_message + "}";
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeStringArray(media_uris);
		dest.writeInt(error_code);
		dest.writeString(error_message);
	}

	public static MediaUploadResult getInstance(final int errorCode, final String errorMessage) {
		return new MediaUploadResult(errorCode, errorMessage);
	}

	public static MediaUploadResult getInstance(final String... mediaUris) {
		return new MediaUploadResult(mediaUris);
	}

}