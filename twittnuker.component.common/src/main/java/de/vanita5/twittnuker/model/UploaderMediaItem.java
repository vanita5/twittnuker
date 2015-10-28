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

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;

@JsonObject
public class UploaderMediaItem implements Parcelable {

	public static final Parcelable.Creator<UploaderMediaItem> CREATOR = new Parcelable.Creator<UploaderMediaItem>() {

		@Override
		public UploaderMediaItem createFromParcel(final Parcel source) {
			return new UploaderMediaItem(source);
		}

		@Override
		public UploaderMediaItem[] newArray(final int size) {
			return new UploaderMediaItem[size];
		}
	};

	@JsonField(name = "path")
	public String path;
	@JsonField(name = "fd")
	public ParcelFileDescriptor fd;
	@JsonField(name = "size")
	public long size;

	public UploaderMediaItem() {
	}

	public UploaderMediaItem(final Context context, final ParcelableMediaUpdate media) throws FileNotFoundException {
		path = Uri.parse(media.uri).getPath();
		final File file = new File(path);
		fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
		size = file.length();
	}

	public UploaderMediaItem(final Parcel src) {
		path = src.readString();
		fd = src.readParcelable(ParcelFileDescriptor.class.getClassLoader());
		size = src.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "MediaUpload{path=" + path + ", fd=" + fd + ", size=" + size + "}";
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(path);
		dest.writeParcelable(fd, flags);
		dest.writeLong(size);
	}

	public static UploaderMediaItem[] getFromStatusUpdate(final Context context, final ParcelableStatusUpdate status)
			throws FileNotFoundException {
		if (status.media == null) return null;
		final UploaderMediaItem[] uploaderItems = new UploaderMediaItem[status.media.length];
		for (int i = 0, j = uploaderItems.length; i < j; i++) {
			uploaderItems[i] = new UploaderMediaItem(context, status.media[i]);
		}
		return uploaderItems;
	}

}