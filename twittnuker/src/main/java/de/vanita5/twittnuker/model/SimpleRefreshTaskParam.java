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

public abstract class SimpleRefreshTaskParam implements RefreshTaskParam {

    UserKey[] cached;

    @NonNull
    @Override
    public final UserKey[] getAccountKeys() {
        if (cached != null) return cached;
        return cached = getAccountKeysWorker();
    }

    @NonNull
    public abstract UserKey[] getAccountKeysWorker();

    @Nullable
    @Override
    public String[] getMaxIds() {
        return null;
    }

    @Nullable
    @Override
    public String[] getSinceIds() {
        return null;
    }

    @Override
    public boolean hasMaxIds() {
        return getMaxIds() != null;
    }

    @Override
    public boolean hasSinceIds() {
        return getSinceIds() != null;
    }

    @Nullable
    @Override
    public long[] getSinceSortIds() {
        return null;
    }

    @Nullable
    @Override
    public long[] getMaxSortIds() {
        return null;
    }

    @Override
    public boolean isLoadingMore() {
        return false;
    }

    @Override
    public boolean shouldAbort() {
        return false;
    }
}