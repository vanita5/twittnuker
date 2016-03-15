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

package de.vanita5.twittnuker.model;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import de.vanita5.twittnuker.annotation.CustomTabType;
import de.vanita5.twittnuker.model.tab.argument.TabArguments;
import de.vanita5.twittnuker.model.tab.extra.TabExtras;
import de.vanita5.twittnuker.model.util.TabArgumentsFieldConverter;
import de.vanita5.twittnuker.model.util.TabExtrasFieldConverter;
import de.vanita5.twittnuker.provider.TwidereDataStore.Tabs;

@CursorObject(valuesCreator = true)
public class Tab {
    @CursorField(value = Tabs._ID, excludeWrite = true)
    long id;

    @CursorField(Tabs.NAME)
    String name;

    @CursorField(Tabs.ICON)
    String icon;

    @CursorField(Tabs.TYPE)
    @CustomTabType
    String type;

    @CursorField(Tabs.POSITION)
    int position;

    @CursorField(value = Tabs.ARGUMENTS, converter = TabArgumentsFieldConverter.class)
    TabArguments arguments;

    @CursorField(value = Tabs.EXTRAS, converter = TabExtrasFieldConverter.class)
    TabExtras extras;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @CustomTabType
    public String getType() {
        return type;
    }

    public void setType(@CustomTabType String type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public TabArguments getArguments() {
        return arguments;
    }

    public void setArguments(TabArguments arguments) {
        this.arguments = arguments;
    }

    public TabExtras getExtras() {
        return extras;
    }

    public void setExtras(TabExtras extras) {
        this.extras = extras;
    }

    @Override
    public String toString() {
        return "Tab{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", type='" + type + '\'' +
                ", position=" + position +
                ", arguments=" + arguments +
                ", extras=" + extras +
                '}';
    }
}