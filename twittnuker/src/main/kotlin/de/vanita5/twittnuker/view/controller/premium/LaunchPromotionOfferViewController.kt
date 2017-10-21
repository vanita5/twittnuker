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

package de.vanita5.twittnuker.view.controller.premium

import android.view.View
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.set
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.activity.PremiumDashboardActivity
import de.vanita5.twittnuker.constant.promotionsEnabledKey
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService

class LaunchPromotionOfferViewController : PremiumDashboardActivity.ExtraFeatureViewController() {

    override fun onCreate() {
        super.onCreate()
        titleView.setText(R.string.title_promotions_reward)
        messageView.text = context.getString(R.string.message_promotions_reward)
        button1.setText(R.string.action_enable)

        button1.visibility = View.VISIBLE
        button2.visibility = View.GONE

        button1.setOnClickListener {
            enablePromotions()
        }
    }

    override fun onViewCreated(view: View) {
        if (extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_FEATURES_PACK)) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    private fun enablePromotions() {
        preferences[promotionsEnabledKey] = true
        val activity = ChameleonUtils.getActivity(context)
        if (activity != null) {
            activity.recreate()
        }
    }
}