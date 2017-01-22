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
import android.support.annotation.CallSuper
import de.vanita5.twittnuker.R
import java.util.*

abstract class ExtraFeaturesService {
    protected lateinit var context: Context

    abstract fun getDashboardLayouts(): IntArray

    @CallSuper
    protected open fun init(context: Context) {
        this.context = context
    }

    open fun release() {
    }

    abstract fun isSupported(): Boolean

    abstract fun isEnabled(feature: String): Boolean

    /**
     * For debug purpose only, this will remove purchased product
     */
    abstract fun destroyPurchase(): Boolean

    abstract fun createPurchaseIntent(context: Context, feature: String): Intent?

    abstract fun createRestorePurchaseIntent(context: Context, feature: String): Intent?

    data class Introduction(val icon: Int, val description: String)

    companion object {

        const val FEATURE_FEATURES_PACK = "features_pack"
        const val FEATURE_FILTERS_IMPORT = "import_filters"
        const val FEATURE_FILTERS_SUBSCRIPTION = "filters_subscriptions"
        const val FEATURE_SYNC_DATA = "sync_data"
        const val FEATURE_SCHEDULE_STATUS = "schedule_status"

        fun newInstance(context: Context): ExtraFeaturesService {
            val instance = ServiceLoader.load(ExtraFeaturesService::class.java).first()
            instance.init(context)
            return instance
        }

        fun getIntroduction(context: Context, feature: String): Introduction {
            return when (feature) {
                FEATURE_FEATURES_PACK -> Introduction(R.drawable.ic_action_infinity, "")
                FEATURE_FILTERS_IMPORT -> Introduction(R.drawable.ic_action_speaker_muted,
                        context.getString(R.string.extra_feature_description_filters_import))
                FEATURE_SYNC_DATA -> Introduction(R.drawable.ic_action_refresh,
                        context.getString(R.string.extra_feature_description_sync_data))
                FEATURE_FILTERS_SUBSCRIPTION -> Introduction(R.drawable.ic_action_speaker_muted,
                        context.getString(R.string.extra_feature_description_filters_subscription))
                FEATURE_SCHEDULE_STATUS -> Introduction(R.drawable.ic_action_time,
                        context.getString(R.string.extra_feature_description_schedule_status))
                else -> throw UnsupportedOperationException(feature)
            }
        }
    }
}