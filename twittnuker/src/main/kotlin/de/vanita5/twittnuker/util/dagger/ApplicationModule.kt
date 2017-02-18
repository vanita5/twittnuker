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
import com.nostra13.universalimageloader.cache.disc.DiskCache
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.QueueProcessingType
import com.nostra13.universalimageloader.utils.L
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import com.twitter.Extractor
import dagger.Module
import dagger.Provides
import okhttp3.ConnectionPool
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.mediaviewer.library.FileCache
import org.mariotaku.mediaviewer.library.MediaDownloader
import org.mariotaku.restfu.http.RestHttpClient
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.constant.SharedPreferenceConstants
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.KEY_CACHE_SIZE_LIMIT
import de.vanita5.twittnuker.constant.autoRefreshCompatibilityModeKey
import de.vanita5.twittnuker.model.DefaultFeatures
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.imageloader.ReadOnlyDiskLRUNameCache
import de.vanita5.twittnuker.util.imageloader.TwidereImageDownloader
import de.vanita5.twittnuker.util.imageloader.URLFileNameGenerator
import de.vanita5.twittnuker.util.media.TwidereMediaDownloader
import de.vanita5.twittnuker.util.media.UILFileCache
import de.vanita5.twittnuker.util.net.TwidereDns
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import de.vanita5.twittnuker.util.refresh.AutoRefreshController
import de.vanita5.twittnuker.util.refresh.JobSchedulerAutoRefreshController
import de.vanita5.twittnuker.util.refresh.LegacyAutoRefreshController
import de.vanita5.twittnuker.util.sync.JobSchedulerSyncController
import de.vanita5.twittnuker.util.sync.LegacySyncController
import de.vanita5.twittnuker.util.sync.SyncController
import de.vanita5.twittnuker.util.sync.SyncPreferences
import java.io.IOException
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
    fun restHttpClient(prefs: SharedPreferencesWrapper, dns: TwidereDns,
                       connectionPool: ConnectionPool): RestHttpClient {
        val conf = HttpClientFactory.HttpClientConfiguration(prefs)
        return HttpClientFactory.createRestHttpClient(conf, dns, connectionPool)
    }

    @Provides
    @Singleton
    fun connectionPoll(): ConnectionPool {
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
    fun imageLoader(preferences: SharedPreferencesWrapper, downloader: MediaDownloader): ImageLoader {
        val loader = ImageLoader.getInstance()
        val cb = ImageLoaderConfiguration.Builder(application)
        cb.threadPriority(Thread.NORM_PRIORITY - 2)
        cb.denyCacheImageMultipleSizesInMemory()
        cb.tasksProcessingOrder(QueueProcessingType.LIFO)
        // cb.memoryCache(new ImageMemoryCache(40));
        cb.diskCache(createDiskCache("images", preferences))
        cb.imageDownloader(TwidereImageDownloader(application, downloader))
        L.writeDebugLogs(BuildConfig.DEBUG)
        loader.init(cb.build())
        return loader
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
    fun mediaLoaderWrapper(loader: ImageLoader, preferences: SharedPreferencesWrapper): MediaLoaderWrapper {
        val wrapper = MediaLoaderWrapper(loader)
        wrapper.reloadOptions(preferences)
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wrapper.isNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(cm)
        return wrapper
    }

    @Provides
    @Singleton
    fun dns(preferences: SharedPreferencesWrapper): TwidereDns {
        return TwidereDns(application, preferences)
    }

    @Provides
    @Singleton
    fun providesDiskCache(preferences: SharedPreferencesWrapper): DiskCache {
        return createDiskCache("files", preferences)
    }

    @Provides
    @Singleton
    fun fileCache(cache: DiskCache): FileCache {
        return UILFileCache(cache)
    }

    @Provides
    @Singleton
    fun mediaDownloader(client: RestHttpClient): MediaDownloader {
        return TwidereMediaDownloader(application, client)
    }

    @Provides
    @Singleton
    fun twidereValidator(): TwidereValidator {
        return TwidereValidator()
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

    private fun createDiskCache(dirName: String, preferences: SharedPreferencesWrapper): DiskCache {
        val cacheDir = Utils.getExternalCacheDir(application, dirName)
        val fallbackCacheDir = Utils.getInternalCacheDir(application, dirName)
        val fileNameGenerator = URLFileNameGenerator()
        val cacheSize = preferences.getInt(KEY_CACHE_SIZE_LIMIT, 300).coerceIn(100..500)
        try {
            val cacheMaxSizeBytes = cacheSize * 1024 * 1024
            if (cacheDir != null)
                return LruDiskCache(cacheDir, fallbackCacheDir, fileNameGenerator, cacheMaxSizeBytes.toLong(), 0)
            return LruDiskCache(fallbackCacheDir, null, fileNameGenerator, cacheMaxSizeBytes.toLong(), 0)
        } catch (e: IOException) {
            return ReadOnlyDiskLRUNameCache(cacheDir, fallbackCacheDir, fileNameGenerator)
        }

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
