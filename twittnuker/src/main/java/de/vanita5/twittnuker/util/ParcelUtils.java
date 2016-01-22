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

package de.vanita5.twittnuker.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;

public class ParcelUtils {

    @Nullable
    public static <T extends Parcelable> T clone(@Nullable T object) {
        if (object == null) return null;
        final Parcel parcel = Parcel.obtain();
        try {
            object.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            final Field creatorField = object.getClass().getDeclaredField("CREATOR");
            //noinspection unchecked
            final Parcelable.Creator<T> creator = (Parcelable.Creator<T>) creatorField.get(null);
            return creator.createFromParcel(parcel);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError("Missing CREATOR field");
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError("Can't access CREATOR field");
        } finally {
            parcel.recycle();
        }
    }
}