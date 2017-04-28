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

import android.support.v7.app.AlertDialog
import android.view.View
import com.bumptech.glide.Glide
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.ParcelableStatus

abstract class AbsStatusDialogFragment : de.vanita5.twittnuker.fragment.BaseDialogFragment() {

    protected abstract val android.app.Dialog.loadProgress: android.view.View
    protected abstract val android.app.Dialog.itemContent: android.view.View

    protected val status: de.vanita5.twittnuker.model.ParcelableStatus?
        get() = arguments.getParcelable<de.vanita5.twittnuker.model.ParcelableStatus>(EXTRA_STATUS)

    protected val statusId: String
        get() = arguments.getString(EXTRA_STATUS_ID)

    protected val accountKey: de.vanita5.twittnuker.model.UserKey
        get() = arguments.getParcelable(EXTRA_ACCOUNT_KEY)

    private lateinit var adapter: de.vanita5.twittnuker.adapter.DummyItemAdapter

    override final fun onCreateDialog(savedInstanceState: android.os.Bundle?): android.app.Dialog {
        val builder = android.support.v7.app.AlertDialog.Builder(context)
        val accountKey = this.accountKey

        builder.setupAlertDialog()

        adapter = de.vanita5.twittnuker.adapter.DummyItemAdapter(context, requestManager = Glide.with(this))
        adapter.showCardActions = false
        adapter.showAccountsColor = true

        val dialog = builder.create()
        dialog.setOnShowListener { dialog ->
            dialog as android.support.v7.app.AlertDialog
            dialog.applyTheme()

            val am = android.accounts.AccountManager.get(context)
            val details = de.vanita5.twittnuker.model.util.AccountUtils.getAccountDetails(am, accountKey, true) ?: run {
                dismiss()
                return@setOnShowListener
            }
            val weakThis = java.lang.ref.WeakReference(this)
            val weakHolder = java.lang.ref.WeakReference(de.vanita5.twittnuker.view.holder.StatusViewHolder(adapter, dialog.itemContent).apply {
                setupViewOptions()
            })
            nl.komponents.kovenant.ui.promiseOnUi {
                val currentDialog = weakThis.get()?.dialog as? AlertDialog ?: return@promiseOnUi
                currentDialog.loadProgress.visibility = View.VISIBLE
                currentDialog.itemContent.visibility = View.GONE
                currentDialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.isEnabled = false
                currentDialog.getButton(android.content.DialogInterface.BUTTON_NEUTRAL)?.isEnabled = false
            } and de.vanita5.twittnuker.fragment.status.AbsStatusDialogFragment.Companion.showStatus(context, details, statusId, status).successUi { status ->
                val fragment = weakThis.get() ?: return@successUi
                val currentDialog = fragment.dialog as? android.support.v7.app.AlertDialog ?: return@successUi
                val holder = weakHolder.get() ?: return@successUi
                currentDialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)?.isEnabled = true
                currentDialog.getButton(android.content.DialogInterface.BUTTON_NEUTRAL)?.isEnabled = true
                currentDialog.itemContent.visibility = android.view.View.VISIBLE
                currentDialog.loadProgress.visibility = android.view.View.GONE
                currentDialog.itemContent.isFocusable = false
                holder.display(status = status, displayInReplyTo = false)
                currentDialog.onStatusLoaded(details, status, savedInstanceState)
            }.failUi {
                val fragment = weakThis.get()?.takeIf { it.dialog != null } ?: return@failUi
                android.widget.Toast.makeText(fragment.context, de.vanita5.twittnuker.R.string.message_toast_error_occurred, android.widget.Toast.LENGTH_SHORT).show()
                fragment.dismiss()
            }
        }
        return dialog
    }

    protected abstract fun android.support.v7.app.AlertDialog.Builder.setupAlertDialog()

    protected abstract fun android.support.v7.app.AlertDialog.onStatusLoaded(details: de.vanita5.twittnuker.model.AccountDetails, status: de.vanita5.twittnuker.model.ParcelableStatus,
            savedInstanceState: android.os.Bundle?)

    companion object {

        fun showStatus(context: android.content.Context, details: de.vanita5.twittnuker.model.AccountDetails, statusId: String,
                status: de.vanita5.twittnuker.model.ParcelableStatus?): nl.komponents.kovenant.Promise<ParcelableStatus, Exception> {
            if (status != null) {
                status.apply {
                    if (account_key != details.key) {
                        my_retweet_id = null
                        is_favorite = false
                    }
                    account_key = details.key
                    account_color = details.color
                }
                return nl.komponents.kovenant.Promise.Companion.ofSuccess(status)
            }
            val microBlog = details.newMicroBlogInstance(context, de.vanita5.microblog.library.MicroBlog::class.java)
            val profileImageSize = context.getString(de.vanita5.twittnuker.R.string.profile_image_size)
            return nl.komponents.kovenant.task { microBlog.showStatus(statusId).toParcelable(details, profileImageSize) }
        }

    }
}