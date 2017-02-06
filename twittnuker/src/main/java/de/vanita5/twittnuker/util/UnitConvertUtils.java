/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import java.util.Locale;

public class UnitConvertUtils {

    public static final String[] fileSizeUnits = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB",
            "YB"};

    public static final String[] countUnits = {null, "K", "M", "B"};

    private UnitConvertUtils() {
    }

    public static String calculateProperSize(double bytes) {
        double value = bytes;
        int index;
        for (index = 0; index < fileSizeUnits.length; index++) {
            if (value < 1024) {
                break;
            }
            value = value / 1024;
        }
        return String.format(Locale.getDefault(), "%.2f %s", value, fileSizeUnits[index]);
    }

    public static String calculateProperCount(long count) {
        if (count < 1000) {
            return String.valueOf(count);
        }
        double value = count;
        int index;
        for (index = 0; index < countUnits.length; index++) {
            if (value < 1000) {
                break;
            }
            value = value / 1000.0;
        }
        if (value < 10 && (value % 1.0) >= 0.049 && (value % 1.0) < 0.5) {
            return String.format(Locale.getDefault(), "%.1f %s", value, countUnits[index]);
        } else {
            return String.format(Locale.getDefault(), "%.0f %s", value, countUnits[index]);
        }
    }
}