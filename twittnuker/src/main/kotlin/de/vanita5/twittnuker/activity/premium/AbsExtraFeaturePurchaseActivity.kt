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

package de.vanita5.twittnuker.activity.premium

import android.content.Context
import android.content.Intent
import de.vanita5.twittnuker.activity.BaseActivity
import de.vanita5.twittnuker.constant.IntentConstants
import de.vanita5.twittnuker.model.premium.PurchaseResult


abstract class AbsExtraFeaturePurchaseActivity : BaseActivity() {
    protected val requestingFeature: String get() = intent.getStringExtra(EXTRA_REQUESTING_FEATURE)

    protected fun finishWithError(code: Int) {
        setResult(code)
        finish()
    }

    protected fun finishWithResult(result: PurchaseResult) {
        setResult(RESULT_OK, Intent().putExtra(EXTRA_PURCHASE_RESULT, result))
        finish()
    }

    companion object {

        const val RESULT_SERVICE_UNAVAILABLE = 1
        const val RESULT_INTERNAL_ERROR = 6
        const val RESULT_NOT_PURCHASED = 8

        const val EXTRA_PURCHASE_RESULT = "purchase_result"
        const val EXTRA_REQUESTING_FEATURE = "requesting_feature"

        const val ACTION_RESTORE_PURCHASE = "${IntentConstants.INTENT_PACKAGE_PREFIX}RESTORE_PURCHASE"

        @JvmStatic
        fun <T : AbsExtraFeaturePurchaseActivity> purchaseIntent(context: Context, cls: Class<T>, feature: String): Intent {
            val intent = Intent(context, cls)
            intent.putExtra(EXTRA_REQUESTING_FEATURE, feature)
            return intent
        }

        @JvmStatic
        fun <T : AbsExtraFeaturePurchaseActivity> restorePurchaseIntent(context: Context, cls: Class<T>, feature: String): Intent {
            val intent = Intent(context, cls)
            intent.action = ACTION_RESTORE_PURCHASE
            intent.putExtra(EXTRA_REQUESTING_FEATURE, feature)
            return intent
        }
    }
}