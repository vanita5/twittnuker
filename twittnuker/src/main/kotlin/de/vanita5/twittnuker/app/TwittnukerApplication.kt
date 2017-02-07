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

package de.vanita5.twittnuker.app

import android.app.Application
import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import android.support.multidex.MultiDex
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant
import nl.komponents.kovenant.task
import org.apache.commons.lang3.ArrayUtils
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.mediaviewer.library.MediaDownloader
import org.mariotaku.restfu.http.RestHttpClient
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.constant.apiLastChangeKey
import de.vanita5.twittnuker.constant.bugReportsKey
import de.vanita5.twittnuker.constant.defaultFeatureLastUpdated
import de.vanita5.twittnuker.model.DefaultFeatures
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.content.TwidereSQLiteOpenHelper
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import de.vanita5.twittnuker.util.net.TwidereDns
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import de.vanita5.twittnuker.util.refresh.AutoRefreshController
import de.vanita5.twittnuker.util.sync.SyncController
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TwittnukerApplication : Application(), Constants, OnSharedPreferenceChangeListener {

    @Inject
    lateinit internal var activityTracker: ActivityTracker
    @Inject
    lateinit internal var restHttpClient: RestHttpClient
    @Inject
    lateinit internal var dns: TwidereDns
    @Inject
    lateinit internal var mediaDownloader: MediaDownloader
    @Inject
    lateinit internal var defaultFeatures: DefaultFeatures
    @Inject
    lateinit internal var externalThemeManager: ExternalThemeManager
    @Inject
    lateinit internal var kPreferences: KPreferences
    @Inject
    lateinit internal var autoRefreshController: AutoRefreshController
    @Inject
    lateinit internal var syncController: SyncController
    @Inject
    lateinit internal var extraFeaturesService: ExtraFeaturesService
    @Inject
    lateinit internal var mediaLoader: MediaLoaderWrapper

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }


    val sqLiteDatabase: SQLiteDatabase by lazy {
        StrictModeUtils.checkDiskIO()
        sqLiteOpenHelper.writableDatabase
    }

    val sqLiteOpenHelper: SQLiteOpenHelper by lazy {
        TwidereSQLiteOpenHelper(this, Constants.DATABASES_NAME, Constants.DATABASES_VERSION)
    }

    override fun onCreate() {
        instance = this
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy()
        }
        super.onCreate()
        startKovenant()
        initializeAsyncTask()
        initDebugMode()
        initBugReport()

        GeneralComponentHelper.build(this).inject(this)

        autoRefreshController.appStarted()
        syncController.appStarted()
        extraFeaturesService.appStarted()

        registerActivityLifecycleCallbacks(activityTracker)

        listenExternalThemeChange()

        loadDefaultFeatures()
    }

    private fun loadDefaultFeatures() {
        val lastUpdated = kPreferences[defaultFeatureLastUpdated]
        if (lastUpdated > 0 && TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastUpdated) < 12) {
            return
        }
        task {
            defaultFeatures.loadRemoteSettings(restHttpClient)
        }.success {
            defaultFeatures.save(sharedPreferences)
            DebugLog.d(LOGTAG, "Loaded remote features")
        }.fail {
            DebugLog.w(LOGTAG, "Unable to load remote features", it)
        }.always {
            kPreferences[defaultFeatureLastUpdated] = System.currentTimeMillis()
        }
    }

    private fun listenExternalThemeChange() {
        val packageFilter = IntentFilter()
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val uid = intent.getIntExtra(Intent.EXTRA_UID, -1)
                val packages = packageManager.getPackagesForUid(uid)
                val manager = externalThemeManager
                if (ArrayUtils.contains(packages, manager.emojiPackageName)) {
                    manager.reloadEmojiPreferences()
                }
            }
        }, packageFilter)
    }

    private fun initDebugMode() {
        DebugModeUtils.initForApplication(this)
    }

    private fun initBugReport() {
        if (!sharedPreferences[bugReportsKey]) return
        Analyzer.implementation = ServiceLoader.load(Analyzer::class.java).firstOrNull()
        Analyzer.init(this)
    }

    private val sharedPreferences: SharedPreferences by lazy {
        val prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)
        return@lazy prefs
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onLowMemory() {
        mediaLoader.clearMemoryCache()
        super.onLowMemory()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        when (key) {
            KEY_REFRESH_INTERVAL -> {
                autoRefreshController.rescheduleAll()
            }
            KEY_ENABLE_PROXY, KEY_PROXY_HOST, KEY_PROXY_PORT, KEY_PROXY_TYPE, KEY_PROXY_USERNAME,
            KEY_PROXY_PASSWORD, KEY_CONNECTION_TIMEOUT, KEY_RETRY_ON_NETWORK_ISSUE -> {
                HttpClientFactory.reloadConnectivitySettings(this)
            }
            KEY_DNS_SERVER, KEY_TCP_DNS_QUERY, KEY_BUILTIN_DNS_RESOLVER -> {
                reloadDnsSettings()
            }
            KEY_CONSUMER_KEY, KEY_CONSUMER_SECRET, KEY_API_URL_FORMAT, KEY_CREDENTIALS_TYPE,
            KEY_SAME_OAUTH_SIGNING_URL -> {
                preferences[apiLastChangeKey] = System.currentTimeMillis()
            }
            KEY_EMOJI_SUPPORT -> {
                externalThemeManager.reloadEmojiPreferences()
            }
            KEY_MEDIA_PRELOAD, KEY_PRELOAD_WIFI_ONLY -> {
                mediaLoader.reloadOptions(preferences)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKovenant()
    }

    private fun reloadDnsSettings() {
        dns.reloadDnsSettings()
    }

    private fun initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask::class.java.name)
        } catch (ignore: ClassNotFoundException) {
        }

    }

    companion object {

        private val KEY_KEYBOARD_SHORTCUT_INITIALIZED = "keyboard_shortcut_initialized"
        var instance: TwittnukerApplication? = null
            private set

        fun getInstance(context: Context): TwittnukerApplication {
            return context.applicationContext as TwittnukerApplication
        }
    }
}