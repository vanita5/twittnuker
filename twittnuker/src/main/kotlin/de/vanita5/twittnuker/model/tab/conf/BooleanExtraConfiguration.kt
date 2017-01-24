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

package de.vanita5.twittnuker.model.tab.conf

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView

import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.fragment.CustomTabsFragment
import de.vanita5.twittnuker.model.tab.BooleanHolder
import de.vanita5.twittnuker.model.tab.TabConfiguration

open class BooleanExtraConfiguration(
        key: String,
        val defaultValue: BooleanHolder
) : TabConfiguration.ExtraConfiguration(key) {

    open var value: Boolean
        get() = checkBox.isChecked
        set(value) {
            checkBox.isChecked = value
        }

    private lateinit var checkBox: CheckBox

    constructor(key: String, def: Boolean) : this(key, BooleanHolder.constant(def))

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_checkbox, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: CustomTabsFragment.TabEditorDialogFragment) {
        super.onViewCreated(context, view, fragment)
        val titleView = view.findViewById(android.R.id.title) as TextView
        val summaryView = view.findViewById(android.R.id.summary) as TextView
        titleView.text = title.createString(context)

        val summary = this.summary
        if (summary != null) {
            summaryView.visibility = View.VISIBLE
            summaryView.text = summary.createString(context)
        } else {
            summaryView.visibility = View.GONE
        }

        checkBox = view.findViewById(android.R.id.checkbox) as CheckBox
        checkBox.visibility = View.VISIBLE
        checkBox.isChecked = defaultValue.createBoolean(context)
        view.setOnClickListener { checkBox.toggle() }
    }
}