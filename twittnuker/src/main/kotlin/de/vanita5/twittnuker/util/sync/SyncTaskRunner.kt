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

package de.vanita5.twittnuker.util.sync

import android.content.Context
import com.squareup.otto.Bus
import nl.komponents.kovenant.task
import de.vanita5.twittnuker.util.TaskServiceRunner
import de.vanita5.twittnuker.util.UserColorNameManager
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject


abstract class SyncTaskRunner(val context: Context) {
    @Inject
    protected lateinit var userColorNameManager: UserColorNameManager
    @Inject
    protected lateinit var bus: Bus
    @Inject
    protected lateinit var syncPreferences: SyncPreferences

    init {
        @Suppress("LeakingThis")
        GeneralComponentHelper.build(context).inject(this)
    }

    /**
     * @param action Action of `TaskServiceRunner.Action`
     * @param callback When task finished, true if task finished successfully
     * @return True if task actually executed, false otherwise
     */
    protected abstract fun onRunningTask(action: String, callback: ((Boolean) -> Unit)): Boolean

    fun runTask(action: String, callback: ((Boolean) -> Unit)? = null): Boolean {
        val syncType = SyncTaskRunner.getSyncType(action) ?: return false
        if (!syncPreferences.isSyncEnabled(syncType)) return false
        return onRunningTask(action) { success ->
            callback?.invoke(success)
            if (success) {
                syncPreferences.setLastSynced(syncType, System.currentTimeMillis())
            }
            bus.post(TaskServiceRunner.SyncFinishedEvent(syncType, success))
        }
    }


    fun cleanupSyncCache() {
        task {
            context.syncDataDir.listFiles { file, name -> file.isFile }?.forEach { file ->
                file.delete()
            }
        }
    }

    fun performSync() {
        val actions = TaskServiceRunner.ACTIONS_SYNC.toCollection(LinkedList())
        val runnable = object : Runnable {
            override fun run() {
                val action = actions.poll() ?: return
                runTask(action) {
                    this.run()
                }
            }
        }
        runnable.run()
    }

    companion object {
        const val SYNC_TYPE_DRAFTS = "drafts"
        const val SYNC_TYPE_FILTERS = "filters"
        const val SYNC_TYPE_USER_COLORS = "user_colors"

        @JvmStatic
        fun getSyncType(action: String): String? {
            return when (action) {
                TaskServiceRunner.ACTION_SYNC_DRAFTS -> SYNC_TYPE_DRAFTS
                TaskServiceRunner.ACTION_SYNC_FILTERS -> SYNC_TYPE_FILTERS
                TaskServiceRunner.ACTION_SYNC_USER_COLORS -> SYNC_TYPE_USER_COLORS
                else -> null
            }
        }
    }
}