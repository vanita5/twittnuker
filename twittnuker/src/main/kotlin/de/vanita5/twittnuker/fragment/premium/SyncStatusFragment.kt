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

package de.vanita5.twittnuker.fragment.premium

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_extra_features_sync_status.*
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import de.vanita5.twittnuker.activity.FragmentContentActivity
import de.vanita5.twittnuker.constant.dataSyncProviderInfoKey
import de.vanita5.twittnuker.fragment.BaseDialogFragment
import de.vanita5.twittnuker.fragment.BaseSupportFragment
import de.vanita5.twittnuker.fragment.ExtraFeaturesIntroductionDialogFragment
import de.vanita5.twittnuker.fragment.sync.SyncSettingsFragment
import de.vanita5.twittnuker.model.analyzer.PurchaseFinished
import de.vanita5.twittnuker.util.Analyzer
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import de.vanita5.twittnuker.util.sync.SyncProviderInfoFactory

class SyncStatusFragment : BaseSupportFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateSyncSettingActions()
        connectButton.setOnClickListener {
            if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                showExtraFeaturesIntroduction()
                return@setOnClickListener
            }
            val df = ConnectNetworkStorageSelectionDialogFragment()
            df.show(childFragmentManager, "connect_to_storage")
        }
        settingsButton.setOnClickListener {
            if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                showExtraFeaturesIntroduction()
                return@setOnClickListener
            }
            val intent = Intent(context, FragmentContentActivity::class.java)
            intent.putExtra(FragmentContentActivity.EXTRA_FRAGMENT, SyncSettingsFragment::class.java.name)
            intent.putExtra(FragmentContentActivity.EXTRA_TITLE, getString(R.string.title_sync_settings))
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONNECT_NETWORK_STORAGE -> {
                updateSyncSettingActions()
            }
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateSyncSettingActions()
    }

    private fun showExtraFeaturesIntroduction() {
        ExtraFeaturesIntroductionDialogFragment.show(childFragmentManager,
                feature = ExtraFeaturesService.FEATURE_SYNC_DATA,
                requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
    }

    private fun updateSyncSettingActions() {
        if (preferences[dataSyncProviderInfoKey] == null) {
            statusText.text = getText(R.string.message_sync_data_connect_hint)
            connectButton.visibility = View.VISIBLE
            settingsButton.visibility = View.GONE
        } else {
            statusText.text = getString(R.string.message_sync_data_synced_with_name, "Dropbox")
            connectButton.visibility = View.GONE
            settingsButton.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_extra_features_sync_status, container, false)
    }

    class ConnectNetworkStorageSelectionDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val providers = SyncProviderInfoFactory.getSupportedProviders(context)
            val itemNames = providers.map { it.name }.toTypedArray()

            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.title_dialog_sync_connect_to)
            builder.setItems(itemNames) { dialog, which ->
                activity.startActivityForResult(providers[which].authIntent, REQUEST_CONNECT_NETWORK_STORAGE)
            }
            return builder.create()
        }
    }

    companion object {
        private val REQUEST_CONNECT_NETWORK_STORAGE: Int = 201
    }

}