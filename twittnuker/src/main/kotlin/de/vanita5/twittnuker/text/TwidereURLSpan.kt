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

package de.vanita5.twittnuker.text

import android.text.TextPaint
import android.text.style.URLSpan
import android.view.View
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.TwidereLinkify.OnLinkClickListener

class TwidereURLSpan(
        url: String,
        private val orig: String?,
        private val accountKey: UserKey?,
        private val extraId: Long,
        private val type: Int,
        private val sensitive: Boolean,
        private val highlightStyle: Int,
        private val start: Int,
        private val end: Int,
        private val listener: OnLinkClickListener?
) : URLSpan(url), Constants {

    override fun onClick(widget: View) {
        listener?.onLinkClick(url, orig, accountKey, extraId, type, sensitive, start, end)
    }

    override fun updateDrawState(ds: TextPaint) {
        if (highlightStyle and VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE != 0) {
            ds.isUnderlineText = true
        }
        if (highlightStyle and VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT != 0) {
            ds.color = ds.linkColor
        }
    }
}