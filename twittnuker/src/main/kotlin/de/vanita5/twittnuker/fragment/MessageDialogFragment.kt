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

package de.vanita5.twittnuker.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_MESSAGE
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_TITLE
import de.vanita5.twittnuker.extension.applyTheme

class MessageDialogFragment : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity
        val builder = AlertDialog.Builder(activity)
        val args = arguments
        builder.setTitle(args.getString(EXTRA_TITLE))
        builder.setMessage(args.getString(EXTRA_MESSAGE))
        builder.setPositiveButton(android.R.string.ok, null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            it as AlertDialog
            it.applyTheme()
        }
        return dialog
    }

    companion object {

        fun show(fm: FragmentManager, title: String? = null, message: String, tag: String): MessageDialogFragment {
            val df = create(title, message)
            df.show(fm, tag)
            return df
        }

        fun create(title: String? = null, message: String): MessageDialogFragment {
            val df = MessageDialogFragment()
            df.arguments = Bundle {
                this[EXTRA_TITLE] = title
                this[EXTRA_MESSAGE] = message
            }
            return df
        }
    }
}