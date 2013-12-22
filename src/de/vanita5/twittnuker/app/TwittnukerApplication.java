/*
 *			Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import static de.vanita5.twittnuker.util.UserColorNicknameUtils.initUserColor;
import static de.vanita5.twittnuker.util.Utils.getBestCacheDir;
import static de.vanita5.twittnuker.util.Utils.initAccountColor;
import static de.vanita5.twittnuker.util.Utils.startRefreshServiceIfNeeded;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.L;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.mariotaku.gallery3d.util.GalleryUtils;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.Main2Activity;
import de.vanita5.twittnuker.activity.MainActivity;
import de.vanita5.twittnuker.service.RefreshService;
import de.vanita5.twittnuker.util.AsyncTaskManager;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ImageLoaderWrapper;
import de.vanita5.twittnuker.util.ImageMemoryCache;
import de.vanita5.twittnuker.util.MessagesManager;
import de.vanita5.twittnuker.util.MultiSelectManager;
import de.vanita5.twittnuker.util.StrictModeUtils;
import de.vanita5.twittnuker.util.SwipebackActivityUtils.SwipebackScreenshotManager;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.content.TwidereSQLiteOpenHelper;
import de.vanita5.twittnuker.util.imageloader.TwidereImageDownloader;
import de.vanita5.twittnuker.util.imageloader.URLFileNameGenerator;
import de.vanita5.twittnuker.util.net.TwidereHostAddressResolver;

import twitter4j.http.HostAddressResolver;

import java.io.File;
import java.util.Date;
import java.util.Locale;

@ReportsCrashes(formKey = "", sharedPreferencesMode = Context.MODE_PRIVATE,
	sharedPreferencesName = Constants.SHARED_PREFERENCES_NAME)
public class TwittnukerApplication extends Application implements Constants, OnSharedPreferenceChangeListener {

	private Handler mHandler;

	private ImageLoaderWrapper mImageLoaderWrapper;
	private ImageLoader mImageLoader;
	private AsyncTaskManager mAsyncTaskManager;
	private SharedPreferences mPreferences;
	private AsyncTwitterWrapper mTwitterWrapper;
	private MultiSelectManager mMultiSelectManager;
	private TwidereImageDownloader mImageDownloader, mFullImageDownloader;
	private DiscCacheAware mDiscCache, mFullDiscCache;
	private MessagesManager mCroutonsManager;
	private SQLiteOpenHelper mSQLiteOpenHelper;
	private SwipebackScreenshotManager mSwipebackScreenshotManager;
	private HostAddressResolver mResolver;
	private SQLiteDatabase mDatabase;

	public AsyncTaskManager getAsyncTaskManager() {
		if (mAsyncTaskManager != null) return mAsyncTaskManager;
		return mAsyncTaskManager = AsyncTaskManager.getInstance();
	}

	public DiscCacheAware getDiscCache() {
		if (mDiscCache != null) return mDiscCache;
		return mDiscCache = getDiscCache(DIR_NAME_IMAGE_CACHE);
	}

	public DiscCacheAware getFullDiscCache() {
		if (mFullDiscCache != null) return mFullDiscCache;
		return mFullDiscCache = getDiscCache(DIR_NAME_FULL_IMAGE_CACHE);
	}

	public ImageDownloader getFullImageDownloader() {
		if (mFullImageDownloader != null) return mFullImageDownloader;
		return mFullImageDownloader = new TwidereImageDownloader(this, true);
	}

	public Handler getHandler() {
		return mHandler;
	}

	public HostAddressResolver getHostAddressResolver() {
		if (mResolver != null) return mResolver;
		return mResolver = new TwidereHostAddressResolver(this);
	}

	public ImageDownloader getImageDownloader() {
		if (mImageDownloader != null) return mImageDownloader;
		return mImageDownloader = new TwidereImageDownloader(this, false);
	}

	public ImageLoader getImageLoader() {
		if (mImageLoader != null) return mImageLoader;
		if (Utils.isDebugBuild()) {
			L.enableLogging();
		} else {
			L.disableLogging();
		}
		final ImageLoader loader = ImageLoader.getInstance();
		final ImageLoaderConfiguration.Builder cb = new ImageLoaderConfiguration.Builder(this);
		cb.threadPoolSize(8);
		cb.memoryCache(new ImageMemoryCache(40));
		cb.discCache(getDiscCache());
		cb.imageDownloader(getImageDownloader());
		loader.init(cb.build());
		return mImageLoader = loader;
	}

	public ImageLoaderWrapper getImageLoaderWrapper() {
		if (mImageLoaderWrapper != null) return mImageLoaderWrapper;
		return mImageLoaderWrapper = new ImageLoaderWrapper(getImageLoader());
	}

	public MessagesManager getMessagesManager() {
		if (mCroutonsManager != null) return mCroutonsManager;
		return mCroutonsManager = new MessagesManager(this);
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

	public SwipebackScreenshotManager getSwipebackScreenshotManager() {
		if (mSwipebackScreenshotManager != null) return mSwipebackScreenshotManager;
		return mSwipebackScreenshotManager = new SwipebackScreenshotManager();
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		if (mTwitterWrapper != null) return mTwitterWrapper;
		return mTwitterWrapper = AsyncTwitterWrapper.getInstance(this);
	}

	@Override
	public void onCreate() {
		if (Utils.isDebugBuild()) {
			StrictModeUtils.detectAllVmPolicy();
		}
		super.onCreate();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mHandler = new Handler();
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		configACRA();
		initializeAsyncTask();
		GalleryUtils.initialize(this);
		initAccountColor(this);
		initUserColor(this);

		final PackageManager pm = getPackageManager();
		final ComponentName main = new ComponentName(this, MainActivity.class);
		final ComponentName main2 = new ComponentName(this, Main2Activity.class);
		final boolean mainDisabled = pm.getComponentEnabledSetting(main) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
		final boolean main2Disabled = pm.getComponentEnabledSetting(main2) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
		final boolean no_entry = mainDisabled && main2Disabled;
		if (no_entry) {
			pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
		} else if (!mainDisabled) {
			pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		}

		startRefreshServiceIfNeeded(this);
	}

	@Override
	public void onLowMemory() {
		if (mImageLoaderWrapper != null) {
			mImageLoaderWrapper.clearMemoryCache();
		}
		super.onLowMemory();
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (PREFERENCE_KEY_REFRESH_INTERVAL.equals(key)) {
			stopService(new Intent(this, RefreshService.class));
			startRefreshServiceIfNeeded(this);
		} else if (PREFERENCE_KEY_ENABLE_PROXY.equals(key) || PREFERENCE_KEY_CONNECTION_TIMEOUT.equals(key)
				|| PREFERENCE_KEY_PROXY_HOST.equals(key) || PREFERENCE_KEY_PROXY_PORT.equals(key)
				|| PREFERENCE_KEY_FAST_IMAGE_LOADING.equals(key)) {
			reloadConnectivitySettings();
		}
	}

	public void reloadConnectivitySettings() {
		if (mImageDownloader != null) {
			mImageDownloader.reloadConnectivitySettings();
		}
	}

	private void configACRA() {
		ACRA.init(this);
		ACRA.getErrorReporter().setReportSender(new EmailIntentSender(this));
	}

	private DiscCacheAware getDiscCache(final String dir_name) {
		final File cache_dir = getBestCacheDir(this, dir_name);
		return new UnlimitedDiscCache(cache_dir, new URLFileNameGenerator());
	}

	private void initializeAsyncTask() {
		// AsyncTask class needs to be loaded in UI thread.
		// So we load it here to comply the rule.
		try {
			Class.forName(AsyncTask.class.getName());
		} catch (final ClassNotFoundException e) {
		}
	}

	public static TwittnukerApplication getInstance(final Context context) {
		if (context == null) return null;
		final Context app = context.getApplicationContext();
		return app instanceof TwittnukerApplication ? (TwittnukerApplication) app : null;
	}

	static class EmailIntentSender implements ReportSender {

		private final Context mContext;

		EmailIntentSender(final Context ctx) {
			mContext = ctx;
		}

		@Override
		public void send(final CrashReportData errorContent) throws ReportSenderException {
			final Intent email = new Intent(Intent.ACTION_SEND);
			email.setType("text/plain");
			email.putExtra(Intent.EXTRA_SUBJECT, String.format("%s Crash Report", getAppName()));
			email.putExtra(Intent.EXTRA_TEXT, buildBody(errorContent));
			email.putExtra(Intent.EXTRA_EMAIL, new String[] { APP_PROJECT_EMAIL });
			final Intent chooser = Intent.createChooser(email, mContext.getString(R.string.send_crash_report));
			chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(chooser);
		}

		private String buildBody(final CrashReportData errorContent) {
			final String stack_trace = errorContent.getProperty(ReportField.STACK_TRACE);
			final StringBuilder builder = new StringBuilder();
			builder.append(String.format(Locale.US, "Report date: %s\n", new Date(System.currentTimeMillis())));
			builder.append(String.format(Locale.US, "Android version: %s\n", Build.VERSION.RELEASE));
			builder.append(String.format(Locale.US, "API version: %d\n", Build.VERSION.SDK_INT));
			builder.append(String.format(Locale.US, "App version name: %s\n", getAppVersionName()));
			builder.append(String.format(Locale.US, "App version code: %d\n", getAppVersionCode()));
			builder.append(String.format(Locale.US, "Configuration: %s\n", mContext.getResources().getConfiguration()));
			builder.append(String.format(Locale.US, "Stack trace:\n%s\n", stack_trace));
			return builder.toString();
		}

		private CharSequence getAppName() {
			final PackageManager pm = mContext.getPackageManager();
			try {
				return pm.getApplicationLabel(pm.getApplicationInfo(mContext.getPackageName(), 0));
			} catch (final NameNotFoundException e) {
				return APP_NAME;
			}
		}

		private int getAppVersionCode() {
			final PackageManager pm = mContext.getPackageManager();
			try {
				return pm.getPackageInfo(mContext.getPackageName(), 0).versionCode;
			} catch (final NameNotFoundException e) {
				return 0;
			}
		}

		private String getAppVersionName() {
			final PackageManager pm = mContext.getPackageManager();
			try {
				return pm.getPackageInfo(mContext.getPackageName(), 0).versionName;
			} catch (final NameNotFoundException e) {
				return "unknown";
			}
		}
	}

}
