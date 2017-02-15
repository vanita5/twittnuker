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

package de.vanita5.twittnuker.adapter

import android.content.Context
import android.support.v4.text.BidiFormatter
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.adapter.iface.IContentAdapter
import de.vanita5.twittnuker.adapter.iface.IItemCountsAdapter
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter
import de.vanita5.twittnuker.constant.*
import de.vanita5.twittnuker.model.ItemCounts
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper

import javax.inject.Inject

open class BaseArrayAdapter<T>(
        context: Context,
        layoutRes: Int,
        collection: Collection<T>? = null
) : ArrayAdapter<T>(context, layoutRes, collection), IContentAdapter, ILoadMoreSupportAdapter,
        IItemCountsAdapter {
    val linkify: TwidereLinkify

    @Inject
    override lateinit var userColorNameManager: UserColorNameManager
    @Inject
    override lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    override lateinit var bidiFormatter: BidiFormatter
    @Inject
    override lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var multiSelectManager: MultiSelectManager
    @Inject
    lateinit var preferences: SharedPreferencesWrapper

    final override val profileImageStyle: Int
    final override val textSize: Float
    final override val profileImageEnabled: Boolean
    final override val showAbsoluteTime: Boolean
    val nameFirst: Boolean

    override val itemCounts: ItemCounts = ItemCounts(1)

    @ILoadMoreSupportAdapter.IndicatorPosition
    override var loadMoreSupportedPosition: Long = 0
        set(value) {
            field = value
            loadMoreIndicatorPosition = ILoadMoreSupportAdapter.apply(loadMoreIndicatorPosition, value)
            notifyDataSetChanged()
        }

    @ILoadMoreSupportAdapter.IndicatorPosition
    override var loadMoreIndicatorPosition: Long = 0
        set(value) {
            if (field == value) return
            field = ILoadMoreSupportAdapter.apply(value, loadMoreSupportedPosition)
            notifyDataSetChanged()
        }

    init {
        @Suppress("UNCHECKED_CAST")
        GeneralComponentHelper.build(context).inject(this as BaseArrayAdapter<Any>)
        linkify = TwidereLinkify(OnLinkClickHandler(context, multiSelectManager, preferences))
        profileImageStyle = preferences[profileImageStyleKey]
        textSize = preferences[textSizeKey].toFloat()
        profileImageEnabled = preferences[displayProfileImageKey]
        showAbsoluteTime = preferences[showAbsoluteTimeKey]
        nameFirst = preferences[nameFirstKey]
    }

    override fun getItemCount(): Int = count

}