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

package de.vanita5.twittnuker.fragment.status

import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.constant.IntentConstants.*

/**
 * Asks user to favorite a status.
 */
class FavoriteConfirmDialogFragment : AbsStatusDialogFragment() {

    override val android.app.Dialog.loadProgress: android.view.View get() = findViewById(de.vanita5.twittnuker.R.id.loadProgress)

    override val android.app.Dialog.itemContent: android.view.View get() = findViewById(de.vanita5.twittnuker.R.id.itemContent)

    override fun android.support.v7.app.AlertDialog.Builder.setupAlertDialog() {
        if (preferences[de.vanita5.twittnuker.constant.iWantMyStarsBackKey]) {
            setTitle(de.vanita5.twittnuker.R.string.title_favorite_confirm)
        } else {
            setTitle(de.vanita5.twittnuker.R.string.title_like_confirm)
        }
        setView(de.vanita5.twittnuker.R.layout.dialog_status_favorite_confirm)
        setPositiveButton(de.vanita5.twittnuker.R.string.action_favorite, null)
        setNegativeButton(android.R.string.cancel, null)
    }

    override fun android.support.v7.app.AlertDialog.onStatusLoaded(details: de.vanita5.twittnuker.model.AccountDetails, status: de.vanita5.twittnuker.model.ParcelableStatus,
            savedInstanceState: android.os.Bundle?) {
        val positiveButton = getButton(android.content.DialogInterface.BUTTON_POSITIVE)
        if (preferences[de.vanita5.twittnuker.constant.iWantMyStarsBackKey]) {
            if (status.is_favorite) {
                positiveButton.setText(de.vanita5.twittnuker.R.string.action_unfavorite)
            } else {
                positiveButton.setText(de.vanita5.twittnuker.R.string.action_favorite)
            }
        } else {
            if (status.is_favorite) {
                positiveButton.setText(de.vanita5.twittnuker.R.string.action_undo_like)
            } else {
                positiveButton.setText(de.vanita5.twittnuker.R.string.action_like)
            }
        }
        positiveButton.setOnClickListener {
            if (status.is_favorite) {
                twitterWrapper.destroyFavoriteAsync(accountKey, status.id)
            } else {
                twitterWrapper.createFavoriteAsync(accountKey, status)
            }
            dismiss()
        }

    }

    override fun onCancel(dialog: android.content.DialogInterface) {
        finishFavoriteConfirmActivity()
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        finishFavoriteConfirmActivity()
    }

    private fun finishFavoriteConfirmActivity() {
        val activity = this.activity
        if (activity is de.vanita5.twittnuker.activity.content.FavoriteConfirmDialogActivity && !activity.isFinishing) {
            activity.finish()
        }
    }

    companion object {

        val FRAGMENT_TAG = "favorite_confirm"

        fun show(fm: android.support.v4.app.FragmentManager, accountKey: de.vanita5.twittnuker.model.UserKey, statusId: String,
                status: de.vanita5.twittnuker.model.ParcelableStatus? = null): de.vanita5.twittnuker.fragment.status.FavoriteConfirmDialogFragment {
            val f = de.vanita5.twittnuker.fragment.status.FavoriteConfirmDialogFragment()
            f.arguments = org.mariotaku.ktextension.Bundle {
                this[EXTRA_ACCOUNT_KEY] = accountKey
                this[EXTRA_STATUS_ID] = statusId
                this[EXTRA_STATUS] = status
            }
            f.show(fm, de.vanita5.twittnuker.fragment.status.FavoriteConfirmDialogFragment.Companion.FRAGMENT_TAG)
            return f
        }
    }
}