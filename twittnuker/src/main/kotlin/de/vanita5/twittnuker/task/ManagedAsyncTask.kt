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

package de.vanita5.twittnuker.task

import android.content.Context
import android.os.AsyncTask

import com.squareup.otto.Bus

import de.vanita5.twittnuker.model.event.TaskStateChangedEvent
import de.vanita5.twittnuker.util.AsyncTaskManager
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.SharedPreferencesWrapper
import de.vanita5.twittnuker.util.UserColorNameManager
import de.vanita5.twittnuker.util.dagger.GeneralComponent

import javax.inject.Inject

abstract class ManagedAsyncTask<Params, Progress, Result> @JvmOverloads constructor(
        val context: Context,
        val tag: String? = null
) : AsyncTask<Params, Progress, Result>() {

    @Inject
    lateinit var manager: AsyncTaskManager
    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var asyncTwitterWrapper: AsyncTwitterWrapper

    init {
        @Suppress("UNCHECKED_CAST")
        GeneralComponent.get(context).inject(this as ManagedAsyncTask<Any, Any, Any>)
    }

    override fun onCancelled() {
        super.onCancelled()
        bus.post(TaskStateChangedEvent())
    }

    override fun onPostExecute(result: Result) {
        super.onPostExecute(result)
        bus.post(TaskStateChangedEvent())
    }

    override fun onPreExecute() {
        super.onPreExecute()
        bus.post(TaskStateChangedEvent())
    }

    protected fun finalize() {
        manager.remove(hashCode())
    }

}