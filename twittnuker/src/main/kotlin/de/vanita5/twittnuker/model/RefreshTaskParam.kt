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

import de.vanita5.twittnuker.model.pagination.Pagination
import de.vanita5.twittnuker.model.pagination.SinceMaxPagination

interface RefreshTaskParam {
    val accountKeys: Array<UserKey>

    val pagination: Array<out Pagination?>? get() = null

    val extraId: Long get() = -1

    val isLoadingMore: Boolean get() = false

    val shouldAbort: Boolean get() = false

    val isBackground: Boolean get() = false

    val hasMaxIds: Boolean
        get() = pagination?.any { (it as? SinceMaxPagination)?.maxId != null } ?: false
}