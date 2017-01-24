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

package de.vanita5.twittnuker.util.premium

import android.content.Context
import android.content.Intent
import com.anjlab.android.iab.v3.BillingProcessor
import de.vanita5.twittnuker.Constants.GOOGLE_PLAY_LICENCING_PUBKEY
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.activity.GooglePlayInAppPurchaseActivity
import de.vanita5.twittnuker.activity.premium.AbsExtraFeaturePurchaseActivity

class GooglePlayExtraFeaturesService() : ExtraFeaturesService() {
    private val PRODUCT_ID_EXTRA_FEATURES_PACK = "twittnuker.extra.features"

    private lateinit var bp: BillingProcessor

    override fun getDashboardLayouts() = intArrayOf(R.layout.card_item_extra_features_sync_status)

    override fun init(context: Context) {
        super.init(context)
        bp = BillingProcessor(context, GOOGLE_PLAY_LICENCING_PUBKEY, null)
    }

    override fun release() {
        bp.release()
    }

    override fun isSupported(): Boolean = true

    override fun isEnabled(feature: String): Boolean {
        if (bp.hasValidTransaction(PRODUCT_ID_EXTRA_FEATURES_PACK)) return true
        val productId = getProductId(feature)
        return bp.hasValidTransaction(productId)
    }

    override fun destroyPurchase(): Boolean {
        return bp.consumePurchase(PRODUCT_ID_EXTRA_FEATURES_PACK)
    }

    override fun createPurchaseIntent(context: Context, feature: String): Intent? {
        return AbsExtraFeaturePurchaseActivity.purchaseIntent(context,
                GooglePlayInAppPurchaseActivity::class.java, feature)
    }

    override fun createRestorePurchaseIntent(context: Context, feature: String): Intent? {
        return AbsExtraFeaturePurchaseActivity.restorePurchaseIntent(context,
                GooglePlayInAppPurchaseActivity::class.java, feature)
    }

    private fun BillingProcessor.hasValidTransaction(productId: String): Boolean {
        val details = getPurchaseTransactionDetails(productId) ?: return false
        return isValidTransactionDetails(details)
    }

    companion object {
        @JvmStatic
        fun getProductId(feature: String): String {
            return when (feature) {
                FEATURE_FEATURES_PACK -> "twittnuker.extra.features"
                FEATURE_SYNC_DATA -> "twittnuker.extra.feature.data_sync"
                FEATURE_FILTERS_IMPORT -> "twittnuker.extra.feature.filter_import"
                FEATURE_FILTERS_SUBSCRIPTION -> "twittnuker.extra.feature.filter_subscription"
                FEATURE_SCHEDULE_STATUS -> "twittnuker.extra.feature.schedule_status"
                else -> throw UnsupportedOperationException(feature)
            }
        }
    }
}