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
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceDialogFragmentCompat
import org.mariotaku.kpreferences.KPreferences
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.util.dagger.GeneralComponent

import javax.inject.Inject

abstract class ThemedPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    @Inject
    lateinit var kPreferences: KPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponent.get(context).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context
        val preference = preference
        onClick(null, DialogInterface.BUTTON_NEGATIVE)
        val builder = AlertDialog.Builder(context)
                .setTitle(preference.dialogTitle)
                .setIcon(preference.dialogIcon)
                .setPositiveButton(preference.positiveButtonText, this)
                .setNegativeButton(preference.negativeButtonText, this)
        val contentView = onCreateDialogView(context)
        if (contentView != null) {
            onBindDialogView(contentView)
            builder.setView(contentView)
        } else {
            builder.setMessage(preference.dialogMessage)
        }
        onPrepareDialogBuilder(builder)
        // Create the dialog
        val dialog = builder.create()
        dialog.setOnShowListener { dialog -> (dialog as AlertDialog).applyTheme() }
        if (needInputMethod()) {
            supportRequestInputMethod(dialog)
        }
        return dialog
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {

    }

    private fun supportRequestInputMethod(dialog: Dialog) {
        val window = dialog.window
        window?.setSoftInputMode(5)
    }

}