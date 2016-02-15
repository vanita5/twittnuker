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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BaseRefreshTaskParam implements RefreshTaskParam {

    private final long[] accountIds, maxIds, sinceIds;

    @NonNull
    @Override
    public long[] getAccountIds() {
        return accountIds;
    }

    @Nullable
    @Override
    public long[] getMaxIds() {
        return maxIds;
    }

    @Nullable
    @Override
    public long[] getSinceIds() {
        return sinceIds;
    }

    public BaseRefreshTaskParam(long[] accountIds, long[] maxIds, long[] sinceIds) {
        this.accountIds = accountIds;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
    }
}