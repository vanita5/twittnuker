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
import kotlinx.android.synthetic.main.layout_extra_config_user_list.view.*
import kotlinx.android.synthetic.main.list_item_simple_user_list.view.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.activity.UserListSelectorActivity
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.fragment.CustomTabsFragment.TabEditorDialogFragment
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.tab.TabConfiguration
import de.vanita5.twittnuker.util.dagger.DependencyHolder
import de.vanita5.twittnuker.extension.view.holder.display
import de.vanita5.twittnuker.view.holder.SimpleUserListViewHolder

class UserListExtraConfiguration(key: String) : TabConfiguration.ExtraConfiguration(key) {
    var value: ParcelableUserList? = null
        private set

    private lateinit var viewHolder: SimpleUserListViewHolder
    private lateinit var dependencyHolder: DependencyHolder
    private lateinit var hintView: View

    override fun onCreate(context: Context) {
        super.onCreate(context)
        this.dependencyHolder = DependencyHolder.get(context)
    }

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_user_list, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: TabEditorDialogFragment) {
        super.onViewCreated(context, view, fragment)
        view.setOnClickListener {
            val account = fragment.account ?: return@setOnClickListener
            val intent = Intent(INTENT_ACTION_SELECT_USER_LIST)
            intent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
            intent.putExtra(EXTRA_SHOW_MY_LISTS, true)
            intent.setClass(context, UserListSelectorActivity::class.java)
            fragment.startExtraConfigurationActivityForResult(this@UserListExtraConfiguration, intent, 1)
        }
        hintView = view.selectUserListHint
        viewHolder = SimpleUserListViewHolder(view.listItem)

        viewHolder.itemView.visibility = View.GONE
        hintView.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val userList: ParcelableUserList = data!!.getParcelableExtra(EXTRA_USER_LIST)
                    viewHolder.display(userList, dependencyHolder.mediaLoader,
                            dependencyHolder.userColorNameManager, true)
                    viewHolder.itemView.visibility = View.VISIBLE
                    hintView.visibility = View.GONE

                    this.value = userList
                }
            }
        }
    }
}
