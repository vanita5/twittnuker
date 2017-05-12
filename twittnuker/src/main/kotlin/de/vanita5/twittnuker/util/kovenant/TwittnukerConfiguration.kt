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
@file:JvmName("KovenantTwittnuker")

package de.vanita5.twittnuker.util.kovenant

import android.os.AsyncTask
import android.os.Process
import nl.komponents.kovenant.Dispatcher
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.android.Disposable
import nl.komponents.kovenant.android.androidUiDispatcher
import nl.komponents.kovenant.buildJvmDispatcher
import nl.komponents.kovenant.ui.KovenantUi
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

private val initCount = AtomicInteger(0)
private val disposable = AtomicReference<Disposable>(null)

fun startKovenant() {
    initCount.onlyFirst {
        disposable.set(configureKovenant())
    }
}


@JvmOverloads fun stopKovenant(force: Boolean = false) {
    val dispose = disposable.get()
    if (dispose != null && disposable.compareAndSet(dispose, null)) {
        dispose.close(force)
        initCount.set(0)
    }
}

/**
 * Configures Kovenant for common Android scenarios.
 *
 * @return `Disposable` to properly shutdown Kovenant
 */
fun configureKovenant(): Disposable {
    KovenantUi.uiContext {
        dispatcher = androidUiDispatcher()
    }

    val callbackDispatcher = buildJvmDispatcher {
        name = "kovenant-callback"
        concurrentTasks = 1

        pollStrategy {
            yielding(numberOfPolls = 100)
            blocking()
        }

        threadFactory = createThreadFactory(Process.THREAD_PRIORITY_BACKGROUND)
    }
    val workerDispatcher = AsyncTaskDispatcher(AsyncTask.SERIAL_EXECUTOR)

    Kovenant.context {
        callbackContext {
            dispatcher = callbackDispatcher
        }
        workerContext {
            dispatcher = workerDispatcher
        }
    }
    return DispatchersDisposable(workerDispatcher, callbackDispatcher)
}

private fun createThreadFactory(priority: Int): (Runnable, String, Int) -> Thread = {
    target, dispatcherName, id ->
    val wrapper = Runnable {
        Process.setThreadPriority(priority)
        target.run()
    }
    Thread(wrapper, "$dispatcherName-$id")
}

private inline fun AtomicInteger.onlyFirst(body: () -> Unit) {
    val threadNumber = incrementAndGet()
    if (threadNumber == 1) {
        body()
    } else {
        decrementAndGet()
    }
}

private class DispatchersDisposable(private vararg val dispatcher: Dispatcher) : Disposable {
    override fun close(force: Boolean) {
        dispatcher.forEach {
            close(force, it)
        }
    }

    private fun close(force: Boolean, dispatcher: Dispatcher) {
        try {
            if (force) {
                dispatcher.stop(force = true)
            } else {
                dispatcher.stop(block = true)
            }
        } catch(e: Exception) {
            //ignore, nothing we can do
        }
    }

}

private class AsyncTaskDispatcher(val executor: Executor) : Dispatcher {
    override fun offer(task: () -> Unit): Boolean {
        if (stopped || terminated) return false
        executor.execute(task)
        return true
    }

    override fun tryCancel(task: () -> Unit): Boolean {
        return false
    }

    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        stopped = true
        return emptyList()
    }

    override var stopped: Boolean = false
        private set

    override var terminated: Boolean = false
        private set

}