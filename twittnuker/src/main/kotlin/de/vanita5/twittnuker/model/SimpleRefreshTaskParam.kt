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

package de.vanita5.twittnuker.model

abstract class SimpleRefreshTaskParam : RefreshTaskParam {

    internal var cached: Array<UserKey>? = null

    override val accountKeys: Array<UserKey>
        get() {
            if (cached != null) return cached!!
            cached = getAccountKeysWorker()
            return cached!!
        }

    abstract fun getAccountKeysWorker(): Array<UserKey>

    override val maxIds: Array<String?>?
        get() = null

    override val sinceIds: Array<String?>?
        get() = null

    override val hasMaxIds: Boolean
        get() = maxIds != null

    override val hasSinceIds: Boolean
        get() = sinceIds != null

    override val sinceSortIds: LongArray?
        get() = null

    override val maxSortIds: LongArray?
        get() = null

    override val isLoadingMore: Boolean
        get() = false

    override val shouldAbort: Boolean
        get() = false
}