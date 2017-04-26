/*
 *          Twittnuker - Twitter client for Android
 *
 *  Copyright 2013-2017 vanita5 <mail@vanit.as>
 *
 *          This program incorporates a modified version of
 *          Twidere - Twitter client for Android
 *
 *  Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.vanita5.twittnuker.extension.model.api.mastodon

import de.vanita5.microblog.library.mastodon.model.Results
import de.vanita5.twittnuker.model.pagination.PaginatedArrayList
import de.vanita5.twittnuker.model.pagination.PaginatedList


inline fun <T, R> Results.mapToPaginated(listSelector: (Results) -> List<T>?, transform: (T) -> R): PaginatedList<R> {
    val list = listSelector(this) ?: return PaginatedArrayList()
    val result = list.mapTo(PaginatedArrayList(list.size), transform)
    result.previousPage = getLinkPagination("prev")
    result.nextPage = getLinkPagination("next")
    return result
}