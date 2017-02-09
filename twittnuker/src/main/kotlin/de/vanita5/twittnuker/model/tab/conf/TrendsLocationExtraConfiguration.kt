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

package de.vanita5.twittnuker.model.tab.conf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.vanita5.twittnuker.library.twitter.model.Location
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.activity.TrendsLocationSelectorActivity
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_LOCATION
import de.vanita5.twittnuker.fragment.CustomTabsFragment
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.tab.TabConfiguration

open class TrendsLocationExtraConfiguration(
        key: String
) : TabConfiguration.ExtraConfiguration(key) {

    open var value: Place? = null
        set(value) {
            field = value
            if (value != null) {
                summaryView.visibility = View.VISIBLE
                summaryView.text = value.name
            } else {
                summaryView.visibility = View.GONE
            }
        }

    private lateinit var summaryView: TextView

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_checkbox, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: CustomTabsFragment.TabEditorDialogFragment) {
        super.onViewCreated(context, view, fragment)
        val titleView = view.findViewById(android.R.id.title) as TextView
        summaryView = view.findViewById(android.R.id.summary) as TextView
        titleView.text = title.createString(context)
        summaryView.visibility = View.GONE
        view.setOnClickListener {
            val account = fragment.account ?: return@setOnClickListener
            val intent = Intent(context, TrendsLocationSelectorActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
            fragment.startExtraConfigurationActivityForResult(this@TrendsLocationExtraConfiguration, intent, 1)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val location = data.getParcelableExtra<Location>(EXTRA_LOCATION)
                    value = Place(location.woeid, location.name)
                }
            }
        }
    }

    override fun onAccountSelectionChanged(account: AccountDetails?) {
        super.onAccountSelectionChanged(account)
        val titleView = view.findViewById(android.R.id.title) as TextView
        val summaryView = view.findViewById(android.R.id.summary) as TextView
        val canSelectLocation = account?.type == AccountType.TWITTER
        view.isEnabled = canSelectLocation
        titleView.isEnabled = canSelectLocation
        summaryView.isEnabled = canSelectLocation
        if (!canSelectLocation) {
            value = null
        }
    }

    data class Place(var woeId: Int, var name: String)
}