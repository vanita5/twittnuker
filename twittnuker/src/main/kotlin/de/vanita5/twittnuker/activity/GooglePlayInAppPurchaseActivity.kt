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

package de.vanita5.twittnuker.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.Constants.*
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.activity.premium.AbsExtraFeaturePurchaseActivity
import de.vanita5.twittnuker.fragment.ProgressDialogFragment
import de.vanita5.twittnuker.model.premium.PurchaseResult
import de.vanita5.twittnuker.util.premium.GooglePlayExtraFeaturesService
import java.lang.ref.WeakReference


class GooglePlayInAppPurchaseActivity : AbsExtraFeaturePurchaseActivity(),
        BillingProcessor.IBillingHandler {

    private lateinit var billingProcessor: BillingProcessor

    private val productId: String get() = GooglePlayExtraFeaturesService.getProductId(requestingFeature)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingProcessor = BillingProcessor(this, Constants.GOOGLE_PLAY_LICENCING_PUBKEY, this)
        if (!isFinishing && !BillingProcessor.isIabServiceAvailable(this)) {
            handleError(BILLING_RESPONSE_RESULT_USER_CANCELED)
        }
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // MARK: Payment methods
    override fun onBillingError(code: Int, error: Throwable?) {
        handleError(code)
    }

    override fun onBillingInitialized() {
        // See https://github.com/anjlab/android-inapp-billing-v3/issues/156
        if (intent.action == ACTION_RESTORE_PURCHASE) {
            getProductDetailsAndFinish()
        } else {
            billingProcessor.purchase(this, productId)
        }
    }

    override fun onProductPurchased(productId: String?, details: TransactionDetails?) {
        getProductDetailsAndFinish()
    }

    override fun onPurchaseHistoryRestored() {
        getProductDetailsAndFinish()
    }

    private fun handleError(billingResponse: Int) {
        when (billingResponse) {
            BILLING_ERROR_OTHER_ERROR, BILLING_ERROR_INVALID_DEVELOPER_PAYLOAD -> {
                getProductDetailsAndFinish()
            }
            else -> {
                finishWithError(getResultCode(billingResponse))
            }
        }
    }

    private fun handlePurchased(sku: SkuDetails, transaction: TransactionDetails) {
        val result = PurchaseResult()
        result.feature = requestingFeature
        result.price = sku.priceValue
        result.currency = sku.currency
        finishWithResult(result)
    }


    private fun getProductDetailsAndFinish() {
        executeAfterFragmentResumed {
            val weakThis = WeakReference(it as GooglePlayInAppPurchaseActivity)
            val dfRef = WeakReference(ProgressDialogFragment.show(it.supportFragmentManager, TAG_PURCHASE_PROCESS))
            task {
                val activity = weakThis.get() ?: throw PurchaseException(BILLING_RESPONSE_RESULT_USER_CANCELED)
                val productId = activity.productId
                val bp = activity.billingProcessor
                bp.loadOwnedPurchasesFromGoogle()
                val skuDetails = bp.getPurchaseListingDetails(productId)
                        ?: throw PurchaseException(BILLING_RESPONSE_RESULT_ERROR)
                val transactionDetails = bp.getPurchaseTransactionDetails(productId)
                        ?: throw PurchaseException(BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED)
                return@task Pair(skuDetails, transactionDetails)
            }.successUi { result ->
                weakThis.get()?.handlePurchased(result.first, result.second)
            }.failUi { error ->
                if (error is PurchaseException) {
                    weakThis.get()?.handleError(error.code)
                } else {
                    weakThis.get()?.handleError(BILLING_RESPONSE_RESULT_ERROR)
                }
            }.alwaysUi {
                weakThis.get()?.executeAfterFragmentResumed {
                    val fm = weakThis.get()?.supportFragmentManager
                    val df = dfRef.get() ?: (fm?.findFragmentByTag(TAG_PURCHASE_PROCESS) as? DialogFragment)
                    df?.dismiss()
                }
            }
        }

    }

    private fun getResultCode(billingResponse: Int): Int {
        val resultCode = when (billingResponse) {
            BILLING_RESPONSE_RESULT_OK -> Activity.RESULT_OK
            BILLING_RESPONSE_RESULT_USER_CANCELED -> Activity.RESULT_CANCELED
            BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE -> RESULT_SERVICE_UNAVAILABLE
            BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED -> RESULT_NOT_PURCHASED
            BILLING_RESPONSE_RESULT_ERROR -> RESULT_INTERNAL_ERROR
            else -> billingResponse
        }
        return resultCode
    }

    class PurchaseException(val code: Int) : Exception()

    companion object {
        private const val TAG_PURCHASE_PROCESS = "get_purchase_process"

    }
}