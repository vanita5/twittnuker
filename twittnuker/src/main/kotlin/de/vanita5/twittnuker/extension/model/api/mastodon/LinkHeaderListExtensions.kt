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

package de.vanita5.twittnuker.extension.model.api.mastodon

import android.net.Uri
import de.vanita5.microblog.library.mastodon.model.LinkHeaderList
import de.vanita5.twittnuker.model.pagination.PaginatedArrayList
import de.vanita5.twittnuker.model.pagination.PaginatedList
import de.vanita5.twittnuker.model.pagination.Pagination
import de.vanita5.twittnuker.model.pagination.SinceMaxPagination


inline fun <T, R> LinkHeaderList<T>.mapToPaginated(transform: (T) -> R): PaginatedList<R> {
    val result = mapTo(PaginatedArrayList(size), transform)
    result.previousPage = getLinkPagination("prev")
    result.nextPage = getLinkPagination("next")
    return result
}

fun LinkHeaderList<*>.getLinkPagination(key: String): Pagination? {
    val uri = getLinkPart(key)?.let(Uri::parse) ?: return null
    val maxId = uri.getQueryParameter("max_id")
    val sinceId = uri.getQueryParameter("since_id")
    if (maxId != null || sinceId != null) {
        return SinceMaxPagination().apply {
            this.maxId = maxId
            this.sinceId = sinceId
        }
    }
    return null
}