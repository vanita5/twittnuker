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

package de.vanita5.twittnuker.extension.model

import android.text.Spannable
import android.text.Spanned
import android.text.style.URLSpan
import de.vanita5.twittnuker.model.SpanItem
import de.vanita5.twittnuker.text.ZeroWidthSpan

val SpanItem.length: Int get() = end - start

fun Array<SpanItem>.applyTo(spannable: Spannable) {
    forEach { span ->
        when (span.type) {
            SpanItem.SpanType.HIDE -> {
                spannable.setSpan(ZeroWidthSpan(), span.start, span.end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                spannable.setSpan(URLSpan(span.link), span.start, span.end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}