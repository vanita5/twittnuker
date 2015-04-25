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

package de.vanita5.twittnuker.util;

import android.util.JsonReader;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import de.vanita5.twittnuker.model.iface.JsonReadable;

public class JsonSerializationUtils {

	public static <T extends JsonReadable> T[] array(JsonReader reader, Class<T> cls) {
		try {
			final ArrayList<T> list = new ArrayList<>();
			while (reader.hasNext()) {
				final T object = cls.newInstance();
				object.read(reader);
				list.add(object);
			}
			@SuppressWarnings("unchecked")
			final T[] array = (T[]) Array.newInstance(cls, list.size());
			return list.toArray(array);
		} catch (IOException | IllegalStateException e) {
			return null;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T extends JsonReadable> T object(JsonReader reader, Class<T> cls) {
		try {
			final T object = cls.newInstance();
			object.read(reader);
			return object;
		} catch (IOException | IllegalStateException e) {
			return null;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}


}