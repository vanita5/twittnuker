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

package de.vanita5.twittnuker.util.refresh

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import org.mariotaku.kpreferences.KPreferences
import de.vanita5.twittnuker.annotation.AutoRefreshType
import de.vanita5.twittnuker.constant.refreshIntervalKey
import de.vanita5.twittnuker.service.JobTaskService
import java.util.concurrent.TimeUnit
import android.Manifest.permission as AndroidPermissions


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerAutoRefreshController(
        context: Context,
        kPreferences: KPreferences
) : AutoRefreshController(context, kPreferences) {
    val scheduler: JobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    override fun appStarted() {
        val allJobs = scheduler.allPendingJobs
        AutoRefreshType.ALL.forEach { type ->
            val jobId = JobTaskService.getJobId(type)
            if (allJobs.none { job -> job.id == jobId }) {
                // Start non existing job
                schedule(type)
            }
        }

    }

    override fun schedule(@AutoRefreshType type: String) {
        val jobId = JobTaskService.getJobId(type)
        scheduler.cancel(jobId)
        scheduleJob(jobId)
    }

    override fun unschedule(type: String) {
        val jobId = JobTaskService.getJobId(type)
        scheduler.cancel(jobId)
    }

    fun scheduleJob(jobId: Int, persisted: Boolean = true) {
        val builder = JobInfo.Builder(jobId, ComponentName(context, JobTaskService::class.java))
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        builder.setPeriodic(TimeUnit.MINUTES.toMillis(kPreferences[refreshIntervalKey]))
        builder.setPersisted(persisted)
        try {
            scheduler.schedule(builder.build())
        } catch (e: IllegalArgumentException) {
            if (persisted) {
                scheduleJob(jobId, false)
            }
        }
    }


}