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

    private final UserKey[] accountKeys;
    private final String[] maxIds;
    private final String[] sinceIds;
    private final long[] maxSortIds;
    private final long[] sinceSortIds;
    private boolean isLoadingMore;
    private boolean shouldAbort;

    public BaseRefreshTaskParam(UserKey[] accountKeys, String[] maxIds, String[] sinceIds) {
        this(accountKeys, maxIds, sinceIds, null, null);
    }

    public BaseRefreshTaskParam(UserKey[] accountKeys, String[] maxIds, String[] sinceIds,
                                long[] maxSortIds, long[] sinceSortIds) {
        this.accountKeys = accountKeys;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
        this.maxSortIds = maxSortIds;
        this.sinceSortIds = sinceSortIds;
    }

    @NonNull
    @Override
    public UserKey[] getAccountKeys() {
        return accountKeys;
    }

    @Nullable
    @Override
    public String[] getMaxIds() {
        return maxIds;
    }

    @Nullable
    @Override
    public String[] getSinceIds() {
        return sinceIds;
    }

    @Override
    public boolean hasMaxIds() {
        return maxIds != null;
    }

    @Override
    public boolean hasSinceIds() {
        return sinceIds != null;
    }

    @Override
    public long[] getMaxSortIds() {
        return maxSortIds;
    }

    @Override
    public long[] getSinceSortIds() {
        return sinceSortIds;
    }

    @Override
    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    public void setLoadingMore(boolean isLoadingMore) {
        this.isLoadingMore = isLoadingMore;
    }

    @Override
    public boolean shouldAbort() {
        return shouldAbort;
    }

    public void setShouldAbort(boolean shouldAbort) {
        this.shouldAbort = shouldAbort;
    }
}