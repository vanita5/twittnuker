/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

open class BaseRefreshTaskParam(
        override val accountKeys: Array<UserKey>,
        override val maxIds: Array<String?>?,
        override val sinceIds: Array<String?>?,
        override val cursors: Array<String?>? = null,
        override val maxSortIds: LongArray? = null,
        override val sinceSortIds: LongArray? = null
) : RefreshTaskParam {
    override var extraId: Long = -1L
    override var isLoadingMore: Boolean = false
    override var shouldAbort: Boolean = false

}