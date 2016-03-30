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

package de.vanita5.twittnuker.model.message;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.ParcelableUserList;

public class UserListMembersChangedEvent {

    @Action
    private final int action;
    @NonNull
    private final ParcelableUserList userList;
    @NonNull
    private final ParcelableUser[] users;

    public UserListMembersChangedEvent(@Action int action, @NonNull ParcelableUserList userList,
                                       @NonNull ParcelableUser[] users) {
        this.action = action;
        this.userList = userList;
        this.users = users;
    }

    @Action
    public int getAction() {
        return action;
    }

    @NonNull
    public ParcelableUserList getUserList() {
        return userList;
    }

    @NonNull
    public ParcelableUser[] getUsers() {
        return users;
    }

    @IntDef({Action.ADDED, Action.REMOVED})
    public @interface Action {
        int ADDED = 1;
        int REMOVED = 2;
    }

}