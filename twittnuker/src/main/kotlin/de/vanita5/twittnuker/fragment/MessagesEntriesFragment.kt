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

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import com.squareup.otto.Subscribe
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.toStringArray
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.MessagesEntriesAdapter
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter
import de.vanita5.twittnuker.constant.newDocumentApiKey
import de.vanita5.twittnuker.extension.model.user
import de.vanita5.twittnuker.loader.ObjectCursorLoader
import de.vanita5.twittnuker.model.ParcelableMessageConversation
import de.vanita5.twittnuker.model.ParcelableMessageConversationCursorIndices
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.GetMessagesTaskEvent
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages.Conversations
import de.vanita5.twittnuker.task.GetMessagesTask
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.ErrorInfoStore
import de.vanita5.twittnuker.util.IntentUtils
import de.vanita5.twittnuker.util.Utils

class MessagesEntriesFragment : AbsContentListRecyclerViewFragment<MessagesEntriesAdapter>(),
        LoaderManager.LoaderCallbacks<List<ParcelableMessageConversation>?>, MessagesEntriesAdapter.MessageConversationClickListener {

    private val accountKeys: Array<UserKey> by lazy {
        Utils.getAccountKeys(context, arguments) ?: DataStoreUtils.getActivatedAccountKeys(context)
    }

    private val errorInfoKey: String = ErrorInfoStore.KEY_DIRECT_MESSAGES

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.listener = this
        adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.END
        loaderManager.initLoader(0, null, this)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableMessageConversation>?> {
        val loader = ObjectCursorLoader(context, ParcelableMessageConversationCursorIndices::class.java)
        loader.uri = Conversations.CONTENT_URI
        loader.selection = Expression.inArgs(Conversations.ACCOUNT_KEY, accountKeys.size).sql
        loader.selectionArgs = accountKeys.toStringArray()
        loader.projection = Conversations.COLUMNS
        loader.sortOrder = OrderBy(Conversations.SORT_ID, false).sql
        return loader
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableMessageConversation>?>?) {
        adapter.conversations = null
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableMessageConversation>?>?, data: List<ParcelableMessageConversation>?) {
        adapter.conversations = data
        adapter.drawAccountColors = accountKeys.size > 1
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
        showContentOrError()
    }

    override fun onCreateAdapter(context: Context): MessagesEntriesAdapter {
        return MessagesEntriesAdapter(context)
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        twitterWrapper.getMessagesAsync(GetMessagesTask.RefreshNewTaskParam(context) {
                this@MessagesEntriesFragment.accountKeys
        })
        return true
    }

    override fun onLoadMoreContents(position: Long) {
        if (position != ILoadMoreSupportAdapter.END) {
            return
        }
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.END)
        twitterWrapper.getMessagesAsync(GetMessagesTask.LoadMoreTaskParam(context) {
            this@MessagesEntriesFragment.accountKeys
        })
    }

    override fun onConversationClick(position: Int) {
        val conversation = adapter.getConversation(position) ?: return
        IntentUtils.openMessageConversation(context, conversation.account_key, conversation.id)
    }

    override fun onProfileImageClick(position: Int) {
        val conversation = adapter.getConversation(position) ?: return
        val user = conversation.user ?: return
        IntentUtils.openUserProfile(context, user, preferences[newDocumentApiKey])
    }

    @Subscribe
    fun onGetMessagesTaskEvent(event: GetMessagesTaskEvent) {
        if (!event.running) {
            refreshing = false
        }
    }

    private fun showContentOrError() {
        val accountKeys = this.accountKeys
        if (adapter.itemCount > 0) {
            showContent()
        } else if (accountKeys.isNotEmpty()) {
            val errorInfo = ErrorInfoStore.getErrorInfo(context,
                    errorInfoStore[errorInfoKey, accountKeys[0]])
            if (errorInfo != null) {
                showEmpty(errorInfo.icon, errorInfo.message)
            } else {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
        } else {
            showError(R.drawable.ic_info_accounts, getString(R.string.message_toast_no_account_selected))
        }
    }
}