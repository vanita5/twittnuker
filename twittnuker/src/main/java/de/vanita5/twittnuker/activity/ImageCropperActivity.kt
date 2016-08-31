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

import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.soundcloud.android.crop.CropImageActivity
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.activity.iface.IThemedActivity
import de.vanita5.twittnuker.util.ThemeUtils

class ImageCropperActivity : CropImageActivity(), IThemedActivity {

    // Data fields
    override val currentThemeBackgroundAlpha by lazy { themeBackgroundAlpha }
    override val currentThemeBackgroundOption by lazy { themeBackgroundOption }

    private var mDoneCancelBar: Toolbar? = null

    override fun onContentChanged() {
        super.onContentChanged()
        mDoneCancelBar = findViewById(R.id.done_cancel_bar) as Toolbar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_image_cropper)
    }

    override val themeBackgroundAlpha: Int
        get() = ThemeUtils.getUserThemeBackgroundAlpha(this)

    override val themeBackgroundOption: String
        get() = ThemeUtils.getThemeBackgroundOption(this)

}