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

package de.vanita5.twittnuker.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.iface.IBaseAdapter
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.ParcelableAccountUtils
import de.vanita5.twittnuker.util.MediaLoaderWrapper
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import de.vanita5.twittnuker.view.holder.AccountViewHolder

import javax.inject.Inject

class AccountDetailsAdapter(context: Context) : ArrayAdapter<AccountDetails>(context, R.layout.list_item_account), IBaseAdapter {

    @Inject
    lateinit override var mediaLoader: MediaLoaderWrapper

    override var isProfileImageDisplayed: Boolean = false
    private var sortEnabled: Boolean = false
    private var switchEnabled: Boolean = false
    private var onAccountToggleListener: OnAccountToggleListener? = null

    private val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        val tag = buttonView.tag as? String ?: return@OnCheckedChangeListener
        val accountKey = UserKey.valueOf(tag) ?: return@OnCheckedChangeListener
        onAccountToggleListener?.onAccountToggle(accountKey, isChecked)
    }

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)
        val holder = view.tag as? AccountViewHolder ?: run {
            val h = AccountViewHolder(view)
            view.tag = h
            return@run h
        }
        val details = getItem(position)
        holder.screenName.text = String.format("@%s", details.user.screen_name)
        holder.setAccountColor(details.color)
        if (isProfileImageDisplayed) {
            mediaLoader.displayProfileImage(holder.profileImage, details.user)
        } else {
            mediaLoader.cancelDisplayTask(holder.profileImage)
        }
        val accountType = details.type
        holder.accountType.setImageResource(ParcelableAccountUtils.getAccountTypeIcon(accountType))
        holder.toggle.isChecked = details.activated
        holder.toggle.setOnCheckedChangeListener(checkedChangeListener)
        holder.toggle.tag = details.user.key
        holder.toggleContainer.visibility = if (switchEnabled) View.VISIBLE else View.GONE
        holder.setSortEnabled(sortEnabled)
        return view
    }

    override val linkHighlightOption: Int
        get() = 0

    override fun setLinkHighlightOption(option: String) {

    }

    override var textSize: Float
        get() = 0f
        set(textSize) {

        }

    override var isDisplayNameFirst: Boolean
        get() = false
        set(nameFirst) {

        }

    override var isShowAccountColor: Boolean
        get() = false
        set(show) {

        }

    fun setSwitchEnabled(enabled: Boolean) {
        if (switchEnabled == enabled) return
        switchEnabled = enabled
        notifyDataSetChanged()
    }

    fun setOnAccountToggleListener(listener: OnAccountToggleListener) {
        onAccountToggleListener = listener
    }

    fun setSortEnabled(sortEnabled: Boolean) {
        if (this.sortEnabled == sortEnabled) return
        this.sortEnabled = sortEnabled
        notifyDataSetChanged()
    }

    interface OnAccountToggleListener {
        fun onAccountToggle(accountId: UserKey, state: Boolean)
    }
}