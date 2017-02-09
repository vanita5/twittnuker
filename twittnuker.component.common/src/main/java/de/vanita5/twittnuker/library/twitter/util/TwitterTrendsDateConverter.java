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

package de.vanita5.twittnuker.library.twitter.util;

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TwitterTrendsDateConverter implements TypeConverter<Date> {
    private static final Object FORMATTER_LOCK = new Object();

    private static final SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    private static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static final SimpleDateFormat DATE_FORMAT_3 = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);

    @Override
    public Date parse(JsonParser jsonParser) throws IOException {
        String dateString = jsonParser.getValueAsString(null);
        if (dateString == null) throw new IOException();
        try {
            synchronized (FORMATTER_LOCK) {
                switch (dateString.length()) {
                    case 10:
                        return new Date(Long.parseLong(dateString) * 1000);
                    case 20:
                        return DATE_FORMAT_1.parse(dateString);
                    default:
                        return parse(dateString, new DateFormat[]{DATE_FORMAT_2, DATE_FORMAT_3});
                }
            }
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void serialize(Date object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) {
        throw new UnsupportedOperationException();
    }

    private static Date parse(String dateString, DateFormat[] formats) throws ParseException {
        for (final DateFormat format : formats) {
            try {
                return format.parse(dateString);
            } catch (ParseException e) {
                // Ignore
            }
        }
        throw new ParseException("Unrecognized date " + dateString, 0);
    }
}