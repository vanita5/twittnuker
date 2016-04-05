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

package de.vanita5.twittnuker.model.util;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import org.apache.commons.lang3.math.NumberUtils;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.api.twitter.model.CardEntity;
import de.vanita5.twittnuker.api.twitter.util.ThreadLocalSimpleDateFormat;
import de.vanita5.twittnuker.model.ParcelableCardEntity;
import de.vanita5.twittnuker.model.UserKey;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ParcelableCardEntityUtils implements TwittnukerConstants {

    static final DateFormat sISOFormat = new ThreadLocalSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.ENGLISH);

    static {
        sISOFormat.setLenient(true);
        sISOFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Nullable
    public static ParcelableCardEntity fromCardEntity(@Nullable CardEntity card, @Nullable UserKey accountKey) {
        if (card == null) return null;
        final ParcelableCardEntity obj = new ParcelableCardEntity();
        obj.name = card.getName();
        obj.url = card.getUrl();
        obj.users = ParcelableUserUtils.fromUsers(card.getUsers(), accountKey);
        obj.account_key = accountKey;
        obj.values = from(card.getBindingValues());
        return obj;
    }

    public static Map<String, ParcelableCardEntity.ParcelableBindingValue> from(@Nullable Map<String, CardEntity.BindingValue> bindingValues) {
        if (bindingValues == null) return null;
        final ArrayMap<String, ParcelableCardEntity.ParcelableBindingValue> map = new ArrayMap<>();
        for (Map.Entry<String, CardEntity.BindingValue> entry : bindingValues.entrySet()) {
            map.put(entry.getKey(), new ParcelableCardEntity.ParcelableBindingValue(entry.getValue()));
        }
        return map;
    }

    public static boolean getAsBoolean(@NonNull ParcelableCardEntity obj, @NonNull String key, boolean def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        return Boolean.parseBoolean(value.value);
    }

    public static String getAsString(@NonNull ParcelableCardEntity obj, @NonNull String key, String def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        return value.value;
    }

    public static String getString(@NonNull ParcelableCardEntity obj, @NonNull String key) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null || !CardEntity.BindingValue.TYPE_STRING.equals(value.type)) return null;
        return getAsString(obj, key, null);
    }

    public static int getAsInteger(@NonNull ParcelableCardEntity obj, @NonNull String key, int def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        return NumberUtils.toInt(value.value, def);
    }

    public static long getAsLong(@NonNull ParcelableCardEntity obj, @NonNull String key, long def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        return NumberUtils.toLong(value.value, def);
    }

    public static Date getAsDate(@NonNull ParcelableCardEntity obj, @NonNull String key, Date def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        try {
            return sISOFormat.parse(value.value);
        } catch (ParseException e) {
            return def;
        }
    }

}