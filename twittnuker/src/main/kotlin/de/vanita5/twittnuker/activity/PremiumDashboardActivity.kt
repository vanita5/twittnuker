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

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_premium_dashboard.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.fragment.ProgressDialogFragment
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class PremiumDashboardActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_dashboard)
        if (extraFeaturesService.isSupported()) {
            extraFeaturesService.getDashboardLayouts().forEach { layout ->
                View.inflate(this, layout, cardsContainer)
            }
        }
    }

    override fun onDestroy() {
        extraFeaturesService.release()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_premium_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.consume_purchase -> {
                if (BuildConfig.DEBUG) {
                    val dfRef = WeakReference(ProgressDialogFragment.show(supportFragmentManager, "consume_purchase_progress"))
                    val weakThis = WeakReference(this)
                    val recreate = AtomicBoolean()
                    task {
                        val activity = weakThis.get() ?: throw IllegalStateException()
                        if (!activity.extraFeaturesService.destroyPurchase()) {
                            throw IllegalStateException()
                    }
                    }.successUi {
                        recreate.set(true)
                    }.failUi {
                        val activity = weakThis.get() ?: return@failUi
                        Toast.makeText(activity, R.string.message_unable_to_consume_purchase, Toast.LENGTH_SHORT).show()
                    }.alwaysUi {
                        weakThis.get()?.executeAfterFragmentResumed {
                            val fm = weakThis.get()?.supportFragmentManager
                            val df = dfRef.get() ?: (fm?.findFragmentByTag("consume_purchase_progress") as? DialogFragment)
                            df?.dismiss()
                            if (recreate.get()) {
                                weakThis.get()?.recreate()
                            }
                        }
                    }
                }
            }
        }
        return true
    }
}