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
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.TextView
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.layout_list_with_empty_view.*
import okhttp3.HttpUrl
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.empty
import org.mariotaku.ktextension.isEmpty
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACTION
import de.vanita5.twittnuker.constant.IntentConstants.INTENT_PACKAGE_PREFIX
import de.vanita5.twittnuker.extension.model.getComponentLabel
import de.vanita5.twittnuker.extension.model.setupUrl
import de.vanita5.twittnuker.fragment.BaseDialogFragment
import de.vanita5.twittnuker.fragment.BaseFragment
import de.vanita5.twittnuker.fragment.ExtraFeaturesIntroductionDialogFragment
import de.vanita5.twittnuker.fragment.ProgressDialogFragment
import de.vanita5.twittnuker.model.FiltersSubscription
import de.vanita5.twittnuker.model.FiltersSubscriptionCursorIndices
import de.vanita5.twittnuker.model.FiltersSubscriptionValuesCreator
import de.vanita5.twittnuker.model.analyzer.PurchaseFinished
import de.vanita5.twittnuker.provider.TwidereDataStore.Filters
import de.vanita5.twittnuker.task.filter.RefreshFiltersSubscriptionsTask
import de.vanita5.twittnuker.util.Analyzer
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import de.vanita5.twittnuker.util.view.SimpleTextWatcher
import java.lang.ref.WeakReference

class FiltersSubscriptionsFragment : BaseFragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private lateinit var adapter: FilterSubscriptionsAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        adapter = FilterSubscriptionsAdapter(context)
        listView.adapter = adapter

        listContainer.visibility = View.GONE
        progressContainer.visibility = View.VISIBLE
        loaderManager.initLoader(0, null, this)


        if (!extraFeaturesService.isSupported()) {
            activity?.finish()
            return
        }

        if (savedInstanceState == null) {
            when (arguments?.getString(EXTRA_ACTION)) {
                ACTION_ADD_URL_SUBSCRIPTION -> {
                    if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_FILTERS_SUBSCRIPTION)) {
                        val df = ExtraFeaturesIntroductionDialogFragment.show(childFragmentManager,
                                ExtraFeaturesService.FEATURE_FILTERS_SUBSCRIPTION)
                        df.setTargetFragment(this, REQUEST_ADD_URL_SUBSCRIPTION_PURCHASE)
                    } else {
                        showAddUrlSubscription()
                    }
                }
                else -> {
                    if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_FILTERS_SUBSCRIPTION)) {
                        val df = ExtraFeaturesIntroductionDialogFragment.show(childFragmentManager,
                                ExtraFeaturesService.FEATURE_FILTERS_SUBSCRIPTION)
                        df.setTargetFragment(this, REQUEST_PURCHASE_EXTRA_FEATURES)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ADD_URL_SUBSCRIPTION_PURCHASE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                    executeAfterFragmentResumed { fragment ->
                        (fragment as FiltersSubscriptionsFragment).showAddUrlSubscription()
                    }
                } else {
                    activity?.finish()
                }
            }
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                } else {
                    activity?.finish()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filters_subscriptions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                val df = AddUrlSubscriptionDialogFragment()
                df.show(fragmentManager, "add_url_subscription")
                return true
            }
            R.id.refresh -> {
                executeAfterFragmentResumed { fragment ->
                    val dfRef = WeakReference(ProgressDialogFragment.show(fragment.childFragmentManager, "refresh_filters"))
                    val task = RefreshFiltersSubscriptionsTask(fragment.context)
                    val fragmentRef = WeakReference(fragment)
                    task.callback = {
                        fragmentRef.get()?.executeAfterFragmentResumed { fragment ->
                            val df = dfRef.get() ?: fragment.childFragmentManager.findFragmentByTag("refresh_filters") as? DialogFragment
                            df?.dismiss()
                        }
                    }
                    TaskStarter.execute(task)
                }
                return true
            }
            else -> return false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_list_with_empty_view, container, false)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val loader = CursorLoader(context)
        loader.uri = Filters.Subscriptions.CONTENT_URI
        loader.projection = Filters.Subscriptions.COLUMNS
        return loader
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.changeCursor(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        adapter.changeCursor(cursor)
        if (cursor.isEmpty) {
            listView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyIcon.setImageResource(R.drawable.ic_info_info_generic)
            emptyText.setText(R.string.hint_empty_filters_subscriptions)
        } else {
            listView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
        listContainer.visibility = View.VISIBLE
        progressContainer.visibility = View.GONE
    }

    private fun showAddUrlSubscription() {
        val df = AddUrlSubscriptionDialogFragment()
        df.arguments = Bundle {
            this[EXTRA_ADD_SUBSCRIPTION_URL] = arguments?.getString(EXTRA_ADD_SUBSCRIPTION_URL)
            this[EXTRA_ADD_SUBSCRIPTION_NAME] = arguments?.getString(EXTRA_ADD_SUBSCRIPTION_NAME)
        }
        df.show(fragmentManager, "add_url_subscription")
    }

    class FilterSubscriptionsAdapter(context: Context) : SimpleCursorAdapter(context,
            R.layout.list_item_two_line, null, arrayOf(Filters.Subscriptions.NAME),
            intArrayOf(android.R.id.text1), 0) {
        private var indices: FiltersSubscriptionCursorIndices? = null
        private var tempObject: FiltersSubscription = FiltersSubscription()

        override fun swapCursor(c: Cursor?): Cursor? {
            indices = if (c != null) FiltersSubscriptionCursorIndices(c) else null
            return super.swapCursor(c)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            super.bindView(view, context, cursor)
            val indices = this.indices!!
            val iconView = view.findViewById(android.R.id.icon)
            val summaryView = view.findViewById(android.R.id.text2) as TextView

            indices.parseFields(tempObject, cursor)

            iconView.visibility = View.GONE
            summaryView.text = tempObject.getComponentLabel(context)
        }
    }

    class AddUrlSubscriptionDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.dialog_add_filters_subscription)
            builder.setPositiveButton(R.string.action_add_filters_subscription) { dialog, which ->
                dialog as AlertDialog
                val editName = dialog.findViewById(R.id.name) as MaterialEditText
                val editUrl = dialog.findViewById(R.id.url) as MaterialEditText
                val subscription = FiltersSubscription()
                subscription.name = editName.text.toString()
                subscription.setupUrl(editUrl.text.toString())
                context.contentResolver.insert(Filters.Subscriptions.CONTENT_URI, FiltersSubscriptionValuesCreator.create(subscription))
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.setOnShowListener { dialog ->
                dialog as AlertDialog
                val editName = dialog.findViewById(R.id.name) as MaterialEditText
                val editUrl = dialog.findViewById(R.id.url) as MaterialEditText
                val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

                fun updateEnableState() {
                    val nameValid = !editName.empty
                    val urlValid = HttpUrl.parse(editUrl.text.toString()) != null
                    positiveButton.isEnabled = nameValid && urlValid
                }

                val watcher = object : SimpleTextWatcher() {
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        updateEnableState()
                    }
                }
                editName.addTextChangedListener(watcher)
                editUrl.addTextChangedListener(watcher)

                val args = arguments
                if (savedInstanceState == null && args != null) {
                    editName.setText(args.getString(EXTRA_ADD_SUBSCRIPTION_NAME))
                    editUrl.setText(args.getString(EXTRA_ADD_SUBSCRIPTION_URL))
                }

                updateEnableState()
            }
            return dialog
        }
    }

    companion object {
        const val ACTION_ADD_URL_SUBSCRIPTION = "${INTENT_PACKAGE_PREFIX}ADD_URL_FILTERS_SUBSCRIPTION"
        const val REQUEST_ADD_URL_SUBSCRIPTION_PURCHASE = 101
        const val EXTRA_ADD_SUBSCRIPTION_URL = "add_subscription.url"
        const val EXTRA_ADD_SUBSCRIPTION_NAME = "add_subscription.name"
    }
}

