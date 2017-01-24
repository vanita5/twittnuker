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

package de.vanita5.twittnuker.preference

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.SHARED_PREFERENCES_NAME
import de.vanita5.twittnuker.adapter.DummyItemAdapter
import de.vanita5.twittnuker.graphic.like.LikeAnimationDrawable
import de.vanita5.twittnuker.view.holder.StatusViewHolder
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder

class CardPreviewPreference(
        context: Context,
        attrs: AttributeSet? = null
) : Preference(context, attrs), OnSharedPreferenceChangeListener {

    private var holder: StatusViewHolder? = null
    private val adapter: DummyItemAdapter = DummyItemAdapter(context)

    init {
        val preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE)
        layoutResource = R.layout.layout_preferences_card_preview_compact
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        adapter.updateOptions()
        holder = null
        notifyChanged()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        if (this.holder == null) {
            this.holder = StatusViewHolder(adapter, holder.itemView).apply {
                setStatusClickListener(object : IStatusViewHolder.StatusClickListener {
                    override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
                        if (id == R.id.favorite) {
                            (holder as StatusViewHolder).playLikeAnimation(LikeAnimationDrawable.OnLikedListener { false })
                        }
                    }
                })
            }
        }
        this.holder?.let {
            it.setupViewOptions()
            it.displaySampleStatus()
        }
        super.onBindViewHolder(holder)
    }

}