/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.model;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.provider.TwidereDataStore.Filters;


@CursorObject(valuesCreator = true, tableInfo = true)
public class FiltersSubscription {
    @CursorField(value = Filters.Subscriptions._ID, excludeWrite = true, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    public long id;

    @CursorField(Filters.Subscriptions.NAME)
    public String name;

    @CursorField(Filters.Subscriptions.COMPONENT)
    public String component;

    @CursorField(Filters.Subscriptions.ARGUMENTS)
    public String arguments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FiltersSubscription that = (FiltersSubscription) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "FiltersSubscription{" +
                "arguments='" + arguments + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", component='" + component + '\'' +
                '}';
    }
}