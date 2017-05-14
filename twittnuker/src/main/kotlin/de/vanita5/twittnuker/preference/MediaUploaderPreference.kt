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

package de.vanita5.twittnuker.preference

import android.content.Context
import android.util.AttributeSet

import de.vanita5.twittnuker.R

import de.vanita5.twittnuker.constant.IntentConstants.INTENT_ACTION_EXTENSION_UPLOAD_MEDIA

class MediaUploaderPreference(context: Context, attrs: AttributeSet? = null) :
        ServicePickerPreference(context, attrs) {

    override val intentAction: String
        get() = INTENT_ACTION_EXTENSION_UPLOAD_MEDIA

    override val noneEntry: String
        get() = context.getString(R.string.media_uploader_default)

}