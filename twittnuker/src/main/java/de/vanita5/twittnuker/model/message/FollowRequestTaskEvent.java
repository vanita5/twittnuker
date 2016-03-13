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

import de.vanita5.twittnuker.model.UserKey;

public class FollowRequestTaskEvent {

    @Action
    private int action;
    private boolean finished;
    private boolean succeeded;
    private UserKey mAccountKey;
    private long userId;

    public FollowRequestTaskEvent(@Action int action, UserKey accountKey, long userId) {
        this.action = action;
        this.mAccountKey = accountKey;
        this.userId = userId;
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

    public UserKey getAccountKey() {
        return mAccountKey;
    }

    public long getUserId() {
        return userId;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    @Override
    public String toString() {
        return "FollowRequestTaskEvent{" +
                "action=" + action +
                ", finished=" + finished +
                ", mAccountKey=" + mAccountKey +
                ", userId=" + userId +
                '}';
    }

    @IntDef({Action.ACCEPT, Action.DENY})
    public @interface Action {
        int ACCEPT = 1;
        int DENY = 2;
    }
}