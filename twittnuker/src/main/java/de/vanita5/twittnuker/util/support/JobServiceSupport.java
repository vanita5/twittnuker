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

package de.vanita5.twittnuker.util.support;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobServiceSupport {
    public static boolean handleStopJob(JobParameters params, boolean reschedule) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Seems fixed after Nougat, ignore!
            return false;
        }
        try {
            final Method getCallbackMethod = JobParameters.class.getDeclaredMethod("getCallback");
            getCallbackMethod.setAccessible(true);
            final Object callback = getCallbackMethod.invoke(params);
            if (callback == null) return false;
            final Class<?> callbackCls = callback.getClass();
            final Method acknowledgeStopMessageMethod = callbackCls.getDeclaredMethod("acknowledgeStopMessage",
                    int.class, boolean.class);
            acknowledgeStopMessageMethod.setAccessible(true);
            // Once method returned true successfully, remove it's callback.
            // Due to Android's Binder implementation, IJobCallback.Stub.asInterface(null) will
            // return null rather than crash
            try {
                acknowledgeStopMessageMethod.invoke(callbackCls, params.getJobId(), reschedule);
                return true;
            } catch (NullPointerException npe) {
                // https://code.google.com/p/android/issues/detail?id=104302
                // Treat as handled
                return true;
            }
        } catch (NoSuchMethodException e) {
            // Framework version mismatch, skip
            return false;
        } catch (IllegalAccessException e) {
            // This shouldn't happen, skip
            return false;
        } catch (InvocationTargetException e) {
            // Internal error, skip
            return false;
        }
    }

    public static boolean removeCallback(JobParameters params) {
        try {
            // Find `callback` field
            final Field callbackField = JobParameters.class.getDeclaredField("callback");
            callbackField.setAccessible(true);
            callbackField.set(params, null);
            return true;
        } catch (NoSuchFieldException e) {
            // Framework version mismatch, skip
            return false;
        } catch (IllegalAccessException e) {
            // This shouldn't happen, skip
            return false;
        }
    }
}