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

package de.vanita5.twittnuker.adapter

import android.content.Context
import android.database.Cursor
import android.graphics.PorterDuff.Mode
import android.support.v4.widget.SimpleCursorAdapter
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import org.apache.commons.lang3.StringUtils
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.constant.displayProfileImageKey
import de.vanita5.twittnuker.constant.profileImageStyleKey
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.provider.TwidereDataStore.Suggestions
import de.vanita5.twittnuker.util.MediaLoaderWrapper
import de.vanita5.twittnuker.util.SharedPreferencesWrapper
import de.vanita5.twittnuker.util.UserColorNameManager
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import de.vanita5.twittnuker.view.ProfileImageView

import javax.inject.Inject

class ComposeAutoCompleteAdapter(context: Context) : SimpleCursorAdapter(context,
        R.layout.list_item_auto_complete, null, emptyArray(), intArrayOf(), 0) {

    @Inject
    lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    private val displayProfileImage: Boolean
    private val profileImageStyle: Int

    private var mTypeIdx: Int = 0
    private var mIconIdx: Int = 0
    private var mTitleIdx: Int = 0
    private var mSummaryIdx: Int = 0
    private var mExtraIdIdx: Int = 0
    private var mValueIdx: Int = 0
    var accountKey: UserKey? = null
    private var token: Char = ' '

    init {
        GeneralComponentHelper.build(context).inject(this)
        displayProfileImage = preferences[displayProfileImageKey]
        profileImageStyle = preferences[profileImageStyleKey]
    }

    override fun bindView(view: View, context: Context?, cursor: Cursor) {
        val text1 = view.findViewById(android.R.id.text1) as TextView
        val text2 = view.findViewById(android.R.id.text2) as TextView
        val icon = view.findViewById(android.R.id.icon) as ProfileImageView

        icon.style = profileImageStyle

        if (Suggestions.AutoComplete.TYPE_USERS == cursor.getString(mTypeIdx)) {
            text1.text = cursor.getString(mTitleIdx)
            text2.text = String.format("@%s", cursor.getString(mSummaryIdx))
            if (displayProfileImage) {
                val profileImageUrl = cursor.getString(mIconIdx)
                mediaLoader.displayProfileImage(icon, profileImageUrl)
            } else {
                mediaLoader.cancelDisplayTask(icon)
            }

            icon.clearColorFilter()
        } else {
            text1.text = String.format("#%s", cursor.getString(mTitleIdx))
            text2.setText(R.string.hashtag)

            icon.setImageResource(R.drawable.ic_action_hashtag)
            icon.setColorFilter(text1.currentTextColor, Mode.SRC_ATOP)
        }
        icon.visibility = if (displayProfileImage) View.VISIBLE else View.GONE
        super.bindView(view, context, cursor)
    }

    fun closeCursor() {
        val cursor = swapCursor(null) ?: return
        if (!cursor.isClosed) {
            cursor.close()
        }
    }

    override fun convertToString(cursor: Cursor?): CharSequence {
        when (StringUtils.defaultIfEmpty(cursor!!.getString(mTypeIdx), "")) {
            Suggestions.AutoComplete.TYPE_HASHTAGS -> {
                return '#' + cursor.getString(mValueIdx)
            }
            Suggestions.AutoComplete.TYPE_USERS -> {
                return '@' + cursor.getString(mValueIdx)
            }
        }
        return cursor.getString(mValueIdx)
    }

    override fun runQueryOnBackgroundThread(constraint: CharSequence?): Cursor? {
        if (constraint == null || constraint.isEmpty()) return null
        val token = constraint[0]
        if (getNormalizedSymbol(token) == getNormalizedSymbol(this.token)) {
            val filter = filterQueryProvider
            if (filter != null) return filter.runQuery(constraint)
        }
        this.token = token
        val builder = Suggestions.AutoComplete.CONTENT_URI.buildUpon()
        builder.appendQueryParameter(QUERY_PARAM_QUERY, constraint.subSequence(1, constraint.length).toString())
        when (getNormalizedSymbol(token)) {
            '#' -> {
                builder.appendQueryParameter(QUERY_PARAM_TYPE, Suggestions.AutoComplete.TYPE_HASHTAGS)
            }
            '@' -> {
                builder.appendQueryParameter(QUERY_PARAM_TYPE, Suggestions.AutoComplete.TYPE_USERS)
            }
            else -> {
                return null
            }
        }
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        return mContext.contentResolver.query(builder.build(), Suggestions.AutoComplete.COLUMNS,
                null, null, null)
    }

    override fun swapCursor(cursor: Cursor?): Cursor? {
        if (cursor != null) {
            mTypeIdx = cursor.getColumnIndex(Suggestions.AutoComplete.TYPE)
            mTitleIdx = cursor.getColumnIndex(Suggestions.AutoComplete.TITLE)
            mSummaryIdx = cursor.getColumnIndex(Suggestions.AutoComplete.SUMMARY)
            mExtraIdIdx = cursor.getColumnIndex(Suggestions.AutoComplete.EXTRA_ID)
            mIconIdx = cursor.getColumnIndex(Suggestions.AutoComplete.ICON)
            mValueIdx = cursor.getColumnIndex(Suggestions.AutoComplete.VALUE)
        }
        return super.swapCursor(cursor)
    }

    companion object {

        private fun getNormalizedSymbol(character: Char): Char {
            when (character) {
                '\uff20', '@' -> return '@'
                '\uff03', '#' -> return '#'
            }
            return '\u0000'
        }
    }

}