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
import java.util.*

abstract class ExtraFeaturesChecker {
    protected lateinit var context: Context

    abstract val introductionLayout: Int
    abstract val statusLayout: Int

    @CallSuper
    protected open fun init(context: Context) {
        this.context = context
    }

    open fun release() {
    }

    abstract fun isSupported(): Boolean

    abstract fun isEnabled(): Boolean

    /**
     * For debug purpose only, this will remove purchased product
     */
    abstract fun destroyPurchase(): Boolean

    abstract fun createPurchaseIntent(context: Context): Intent

    abstract fun createRestorePurchaseIntent(context: Context): Intent?


    companion object {

        fun newInstance(context: Context): ExtraFeaturesChecker {
            val instance = ServiceLoader.load(ExtraFeaturesChecker::class.java).first()
            instance.init(context)
            return instance
        }

    }
}