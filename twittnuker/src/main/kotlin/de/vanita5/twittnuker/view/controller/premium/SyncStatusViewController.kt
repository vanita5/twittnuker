/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.view.controller.premium

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import de.vanita5.twittnuker.activity.FragmentContentActivity
import de.vanita5.twittnuker.activity.PremiumDashboardActivity
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_POSITION
import de.vanita5.twittnuker.constant.dataSyncProviderInfoKey
import de.vanita5.twittnuker.fragment.BaseDialogFragment
import de.vanita5.twittnuker.fragment.ExtraFeaturesIntroductionDialogFragment
import de.vanita5.twittnuker.fragment.sync.SyncSettingsFragment
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import de.vanita5.twittnuker.util.sync.SyncProviderInfoFactory


class SyncStatusViewController : PremiumDashboardActivity.ExtraFeatureViewController() {
    override fun onCreate() {
        super.onCreate()
        updateSyncSettingActions()
        titleView.setText(R.string.title_sync)
        button1.setText(R.string.action_sync_connect_to_storage)
        button2.setText(R.string.action_sync_settings)
        button1.setOnClickListener {
            if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                showExtraFeaturesIntroduction()
                return@setOnClickListener
            }
            val df = ConnectNetworkStorageSelectionDialogFragment()
            df.arguments = Bundle { this[EXTRA_POSITION] = position }
            df.show(activity.supportFragmentManager, "connect_to_storage")
        }
        button2.setOnClickListener {
            if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                showExtraFeaturesIntroduction()
                return@setOnClickListener
            }
            val intent = Intent(context, FragmentContentActivity::class.java)
            intent.putExtra(FragmentContentActivity.EXTRA_FRAGMENT, SyncSettingsFragment::class.java.name)
            intent.putExtra(FragmentContentActivity.EXTRA_TITLE, context.getString(R.string.title_sync_settings))
            activity.startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateSyncSettingActions()
    }

    private fun updateSyncSettingActions() {
        val providerInfo = preferences[dataSyncProviderInfoKey]
        if (providerInfo == null) {
            messageView.text = context.getString(R.string.message_sync_data_connect_hint)
            button1.visibility = View.VISIBLE
            button2.visibility = View.GONE

            if (extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                button1.setText(R.string.action_sync_connect_to_storage)
            } else {
                button1.setText(R.string.action_purchase)
            }
        } else {
            val providerEntry = SyncProviderInfoFactory.getProviderEntry(context, providerInfo.type)!!
            messageView.text = context.getString(R.string.message_sync_data_synced_with_name, providerEntry.name)
            button1.visibility = View.GONE
            button2.visibility = View.VISIBLE
        }
    }

    private fun showExtraFeaturesIntroduction() {
        ExtraFeaturesIntroductionDialogFragment.show(activity.supportFragmentManager,
                feature = ExtraFeaturesService.FEATURE_SYNC_DATA,
                requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
    }


    class ConnectNetworkStorageSelectionDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val providers = SyncProviderInfoFactory.getSupportedProviders(context)
            val itemNames = providers.map { it.name }.toTypedArray()

            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.title_dialog_sync_connect_to)
            builder.setItems(itemNames) { dialog, which ->
                val activity = activity as PremiumDashboardActivity
                activity.startActivityForControllerResult(providers[which].authIntent,
                        arguments.getInt(EXTRA_POSITION), REQUEST_CONNECT_NETWORK_STORAGE)
            }
            return builder.create()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONNECT_NETWORK_STORAGE -> {
                updateSyncSettingActions()
            }
        }
    }

    companion object {
        private val REQUEST_CONNECT_NETWORK_STORAGE: Int = 201
    }

}