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

package de.vanita5.twittnuker.fragment.filter

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_block_mute_filter_user_confirm.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import org.mariotaku.ktextension.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import de.vanita5.twittnuker.activity.BaseActivity
import de.vanita5.twittnuker.adapter.SelectableUsersAdapter
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.extension.onShow
import de.vanita5.twittnuker.fragment.*
import de.vanita5.twittnuker.loader.iface.IExtendedLoader
import de.vanita5.twittnuker.loader.users.AbsRequestUsersLoader
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.analyzer.PurchaseFinished
import de.vanita5.twittnuker.model.pagination.Pagination
import de.vanita5.twittnuker.util.Analyzer
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import java.lang.ref.WeakReference


abstract class BaseFiltersImportFragment : AbsContentListRecyclerViewFragment<SelectableUsersAdapter>(),
        LoaderManager.LoaderCallbacks<List<ParcelableUser>?> {

    protected var nextPagination: Pagination? = null
        private set
    protected var prevPagination: Pagination? = null
        private set

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderManager.initLoader(0, loaderArgs, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filters_import, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val checkedCount = adapter.checkedCount
        val userCount = adapter.userCount
        menu.setItemAvailability(R.id.select_none, checkedCount > 0)
        menu.setItemAvailability(R.id.select_all, checkedCount < userCount)
        menu.setItemAvailability(R.id.invert_selection, checkedCount in 1 until userCount)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.select_none -> {
                adapter.clearSelection()
                adapter.notifyDataSetChanged()
            }
            R.id.select_all -> {
                for (idx in rangeOfSize(adapter.userStartIndex, adapter.userCount)) {
                    adapter.setItemChecked(idx, true)
                }
                adapter.notifyDataSetChanged()
            }
            R.id.invert_selection -> {
                for (idx in rangeOfSize(adapter.userStartIndex, adapter.userCount)) {
                    adapter.setItemChecked(idx, !adapter.isItemChecked(idx))
                }
                adapter.notifyDataSetChanged()
            }
            R.id.perform_import -> {
                if (adapter.checkedCount == 0) {
                    Toast.makeText(context, R.string.message_toast_no_user_selected, Toast.LENGTH_SHORT).show()
                    return true
                }
                if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_FILTERS_IMPORT)) {
                    ExtraFeaturesIntroductionDialogFragment.show(fragmentManager,
                            feature = ExtraFeaturesService.FEATURE_FILTERS_IMPORT,
                            requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
                    return true
                }
                val df = ImportConfirmDialogFragment()
                df.arguments = Bundle {
                    this[EXTRA_COUNT] = adapter.checkedCount
                }
                df.show(childFragmentManager, "import_confirm")
            }
            else -> {
                return false
            }
        }
        return true
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableUser>?> {
        val fromUser = args.getBoolean(EXTRA_FROM_USER)
        args.remove(EXTRA_FROM_USER)
        return onCreateUsersLoader(context, args, fromUser)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUser>?>) {
        adapter.data = null
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUser>?>, data: List<ParcelableUser>?) {
        val hasMoreData = run {
            val previousCount = adapter.data?.size
            if (previousCount != data?.size) return@run true
            val previousFirst = adapter.data?.firstOrNull()
            val previousLast = adapter.data?.lastOrNull()
            // If first and last data not changed, assume no more data
            return@run previousFirst != data?.firstOrNull() && previousLast != data?.lastOrNull()
        }
        adapter.clearLockedState()
        data?.forEach { user ->
            if (user.is_filtered) {
                adapter.setLockedState(user.key, true)
            }
        }
        adapter.data = data
        if (loader !is IExtendedLoader || loader.fromUser) {
            adapter.loadMoreSupportedPosition = if (hasMoreData) {
                ILoadMoreSupportAdapter.END
            } else {
                ILoadMoreSupportAdapter.NONE
            }
            refreshEnabled = true
        }
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
        showContent()
        refreshEnabled = data.isNullOrEmpty()
        refreshing = false
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
        val cursorLoader = loader as AbsRequestUsersLoader
        nextPagination = cursorLoader.nextPagination
        prevPagination = cursorLoader.prevPagination
        activity.supportInvalidateOptionsMenu()
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (ILoadMoreSupportAdapter.START in position) return
        super.onLoadMoreContents(position)
        if (position == 0L) return
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderArgs.putParcelable(EXTRA_NEXT_PAGINATION, nextPagination)
        loaderManager.restartLoader(0, loaderArgs, this)
    }

    override fun onCreateAdapter(context: Context): SelectableUsersAdapter {
        val adapter = SelectableUsersAdapter(context, Glide.with(this))
        adapter.itemCheckedListener = listener@ { _, _ ->
            if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_FILTERS_IMPORT)) {
                ExtraFeaturesIntroductionDialogFragment.show(fragmentManager,
                        feature = ExtraFeaturesService.FEATURE_FILTERS_IMPORT,
                        requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
                return@listener false
            }
            val count = adapter.checkedCount
            val actionBar = (activity as BaseActivity).supportActionBar
            actionBar?.subtitle = if (count > 0) {
                resources.getQuantityString(R.plurals.Nitems_selected, count, count)
            } else {
                null
            }
            activity.supportInvalidateOptionsMenu()
            return@listener true
        }
        return adapter
    }

    protected abstract fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableUser>?>

    private fun performImport(filterEverywhere: Boolean) {
        val selectedUsers = rangeOfSize(adapter.userStartIndex, adapter.userCount)
                .filter { adapter.isItemChecked(it) }
                .mapNotNull {
                    val user = adapter.getUser(it)
                    // Skip if already filtered
                    if (user.is_filtered) return@mapNotNull null
                    return@mapNotNull user
                }
        selectedUsers.forEach { it.is_filtered = true }
        val weakThis = WeakReference(this)
        executeAfterFragmentResumed {
            ProgressDialogFragment.show(it.childFragmentManager, "import_progress")
        } and task {
            val context = weakThis.get()?.context ?: return@task
            DataStoreUtils.addToFilter(context, selectedUsers, filterEverywhere)
        }.alwaysUi {
            executeAfterFragmentResumed(true) { fragment ->
                fragment.childFragmentManager.dismissDialogFragment("import_progress")
            }
            weakThis.get()?.adapter?.notifyDataSetChanged()
        }
    }

    class ImportConfirmDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val filterEverywhere = (dialog as Dialog).findViewById<CheckBox>(R.id.filterEverywhereToggle).isChecked
                    (parentFragment as BaseFiltersImportFragment).performImport(filterEverywhere)
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.action_add_to_filter)
            builder.setView(R.layout.dialog_block_mute_filter_user_confirm)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.onShow { dialog ->
                dialog.applyTheme()
                val confirmMessageView = dialog.confirmMessage
                val filterEverywhereHelp = dialog.filterEverywhereHelp
                filterEverywhereHelp.setOnClickListener {
                    MessageDialogFragment.show(childFragmentManager, title = getString(R.string.filter_everywhere),
                            message = getString(R.string.filter_everywhere_description), tag = "filter_everywhere_help")
                }
                val usersCount = arguments.getInt(EXTRA_COUNT)
                val nUsers = resources.getQuantityString(R.plurals.N_users, usersCount, usersCount)
                confirmMessageView.text = getString(R.string.filter_user_confirm_message, nUsers)
            }
            return dialog
        }
    }

}