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

package de.vanita5.twittnuker.api.twitter.util;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TwitterDateConverterTest {

    private final TwitterDateConverter converter = new TwitterDateConverter();
    private final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);

    @Test
    public void testGetFromString() throws Exception {
        testDate("Fri Jan 29 04:12:49 +0100 2016");
        testDate("Thu Jan 28 11:08:47 +0000 2016");
        testDate("Sat Oct 03 16:05:32 +0000 2015");
        testDate("Tue Jan 26 18:30:19 +0100 2016");
        assertNull(converter.getFromString("Tue Jan 26 18:30:19 +0100"));
        assertNull(converter.getFromString("Tue"));
        assertNull(converter.getFromString("++++"));
    }

    private void testDate(String s) throws ParseException {
        assertEquals(converter.getFromString(s), format.parse(s));
    }

    @Test
    public void testConvertToString() throws Exception {

    }
}