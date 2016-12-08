/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.service

import android.app.Service
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.os.IBinder
import org.mariotaku.ktextension.convert
import de.vanita5.twittnuker.IDataSyncService
import de.vanita5.twittnuker.activity.DropboxAuthStarterActivity
import de.vanita5.twittnuker.model.SyncAuthInfo
import de.vanita5.twittnuker.util.JsonSerializer
import java.lang.ref.WeakReference

class DropboxDataSyncService : Service() {
    private val serviceInterface: ServiceInterface

    init {
        serviceInterface = ServiceInterface(WeakReference(this))
    }

    override fun onBind(intent: Intent?): IBinder {
        return serviceInterface.asBinder()
    }

    private fun getAuthInfo(): SyncAuthInfo? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getAuthRequestIntent(info: SyncAuthInfo?): Intent {
        return Intent(this, DropboxAuthStarterActivity::class.java)
    }

    private fun onPerformSync(info: SyncAuthInfo, extras: Bundle?, syncResult: SyncResult) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    internal class ServiceInterface(val service: WeakReference<DropboxDataSyncService>) : IDataSyncService.Stub() {

        override fun getAuthInfo(): String? {
            val info = service.get().getAuthInfo() ?: return null
            return JsonSerializer.serialize(info)
        }

        override fun getAuthRequestIntent(infoJson: String?): Intent {
            val info = infoJson?.convert { JsonSerializer.parse(infoJson, SyncAuthInfo::class.java) }
            return service.get().getAuthRequestIntent(info)
        }

        override fun onPerformSync(infoJson: String, extras: Bundle?, syncResult: SyncResult) {
            val info = infoJson.convert { JsonSerializer.parse(infoJson, SyncAuthInfo::class.java) }!!
            service.get().onPerformSync(info, extras, syncResult)
        }

    }

}
