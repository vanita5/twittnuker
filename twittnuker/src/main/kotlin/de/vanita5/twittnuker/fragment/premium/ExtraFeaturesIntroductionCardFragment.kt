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

package de.vanita5.twittnuker.fragment.premium

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_extra_features_introduction.*
import kotlinx.android.synthetic.main.layout_extra_features_introduction.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.RESULT_NOT_PURCHASED
import de.vanita5.twittnuker.constant.RESULT_SERVICE_UNAVAILABLE
import de.vanita5.twittnuker.fragment.BaseSupportFragment
import de.vanita5.twittnuker.model.analyzer.PurchaseConfirm
import de.vanita5.twittnuker.model.analyzer.PurchaseFinished
import de.vanita5.twittnuker.model.analyzer.PurchaseIntroduction
import de.vanita5.twittnuker.util.Analyzer
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService

class ExtraFeaturesIntroductionCardFragment : BaseSupportFragment() {

    lateinit var extraFeaturesService: ExtraFeaturesService

    private val REQUEST_PURCHASE: Int = 301
    private val REQUEST_RESTORE_PURCHASE: Int = 302

    // MARK: Fragment lifecycle
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        extraFeaturesService = ExtraFeaturesService.newInstance(context)
        purchaseButton.setOnClickListener {
            Analyzer.log(PurchaseConfirm(PurchaseFinished.NAME_EXTRA_FEATURES))
            startActivityForResult(extraFeaturesService.createPurchaseIntent(context), REQUEST_PURCHASE)
        }
        val restorePurchaseIntent = extraFeaturesService.createRestorePurchaseIntent(context)
        if (restorePurchaseIntent != null) {
            restorePurchaseHint.visibility = View.VISIBLE
            restorePurchaseButton.visibility = View.VISIBLE
            restorePurchaseButton.setOnClickListener {
                startActivityForResult(restorePurchaseIntent, REQUEST_RESTORE_PURCHASE)
            }
        } else {
            restorePurchaseHint.visibility = View.GONE
            restorePurchaseButton.visibility = View.GONE
            restorePurchaseButton.setOnClickListener(null)
        }
        if (savedInstanceState == null) {
            Analyzer.log(PurchaseIntroduction(PurchaseFinished.NAME_EXTRA_FEATURES, "enhanced features dashboard"))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PURCHASE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Analyzer.log(PurchaseFinished.create(PurchaseFinished.NAME_EXTRA_FEATURES, data))
                        activity?.recreate()
                    }
                }
            }
            REQUEST_RESTORE_PURCHASE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        activity?.recreate()
                    }
                    RESULT_NOT_PURCHASED -> {
                        Toast.makeText(context, R.string.message_extra_features_not_purchased, Toast.LENGTH_SHORT).show()
                    }
                    RESULT_SERVICE_UNAVAILABLE -> {
                        Toast.makeText(context, R.string.message_network_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_extra_features_introduction, container, false)
    }

}