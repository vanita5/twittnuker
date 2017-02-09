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

package de.vanita5.twittnuker.task

import android.content.Context
import android.net.Uri
import android.support.v4.app.DialogFragment
import de.vanita5.twittnuker.activity.iface.IExtendedActivity
import de.vanita5.twittnuker.fragment.ProgressDialogFragment

import java.io.File

abstract class ProgressSaveFileTask(
        context: Context,
        source: Uri,
        destination: File,
        getMimeType: SaveFileTask.FileInfoCallback
) : SaveFileTask(context, source, destination, getMimeType) {

    override fun showProgress() {
        (context as IExtendedActivity<*>).executeAfterFragmentResumed { activity ->
            val fragment = ProgressDialogFragment()
            fragment.isCancelable = false
            fragment.show(activity.supportFragmentManager, PROGRESS_FRAGMENT_TAG)
        }
    }

    override fun dismissProgress() {
        (context as IExtendedActivity<*>).executeAfterFragmentResumed { activity ->
            val fm = activity.supportFragmentManager
            val fragment = fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG) as? DialogFragment
            fragment?.dismiss()
        }
    }

    companion object {
        private val PROGRESS_FRAGMENT_TAG = "progress"
    }
}