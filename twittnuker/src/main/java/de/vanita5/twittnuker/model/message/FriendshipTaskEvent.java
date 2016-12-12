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
import de.vanita5.twittnuker.model.UserKey;

public class FriendshipTaskEvent {

    @Action
    private int action;
    private boolean finished;
    private boolean succeeded;
    @NonNull
    private UserKey accountKey;
    @NonNull
    private UserKey userKey;
    private ParcelableUser user;

    public FriendshipTaskEvent(@Action int action, @NonNull UserKey accountKey, @NonNull UserKey userKey) {
        this.action = action;
        this.accountKey = accountKey;
        this.userKey = userKey;
    }

    @Action
    public int getAction() {
        return action;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @NonNull
    public UserKey getAccountKey() {
        return accountKey;
    }

    @NonNull
    public UserKey getUserKey() {
        return userKey;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public ParcelableUser getUser() {
        return user;
    }

    public void setUser(ParcelableUser user) {
        this.user = user;
    }

    public final boolean isUser(@NonNull ParcelableUser user) {
        return userKey.equals(user.key);
    }

    @Override
    public String toString() {
        return "FriendshipTaskEvent{" +
                "action=" + action +
                ", finished=" + finished +
                ", mAccountKey=" + accountKey +
                ", userId=" + userKey +
                '}';
    }

    @IntDef({Action.ACCEPT, Action.DENY, Action.FOLLOW, Action.UNFOLLOW, Action.BLOCK,
            Action.UNBLOCK, Action.MUTE, Action.UNMUTE, Action.FILTER, Action.UNFILTER})
    public @interface Action {
        int ACCEPT = 1;
        int DENY = 2;
        int FOLLOW = 3;
        int UNFOLLOW = 4;
        int BLOCK = 5;
        int UNBLOCK = 6;
        int MUTE = 7;
        int UNMUTE = 8;
        int FILTER = 9;
        int UNFILTER = 10;
    }
}