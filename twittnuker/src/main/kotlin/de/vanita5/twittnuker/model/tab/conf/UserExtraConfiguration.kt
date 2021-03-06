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
import kotlinx.android.synthetic.main.layout_extra_config_user.view.*
import kotlinx.android.synthetic.main.list_item_simple_user.view.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.activity.UserSelectorActivity
import de.vanita5.twittnuker.adapter.DummyItemAdapter
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.fragment.CustomTabsFragment.TabEditorDialogFragment
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.tab.TabConfiguration
import de.vanita5.twittnuker.util.dagger.DependencyHolder
import de.vanita5.twittnuker.view.holder.SimpleUserViewHolder

class UserExtraConfiguration(key: String) : TabConfiguration.ExtraConfiguration(key,
        R.string.title_user) {
    var value: ParcelableUser? = null
        private set

    private lateinit var viewHolder: SimpleUserViewHolder<*>
    private lateinit var dependencyHolder: DependencyHolder
    private lateinit var hintView: View

    override fun onCreate(context: Context) {
        super.onCreate(context)
        this.dependencyHolder = DependencyHolder.get(context)
    }

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_user, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: TabEditorDialogFragment) {
        super.onViewCreated(context, view, fragment)
        view.setOnClickListener {
            val account = fragment.account ?: return@setOnClickListener
            val intent = Intent(INTENT_ACTION_SELECT_USER)
            intent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
            intent.setClass(context, UserSelectorActivity::class.java)
            fragment.startExtraConfigurationActivityForResult(this@UserExtraConfiguration, intent, 1)
        }
        hintView = view.selectUserHint
        val adapter = DummyItemAdapter(context, requestManager = fragment.requestManager)
        adapter.updateOptions()
        viewHolder = SimpleUserViewHolder(view.listItem, adapter)

        viewHolder.itemView.visibility = View.GONE
        hintView.visibility = View.VISIBLE
    }

    override fun onActivityResult(fragment: TabEditorDialogFragment, requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val user: ParcelableUser = data.getParcelableExtra(EXTRA_USER)
                    viewHolder.displayUser(user)
                    viewHolder.itemView.visibility = View.VISIBLE
                    hintView.visibility = View.GONE
                    this.value = user
                }
            }
        }
    }
}