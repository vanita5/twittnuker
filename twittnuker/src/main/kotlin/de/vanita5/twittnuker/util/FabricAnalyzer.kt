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

package de.vanita5.twittnuker.util

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.Application
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.AnswersEvent
import com.crashlytics.android.answers.LoginEvent
import com.crashlytics.android.answers.SearchEvent
import io.fabric.sdk.android.Fabric
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.configure
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_TYPE
import de.vanita5.twittnuker.model.analyzer.Search
import de.vanita5.twittnuker.model.analyzer.SignIn

class FabricAnalyzer : Analyzer(), Constants {

    override fun log(priority: Int, tag: String, msg: String) {
        Crashlytics.log(priority, tag, msg)
    }

    override fun logException(throwable: Throwable) {
        Crashlytics.logException(throwable)
    }

    override fun log(event: Event) {
        val answers = Answers.getInstance()
        when (event) {
            is SignIn -> {
                answers.logLogin(configure(LoginEvent()) {
                    putMethod(event.type)
                    putSuccess(event.success)
                    putAttributes(event)
                })
            }
            is Search -> {
                answers.logSearch(configure(SearchEvent()) {
                    putQuery(event.query)
                    putAttributes(event)
                })
            }
        }
    }

    override fun init(application: Application) {
        Fabric.with(application, Crashlytics())
        Crashlytics.setBool("debug", BuildConfig.DEBUG)
        Crashlytics.setString("build.brand", Build.BRAND)
        Crashlytics.setString("build.device", Build.DEVICE)
        Crashlytics.setString("build.display", Build.DISPLAY)
        Crashlytics.setString("build.hardware", Build.HARDWARE)
        Crashlytics.setString("build.manufacturer", Build.MANUFACTURER)
        Crashlytics.setString("build.model", Build.MODEL)
        Crashlytics.setString("build.product", Build.PRODUCT)
        val am = AccountManager.get(application)
        am.addOnAccountsUpdatedListenerSafe(OnAccountsUpdateListener { accounts ->
            Crashlytics.setString("twidere.accounts", accounts.filter { it.type == ACCOUNT_TYPE }
                    .joinToString(transform = Account::name))
        }, updateImmediately = true)
    }

    private fun AnswersEvent<*>.putAttributes(event: Analyzer.Event) {
        if (event.account != null) {
            putCustomAttribute("account", event.account)
        }
    }
}