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

package de.vanita5.twittnuker.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService

class ExtraFeaturesIntroductionDialogFragment : BaseDialogFragment() {

    private lateinit var extraFeaturesService: ExtraFeaturesService

    override fun onDestroy() {
        extraFeaturesService.release()
        super.onDestroy()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        extraFeaturesService = ExtraFeaturesService.newInstance(context)
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.title_extra_features)
        builder.setView(R.layout.dialog_extra_features_introduction)
        builder.setPositiveButton(R.string.action_purchase) { dialog, which ->
            startActivity(extraFeaturesService.createPurchaseIntent(context))
        }
        builder.setNegativeButton(R.string.action_later) { dialog, which ->

        }
        val restorePurchaseIntent = extraFeaturesService.createRestorePurchaseIntent(context)
        if (restorePurchaseIntent != null) {
            builder.setNeutralButton(R.string.action_restore_purchase) { dialog, which ->
                startActivity(restorePurchaseIntent)
            }
        }
        val dialog = builder.create()
        dialog.setOnShowListener {
            it as Dialog
            it.findViewById(R.id.restorePurchaseHint).visibility = if (restorePurchaseIntent != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        return dialog
    }
}