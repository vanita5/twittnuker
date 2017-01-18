package de.vanita5.twittnuker.view.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_simple_user.view.*
import de.vanita5.twittnuker.adapter.iface.IContentAdapter
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.view.ProfileImageView


open class SimpleUserViewHolder(itemView: View, val adapter: IContentAdapter) : RecyclerView.ViewHolder(itemView) {

    val nameView: TextView = itemView.name
    val secondaryNameView: TextView = itemView.screenName
    val profileImageView: ProfileImageView = itemView.profileImage
    val checkBox: CheckBox = itemView.checkBox

    init {
        profileImageView.style = adapter.profileImageStyle
    }

    open fun displayUser(user: ParcelableUser) {
        nameView.text = user.name
        secondaryNameView.text = "@${user.screen_name}"
        if (adapter.profileImageEnabled) {
            adapter.mediaLoader.displayProfileImage(profileImageView, user)
            profileImageView.visibility = View.VISIBLE
        } else {
            adapter.mediaLoader.cancelDisplayTask(profileImageView)
            profileImageView.visibility = View.GONE
        }
    }
}