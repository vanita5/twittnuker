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


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.bluelinelabs.logansquare.annotation.OnPreJsonSerialize;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import de.vanita5.twittnuker.annotation.CustomTabType;
import de.vanita5.twittnuker.model.tab.argument.TabArguments;
import de.vanita5.twittnuker.model.tab.argument.TextQueryArguments;
import de.vanita5.twittnuker.model.tab.argument.UserArguments;
import de.vanita5.twittnuker.model.tab.argument.UserListArguments;
import de.vanita5.twittnuker.model.tab.extra.HomeTabExtras;
import de.vanita5.twittnuker.model.tab.extra.InteractionsTabExtras;
import de.vanita5.twittnuker.model.tab.extra.TabExtras;
import de.vanita5.twittnuker.model.tab.extra.TrendsTabExtras;
import de.vanita5.twittnuker.model.util.TabArgumentsFieldConverter;
import de.vanita5.twittnuker.model.util.TabExtrasFieldConverter;
import de.vanita5.twittnuker.provider.TwidereDataStore.Tabs;

@ParcelablePlease(allFields = false)
@CursorObject(valuesCreator = true, tableInfo = true)
@JsonObject
public class Tab implements Parcelable {
    @CursorField(value = Tabs._ID, excludeWrite = true)
    @JsonField(name = "id")
    @ParcelableThisPlease
    long id;

    @CursorField(Tabs.NAME)
    @JsonField(name = "name")
    @ParcelableThisPlease
    String name;

    @CursorField(Tabs.ICON)
    @JsonField(name = "icon")
    @ParcelableThisPlease
    String icon;

    @CursorField(Tabs.TYPE)
    @JsonField(name = "type")
    @CustomTabType
    @ParcelableThisPlease
    String type;

    @CursorField(Tabs.POSITION)
    @JsonField(name = "position")
    @ParcelableThisPlease
    int position;

    @Nullable
    @CursorField(value = Tabs.ARGUMENTS, converter = TabArgumentsFieldConverter.class)
    TabArguments arguments;

    @Nullable
    @CursorField(value = Tabs.EXTRAS, converter = TabExtrasFieldConverter.class)
    TabExtras extras;

    @Nullable
    @JsonField(name = "arguments")
    @ParcelableThisPlease
    InternalArguments internalArguments;

    @Nullable
    @JsonField(name = "extras")
    @ParcelableNoThanks
    InternalExtras internalExtras;

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

    @Nullable
    public TabArguments getArguments() {
        if (arguments == null && internalArguments != null) {
            arguments = internalArguments.getArguments();
        }
        return arguments;
    }

    public void setArguments(@Nullable TabArguments arguments) {
        this.arguments = arguments;
        this.internalArguments = InternalArguments.from(arguments);
    }

    @Nullable
    public TabExtras getExtras() {
        if (extras == null && internalExtras != null) {
            extras = internalExtras.getExtras();
        }
        return extras;
    }

    public void setExtras(@Nullable TabExtras extras) {
        this.extras = extras;
        this.internalExtras = InternalExtras.from(extras);
    }

    @OnPreJsonSerialize
    void beforeJsonSerialize() {
        internalArguments = InternalArguments.from(arguments);
        internalExtras = InternalExtras.from(extras);
    }


    @OnJsonParseComplete
    void onJsonParseComplete() {
        if (internalArguments != null) {
            arguments = internalArguments.getArguments();
        }
        if (internalExtras != null) {
            extras = internalExtras.getExtras();
        }
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

    @CustomTabType
    public static String getTypeAlias(String key) {
        if (key == null) return null;
        switch (key) {
            case "mentions":
            case "mentions_timeline":
            case "activities_about_me":
                return CustomTabType.NOTIFICATIONS_TIMELINE;
            case "home":
                return CustomTabType.HOME_TIMELINE;
        }
        return key;
    }

    @ParcelablePlease(allFields = false)
    @JsonObject
    static class InternalArguments implements Parcelable {
        @JsonField(name = "base")
        TabArguments base;
        @JsonField(name = "text_query")
        @ParcelableThisPlease
        TextQueryArguments textQuery;
        @JsonField(name = "user")
        @ParcelableThisPlease
        UserArguments user;
        @JsonField(name = "user_list")
        @ParcelableThisPlease
        UserListArguments userList;

        public static InternalArguments from(TabArguments arguments) {
            if (arguments == null) return null;
            InternalArguments result = new InternalArguments();
            if (arguments instanceof TextQueryArguments) {
                result.textQuery = (TextQueryArguments) arguments;
            } else if (arguments instanceof UserArguments) {
                result.user = (UserArguments) arguments;
            } else if (arguments instanceof UserListArguments) {
                result.userList = (UserListArguments) arguments;
            } else {
                result.base = arguments;
            }
            return result;
        }

        public TabArguments getArguments() {
            if (userList != null) {
                return userList;
            } else if (user != null) {
                return user;
            } else if (textQuery != null) {
                return textQuery;
            } else {
                return base;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Tab$InternalArgumentsParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<InternalArguments> CREATOR = new Creator<InternalArguments>() {
            public InternalArguments createFromParcel(Parcel source) {
                InternalArguments target = new InternalArguments();
                Tab$InternalArgumentsParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public InternalArguments[] newArray(int size) {
                return new InternalArguments[size];
            }
        };
    }

    @JsonObject
    static class InternalExtras  {

        @JsonField(name = "base")
        TabExtras base;
        @JsonField(name = "interactions")
        InteractionsTabExtras interactions;
        @JsonField(name = "home")
        HomeTabExtras home;
        @JsonField(name = "trends")
        TrendsTabExtras trends;

        public static InternalExtras from(TabExtras extras) {
            if (extras == null) return null;
            InternalExtras result = new InternalExtras();
            if (extras instanceof InteractionsTabExtras) {
                result.interactions = (InteractionsTabExtras) extras;
            } else if (extras instanceof HomeTabExtras) {
                result.home = (HomeTabExtras) extras;
            } else if (extras instanceof TrendsTabExtras) {
                result.trends = (TrendsTabExtras) extras;
            } else {
                result.base = extras;
            }
            return result;
        }

        public TabExtras getExtras() {
            if (interactions != null) {
                return interactions;
            } else if (home != null) {
                return home;
            } else if (trends != null) {
                return trends;
            } else {
                return base;
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TabParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Tab> CREATOR = new Creator<Tab>() {
        public Tab createFromParcel(Parcel source) {
            Tab target = new Tab();
            TabParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public Tab[] newArray(int size) {
            return new Tab[size];
        }
    };
}