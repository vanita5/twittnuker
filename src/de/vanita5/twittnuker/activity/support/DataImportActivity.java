/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.activity.support;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.ProgressDialogFragment;
import de.vanita5.twittnuker.fragment.support.DataExportImportTypeSelectorDialogFragment;
import de.vanita5.twittnuker.fragment.support.FileSelectorDialogFragment;
import de.vanita5.twittnuker.util.DataImportExportUtils;
import de.vanita5.twittnuker.util.ThemeUtils;

import java.io.File;
import java.io.IOException;

public class DataImportActivity extends BaseSupportActivity implements FileSelectorDialogFragment.Callback,
		DataExportImportTypeSelectorDialogFragment.Callback {

	private ImportSettingsTask mImportSettingsTask;
	private OpenImportTypeTask mOpenImportTypeTask;

	@Override
	public Resources getResources() {
		return getDefaultResources();
	}

	@Override
	public int getThemeResourceId() {
		return ThemeUtils.getNoDisplayThemeResource(this);
	}

	@Override
	public void onCancelled(final DialogFragment df) {
		if (!isFinishing()) {
			finish();
		}
	}

	@Override
	public void onDismissed(final DialogFragment df) {
		if (df instanceof DataExportImportTypeSelectorDialogFragment) {
			finish();
		}
	}

	@Override
	public void onFilePicked(final File file) {
		if (file == null) {
			finish();
			return;
		}
		final String path = file.getAbsolutePath();
		if (mOpenImportTypeTask == null || mOpenImportTypeTask.getStatus() != AsyncTask.Status.RUNNING) {
			mOpenImportTypeTask = new OpenImportTypeTask(this, path);
			mOpenImportTypeTask.execute();
		}
	}

	@Override
	public void onPositiveButtonClicked(final String path, final int flags) {
		if (path == null || flags == 0) {
			finish();
			return;
		}
		if (mImportSettingsTask == null || mImportSettingsTask.getStatus() != AsyncTask.Status.RUNNING) {
			mImportSettingsTask = new ImportSettingsTask(this, path, flags);
			mImportSettingsTask.execute();
		}
	}

	public void showImportTypeDialog(final String path, final Integer flags) {
		final DialogFragment df = new DataExportImportTypeSelectorDialogFragment();
		final Bundle args = new Bundle();
		args.putString(EXTRA_PATH, path);
		args.putString(EXTRA_TITLE, getString(R.string.export_settings_type_dialog_title));
		if (flags != null) {
			args.putInt(EXTRA_FLAGS, flags);
		} else {
			args.putInt(EXTRA_FLAGS, 0);
		}
		df.setArguments(args);
		df.show(getSupportFragmentManager(), "select_import_type");
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			final File extStorage = Environment.getExternalStorageDirectory();
			final String storagePath = extStorage != null ? extStorage.getAbsolutePath() : "/";
			final FileSelectorDialogFragment f = new FileSelectorDialogFragment();
			final Bundle args = new Bundle();
			args.putString(EXTRA_ACTION, INTENT_ACTION_PICK_FILE);
			args.putString(EXTRA_PATH, storagePath);
			f.setArguments(args);
			f.show(getSupportFragmentManager(), "select_file");
		}
	}

	static class ImportSettingsTask extends AsyncTask<Void, Void, Boolean> {
		private static final String FRAGMENT_TAG = "import_settings_dialog";

		private final DataImportActivity mActivity;
		private final String mPath;
		private final int mFlags;

		ImportSettingsTask(final DataImportActivity activity, final String path, final int flags) {
			mActivity = activity;
			mPath = path;
			mFlags = flags;
		}

		@Override
		protected Boolean doInBackground(final Void... params) {
			if (mPath == null) return false;
			final File file = new File(mPath);
			if (!file.isFile()) return false;
			try {
				DataImportExportUtils.importData(mActivity, file, mFlags);
				return true;
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			final FragmentManager fm = mActivity.getSupportFragmentManager();
			final Fragment f = fm.findFragmentByTag(FRAGMENT_TAG);
			if (f instanceof DialogFragment) {
				((DialogFragment) f).dismiss();
			}
			if (result != null && result) {
				mActivity.setResult(RESULT_OK);
			} else {
				mActivity.setResult(RESULT_CANCELED);
			}
			mActivity.finish();
		}

		@Override
		protected void onPreExecute() {
			ProgressDialogFragment.show(mActivity, FRAGMENT_TAG).setCancelable(false);
		}

	}

	static class OpenImportTypeTask extends AsyncTask<Void, Void, Integer> {

		private static final String FRAGMENT_TAG = "read_settings_data_dialog";

		private final DataImportActivity mActivity;
		private final String mPath;

		OpenImportTypeTask(final DataImportActivity activity, final String path) {
			mActivity = activity;
			mPath = path;
		}

		@Override
		protected Integer doInBackground(final Void... params) {
			if (mPath == null) return 0;
			final File file = new File(mPath);
			if (!file.isFile()) return 0;
			try {
				return DataImportExportUtils.getImportedSettingsFlags(file);
			} catch (final IOException e) {
				return 0;
			}
		}

		@Override
		protected void onPostExecute(final Integer flags) {
			final FragmentManager fm = mActivity.getSupportFragmentManager();
			final Fragment f = fm.findFragmentByTag(FRAGMENT_TAG);
			if (f instanceof DialogFragment) {
				((DialogFragment) f).dismiss();
			}
			final DialogFragment df = new DataExportImportTypeSelectorDialogFragment();
			final Bundle args = new Bundle();
			args.putString(EXTRA_PATH, mPath);
			args.putString(EXTRA_TITLE, mActivity.getString(R.string.import_settings_type_dialog_title));
			if (flags != null) {
				args.putInt(EXTRA_FLAGS, flags);
			} else {
				args.putInt(EXTRA_FLAGS, 0);
			}
			df.setArguments(args);
			df.show(mActivity.getSupportFragmentManager(), "select_import_type");
		}

		@Override
		protected void onPreExecute() {
			ProgressDialogFragment.show(mActivity, FRAGMENT_TAG).setCancelable(false);
		}

	}
}