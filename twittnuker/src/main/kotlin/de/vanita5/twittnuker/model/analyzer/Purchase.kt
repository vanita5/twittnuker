/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.model.analyzer

import android.app.Activity
import android.content.Intent
import de.vanita5.twittnuker.constant.*
import de.vanita5.twittnuker.util.Analyzer


data class Purchase(val productName: String) : Analyzer.Event {
    override val name: String = "Purchase"
    override var accountType: String? = null
    var resultCode: Int = Activity.RESULT_OK
    var price: Double = Double.NaN
    var currency: String? = null

    override fun forEachValues(action: (String, String?) -> Unit) {
        if (resultCode != Activity.RESULT_OK) {
            action("Fail reason", getFailReason(resultCode))
        }
    }

    companion object {
        const val NAME_EXTRA_FEATURES = "Enhanced Features"

        internal fun getFailReason(resultCode: Int): String {
            return when (resultCode) {
                Activity.RESULT_CANCELED -> "cancelled"
                RESULT_SERVICE_UNAVAILABLE -> "service unavailable"
                RESULT_INTERNAL_ERROR -> "internal error"
                RESULT_NOT_PURCHASED -> "not purchased"
                else -> "unknown"
            }
        }

        fun fromActivityResult(name: String, resultCode: Int, data: Intent?): Purchase {
            val result = Purchase(name)
            result.resultCode = resultCode
            if (data != null) {
                result.price = data.getDoubleExtra(EXTRA_PRICE, Double.NaN)
                result.currency = data.getStringExtra(EXTRA_CURRENCY)
            }
            return result
        }
    }
}