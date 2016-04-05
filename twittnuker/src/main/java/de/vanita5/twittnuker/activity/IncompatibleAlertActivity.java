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

package de.vanita5.twittnuker.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.R;

import java.util.Locale;

public class IncompatibleAlertActivity extends Activity {
    private TextView mInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_incompatible);

        mInfoText.append(String.format(Locale.US, "Twidere version %s (%d)\n",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        mInfoText.append(String.format(Locale.US, "Classpath %s\n", ClassLoader.getSystemClassLoader()));
        mInfoText.append(String.format(Locale.US, "Brand %s\n", Build.BRAND));
        mInfoText.append(String.format(Locale.US, "Device %s\n", Build.DEVICE));
        mInfoText.append(String.format(Locale.US, "Display %s\n", Build.DISPLAY));
        mInfoText.append(String.format(Locale.US, "Hardware %s\n", Build.HARDWARE));
        mInfoText.append(String.format(Locale.US, "Manufacturer %s\n", Build.MANUFACTURER));
        mInfoText.append(String.format(Locale.US, "Model %s\n", Build.MODEL));
        mInfoText.append(String.format(Locale.US, "Product %s\n", Build.PRODUCT));
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mInfoText = (TextView) findViewById(R.id.info_text);
    }
}