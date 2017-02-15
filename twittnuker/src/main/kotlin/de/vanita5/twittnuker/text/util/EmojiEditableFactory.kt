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

package de.vanita5.twittnuker.text.util

import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.widget.TextView
import de.vanita5.twittnuker.text.SafeSpannableStringBuilder

import de.vanita5.twittnuker.util.EmojiSupportUtils
import de.vanita5.twittnuker.util.ExternalThemeManager
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper

import javax.inject.Inject

class EmojiEditableFactory(textView: TextView) : Editable.Factory() {

    @Inject
    lateinit internal var externalThemeManager: ExternalThemeManager

    init {
        GeneralComponentHelper.build(textView.context).inject(this)
    }

    override fun newEditable(source: CharSequence): Editable {
        val editable = SafeSpannableStringBuilder(source)
        EmojiSupportUtils.applyEmoji(externalThemeManager, editable)
        editable.setSpan(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count <= 0) return
                EmojiSupportUtils.applyEmoji(externalThemeManager, editable, start, count)
            }

            override fun afterTextChanged(s: Editable) {

            }
        }, 0, editable.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return editable
    }
}