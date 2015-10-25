/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
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

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class BuildProperties {

	private final Properties properties;

	private BuildProperties() throws IOException {
		properties = new Properties();
        final InputStream is = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
        try {
            properties.load(is);
        } finally {
            Utils.closeSilently(is);
        }
	}

	public boolean containsKey(final Object key) {
		return properties.containsKey(key);
	}

	public boolean containsValue(final Object value) {
		return properties.containsValue(value);
	}

	public Set<Entry<Object, Object>> entrySet() {
		return properties.entrySet();
	}

	public String getProperty(final String name) {
		return properties.getProperty(name);
	}

	public String getProperty(final String name, final String defaultValue) {
		return properties.getProperty(name, defaultValue);
	}

	public boolean isEmpty() {
		return properties.isEmpty();
	}

	public Enumeration<Object> keys() {
		return properties.keys();
	}

	public Set<Object> keySet() {
		return properties.keySet();
	}

	public int size() {
		return properties.size();
	}

	public Collection<Object> values() {
		return properties.values();
	}

	public static BuildProperties newInstance() throws IOException {
		return new BuildProperties();
	}

}