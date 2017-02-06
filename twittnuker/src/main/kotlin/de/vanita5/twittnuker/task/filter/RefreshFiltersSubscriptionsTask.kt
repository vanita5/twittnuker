/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.task.filter

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.ktextension.useCursor
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.extension.model.instantiateComponent
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.model.FiltersSubscriptionCursorIndices
import de.vanita5.twittnuker.model.`FiltersData$BaseItemValuesCreator`
import de.vanita5.twittnuker.model.`FiltersData$UserItemValuesCreator`
import de.vanita5.twittnuker.provider.TwidereDataStore.Filters
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import java.io.IOException

class RefreshFiltersSubscriptionsTask(val context: Context) : AbstractTask<Unit?, Boolean, (Boolean) -> Unit>() {

    override fun doLongOperation(param: Unit?): Boolean {
        val resolver = context.contentResolver
        return resolver.query(Filters.Subscriptions.CONTENT_URI, Filters.Subscriptions.COLUMNS, null, null, null)?.useCursor { cursor ->
            val indices = FiltersSubscriptionCursorIndices(cursor)
            cursor.moveToPosition(-1)
            while (cursor.moveToNext()) {
                val subscription = indices.newObject(cursor)
                val component = subscription.instantiateComponent(context) ?: continue
                try {
                    if (component.fetchFilters()) {
                        updateUserItems(resolver, component.users, subscription.id)
                        updateBaseItems(resolver, component.keywords, Filters.Keywords.CONTENT_URI, subscription.id)
                        updateBaseItems(resolver, component.links, Filters.Links.CONTENT_URI, subscription.id)
                        updateBaseItems(resolver, component.sources, Filters.Sources.CONTENT_URI, subscription.id)
                    }
                } catch (e: IOException) {
                    // Ignore
                }
            }
            return@useCursor true
        } ?: false
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Boolean) {
        callback?.invoke(result)
    }

    private fun updateUserItems(resolver: ContentResolver, items: List<FiltersData.UserItem>?, sourceId: Long) {
        resolver.delete(Filters.Users.CONTENT_URI, Expression.equalsArgs(Filters.Users.SOURCE).sql,
                arrayOf(sourceId.toString()))
        items?.map { item ->
            item.source = sourceId
            return@map `FiltersData$UserItemValuesCreator`.create(item)
        }?.let { items ->
            ContentResolverUtils.bulkInsert(resolver, Filters.Users.CONTENT_URI, items)
        }
    }

    private fun updateBaseItems(resolver: ContentResolver, items: List<FiltersData.BaseItem>?, uri: Uri, sourceId: Long) {
        resolver.delete(uri, Expression.equalsArgs(Filters.SOURCE).sql,
                arrayOf(sourceId.toString()))
        items?.map { item ->
            item.source = sourceId
            return@map `FiltersData$BaseItemValuesCreator`.create(item)
        }?.let { items ->
            ContentResolverUtils.bulkInsert(resolver, uri, items)
        }
    }

}