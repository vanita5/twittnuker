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

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log

import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.fragment.DataExportImportTypeSelectorDialogFragment
import de.vanita5.twittnuker.fragment.ProgressDialogFragment
import de.vanita5.twittnuker.util.DataImportExportUtils

import java.io.File
import java.io.IOException

class DataImportActivity : BaseActivity(), DataExportImportTypeSelectorDialogFragment.Callback {

    private var importSettingsTask: ImportSettingsTask? = null
    private var openImportTypeTask: OpenImportTypeTask? = null
    private var resumeFragmentsRunnable: Runnable? = null

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
            REQUEST_PICK_FILE -> {
                resumeFragmentsRunnable = Runnable {
                    if (resultCode == RESULT_OK && data != null) {
                        val path = data.data.path
                        if (openImportTypeTask == null || openImportTypeTask!!.status != AsyncTask.Status.RUNNING) {
                            openImportTypeTask = OpenImportTypeTask(this@DataImportActivity, path)
                            openImportTypeTask!!.execute()
                        }
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


    override fun onResumeFragments() {
        super.onResumeFragments()
        if (resumeFragmentsRunnable != null) {
            resumeFragmentsRunnable!!.run()
        }
    }

    override fun onPositiveButtonClicked(path: String?, flags: Int) {
        if (path == null || flags == 0) {
            finish()
            return
        }
        if (importSettingsTask == null || importSettingsTask!!.status != AsyncTask.Status.RUNNING) {
            importSettingsTask = ImportSettingsTask(this, path, flags)
            importSettingsTask!!.execute()
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
            intent.action = INTENT_ACTION_PICK_FILE
            startActivityForResult(intent, REQUEST_PICK_FILE)
        }
    }

    internal class ImportSettingsTask(
            private val activity: DataImportActivity,
            private val path: String?,
            private val flags: Int
    ) : AsyncTask<Any, Any, Boolean>() {

        override fun doInBackground(vararg params: Any): Boolean? {
            if (path == null) return false
            val file = File(path)
            if (!file.isFile) return false
            try {
                DataImportExportUtils.importData(activity, file, flags)
                return true
            } catch (e: IOException) {
                Log.w(LOGTAG, e)
                return false
            }

        }

        override fun onPostExecute(result: Boolean?) {
            val fm = activity.supportFragmentManager
            val f = fm.findFragmentByTag(FRAGMENT_TAG)
            if (f is DialogFragment) {
                f.dismiss()
            }
            if (result != null && result) {
                activity.setResult(RESULT_OK)
            } else {
                activity.setResult(RESULT_CANCELED)
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

    internal class OpenImportTypeTask(private val mActivity: DataImportActivity, private val mPath: String?) : AsyncTask<Any, Any, Int>() {

        override fun doInBackground(vararg params: Any): Int? {
            if (mPath == null) return 0
            val file = File(mPath)
            if (!file.isFile) return 0
            try {
                return DataImportExportUtils.getImportedSettingsFlags(file)
            } catch (e: IOException) {
                return 0
            }

        }

        override fun onPostExecute(flags: Int?) {
            val fm = mActivity.supportFragmentManager
            val f = fm.findFragmentByTag(FRAGMENT_TAG)
            if (f is DialogFragment) {
                f.dismiss()
            }
            val df = DataExportImportTypeSelectorDialogFragment()
            val args = Bundle()
            args.putString(EXTRA_PATH, mPath)
            args.putString(EXTRA_TITLE, mActivity.getString(R.string.import_settings_type_dialog_title))
            if (flags != null) {
                args.putInt(EXTRA_FLAGS, flags)
            } else {
                args.putInt(EXTRA_FLAGS, 0)
            }
            df.arguments = args
            df.show(mActivity.supportFragmentManager, "select_import_type")
        }

        override fun onPreExecute() {
            ProgressDialogFragment.show(mActivity, FRAGMENT_TAG).isCancelable = false
        }

        companion object {

            private val FRAGMENT_TAG = "read_settings_data_dialog"
        }

    }
}