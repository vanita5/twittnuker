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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.activity.DropboxAuthStarterActivity
import de.vanita5.twittnuker.dropboxAuthTokenKey
import de.vanita5.twittnuker.fragment.BaseSupportFragment
import de.vanita5.twittnuker.service.DropboxDataSyncService
import kotlinx.android.synthetic.main.fragment_extra_features_sync_status_play_store.*


class PlayStoreExtraFeaturesSyncStatusFragment : BaseSupportFragment() {
    private val REQUEST_DROPBOX_AUTH: Int = 201

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateButtons()
        connectStorageService.setOnClickListener {
            startActivityForResult(Intent(context, DropboxAuthStarterActivity::class.java), REQUEST_DROPBOX_AUTH)
        }
        performSync.setOnClickListener {
            context.startService(Intent(context, DropboxDataSyncService::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_DROPBOX_AUTH -> {
                updateButtons()
            }
        }
    }

    private fun updateButtons() {
        if (preferences[dropboxAuthTokenKey] == null) {
            connectStorageService.visibility = View.VISIBLE
            performSync.visibility = View.GONE
        } else {
            connectStorageService.visibility = View.GONE
            performSync.visibility = View.VISIBLE

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_extra_features_sync_status_play_store, container, false)
    }
}