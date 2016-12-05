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

package de.vanita5.twittnuker.model.tab;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.BoolRes;

public abstract class BooleanHolder implements Parcelable {

    public abstract boolean createBoolean(Context context);

    public static BooleanHolder resource(@BoolRes int resourceId) {
        return new Resource(resourceId);
    }

    public static BooleanHolder constant(boolean value) {
        return new Constant(value);
    }

    private static class Constant extends BooleanHolder implements Parcelable {

        private final boolean constant;

        private Constant(boolean constant) {
            this.constant = constant;
        }

        @Override
        public boolean createBoolean(Context context) {
            return false;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (constant ? 1 : 0));
        }

        public static final Creator<Constant> CREATOR = new Creator<Constant>() {
            public Constant createFromParcel(Parcel source) {
                return new Constant(source.readByte() == 1);
            }

            public Constant[] newArray(int size) {
                return new Constant[size];
            }
        };
    }

    private static class Resource extends BooleanHolder implements Parcelable {

        @BoolRes
        private final int resourceId;

        Resource(@BoolRes int resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public boolean createBoolean(Context context) {
            return context.getResources().getBoolean(resourceId);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(resourceId);
        }

        public static final Creator<Resource> CREATOR = new Creator<Resource>() {
            public Resource createFromParcel(Parcel source) {
                return new Resource(source.readInt());
            }

            public Resource[] newArray(int size) {
                return new Resource[size];
            }
        };
    }
}