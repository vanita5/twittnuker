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

package de.vanita5.twittnuker.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;

import java.util.ArrayList;
import java.util.List;

public class ImageUploaderPreference extends DialogPreference implements Constants, OnClickListener {

	private SharedPreferences mPreferences;
	private String[] mAvailableImageUploaders;

	public ImageUploaderPreference(final Context context) {
		this(context, null);
	}

	public ImageUploaderPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ImageUploaderPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences.Editor editor = getEditor();
		if (editor == null) return;
		final String spec = mAvailableImageUploaders[which];
		if (spec != null) {
			editor.putString(KEY_IMAGE_UPLOADER, spec);
			editor.commit();
		}
		dialog.dismiss();
	}

	@Override
	public void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
		mPreferences = getSharedPreferences();
		super.onPrepareDialogBuilder(builder);
		if (mPreferences == null) return;
		final String component = mPreferences.getString(KEY_IMAGE_UPLOADER, null);
		final ArrayList<String> specs = new ArrayList<String>();
		specs.add(getContext().getString(R.string.image_uploader_default));
		mAvailableImageUploaders = specs.toArray(new String[specs.size()]);
		builder.setSingleChoiceItems(mAvailableImageUploaders, getIndex(component), ImageUploaderPreference.this);
		builder.setNegativeButton(android.R.string.cancel, null);
	}

	private int getIndex(final String cls) {
		if (mAvailableImageUploaders == null) return -1;
		if (cls == null) return 0;
		final int count = mAvailableImageUploaders.length;
		for (int i = 0; i < count; i++) {
			final String spec = mAvailableImageUploaders[i];
			if (cls.equals(spec)) return i;
		}
		return -1;
	}

}
