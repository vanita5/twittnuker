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
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.TwittnukerConstants.EXTRA_DATA

class GooglePlayInAppPurchaseActivity : Activity(), BillingProcessor.IBillingHandler {

    lateinit var billingProcessor: BillingProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingProcessor = BillingProcessor(this, Constants.GOOGLE_PLAY_LICENCING_PUBKEY, this)
        if (!isFinishing && BillingProcessor.isIabServiceAvailable(this)) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // MARK: Payment methods
    override fun onBillingError(code: Int, error: Throwable?) {
        setResult(RESULT_CANCELED)
        finish()
    }

    private val productId: String get() = intent.getStringExtra(EXTRA_PRODUCT_ID)

    override fun onBillingInitialized() {
        billingProcessor.purchase(this, productId)
    }

    override fun onProductPurchased(productId: String?, details: TransactionDetails?) {
        billingProcessor.getPurchaseTransactionDetails(productId)
        val data = Intent()
        details?.purchaseInfo?.purchaseData?.let { purchaseData ->
            data.putExtra(EXTRA_DATA, purchaseData)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onPurchaseHistoryRestored() {
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
    }
}