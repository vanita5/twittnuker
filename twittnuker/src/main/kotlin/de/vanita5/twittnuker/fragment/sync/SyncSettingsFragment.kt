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

package de.vanita5.twittnuker.fragment.sync

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.squareup.otto.Subscribe
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.SYNC_PREFERENCES_NAME
import de.vanita5.twittnuker.constant.dataSyncProviderInfoKey
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.fragment.BaseDialogFragment
import de.vanita5.twittnuker.fragment.BasePreferenceFragment
import de.vanita5.twittnuker.util.TaskServiceRunner
import de.vanita5.twittnuker.util.sync.DataSyncProvider

class SyncSettingsFragment : BasePreferenceFragment() {

    private var syncProvider: DataSyncProvider? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        syncProvider = kPreferences[dataSyncProviderInfoKey]
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SYNC_PREFERENCES_NAME
        addPreferencesFromResource(R.xml.preferences_sync)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sync_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.disconnect -> {
                val df = DisconnectSyncConfirmDialogFragment()
                df.show(childFragmentManager, "disconnect_confirm")
            }
            R.id.sync_now -> {
                val providerInfo = kPreferences[dataSyncProviderInfoKey]!!
                syncController.performSync(providerInfo)
            }
            else -> {
                return false
            }
        }
        return true
    }

    @Subscribe
    fun onSyncFinishedEvent(event: TaskServiceRunner.SyncFinishedEvent) {
        listView?.adapter?.notifyDataSetChanged()
    }

    private fun cleanupAndDisconnect() {
        val providerInfo = kPreferences[dataSyncProviderInfoKey] ?: return
        syncController.cleanupSyncCache(providerInfo)
        kPreferences[dataSyncProviderInfoKey] = null
        DataSyncProvider.Factory.notifyUpdate(context)
        activity?.finish()
    }

    class DisconnectSyncConfirmDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            val providerInfo = kPreferences[dataSyncProviderInfoKey]!!
            val entry = DataSyncProvider.Factory.getProviderEntry(context, providerInfo.type)!!
            builder.setMessage(getString(R.string.message_sync_disconnect_from_name_confirm, entry.name))
            builder.setPositiveButton(R.string.action_sync_disconnect) { _, _ ->
                (parentFragment as SyncSettingsFragment).cleanupAndDisconnect()
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                it as AlertDialog
                it.applyTheme()
            }
            return dialog
        }

    }
}