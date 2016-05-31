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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;
import android.text.style.URLSpan;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

@JsonObject
@ParcelablePlease
public class SpanItem implements Parcelable {
    public static final Creator<SpanItem> CREATOR = new Creator<SpanItem>() {
        @Override
        public SpanItem createFromParcel(Parcel in) {
            final SpanItem obj = new SpanItem();
            SpanItemParcelablePlease.readFromParcel(obj, in);
            return obj;
        }

        @Override
        public SpanItem[] newArray(int size) {
            return new SpanItem[size];
        }
    };

    @JsonField(name = "start")
    @ParcelableThisPlease
    public int start;
    @JsonField(name = "end")
    @ParcelableThisPlease
    public int end;
    @JsonField(name = "link")
    @ParcelableThisPlease
    public String link;

    @ParcelableNoThanks
    public int orig_start = -1;
    @ParcelableNoThanks
    public int orig_end = -1;

    @Override
    public String toString() {
        return "SpanItem{" +
                "start=" + start +
                ", end=" + end +
                ", link='" + link + '\'' +
                ", orig_start=" + orig_start +
                ", orig_end=" + orig_end +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SpanItemParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static SpanItem from(Spanned spanned, URLSpan span) {
        SpanItem spanItem = new SpanItem();
        spanItem.link = span.getURL();
        spanItem.start = spanned.getSpanStart(span);
        spanItem.end = spanned.getSpanEnd(span);
        return spanItem;
    }
}