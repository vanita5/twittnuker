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

package de.vanita5.twittnuker.fragment

import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_messages_conversation.*
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.MessagesConversationAdapter
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_CONVERSATION_ID
import de.vanita5.twittnuker.loader.ObjectCursorLoader
import de.vanita5.twittnuker.model.ParcelableMessage
import de.vanita5.twittnuker.model.ParcelableMessageCursorIndices
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages

class MessagesConversationFragment : BaseFragment(), LoaderManager.LoaderCallbacks<List<ParcelableMessage>?> {
    private lateinit var adapter: MessagesConversationAdapter

    private val accountKey: UserKey get() = arguments.getParcelable(EXTRA_ACCOUNT_KEY)
    private val conversationId: String get() = arguments.getString(EXTRA_CONVERSATION_ID)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = MessagesConversationAdapter(context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = FixedLinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        loaderManager.initLoader(0, null, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_messages_conversation, container, false)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableMessage>?> {
        val loader = ObjectCursorLoader(context, ParcelableMessageCursorIndices::class.java)
        loader.uri = Messages.CONTENT_URI
        loader.projection = Messages.COLUMNS
        loader.selection = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
        loader.selectionArgs = arrayOf(accountKey.toString(), conversationId)
        loader.sortOrder = OrderBy(Messages.SORT_ID, false).sql
        return loader
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableMessage>?>) {
        adapter.messages = null
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableMessage>?>, data: List<ParcelableMessage>?) {
        adapter.messages = data
    }

}
