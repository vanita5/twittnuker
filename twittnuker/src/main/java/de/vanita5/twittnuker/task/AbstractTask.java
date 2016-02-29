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
import android.support.annotation.WorkerThread;

public abstract class AbstractTask<Params, Result, Callback> {

    private Params mParams;
    private Callback mCallback;

    @WorkerThread
    protected abstract Result doLongOperation(Params params);

    @MainThread
    protected void beforeExecute() {

    }

    @MainThread
    protected void afterExecute(Result result) {

    }

    @MainThread
    protected void afterExecute(Callback callback, Result result) {

    }

    public void setParams(Params params) {
        mParams = params;
    }

    public AbstractTask<Params, Result, Callback> setResultHandler(Callback callback) {
        mCallback = callback;
        return this;
    }

    @MainThread
    public void invokeAfterExecute(Result result) {
        if (mCallback != null) {
            afterExecute(mCallback, result);
        } else {
            afterExecute(result);
        }
    }

    public Result invokeExecute() {
        return doLongOperation(mParams);
    }

    public void invokeBeforeExecute() {
        beforeExecute();
    }
}