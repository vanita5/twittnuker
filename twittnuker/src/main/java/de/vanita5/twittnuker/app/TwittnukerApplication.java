/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.L;
import com.squareup.okhttp.internal.Network;
import com.squareup.otto.Bus;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.activity.AssistLauncherActivity;
import de.vanita5.twittnuker.activity.MainActivity;
import de.vanita5.twittnuker.service.RefreshService;
import de.vanita5.twittnuker.util.AsyncTaskManager;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ErrorLogger;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.MultiSelectManager;
import de.vanita5.twittnuker.util.ReadStateManager;
import de.vanita5.twittnuker.util.StrictModeUtils;
import de.vanita5.twittnuker.util.UserAgentUtils;
import de.vanita5.twittnuker.util.UserColorNameManager;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.VideoLoader;
import de.vanita5.twittnuker.util.content.TwidereSQLiteOpenHelper;
import de.vanita5.twittnuker.util.imageloader.TwidereImageDownloader;
import de.vanita5.twittnuker.util.imageloader.URLFileNameGenerator;
import de.vanita5.twittnuker.util.net.TwidereHostAddressResolver;

import java.io.File;

import static de.vanita5.twittnuker.util.Utils.getBestCacheDir;
import static de.vanita5.twittnuker.util.Utils.getInternalCacheDir;
import static de.vanita5.twittnuker.util.Utils.initAccountColor;
import static de.vanita5.twittnuker.util.Utils.startRefreshServiceIfNeeded;

@ReportsCrashes(formUri = "https://vanita5.cloudant.com/acra-twittnuker/_design/acra-storage/_update/report",
		reportType = HttpSender.Type.JSON,
		httpMethod = HttpSender.Method.PUT,
		formUriBasicAuthLogin = "ionstoweneringstantleare",
		formUriBasicAuthPassword = "MNNNyLKyTDvuaqbaCtOkqdMC",
		buildConfigClass = BuildConfig.class)
public class TwittnukerApplication extends Application implements Constants,
		OnSharedPreferenceChangeListener {

	private static final String KEY_KEYBOARD_SHORTCUT_INITIALIZED = "keyboard_shortcut_initialized";

	private Handler mHandler;
    private MediaLoaderWrapper mMediaLoaderWrapper;
	private ImageLoader mImageLoader;
	private AsyncTaskManager mAsyncTaskManager;
	private SharedPreferences mPreferences;
	private AsyncTwitterWrapper mTwitterWrapper;
	private MultiSelectManager mMultiSelectManager;
	private TwidereImageDownloader mImageDownloader, mFullImageDownloader;
	private DiskCache mDiskCache, mFullDiskCache;
	private SQLiteOpenHelper mSQLiteOpenHelper;
    private Network mNetwork;
	private SQLiteDatabase mDatabase;
    private Bus mMessageBus;
    private VideoLoader mVideoLoader;
    private ReadStateManager mReadStateManager;
    private KeyboardShortcutsHandler mKeyboardShortcutsHandler;
    private UserColorNameManager mUserColorNameManager;

    private String mDefaultUserAgent;

    @NonNull
    public static TwittnukerApplication getInstance(@NonNull final Context context) {
        return (TwittnukerApplication) context.getApplicationContext();
    }

	public AsyncTaskManager getAsyncTaskManager() {
		if (mAsyncTaskManager != null) return mAsyncTaskManager;
		return mAsyncTaskManager = AsyncTaskManager.getInstance();
	}

    public String getDefaultUserAgent() {
        return mDefaultUserAgent;
    }

	public DiskCache getDiskCache() {
		if (mDiskCache != null) return mDiskCache;
        return mDiskCache = createDiskCache(DIR_NAME_IMAGE_CACHE);
	}

	public DiskCache getFullDiskCache() {
		if (mFullDiskCache != null) return mFullDiskCache;
        return mFullDiskCache = createDiskCache(DIR_NAME_FULL_IMAGE_CACHE);
	}

    public UserColorNameManager getUserColorNameManager() {
        if (mUserColorNameManager != null) return mUserColorNameManager;
        return mUserColorNameManager = new UserColorNameManager(this);
    }

	public ImageDownloader getFullImageDownloader() {
		if (mFullImageDownloader != null) return mFullImageDownloader;
        return mFullImageDownloader = new TwidereImageDownloader(this, true);
	}

	public Handler getHandler() {
		return mHandler;
	}

    public Network getNetwork() {
        if (mNetwork != null) return mNetwork;
        return mNetwork = new TwidereHostAddressResolver(this);
	}

    public ReadStateManager getReadStateManager() {
        if (mReadStateManager != null) return mReadStateManager;
        return mReadStateManager = new ReadStateManager(this);
    }

    public KeyboardShortcutsHandler getKeyboardShortcutsHandler() {
        if (mKeyboardShortcutsHandler != null) return mKeyboardShortcutsHandler;
        mKeyboardShortcutsHandler = new KeyboardShortcutsHandler(this);
        final SharedPreferences preferences = getSharedPreferences();
        if (!preferences.getBoolean(KEY_KEYBOARD_SHORTCUT_INITIALIZED, false)) {
            mKeyboardShortcutsHandler.reset();
            preferences.edit().putBoolean(KEY_KEYBOARD_SHORTCUT_INITIALIZED, true).apply();
        }
        return mKeyboardShortcutsHandler;
    }

	public ImageDownloader getImageDownloader() {
		if (mImageDownloader != null) return mImageDownloader;
        return mImageDownloader = new TwidereImageDownloader(this, false);
	}

	public ImageLoader getImageLoader() {
		if (mImageLoader != null) return mImageLoader;
		final ImageLoader loader = ImageLoader.getInstance();
		final ImageLoaderConfiguration.Builder cb = new ImageLoaderConfiguration.Builder(this);
		cb.threadPriority(Thread.NORM_PRIORITY - 2);
		cb.denyCacheImageMultipleSizesInMemory();
		cb.tasksProcessingOrder(QueueProcessingType.LIFO);
		// cb.memoryCache(new ImageMemoryCache(40));
		cb.diskCache(getDiskCache());
		cb.imageDownloader(getImageDownloader());
		L.writeDebugLogs(BuildConfig.DEBUG);
		loader.init(cb.build());
		return mImageLoader = loader;
	}

    public VideoLoader getVideoLoader() {
        if (mVideoLoader != null) return mVideoLoader;
        final VideoLoader loader = new VideoLoader(this);
        return mVideoLoader = loader;
	}

    public MediaLoaderWrapper getMediaLoaderWrapper() {
        if (mMediaLoaderWrapper != null) return mMediaLoaderWrapper;
        return mMediaLoaderWrapper = new MediaLoaderWrapper(getImageLoader(), getVideoLoader());
    }

	@Nullable
    public Bus getMessageBus() {
        return mMessageBus;
    }

	public MultiSelectManager getMultiSelectManager() {
		if (mMultiSelectManager != null) return mMultiSelectManager;
		return mMultiSelectManager = new MultiSelectManager();
	}

	public SQLiteDatabase getSQLiteDatabase() {
		if (mDatabase != null) return mDatabase;
		StrictModeUtils.checkDiskIO();
		return mDatabase = getSQLiteOpenHelper().getWritableDatabase();
	}

	public SQLiteOpenHelper getSQLiteOpenHelper() {
		if (mSQLiteOpenHelper != null) return mSQLiteOpenHelper;
		return mSQLiteOpenHelper = new TwidereSQLiteOpenHelper(this, DATABASES_NAME, DATABASES_VERSION);
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		if (mTwitterWrapper != null) return mTwitterWrapper;
        return mTwitterWrapper = new AsyncTwitterWrapper(this);
	}

	@Override
	public void onCreate() {
		if (BuildConfig.DEBUG) {
			StrictModeUtils.detectAllVmPolicy();
		}
		super.onCreate();
        initBugReport();
        mDefaultUserAgent = UserAgentUtils.getDefaultUserAgentString(this);
        mHandler = new Handler();
        mMessageBus = new Bus();
		initializeAsyncTask();
		initAccountColor(this);

		final PackageManager pm = getPackageManager();
		final ComponentName main = new ComponentName(this, MainActivity.class);
		pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
		if (!Utils.isComposeNowSupported(this)) {
			final ComponentName assist = new ComponentName(this, AssistLauncherActivity.class);
			pm.setComponentEnabledSetting(assist, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		}
		startRefreshServiceIfNeeded(this);

		reloadConnectivitySettings();
	}

    private void initBugReport() {
        ACRA.init(this);
		ErrorLogger.setEnabled(BuildConfig.DEBUG);
    }

	private SharedPreferences getSharedPreferences() {
		if (mPreferences != null) return mPreferences;
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		return mPreferences;
	}

	@Override
	public void onLowMemory() {
        if (mMediaLoaderWrapper != null) {
            mMediaLoaderWrapper.clearMemoryCache();
		}
		super.onLowMemory();
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (KEY_REFRESH_INTERVAL.equals(key)) {
			stopService(new Intent(this, RefreshService.class));
			startRefreshServiceIfNeeded(this);
		} else if (KEY_ENABLE_PROXY.equals(key) || KEY_CONNECTION_TIMEOUT.equals(key) || KEY_PROXY_HOST.equals(key)
				|| KEY_PROXY_PORT.equals(key)) {
			reloadConnectivitySettings();
		} else if (KEY_CONSUMER_KEY.equals(key) || KEY_CONSUMER_SECRET.equals(key) || KEY_API_URL_FORMAT.equals(key)
				|| KEY_AUTH_TYPE.equals(key) || KEY_SAME_OAUTH_SIGNING_URL.equals(key)) {
			final SharedPreferences.Editor editor = preferences.edit();
			editor.putLong(KEY_API_LAST_CHANGE, System.currentTimeMillis());
			editor.apply();
		}
	}

	public void reloadConnectivitySettings() {
		if (mImageDownloader != null) {
			mImageDownloader.reloadConnectivitySettings();
		}
        if (mFullImageDownloader != null) {
            mFullImageDownloader.reloadConnectivitySettings();
        }
	}

    private DiskCache createDiskCache(final String dirName) {
        final File cacheDir = getBestCacheDir(this, dirName);
		final File fallbackCacheDir = getInternalCacheDir(this, dirName);
//        final LruDiscCache discCache = new LruDiscCache(cacheDir, new URLFileNameGenerator(), 384 *
//                1024 * 1024);
//        discCache.setReserveCacheDir(fallbackCacheDir);
//        return discCache;
		return new UnlimitedDiscCache(cacheDir, fallbackCacheDir, new URLFileNameGenerator());
	}

	private void initializeAsyncTask() {
		// AsyncTask class needs to be loaded in UI thread.
		// So we load it here to comply the rule.
		try {
			Class.forName(AsyncTask.class.getName());
        } catch (final ClassNotFoundException ignore) {
		}
	}

}