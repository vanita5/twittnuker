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

package de.vanita5.twittnuker.view.holder

import android.graphics.PorterDuff
import android.support.v4.view.MarginLayoutParamsCompat
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.ParcelableActivitiesAdapter
import de.vanita5.twittnuker.adapter.iface.IActivitiesAdapter
import de.vanita5.twittnuker.model.ActivityTitleSummaryMessage
import de.vanita5.twittnuker.model.ParcelableActivity
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.util.ParcelableActivityUtils
import de.vanita5.twittnuker.view.BadgeView
import de.vanita5.twittnuker.view.IconActionView
import de.vanita5.twittnuker.view.ShortTimeView
import de.vanita5.twittnuker.view.iface.IColorLabelView

class ActivityTitleSummaryViewHolder(private val adapter: ParcelableActivitiesAdapter, itemView: View) : ViewHolder(itemView), View.OnClickListener {

    private val itemContent: IColorLabelView
    private val activityTypeView: IconActionView
    private val titleView: TextView
    private val summaryView: TextView
    private val timeView: ShortTimeView
    private val profileImagesContainer: ViewGroup
    private val profileImageMoreNumber: BadgeView
    private val profileImageViews: Array<ImageView>
    private val profileImageSpace: View

    private var mActivityEventListener: IActivitiesAdapter.ActivityEventListener? = null

    init {

        itemContent = itemView.findViewById(R.id.itemContent) as IColorLabelView
        activityTypeView = itemView.findViewById(R.id.activity_type) as IconActionView
        titleView = itemView.findViewById(R.id.title) as TextView
        summaryView = itemView.findViewById(R.id.summary) as TextView
        timeView = itemView.findViewById(R.id.time) as ShortTimeView
        profileImageSpace = itemView.findViewById(R.id.profile_image_space)

        profileImagesContainer = itemView.findViewById(R.id.profile_images_container) as ViewGroup
        profileImageViews = arrayOf(
                itemView.findViewById(R.id.activity_profile_image_0) as ImageView,
                itemView.findViewById(R.id.activity_profile_image_1) as ImageView,
                itemView.findViewById(R.id.activity_profile_image_2) as ImageView,
                itemView.findViewById(R.id.activity_profile_image_3) as ImageView,
                itemView.findViewById(R.id.activity_profile_image_4) as ImageView
        )
        profileImageMoreNumber = itemView.findViewById(R.id.activity_profile_image_more_number) as BadgeView

        val resources = adapter.context.resources
        val lp = titleView.layoutParams as ViewGroup.MarginLayoutParams
        val spacing = resources.getDimensionPixelSize(R.dimen.element_spacing_small)
        lp.leftMargin = spacing
        MarginLayoutParamsCompat.setMarginStart(lp, spacing)
        timeView.setShowAbsoluteTime(adapter.isShowAbsoluteTime)
    }

    fun displayActivity(activity: ParcelableActivity, byFriends: Boolean) {
        val context = adapter.context
        val sources = ParcelableActivityUtils.getAfterFilteredSources(activity)
        val message = ActivityTitleSummaryMessage.get(context,
                adapter.userColorNameManager, activity, sources, activityTypeView.defaultColor,
                adapter.useStarsForLikes, adapter.isNameFirst)
        if (message == null) {
            showNotSupported()
            return
        }
        activityTypeView.setColorFilter(message.color, PorterDuff.Mode.SRC_ATOP)
        activityTypeView.setImageResource(message.icon)
        titleView.text = message.title
        summaryView.text = message.summary
        summaryView.visibility = if (summaryView.length() > 0) View.VISIBLE else View.GONE
        timeView.setTime(activity.timestamp)
        if (adapter.showAccountsColor) {
            itemContent.drawEnd(activity.account_color)
        } else {
            itemContent.drawEnd()
        }
        displayUserProfileImages(sources)
    }

    private fun showNotSupported() {

    }

    fun setTextSize(textSize: Float) {
        titleView.textSize = textSize
        summaryView.textSize = textSize * 0.85f
        timeView.textSize = textSize * 0.80f
    }

    private fun displayUserProfileImages(statuses: Array<ParcelableUser>?) {
        val shouldDisplayImages = adapter.profileImageEnabled
        profileImagesContainer.visibility = if (shouldDisplayImages) View.VISIBLE else View.GONE
        profileImageSpace.visibility = if (shouldDisplayImages) View.VISIBLE else View.GONE
        if (!shouldDisplayImages) return
        val imageLoader = adapter.mediaLoader
        if (statuses == null) {
            for (view in profileImageViews) {
                imageLoader.cancelDisplayTask(view)
                view.visibility = View.GONE
            }
            return
        }
        val length = Math.min(profileImageViews.size, statuses.size)
        for (i in 0 until profileImageViews.size) {
            val view = profileImageViews[i]
            view.setImageDrawable(null)
            if (i < length) {
                view.visibility = View.VISIBLE
                imageLoader.displayProfileImage(view, statuses[i])
            } else {
                imageLoader.cancelDisplayTask(view)
                view.visibility = View.GONE
            }
        }
        if (statuses.size > profileImageViews.size) {
            val moreNumber = statuses.size - profileImageViews.size
            profileImageMoreNumber.visibility = View.VISIBLE
            profileImageMoreNumber.setText(moreNumber.toString())
        } else {
            profileImageMoreNumber.visibility = View.GONE
        }
    }

    fun setOnClickListeners() {
        setActivityEventListener(adapter.activityEventListener!!)
    }

    fun setActivityEventListener(listener: IActivitiesAdapter.ActivityEventListener) {
        mActivityEventListener = listener
        (itemContent as View).setOnClickListener(this)
        //        ((View) itemContent).setOnLongClickListener(this);

    }

    override fun onClick(v: View) {
        if (mActivityEventListener == null) return
        val position = layoutPosition
        when (v.id) {
            R.id.itemContent -> {
                mActivityEventListener!!.onActivityClick(this, position)
            }
        }
    }

}