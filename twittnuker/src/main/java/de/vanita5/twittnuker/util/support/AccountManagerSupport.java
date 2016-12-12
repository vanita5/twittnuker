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

package de.vanita5.twittnuker.util.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AccountManagerSupport {
    public static AccountManagerFuture<Bundle> removeAccount(@NonNull AccountManager am,
                                                             @NonNull Account account,
                                                             @Nullable Activity activity,
                                                             @Nullable final AccountManagerCallback<Bundle> callback,
                                                             @Nullable Handler handler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return AccountManagerSupportL.removeAccount(am, account, activity, callback, handler);
        }
        //noinspection deprecation
        final AccountManagerFuture<Boolean> future = am.removeAccount(account, new AccountManagerCallback<Boolean>() {
            @Override
            public void run(AccountManagerFuture<Boolean> future) {
                if (callback != null) {
                    callback.run(new BooleanToBundleAccountManagerFuture(future));
                }
            }
        }, handler);
        return new BooleanToBundleAccountManagerFuture(future);
    }

    private static class AccountManagerSupportL {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
        static AccountManagerFuture<Bundle> removeAccount(AccountManager am, Account account,
                                                          Activity activity,
                                                          AccountManagerCallback<Bundle> callback,
                                                          Handler handler) {
            return am.removeAccount(account, activity, callback, handler);
        }
    }

    private static class BooleanToBundleAccountManagerFuture implements AccountManagerFuture<Bundle> {

        private final AccountManagerFuture<Boolean> future;

        BooleanToBundleAccountManagerFuture(AccountManagerFuture<Boolean> future) {
            this.future = future;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public Bundle getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, future.getResult());
            return result;
        }

        @Override
        public Bundle getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, future.getResult(timeout, unit));
            return result;
        }
    }
}