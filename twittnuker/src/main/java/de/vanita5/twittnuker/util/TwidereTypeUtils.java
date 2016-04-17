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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TwidereTypeUtils {

    private TwidereTypeUtils() {
    }

    public static String toSimpleName(Type type) {
        final StringBuilder sb = new StringBuilder();
        buildSimpleName(type, sb);
        return sb.toString();
    }

    private static void buildSimpleName(Type type, StringBuilder sb) {
        if (type instanceof Class) {
            sb.append(((Class) type).getSimpleName());
        } else if (type instanceof ParameterizedType) {
            buildSimpleName(((ParameterizedType) type).getRawType(), sb);
            sb.append("<");
            final Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            for (int i = 0; i < args.length; i++) {
                if (i != 0) {
                    sb.append(",");
                }
                buildSimpleName(args[i], sb);
            }
            sb.append(">");
        }
    }
}