package de.vanita5.twittnuker.view.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_simple_user.view.*
import org.mariotaku.ktextension.spannable
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.iface.IContentAdapter
import de.vanita5.twittnuker.extension.loadProfileImage
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.view.ProfileImageView


open class SimpleUserViewHolder<out A : IContentAdapter>(
        itemView: View,
        val adapter: A
) : RecyclerView.ViewHolder(itemView) {

    val nameView: TextView = itemView.name
    val secondaryNameView: TextView = itemView.screenName
    val profileImageView: ProfileImageView = itemView.profileImage
    val checkBox: CheckBox = itemView.checkBox

    init {
        profileImageView.style = adapter.profileImageStyle
    }

    open fun displayUser(user: ParcelableUser) {
        nameView.spannable = user.name
        secondaryNameView.spannable = "@${user.screen_name}"
        if (adapter.profileImageEnabled) {
            val context = itemView.context
            adapter.requestManager.loadProfileImage(context, user, adapter.profileImageStyle,
                    profileImageView.cornerRadius, profileImageView.cornerRadiusRatio,
                    adapter.profileImageSize).into(profileImageView)
            profileImageView.visibility = View.VISIBLE
        } else {
            profileImageView.visibility = View.GONE
        }
    }

    companion object {
        const val layoutResource = R.layout.list_item_simple_user
    }
}