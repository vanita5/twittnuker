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

import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.mariotaku.restfu.RestFuUtils;
import de.vanita5.twittnuker.TwittnukerConstants;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class InternalParseUtils {
    public static String bundleToJSON(final Bundle args) {
        final Set<String> keys = args.keySet();
        final StringWriter sw = new StringWriter();
        final JsonWriter json = new JsonWriter(sw);
        try {
            json.beginObject();
            for (final String key : keys) {
                json.name(key);
                final Object value = args.get(key);
                if (value == null) {
                    json.nullValue();
                } else if (value instanceof Boolean) {
                    json.value((Boolean) value);
                } else if (value instanceof Integer) {
                    json.value((Integer) value);
                } else if (value instanceof Long) {
                    json.value((Long) value);
                } else if (value instanceof String) {
                    json.value((String) value);
                } else if (value instanceof Float) {
                    json.value((Float) value);
                } else if (value instanceof Double) {
                    json.value((Double) value);
                } else {
                    json.nullValue();
                    Log.w(TwittnukerConstants.LOGTAG, "Unknown type " + value.getClass().getSimpleName() + " in arguments key " + key);
                }
            }
            json.endObject();
            json.flush();
            sw.flush();
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            RestFuUtils.closeSilently(json);
        }
    }

    public static String parsePrettyDecimal(double num, int decimalDigits) {
        String result = String.format(Locale.US, "%." + decimalDigits + "f", num);
        int dotIdx = result.lastIndexOf('.');
        if (dotIdx == -1) return result;
        int i;
        for (i = result.length() - 1; i >= 0; i--) {
            if (result.charAt(i) != '0') break;
        }
        return result.substring(0, i == dotIdx ? dotIdx : i + 1);
    }

    public static Date parseISODateTime(String str, Date def) {
        try {
            return DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.parse(str);
        } catch (ParseException e) {
            return def;
        } catch (Error nsme) {
            // Fuck Xiaomi http://crashes.to/s/a84a3d257dc
            try {
                return DateUtils.parseDate(str, Locale.ENGLISH, "yyyy-MM-dd'T'HH:mm:ssZZ");
            } catch (ParseException e1) {
                return def;
            }
        }
    }
}