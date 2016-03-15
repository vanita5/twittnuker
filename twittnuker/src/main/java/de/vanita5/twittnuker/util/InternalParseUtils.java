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

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.restfu.RestFuUtils;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.constant.CompatibilityConstants;
import de.vanita5.twittnuker.constant.IntentConstants;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
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

    public static Bundle jsonToBundle(final String string) {
        final Bundle bundle = new Bundle();
        if (string == null) return bundle;
        try {
            final JSONObject json = new JSONObject(string);
            final Iterator<?> it = json.keys();
            while (it.hasNext()) {
                final Object key_obj = it.next();
                if (key_obj == null) {
                    continue;
                }
                final String key = key_obj.toString();
                final Object value = json.get(key);
                if (shouldUseString(key)) {
                    bundle.putString(key, json.optString(key));
                } else if (shouldPutLong(key)) {
                    bundle.putLong(key, json.optLong(key));
                } else if (value instanceof Boolean) {
                    bundle.putBoolean(key, json.optBoolean(key));
                } else if (value instanceof Integer) {
                    bundle.putInt(key, json.optInt(key));
                } else if (value instanceof Long) {
                    bundle.putLong(key, json.optLong(key));
                } else if (value instanceof String) {
                    bundle.putString(key, json.optString(key));
                } else {
                    Log.w(TwittnukerConstants.LOGTAG, "Unknown type " + value.getClass().getSimpleName() + " in arguments key " + key);
                }
            }
        } catch (final JSONException | ClassCastException e) {
            e.printStackTrace();
        }
        return bundle;
    }

    private static boolean shouldUseString(final String key) {
        switch (key) {
            case CompatibilityConstants.EXTRA_ACCOUNT_ID:
            case IntentConstants.EXTRA_USER_ID:
                return true;
        }
        return IntentConstants.EXTRA_LIST_ID.equals(key);
    }

    private static boolean shouldPutLong(final String key) {
        return IntentConstants.EXTRA_LIST_ID.equals(key);
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
}