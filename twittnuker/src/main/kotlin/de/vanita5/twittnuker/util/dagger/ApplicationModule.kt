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

package de.vanita5.twittnuker.util.dagger

import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Looper
import android.support.v4.net.ConnectivityManagerCompat
import android.support.v4.text.BidiFormatter
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import com.twitter.Extractor
import com.twitter.Validator
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.mediaviewer.library.FileCache
import org.mariotaku.mediaviewer.library.MediaDownloader
import org.mariotaku.restfu.http.RestHttpClient
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.constant.SharedPreferenceConstants
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.KEY_CACHE_SIZE_LIMIT
import de.vanita5.twittnuker.constant.autoRefreshCompatibilityModeKey
import de.vanita5.twittnuker.model.DefaultFeatures
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.cache.DiskLRUFileCache
import de.vanita5.twittnuker.util.cache.JsonCache
import de.vanita5.twittnuker.util.gifshare.GifShareProvider
import de.vanita5.twittnuker.util.media.MediaPreloader
import de.vanita5.twittnuker.util.media.TwidereMediaDownloader
import de.vanita5.twittnuker.util.net.TwidereDns
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import de.vanita5.twittnuker.util.refresh.AutoRefreshController
import de.vanita5.twittnuker.util.refresh.JobSchedulerAutoRefreshController
import de.vanita5.twittnuker.util.refresh.LegacyAutoRefreshController
import de.vanita5.twittnuker.util.schedule.StatusScheduleProvider
import de.vanita5.twittnuker.util.sync.JobSchedulerSyncController
import de.vanita5.twittnuker.util.sync.LegacySyncController
import de.vanita5.twittnuker.util.sync.SyncController
import de.vanita5.twittnuker.util.sync.SyncPreferences
import java.io.File
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Application) {

    init {
        if (Thread.currentThread() !== Looper.getMainLooper().thread) {
            throw RuntimeException("Module must be created inside main thread")
        }
    }

    @Provides
    @Singleton
    fun keyboardShortcutsHandler(): KeyboardShortcutsHandler {
        return KeyboardShortcutsHandler(application)
    }

    @Provides
    @Singleton
    fun externalThemeManager(preferences: SharedPreferencesWrapper): ExternalThemeManager {
        return ExternalThemeManager(application, preferences)
    }

    @Provides
    @Singleton
    fun notificationManagerWrapper(): NotificationManagerWrapper {
        return NotificationManagerWrapper(application)
    }

    @Provides
    @Singleton
    fun sharedPreferences(): SharedPreferencesWrapper {
        return SharedPreferencesWrapper.getInstance(application, Constants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE, SharedPreferenceConstants::class.java)
    }

    @Provides
    @Singleton
    fun kPreferences(sharedPreferences: SharedPreferencesWrapper): KPreferences {
        return KPreferences(sharedPreferences)
    }

    @Provides
    @Singleton
    fun userColorNameManager(): UserColorNameManager {
        return UserColorNameManager(application)
    }

    @Provides
    @Singleton
    fun multiSelectManager(): MultiSelectManager {
        return MultiSelectManager()
    }

    @Provides
    @Singleton
    fun restHttpClient(prefs: SharedPreferencesWrapper, dns: Dns,
            connectionPool: ConnectionPool, cache: Cache): RestHttpClient {
        val conf = HttpClientFactory.HttpClientConfiguration(prefs)
        return HttpClientFactory.createRestHttpClient(conf, dns, connectionPool, cache)
    }

    @Provides
    @Singleton
    fun connectionPool(): ConnectionPool {
        return ConnectionPool()
    }

    @Provides
    @Singleton
    fun bus(): Bus {
        return Bus(ThreadEnforcer.MAIN)
    }

    @Provides
    @Singleton
    fun asyncTaskManager(): AsyncTaskManager {
        return AsyncTaskManager()
    }

    @Provides
    @Singleton
    fun activityTracker(): ActivityTracker {
        return ActivityTracker()
    }

    @Provides
    @Singleton
    fun asyncTwitterWrapper(bus: Bus, preferences: SharedPreferencesWrapper,
            asyncTaskManager: AsyncTaskManager, notificationManagerWrapper: NotificationManagerWrapper): AsyncTwitterWrapper {
        return AsyncTwitterWrapper(application, bus, preferences, asyncTaskManager, notificationManagerWrapper)
    }

    @Provides
    @Singleton
    fun readStateManager(): ReadStateManager {
        return ReadStateManager(application)
    }

    @Provides
    @Singleton
    fun contentNotificationManager(activityTracker: ActivityTracker, userColorNameManager: UserColorNameManager,
            notificationManagerWrapper: NotificationManagerWrapper, preferences: SharedPreferencesWrapper): ContentNotificationManager {
        return ContentNotificationManager(application, activityTracker, userColorNameManager, notificationManagerWrapper, preferences)
    }

    @Provides
    @Singleton
    fun mediaLoaderWrapper(preferences: SharedPreferencesWrapper): MediaPreloader {
        val preloader = MediaPreloader(application)
        preloader.reloadOptions(preferences)
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        preloader.isNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(cm)
        return preloader
    }

    @Provides
    @Singleton
    fun dns(preferences: SharedPreferencesWrapper): Dns {
        return TwidereDns(application, preferences)
    }

    @Provides
    @Singleton
    fun mediaDownloader(client: RestHttpClient): MediaDownloader {
        return TwidereMediaDownloader(application, client)
    }

    @Provides
    @Singleton
    fun validator(): Validator {
        return Validator()
    }

    @Provides
    @Singleton
    fun extractor(): Extractor {
        return Extractor()
    }

    @Provides
    @Singleton
    fun errorInfoStore(): ErrorInfoStore {
        return ErrorInfoStore(application)
    }

    @Provides
    fun provideBidiFormatter(): BidiFormatter {
        return BidiFormatter.getInstance()
    }

    @Provides
    @Singleton
    fun autoRefreshController(kPreferences: KPreferences): AutoRefreshController {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !kPreferences[autoRefreshCompatibilityModeKey]) {
            return JobSchedulerAutoRefreshController(application, kPreferences)
        }
        return LegacyAutoRefreshController(application, kPreferences)
    }

    @Provides
    @Singleton
    fun syncController(): SyncController {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return JobSchedulerSyncController(application)
        }
        return LegacySyncController(application)
    }

    @Provides
    @Singleton
    fun statusScheduleProviderFactory(): StatusScheduleProvider.Factory {
        return StatusScheduleProvider.Factory.instance
    }

    @Provides
    @Singleton
    fun gifShareProviderFactory(): GifShareProvider.Factory {
        return GifShareProvider.Factory.instance
    }

    @Provides
    @Singleton
    fun syncPreferences(): SyncPreferences {
        return SyncPreferences(application)
    }

    @Provides
    @Singleton
    fun taskCreator(kPreferences: KPreferences, bus: Bus): TaskServiceRunner {
        return TaskServiceRunner(application, kPreferences, bus)
    }

    @Provides
    @Singleton
    fun defaultFeatures(preferences: SharedPreferencesWrapper): DefaultFeatures {
        val features = DefaultFeatures()
        features.load(preferences)
        return features
    }

    @Provides
    @Singleton
    fun extraFeaturesService(): ExtraFeaturesService {
        return ExtraFeaturesService.newInstance(application)
    }

    @Provides
    @Singleton
    fun etagCache(): ETagCache {
        return ETagCache(application)
    }

    @Provides
    fun locationManager(): LocationManager {
        return application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Provides
    fun connectivityManager(): ConnectivityManager {
        return application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    fun okHttpClient(preferences: SharedPreferencesWrapper, dns: Dns, connectionPool: ConnectionPool,
            cache: Cache): OkHttpClient {
        val conf = HttpClientFactory.HttpClientConfiguration(preferences)
        val builder = OkHttpClient.Builder()
        HttpClientFactory.initOkHttpClient(conf, builder, dns, connectionPool, cache)
        return builder.build()
    }

    @Provides
    @Singleton
    fun dataSourceFactory(preferences: SharedPreferencesWrapper, dns: Dns, connectionPool: ConnectionPool,
            cache: Cache): DataSource.Factory {
        val conf = HttpClientFactory.HttpClientConfiguration(preferences)
        val builder = OkHttpClient.Builder()
        HttpClientFactory.initOkHttpClient(conf, builder, dns, connectionPool, cache)
        val userAgent = UserAgentUtils.getDefaultUserAgentStringSafe(application)
        return OkHttpDataSourceFactory(builder.build(), userAgent, null)
    }

    @Provides
    @Singleton
    fun cache(preferences: SharedPreferencesWrapper): Cache {
        val cacheSizeMB = preferences.getInt(KEY_CACHE_SIZE_LIMIT, 300).coerceIn(100..500)
        // Convert to bytes
        return Cache(getCacheDir("network", cacheSizeMB * 1048576L), cacheSizeMB * 1048576L)
    }

    @Provides
    @Singleton
    fun extractorsFactory(): ExtractorsFactory {
        return DefaultExtractorsFactory()
    }

    @Provides
    @Singleton
    fun jsonCache(): JsonCache {
        return JsonCache(getCacheDir("json", 100 * 1048576L))
    }

    @Provides
    @Singleton
    fun fileCache(): FileCache {
        return DiskLRUFileCache(getCacheDir("media", 100 * 1048576L))
    }

    private fun getCacheDir(dirName: String, sizeInBytes: Long): File {
        return Utils.getExternalCacheDir(application, dirName, sizeInBytes) ?:
                Utils.getInternalCacheDir(application, dirName)
    }

    companion object {

        private var sApplicationModule: ApplicationModule? = null

        fun get(context: Context): ApplicationModule {
            if (sApplicationModule != null) return sApplicationModule!!
            val application = context.applicationContext as Application
            sApplicationModule = ApplicationModule(application)
            return sApplicationModule!!
        }
    }
}
