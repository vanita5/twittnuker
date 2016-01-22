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

package de.vanita5.twittnuker.activity.support;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.mariotaku.pickncrop.library.ImagePickerActivity;

import de.vanita5.twittnuker.activity.ImageCropperActivity;
import de.vanita5.twittnuker.util.RestFuNetworkStreamDownloader;
import de.vanita5.twittnuker.util.ThemeUtils;

public class ThemedImagePickerActivity extends ImagePickerActivity {

    @Override
    public void setTheme(final int resid) {
        super.setTheme(ThemeUtils.getNoDisplayThemeResource(this));
    }

    public static ThemedIntentBuilder withThemed(Context context) {
        return new ThemedIntentBuilder(context);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    public static final class ThemedIntentBuilder {
        private final Context context;
        private final IntentBuilder intentBuilder;

        public ThemedIntentBuilder(final Context context) {
            this.context = context;
            this.intentBuilder = new IntentBuilder(context);
            intentBuilder.cropImageActivityClass(ImageCropperActivity.class);
            intentBuilder.streamDownloaderClass(RestFuNetworkStreamDownloader.class);
        }

        public ThemedIntentBuilder takePhoto() {
            intentBuilder.takePhoto();
            return this;
        }

        public ThemedIntentBuilder getImage(@NonNull final Uri uri) {
            intentBuilder.getImage(uri);
            return this;
        }

        public Intent build() {
            final Intent intent = intentBuilder.build();
            intent.setClass(context, ThemedImagePickerActivity.class);
            return intent;
        }

        public ThemedIntentBuilder pickImage() {
            intentBuilder.pickImage();
            return this;
        }

        public ThemedIntentBuilder addEntry(final String name, final String value, final int result) {
            intentBuilder.addEntry(name, value, result);
            return this;
        }

        public ThemedIntentBuilder maximumSize(final int w, final int h) {
            intentBuilder.maximumSize(w, h);
            return this;
        }

        public ThemedIntentBuilder aspectRatio(final int x, final int y) {
            intentBuilder.aspectRatio(x, y);
            return this;
        }
    }


}