/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.extension.onShow

class PermissionRequestDialog : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val permissions = arguments.getStringArray(EXTRA_PERMISSIONS)
        val requestCode = arguments.getInt(EXTRA_REQUEST_CODE)
        builder.setMessage(arguments.getString(EXTRA_MESSAGE))
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
        builder.setNegativeButton(R.string.action_later) { _, _ ->
            val callback = parentFragment as? PermissionRequestCancelCallback ?: activity as?
                    PermissionRequestCancelCallback ?: return@setNegativeButton
            callback.onRequestPermissionCancelled(requestCode)
        }
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }

    interface PermissionRequestCancelCallback {
        fun onRequestPermissionCancelled(requestCode: Int)
    }

    companion object {

        fun show(fragmentManager: FragmentManager, message: String, permissions: Array<String>,
                requestCode: Int): PermissionRequestDialog {
            val df = PermissionRequestDialog()
            df.arguments = Bundle {
                this[EXTRA_MESSAGE] = message
                this[EXTRA_PERMISSIONS] = permissions
                this[EXTRA_REQUEST_CODE] = requestCode
            }
            df.show(fragmentManager, "request_permission_message")
            return df
        }

    }
}