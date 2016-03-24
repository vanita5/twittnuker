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

package de.vanita5.twittnuker.task;

import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.lang.ref.WeakReference;

/**
 * Abstract Task class can be used with different implementations
 */
public abstract class AbstractTask<Params, Result, Callback> {

    private Params mParams;
    private WeakReference<Callback> mCallbackRef;

    @WorkerThread
    protected abstract Result doLongOperation(Params params);

    @MainThread
    protected void beforeExecute(Params params) {

    }

    @MainThread
    protected void afterExecute(Params params, Result result) {

    }

    @MainThread
    protected void afterExecute(Callback callback, Params params, Result result) {

    }

    public void setParams(Params params) {
        mParams = params;
    }

    public AbstractTask<Params, Result, Callback> setResultHandler(Callback callback) {
        mCallbackRef = new WeakReference<>(callback);
        return this;
    }

    @MainThread
    public void invokeAfterExecute(Result result) {
        Callback callback = getCallback();
        if (callback != null) {
            afterExecute(callback, mParams, result);
        } else {
            afterExecute(mParams, result);
        }
    }

    @Nullable
    protected Callback getCallback() {
        if (mCallbackRef == null) return null;
        return mCallbackRef.get();
    }

    public Result invokeExecute() {
        return doLongOperation(mParams);
    }

    public void invokeBeforeExecute() {
        beforeExecute(mParams);
    }
}