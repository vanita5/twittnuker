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
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.preference.spec.ServiceSpec;

import java.util.ArrayList;

public class MediaUploaderPreference extends DialogPreference implements Constants {

    private final ServiceSpec TWITTER_SERVICE = new ServiceSpec(getContext().getString(R.string.image_uploader_default), null);
	private final ServiceSpec TWIPPLE_SERVICE = new ServiceSpec(getContext().getString(R.string.image_uploader_twipple), SERVICE_UPLOADER_TWIPPLE);

    private final ServiceSpec[] AVAILABLE_SERVICES = { TWITTER_SERVICE, TWIPPLE_SERVICE };

	private SharedPreferences mPreferences;

	private ServiceSpec[] mAvailableImageUploaders;

	public MediaUploaderPreference(final Context context) {
		this(context, null);
	}

	public MediaUploaderPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public MediaUploaderPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

    @Override
    protected View onCreateView(ViewGroup parent) {
        mPreferences = getSharedPreferences();
        final String component = mPreferences.getString(KEY_MEDIA_UPLOADER, null);
        setSummary(getServiceName(component));
        return super.onCreateView(parent);
    }

    @Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences.Editor editor = getEditor();
		if (editor == null) return;
		final ServiceSpec spec = mAvailableImageUploaders[which];
		if (spec != null) {
			editor.putString(KEY_MEDIA_UPLOADER, spec.service);
			editor.commit();
		}
        setSummary(spec.name);
		dialog.dismiss();
	}

	@Override
	public void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
		super.onPrepareDialogBuilder(builder);
		if (mPreferences == null) return;
		final String component = mPreferences.getString(KEY_MEDIA_UPLOADER, null);
		final ArrayList<ServiceSpec> specs = new ArrayList<ServiceSpec>();
        for (ServiceSpec spec : AVAILABLE_SERVICES) {
            specs.add(spec);
        }
		mAvailableImageUploaders = specs.toArray(new ServiceSpec[specs.size()]);
		builder.setSingleChoiceItems(mAvailableImageUploaders, getIndex(component), MediaUploaderPreference.this);
		builder.setNegativeButton(android.R.string.cancel, null);
	}

	private int getIndex(final String cls) {
		if (mAvailableImageUploaders == null) return -1;
		if (cls == null) return 0;
		final int count = mAvailableImageUploaders.length;
		for (int i = 0; i < count; i++) {
			final ServiceSpec spec = mAvailableImageUploaders[i];
			if (cls.equals(spec.service)) return i;
		}
		return -1;
	}

    private String getServiceName(String service) {
        if (service == null) return getContext().getString(R.string.image_uploader_default);
        for (ServiceSpec spec : AVAILABLE_SERVICES) {
            if (service.equals(spec.service)) {
                return spec.name;
            }
        }
        return "";
    }

}
