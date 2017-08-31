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

package de.vanita5.twittnuker.activity

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.StyleRes
import android.support.v4.view.ViewCompat
import android.support.v7.app.TwilightManagerAccessor
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import kotlinx.android.synthetic.main.activity_main.*
import nl.komponents.kovenant.Promise
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonActivity
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.ktextension.componentIcon
import org.mariotaku.ktextension.contains
import org.mariotaku.restfu.http.RestHttpClient
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.SHARED_PREFERENCES_NAME
import de.vanita5.twittnuker.activity.iface.IBaseActivity
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_INTENT
import de.vanita5.twittnuker.constant.lastLaunchTimeKey
import de.vanita5.twittnuker.constant.promotionsEnabledKey
import de.vanita5.twittnuker.constant.themeColorKey
import de.vanita5.twittnuker.constant.themeKey
import de.vanita5.twittnuker.extension.model.hasInvalidAccount
import de.vanita5.twittnuker.extension.model.shouldShow
import de.vanita5.twittnuker.model.presentation.LaunchPresentation
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.task.filter.RefreshLaunchPresentationsTask
import de.vanita5.twittnuker.util.DeviceUtils
import de.vanita5.twittnuker.util.OnLinkClickHandler
import de.vanita5.twittnuker.util.StrictModeUtils
import de.vanita5.twittnuker.util.ThemeUtils
import de.vanita5.twittnuker.util.cache.JsonCache
import de.vanita5.twittnuker.util.dagger.GeneralComponent
import de.vanita5.twittnuker.util.support.ViewSupport
import de.vanita5.twittnuker.util.support.view.ViewOutlineProviderCompat
import de.vanita5.twittnuker.util.theme.getCurrentThemeResource
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class MainActivity : ChameleonActivity(), IBaseActivity<MainActivity> {

    private val handler = Handler(Looper.getMainLooper())
    private val launchLaterRunnable: Runnable = Runnable { launchMain() }
    private val actionHelper = IBaseActivity.ActionHelper(this)

    private var isNightBackup: Int = TwilightManagerAccessor.UNSPECIFIED

    private val themePreferences by lazy {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private val userTheme: Chameleon.Theme by lazy {
        return@lazy ThemeUtils.getUserTheme(this, themePreferences)
    }

    @Inject
    lateinit var restHttpClient: RestHttpClient

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var jsonCache: JsonCache

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy()
            StrictModeUtils.detectAllThreadPolicy()
        }
        val themeColor = themePreferences[themeColorKey]
        val themeResource = getThemeResource(themePreferences, themePreferences[themeKey], themeColor)
        if (themeResource != 0) {
            setTheme(themeResource)
        }
        super.onCreate(savedInstanceState)
        GeneralComponent.get(this).inject(this)
        setContentView(R.layout.activity_main)

        if (!preferences[promotionsEnabledKey]) {
            main.visibility = View.GONE
            launchDirectly()
            return
        }

        main.visibility = View.VISIBLE
        appIcon.setImageDrawable(componentIcon)
        skipPresentation.setOnClickListener {
            launchDirectly()
        }
        if (BuildConfig.DEBUG) {
            skipPresentation.setOnLongClickListener {
                handler.removeCallbacks(launchLaterRunnable)
                return@setOnLongClickListener true
            }
        }
        controlOverlay.setOnClickListener {
            val presentation = controlOverlay.tag as? LaunchPresentation ?: return@setOnClickListener
            val uri = presentation.url?.let(Uri::parse) ?: return@setOnClickListener
            OnLinkClickHandler.openLink(this, preferences, uri)
        }

        ViewCompat.setOnApplyWindowInsetsListener(main) lambda@ { _, insets ->
            main.setPadding(0, 0, 0, insets.systemWindowInsetBottom)

            controlOverlay.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, 0)
            return@lambda insets.consumeSystemWindowInsets()
        }
        ViewSupport.setOutlineProvider(skipPresentation, ViewOutlineProviderCompat.BACKGROUND)

        showPresentationOrLaunch()
    }

    private fun showPresentationOrLaunch() {
        val lastLaunchTime = preferences[lastLaunchTimeKey]
        val maximumDuration = if (BuildConfig.DEBUG) {
            TimeUnit.SECONDS.toMillis(30)
        } else {
            TimeUnit.HOURS.toMillis(6)
        }
        // Show again at least 6 hours later (30 secs for debug builds)
        if (lastLaunchTime >= 0 && System.currentTimeMillis() - lastLaunchTime < maximumDuration) {
            launchDirectly()
            return
        }
        val presentation = jsonCache.getList(RefreshLaunchPresentationsTask.JSON_CACHE_KEY,
                LaunchPresentation::class.java)?.firstOrNull {
            it.shouldShow()
        }
        if (presentation != null) {
            displayPresentation(presentation)
            launchLater()
        } else {
            launchDirectly()
        }
    }

    override fun onPause() {
        actionHelper.dispatchOnPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        updateNightMode()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        actionHelper.dispatchOnResumeFragments()
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (MainActivity) -> Unit): Promise<Unit, Exception> {
        return actionHelper.executeAfterFragmentResumed(useHandler, action)
    }

    override fun getOverrideTheme(): Chameleon.Theme? {
        return userTheme
    }

    @StyleRes
    protected open fun getThemeResource(preferences: SharedPreferences, theme: String, themeColor: Int): Int {
        return getCurrentThemeResource(this, theme)
    }

    private fun updateNightMode() {
        val nightState = TwilightManagerAccessor.getNightState(this)
        if (isNightBackup != TwilightManagerAccessor.UNSPECIFIED && nightState != isNightBackup) {
            recreate()
            return
        }
        isNightBackup = nightState
    }

    private fun displayPresentation(presentation: LaunchPresentation) {
        skipPresentation.visibility = View.VISIBLE
        controlOverlay.tag = presentation
        Glide.with(this).load(presentation.images.first().url)
                .priority(Priority.HIGH)
                .into(presentationView)
    }

    private fun launchDirectly() {
        handler.removeCallbacks(launchLaterRunnable)
        launchMain()
    }

    private fun launchLater() {
        handler.postDelayed(launchLaterRunnable, 5000L)
    }

    private fun launchMain() {
        if (isFinishing) return
        executeAfterFragmentResumed { performLaunch() }
    }

    private fun performLaunch() {
        preferences[lastLaunchTimeKey] = System.currentTimeMillis()
        val am = AccountManager.get(this)
        if (!DeviceUtils.checkCompatibility()) {
            startActivity(Intent(this, IncompatibleAlertActivity::class.java))
        } else if (!AccountUtils.hasAccountPermission(am)) {
            Toast.makeText(this, R.string.message_toast_no_account_permission, Toast.LENGTH_SHORT).show()
        } else if (am.hasInvalidAccount()) {
            val intent = Intent(this, InvalidAccountAlertActivity::class.java)
            intent.putExtra(EXTRA_INTENT, Intent(this, HomeActivity::class.java))
            startActivity(intent)
        } else {
            if (ApplicationInfo.FLAG_EXTERNAL_STORAGE in packageManager.getApplicationInfo(packageName, 0).flags) {
                Toast.makeText(this, R.string.message_toast_internal_storage_install_required, Toast.LENGTH_LONG).show()
            }
            startActivity(Intent(this, HomeActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }

}
