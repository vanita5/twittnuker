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

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_draggable_list_with_empty_view.*
import nl.komponents.kovenant.task
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.Constants.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_AUTH_TOKEN_TYPE
import de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_TYPE
import de.vanita5.twittnuker.activity.ColorPickerDialogActivity
import de.vanita5.twittnuker.adapter.AccountDetailsAdapter
import de.vanita5.twittnuker.annotation.Referral
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import de.vanita5.twittnuker.constant.newDocumentApiKey
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.extension.model.getAccountKey
import de.vanita5.twittnuker.extension.model.setActivated
import de.vanita5.twittnuker.extension.model.setColor
import de.vanita5.twittnuker.extension.model.setPosition
import de.vanita5.twittnuker.extension.onShow
import de.vanita5.twittnuker.loader.AccountDetailsLoader
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.AccountPreferences
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.IntentUtils
import de.vanita5.twittnuker.util.deleteAccountData
import de.vanita5.twittnuker.util.support.removeAccountSupport

/**
 * Sort and toggle account availability
 */
class AccountsManagerFragment : BaseFragment(), LoaderManager.LoaderCallbacks<List<AccountDetails>>,
        AdapterView.OnItemClickListener {

    private lateinit var adapter: AccountDetailsAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        val am = AccountManager.get(context)
        adapter = AccountDetailsAdapter(context, Glide.with(this)).apply {
            sortEnabled = true
            switchEnabled = true
            accountToggleListener = { pos, checked ->
                val item = getItem(pos)
                item.activated = checked
                item.account.setActivated(am, checked)
            }
        }
        listView.adapter = adapter
        listView.isDragEnabled = true
        listView.onItemClickListener = this
        listView.setDropListener { from, to ->
            adapter.drop(from, to)
            for (i in 0 until adapter.count) {
                val item = adapter.getItem(i)
                item.account.setActivated(am, item.activated)
                item.account.setPosition(am, i)
            }
        }
        listView.setOnCreateContextMenuListener(this)
        listView.emptyView = emptyView
        emptyText.setText(R.string.message_toast_no_account)
        emptyIcon.setImageResource(R.drawable.ic_info_error_generic)
        setListShown(false)

        loaderManager.initLoader(0, null, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SET_COLOR -> {
                if (resultCode != Activity.RESULT_OK || data == null)
                    return
                val am = AccountManager.get(context)
                val accountKey: UserKey = data.getBundleExtra(EXTRA_EXTRAS).getParcelable(EXTRA_ACCOUNT_KEY) ?: return
                val color = data.getIntExtra(EXTRA_COLOR, Color.WHITE)
                val details = adapter.findItem(accountKey) ?: return
                details.color = color
                details.account.setColor(am, color)
                val resolver = context.contentResolver
                task {
                    updateContentsColor(resolver, details)
                }
                adapter.notifyDataSetChanged()
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_account -> {
                AccountManager.get(context).addAccount(ACCOUNT_TYPE, ACCOUNT_AUTH_TOKEN_TYPE,
                        null, null, activity, null, null)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_accounts_manager, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo as? AdapterContextMenuInfo ?: return false
        val details = adapter.getItem(menuInfo.position) ?: return false
        when (item.itemId) {
            R.id.set_color -> {
                val intent = Intent(activity, ColorPickerDialogActivity::class.java)
                intent.putExtra(EXTRA_COLOR, details.color)
                intent.putExtra(EXTRA_EXTRAS, Bundle {
                    this[EXTRA_ACCOUNT_KEY] = details.key
                })
                intent.putExtra(EXTRA_ALPHA_SLIDER, false)
                startActivityForResult(intent, REQUEST_SET_COLOR)
            }
            R.id.delete -> {
                val f = AccountDeletionDialogFragment()
                val args = Bundle()
                args.putParcelable(EXTRA_ACCOUNT, details.account)
                f.arguments = args
                f.show(childFragmentManager, FRAGMENT_TAG_ACCOUNT_DELETION)
            }
        }
        return false
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val account = adapter.getItem(position)
        IntentUtils.openUserProfile(context, account.user, preferences[newDocumentApiKey],
                Referral.SELF_PROFILE, null)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<AccountDetails>> {
        return AccountDetailsLoader(context)
    }

    override fun onLoaderReset(loader: Loader<List<AccountDetails>>) {

    }

    override fun onLoadFinished(loader: Loader<List<AccountDetails>>, data: List<AccountDetails>) {
        adapter.apply {
            clear()
            addAll(data)
        }
        setListShown(true)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        if (menuInfo !is AdapterContextMenuInfo) return
        val account = adapter.getItem(menuInfo.position)!!
        menu.setHeaderTitle(account.user.name)
        val inflater = MenuInflater(v.context)
        inflater.inflate(R.menu.action_manager_account, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_draggable_list_with_empty_view, container, false)
    }

    private fun setListShown(shown: Boolean) {
        listContainer.visibility = if (shown) View.VISIBLE else View.GONE
        progressContainer.visibility = if (shown) View.GONE else View.VISIBLE
    }

    private fun updateContentsColor(resolver: ContentResolver, details: AccountDetails) {
        val statusValues = ContentValues().apply {
            put(Statuses.ACCOUNT_COLOR, details.color)
        }
        val statusesWhere = Expression.equalsArgs(Statuses.ACCOUNT_KEY)
        val statusesWhereArgs = arrayOf(details.key.toString())

        DataStoreUtils.STATUSES_ACTIVITIES_URIS.forEach { uri ->
            resolver.update(uri, statusValues, statusesWhere.sql, statusesWhereArgs)
        }
    }

    /**
     * DELETE YOUR ACCOUNT
     */
    class AccountDeletionDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            val account: Account = arguments.getParcelable(EXTRA_ACCOUNT)
            val resolver = context.contentResolver
            val am = AccountManager.get(context)
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val accountKey = account.getAccountKey(am)
                    resolver.deleteAccountData(accountKey)
                    AccountPreferences.getSharedPreferencesForAccount(context, accountKey).edit()
                            .clear().apply()
                    am.removeAccountSupport(account)
                }
            }
        }


        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = context
            val builder = AlertDialog.Builder(context)
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setTitle(R.string.title_account_delete_confirm)
            builder.setMessage(R.string.message_account_delete_confirm)
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

    }

    companion object {

        private val FRAGMENT_TAG_ACCOUNT_DELETION = "account_deletion"
    }
}