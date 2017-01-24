/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.view

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.support.v7.widget.AppCompatTextView
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.AttributeSet

import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.R

import java.lang.ref.WeakReference

import de.vanita5.twittnuker.util.Utils.formatSameDayTime

class ShortTimeView(
        context: Context,
        attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs, android.R.attr.textViewStyle), Constants {

    private val ticker = TickerRunnable(this)

    var showAbsoluteTime: Boolean = false
        set(value) {
            field = value
            invalidateTime()
        }

    var time: Long = 0
        set(value) {
            field = value
            invalidateTime()
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(ticker)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    private fun invalidateTime() {
        if (showAbsoluteTime) {
            setTextIfChanged(formatSameDayTime(context, time))
        } else {
            val current = System.currentTimeMillis()
            if (Math.abs(current - time) > 60 * 1000) {
                setTextIfChanged(DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL))
            } else {
                setTextIfChanged(context.getString(R.string.just_now))
            }
        }
    }

    private fun setTextIfChanged(text: CharSequence) {
        if (TextUtils.equals(text, getText())) return
        setText(text)
    }

    private class TickerRunnable(view: ShortTimeView) : Runnable {

        private val viewRef = WeakReference(view)

        override fun run() {
            val view = viewRef.get() ?: return
            val handler = view.handler ?: return
            view.invalidateTime()
            val now = SystemClock.uptimeMillis()
            val next = now + TICKER_DURATION - now % TICKER_DURATION
            handler.postAtTime(this, next)
        }
    }

    companion object {

        private const val TICKER_DURATION = 5000L
    }

}