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

package de.vanita5.twittnuker.util.media.preview;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class PreviewMediaExtractorTest {

    @Test
    public void testGetAuthority() throws Exception {
        assertEquals("www.google.com", PreviewMediaExtractor.getAuthority("http://www.google.com/"));
        assertEquals("twitter.com", PreviewMediaExtractor.getAuthority("https://twitter.com"));
        assertNull(PreviewMediaExtractor.getAuthority("www.google.com/"));
    }

    @Test
    public void testGetPath() throws Exception {
        assertEquals("/", PreviewMediaExtractor.getPath("http://www.example.com/"));
        assertEquals("", PreviewMediaExtractor.getPath("http://www.example.com"));
        assertEquals("/test/path", PreviewMediaExtractor.getPath("https://example.com/test/path"));
        assertEquals("/test/path", PreviewMediaExtractor.getPath("https://example.com/test/path?with=query"));
    }
}