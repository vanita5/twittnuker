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

package de.vanita5.twittnuker.util.premium

import android.content.Context
import android.content.Intent
import com.anjlab.android.iab.v3.BillingProcessor
import nl.komponents.kovenant.task
import de.vanita5.twittnuker.Constants.GOOGLE_PLAY_LICENCING_PUBKEY
import de.vanita5.twittnuker.activity.GooglePlayInAppPurchaseActivity
import de.vanita5.twittnuker.activity.premium.AbsExtraFeaturePurchaseActivity
import de.vanita5.twittnuker.view.controller.premium.GoogleFiltersImportViewController
import de.vanita5.twittnuker.view.controller.premium.GoogleFiltersSubscriptionsViewController
import de.vanita5.twittnuker.view.controller.premium.SyncStatusViewController

class GooglePlayExtraFeaturesService : ExtraFeaturesService() {

    private lateinit var bp: BillingProcessor

    override fun getDashboardControllers() = listOf(
            SyncStatusViewController::class.java,
            GoogleFiltersImportViewController::class.java,
            GoogleFiltersSubscriptionsViewController::class.java
    )

    override fun init(context: Context) {
        super.init(context)
        bp = BillingProcessor(context, GOOGLE_PLAY_LICENCING_PUBKEY, null)
    }

    override fun appStarted() {
        task {
            bp.loadOwnedPurchasesFromGoogle()
        }
    }

    override fun release() {
        bp.release()
    }

    override fun isSupported(): Boolean = BillingProcessor.isIabServiceAvailable(context)

    override fun isEnabled(feature: String): Boolean {
        if (bp.hasValidTransaction(PRODUCT_ID_EXTRA_FEATURES_PACK)) return true
        val productId = getProductId(feature)
        return bp.hasValidTransaction(productId)
    }

    override fun destroyPurchase(): Boolean {
        bp.consumePurchase(PRODUCT_ID_EXTRA_FEATURES_PACK)
        bp.consumePurchase(PRODUCT_ID_DATA_SYNC)
        bp.consumePurchase(PRODUCT_ID_FILTERS_IMPORT)
        bp.consumePurchase(PRODUCT_ID_FILTERS_SUBSCRIPTION)
        bp.consumePurchase(PRODUCT_ID_SCHEDULE_STATUS)
        return true
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
        private const val PRODUCT_ID_EXTRA_FEATURES_PACK = "twittnuker.extra.features"
        private const val PRODUCT_ID_DATA_SYNC = "twittnuker.extra.feature.data_sync"
        private const val PRODUCT_ID_FILTERS_IMPORT = "twittnuker.extra.feature.filter_import"
        private const val PRODUCT_ID_FILTERS_SUBSCRIPTION = "twittnuker.extra.feature.filter_subscription"
        private const val PRODUCT_ID_SCHEDULE_STATUS = "twittnuker.extra.feature.schedule_status"

        @JvmStatic
        fun getProductId(feature: String): String {
            return when (feature) {
                FEATURE_FEATURES_PACK -> PRODUCT_ID_EXTRA_FEATURES_PACK
                FEATURE_SYNC_DATA -> PRODUCT_ID_DATA_SYNC
                FEATURE_FILTERS_IMPORT -> PRODUCT_ID_FILTERS_IMPORT
                FEATURE_FILTERS_SUBSCRIPTION -> PRODUCT_ID_FILTERS_SUBSCRIPTION
                FEATURE_SCHEDULE_STATUS -> PRODUCT_ID_SCHEDULE_STATUS
                else -> throw UnsupportedOperationException(feature)
            }
        }
    }
}