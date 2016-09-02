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

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import de.vanita5.twittnuker.Constants.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants
import de.vanita5.twittnuker.fragment.DataExportImportTypeSelectorDialogFragment
import de.vanita5.twittnuker.fragment.ProgressDialogFragment
import de.vanita5.twittnuker.util.DataImportExportUtils

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DataExportActivity : BaseActivity(), DataExportImportTypeSelectorDialogFragment.Callback {

    private var task: ExportSettingsTask? = null

    override fun onCancelled(df: DialogFragment) {
        if (!isFinishing) {
            finish()
        }
    }

    override fun onDismissed(df: DialogFragment) {
        if (df is DataExportImportTypeSelectorDialogFragment) {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_DIRECTORY -> {
                executeAfterFragmentResumed {
                    if (resultCode == RESULT_OK && data != null) {
                        val path = data.data.path
                        val df = DataExportImportTypeSelectorDialogFragment()
                        val args = Bundle()
                        args.putString(EXTRA_PATH, path)
                        args.putString(EXTRA_TITLE, getString(R.string.export_settings_type_dialog_title))
                        df.arguments = args
                        df.show(supportFragmentManager, "select_export_type")
                    } else {
                        if (!isFinishing) {
                            finish()
                        }
                    }
                }
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPositiveButtonClicked(path: String?, flags: Int) {
        if (path == null || flags == 0) {
            finish()
            return
        }
        if (task == null || task!!.status != AsyncTask.Status.RUNNING) {
            task = ExportSettingsTask(this, path, flags)
            task!!.execute()
        }
    }

    override fun onStart() {
        super.onStart()
        setVisible(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val intent = Intent(this, FileSelectorActivity::class.java)
            intent.action = IntentConstants.INTENT_ACTION_PICK_DIRECTORY
            startActivityForResult(intent, REQUEST_PICK_DIRECTORY)
        }
    }

    internal class ExportSettingsTask(private val activity: DataExportActivity, private val mPath: String?, private val mFlags: Int) : AsyncTask<Any, Any, Boolean>() {

        override fun doInBackground(vararg params: Any): Boolean? {
            if (mPath == null) return false
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val fileName = String.format("Twittnuker_Settings_%s.zip", sdf.format(Date()))
            val file = File(mPath, fileName)
            file.delete()
            try {
                DataImportExportUtils.exportData(activity, file, mFlags)
                return true
            } catch (e: IOException) {
                Log.w(LOGTAG, e)
                return false
            }

        }

        override fun onPostExecute(result: Boolean?) {
            val fm = activity.supportFragmentManager
            val f = fm.findFragmentByTag(FRAGMENT_TAG) as DialogFragment
            f.dismiss()
            if (result != null && result) {
                activity.setResult(Activity.RESULT_OK)
            } else {
                activity.setResult(Activity.RESULT_CANCELED)
            }
            activity.finish()
        }

        override fun onPreExecute() {
            ProgressDialogFragment.show(activity, FRAGMENT_TAG).isCancelable = false
        }

        companion object {
            private val FRAGMENT_TAG = "import_settings_dialog"
        }

    }
}