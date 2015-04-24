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

package de.vanita5.twittnuker.activity.support;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.ArrayUtils;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.SettingsActivity;
import de.vanita5.twittnuker.activity.SettingsWizardActivity;
import de.vanita5.twittnuker.adapter.support.SupportTabsAdapter;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.fragment.CustomTabsFragment;
import de.vanita5.twittnuker.fragment.iface.RefreshScrollTopInterface;
import de.vanita5.twittnuker.fragment.iface.SupportFragmentCallback;
import de.vanita5.twittnuker.fragment.support.AccountsDashboardFragment;
import de.vanita5.twittnuker.fragment.support.DirectMessagesFragment;
import de.vanita5.twittnuker.fragment.support.TrendsSuggectionsFragment;
import de.vanita5.twittnuker.gcm.GCMHelper;
import de.vanita5.twittnuker.graphic.EmptyDrawable;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.SupportTabSpec;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.provider.TwidereDataStore.Mentions;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.service.StreamingService;
import de.vanita5.twittnuker.util.AsyncTaskUtils;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.TwidereColorUtils;
import de.vanita5.twittnuker.util.CustomTabUtils;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import de.vanita5.twittnuker.util.MathUtils;
import de.vanita5.twittnuker.util.MultiSelectEventHandler;
import de.vanita5.twittnuker.util.ParseUtils;
import de.vanita5.twittnuker.util.ReadStateManager;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.ViewUtils;
import de.vanita5.twittnuker.util.accessor.ActivityAccessor;
import de.vanita5.twittnuker.util.accessor.ActivityAccessor.TaskDescriptionCompat;
import de.vanita5.twittnuker.util.message.TaskStateChangedEvent;
import de.vanita5.twittnuker.util.message.UnreadCountUpdatedEvent;
import de.vanita5.twittnuker.view.ExtendedViewPager;
import de.vanita5.twittnuker.view.HomeSlidingMenu;
import de.vanita5.twittnuker.view.LeftDrawerFrameLayout;
import de.vanita5.twittnuker.view.RightDrawerFrameLayout;
import de.vanita5.twittnuker.view.TabPagerIndicator;
import de.vanita5.twittnuker.view.TintedStatusFrameLayout;
import de.vanita5.twittnuker.view.iface.IHomeActionButton;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import static de.vanita5.twittnuker.util.CompareUtils.classEquals;
import static de.vanita5.twittnuker.util.Utils.cleanDatabasesByItemLimit;
import static de.vanita5.twittnuker.util.Utils.getAccountIds;
import static de.vanita5.twittnuker.util.Utils.getDefaultAccountId;
import static de.vanita5.twittnuker.util.Utils.getTabDisplayOptionInt;
import static de.vanita5.twittnuker.util.Utils.isDatabaseReady;
import static de.vanita5.twittnuker.util.Utils.isPushEnabled;
import static de.vanita5.twittnuker.util.Utils.openMessageConversation;
import static de.vanita5.twittnuker.util.Utils.openSearch;
import static de.vanita5.twittnuker.util.Utils.showMenuItemToast;

public class HomeActivity extends BaseAppCompatActivity implements OnClickListener, OnPageChangeListener,
        SupportFragmentCallback, OnOpenedListener, OnClosedListener, OnLongClickListener {

	private final Handler mHandler = new Handler();

	private final ContentObserver mAccountChangeObserver = new AccountChangeObserver(this, mHandler);

    private ParcelableAccount mSelectedAccountToSearch;

	private SharedPreferences mPreferences;

	private AsyncTwitterWrapper mTwitterWrapper;

	private MultiSelectEventHandler mMultiSelectHandler;
    private ReadStateManager mReadStateManager;
    private KeyboardShortcutsHandler mKeyboardShortcutsHandler;

	private SupportTabsAdapter mPagerAdapter;

	private ExtendedViewPager mViewPager;
    private TabPagerIndicator mTabIndicator;
	private HomeSlidingMenu mSlidingMenu;
	private View mEmptyTabHint;
    private View mActionsButton;
    private View mTabsContainer;
    private View mActionBarOverlay;
	private LeftDrawerFrameLayout mLeftDrawerContainer;
	private RightDrawerFrameLayout mRightDrawerContainer;
    private TintedStatusFrameLayout mColorStatusFrameLayout;

	private UpdateUnreadCountTask mUpdateUnreadCountTask;

	private int mTabDisplayOption;
	private boolean isStreamingServiceRunning = false;

	private boolean mPushEnabled;
    private Toolbar mActionBar;

    private OnSharedPreferenceChangeListener mReadStateChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateUnreadCount();
        }
    };
    private ControlBarShowHideHelper mControlBarShowHideHelper = new ControlBarShowHideHelper(this);
    private int mTabColumns;

	public void closeAccountsDrawer() {
		if (mSlidingMenu == null) return;
		mSlidingMenu.showContent();
	}

    public long[] getActivatedAccountIds() {
        final Fragment fragment = getLeftDrawerFragment();
        if (fragment instanceof AccountsDashboardFragment) {
            return ((AccountsDashboardFragment) fragment).getActivatedAccountIds();
        }
        return Utils.getActivatedAccountIds(this);
    }

	@Override
	public Fragment getCurrentVisibleFragment() {
        final int currentItem = mViewPager.getCurrentItem();
        if (currentItem < 0 || currentItem >= mPagerAdapter.getCount()) return null;
        return (Fragment) mPagerAdapter.instantiateItem(mViewPager, currentItem);
	}

	@Override
    public boolean triggerRefresh(final int position) {
        final Fragment f = (Fragment) mPagerAdapter.instantiateItem(mViewPager, position);
        if (!(f instanceof RefreshScrollTopInterface)) return false;
        if (f.getActivity() == null || f.isDetached()) return false;
        return ((RefreshScrollTopInterface) f).triggerRefresh();
    }

    public Fragment getLeftDrawerFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.left_drawer);
    }

	@Override
    public boolean getSystemWindowsInsets(Rect insets) {
        final int height = mTabIndicator != null ? mTabIndicator.getHeight() : 0;
        insets.top = height != 0 ? height : Utils.getActionBarHeight(this);
        return true;
    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {
        mControlBarShowHideHelper.setControlBarVisibleAnimate(visible);
	}

	@Override
    public boolean onKeyUp(final int keyCode, @NonNull final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU: {
                if (mSlidingMenu != null) {
                    mSlidingMenu.toggle(true);
                    return true;
                }
                break;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				final FragmentManager fm = getSupportFragmentManager();
				final int count = fm.getBackStackEntryCount();

				if (mSlidingMenu.isMenuShowing()) {
					mSlidingMenu.showContent();
					return true;
				} else if (count == 0) {
					mSlidingMenu.showMenu();
					return true;
				}
				return true;
			}
			case MENU_SEARCH: {
				openSearchView(mSelectedAccountToSearch);
				return true;
			}
            case MENU_ACTIONS: {
                triggerActionsClick();
                return true;
            }
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    public boolean handleKeyboardShortcutSingle(int keyCode, @NonNull KeyEvent event) {
        if (handleFragmentKeyboardShortcutSingle(keyCode, event)) return true;
        String action = mKeyboardShortcutsHandler.getKeyAction("home", keyCode, event);
        if (action != null) {
            switch (action) {
                case "home.accounts_dashboard": {
                    if (mSlidingMenu.isMenuShowing()) {
                        mSlidingMenu.showContent(true);
                    } else {
                        mSlidingMenu.showMenu(true);
                        setControlBarVisibleAnimate(true);
                    }
                    return true;
                }
            }
        }
        action = mKeyboardShortcutsHandler.getKeyAction("navigation", keyCode, event);
        if (action != null) {
            switch (action) {
                case "navigation.previous_tab": {
                    final int previous = mViewPager.getCurrentItem() - 1;
                    if (previous < 0) {
                        mSlidingMenu.showMenu(true);
                        setControlBarVisibleAnimate(true);
                    } else if (previous < mPagerAdapter.getCount()) {
                        if (mSlidingMenu.isSecondaryMenuShowing()) {
                            mSlidingMenu.showContent(true);
                        } else {
							mViewPager.setCurrentItem(previous, true);
						}
                    }
                    return true;
                }
                case "navigation.next_tab": {
                    final int next = mViewPager.getCurrentItem() + 1;
                    if (next >= mPagerAdapter.getCount()) {
                        mSlidingMenu.showSecondaryMenu(true);
                        setControlBarVisibleAnimate(true);
                    } else if (next >= 0) {
                        if (mSlidingMenu.isMenuShowing()) {
                            mSlidingMenu.showContent(true);
                        } else {
							mViewPager.setCurrentItem(next, true);
						}
                    }
                    return true;
                }
            }
        }
        return mKeyboardShortcutsHandler.handleKey(this, null, keyCode, event);
    }

    @Override
    public void setControlBarOffset(float offset) {
        mTabsContainer.setTranslationY(mTabColumns > 1 ? 0 : getControlBarHeight() * (offset - 1));
        final ViewGroup.LayoutParams lp = mActionsButton.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            mActionsButton.setTranslationY((((MarginLayoutParams) lp).bottomMargin + mActionsButton.getHeight()) * (1 - offset));
        } else {
            mActionsButton.setTranslationY(mActionsButton.getHeight() * (1 - offset));
        }
        notifyControlBarOffsetChanged();
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(int keyCode, int repeatCount, @NonNull KeyEvent event) {
        if (handleFragmentKeyboardShortcutRepeat(keyCode, repeatCount, event)) return true;
        return super.handleKeyboardShortcutRepeat(keyCode, repeatCount, event);
	}

    /**
     * Called when the context is first created.
     */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
        final Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
		super.onCreate(savedInstanceState);
		if (!isDatabaseReady(this)) {
			Toast.makeText(this, R.string.preparing_database_toast, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mTwitterWrapper = getTwitterWrapper();
        final TwittnukerApplication app = TwittnukerApplication.getInstance(this);
        mReadStateManager = app.getReadStateManager();
		mMultiSelectHandler = new MultiSelectEventHandler(this);
        mKeyboardShortcutsHandler = app.getKeyboardShortcutsHandler();
		mMultiSelectHandler.dispatchOnCreate();
		final long[] accountIds = getAccountIds(this);
		if (accountIds.length == 0) {
            final Intent signInIntent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
            signInIntent.setClass(this, SignInActivity.class);
            startActivity(signInIntent);
			finish();
			return;
		} else {
			notifyAccountsChanged();
		}
		final Intent intent = getIntent();
		if (openSettingsWizard()) {
			finish();
			return;
		}
		setContentView(R.layout.activity_home);
        setSupportActionBar(mActionBar);
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONCREATE));
        final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false);
        mTabDisplayOption = getTabDisplayOptionInt(this);

        mTabColumns = getResources().getInteger(R.integer.default_tab_columns);

        mColorStatusFrameLayout.setOnFitSystemWindowsListener(this);
        ThemeUtils.applyBackground(mTabIndicator);
        mPagerAdapter = new SupportTabsAdapter(this, getSupportFragmentManager(), mTabIndicator, mTabColumns);
		mPushEnabled = isPushEnabled(this);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
        mTabIndicator.setViewPager(mViewPager);
        mTabIndicator.setOnPageChangeListener(this);
        mTabIndicator.setColumns(mTabColumns);
        if (mTabDisplayOption != 0) {
            mTabIndicator.setTabDisplayOption(mTabDisplayOption);
        } else {
            mTabIndicator.setTabDisplayOption(TabPagerIndicator.ICON);
        }
        mTabIndicator.setDisplayBadge(mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
        mActionsButton.setOnClickListener(this);
        mActionsButton.setOnLongClickListener(this);
        mEmptyTabHint.setOnClickListener(this);

		setupSlidingMenu();
        setupBars();
		initUnreadCount();
		updateActionsButton();
		updateSlidingMenuTouchMode();

		if (savedInstanceState == null) {
			if (refreshOnStart) {
				mTwitterWrapper.refreshAll();
			}
			if (intent.getBooleanExtra(EXTRA_OPEN_ACCOUNTS_DRAWER, false)) {
				openAccountsDrawer();
			}
		}
        setupHomeTabs();

        final int initialTabPosition = handleIntent(intent, savedInstanceState == null);
        setTabPosition(initialTabPosition);
    }

	@Override
	protected void onStart() {
		super.onStart();
		mMultiSelectHandler.dispatchOnStart();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTART));
		final ContentResolver resolver = getContentResolver();
		resolver.registerContentObserver(Accounts.CONTENT_URI, true, mAccountChangeObserver);
        final Bus bus = TwittnukerApplication.getInstance(this).getMessageBus();
        bus.register(this);
        if (getTabDisplayOptionInt(this) != mTabDisplayOption) {
			restart();
		}
        mReadStateManager.registerOnSharedPreferenceChangeListener(mReadStateChangeListener);
		updateUnreadCount();

		if (mPushEnabled != isPushEnabled(this) || mPushEnabled && !isPushRegistered()) {
			mPushEnabled = isPushEnabled(this);
			if (mPushEnabled) {
				GCMHelper.registerIfNotAlreadyDone(this);
			} else {
				GCMHelper.unregisterGCM(this);
			}
		}
		if (mPreferences.getBoolean(KEY_STREAMING_ENABLED, true)) {
			startStreamingService();
		} else {
			stopStreamingService();
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONRESUME));
        invalidateOptionsMenu();
        updateActionsButtonStyle();
        updateActionsButton();
        updateSlidingMenuTouchMode();

        if (mPreferences.getBoolean(KEY_STREAMING_ENABLED, true)) {
            startStreamingService();
        } else {
            stopStreamingService();
        }
    }

    @Override
    protected void onPause() {
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONPAUSE));
        super.onPause();
    }

	@Override
	protected void onStop() {
		mMultiSelectHandler.dispatchOnStop();
        mReadStateManager.unregisterOnSharedPreferenceChangeListener(mReadStateChangeListener);
        final Bus bus = TwittnukerApplication.getInstance(this).getMessageBus();
        bus.unregister(this);
		final ContentResolver resolver = getContentResolver();
		resolver.unregisterContentObserver(mAccountChangeObserver);
		mPreferences.edit().putInt(KEY_SAVED_TAB_POSITION, mViewPager.getCurrentItem()).apply();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTOP));

		super.onStop();
	}

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void notifyAccountsChanged() {
        if (mPreferences == null) return;
        final long[] account_ids = getAccountIds(this);
        final long default_id = mPreferences.getLong(KEY_DEFAULT_ACCOUNT_ID, -1);
        if (account_ids == null || account_ids.length == 0) {
            finish();
        } else if (account_ids.length > 0 && !ArrayUtils.contains(account_ids, default_id)) {
            mPreferences.edit().putLong(KEY_DEFAULT_ACCOUNT_ID, account_ids[0]).apply();
        }
    }

    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        updateActionsButton();
    }

    @Subscribe
    public void notifyUnreadCountUpdated(UnreadCountUpdatedEvent event) {
        updateUnreadCount();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.action_buttons: {
                triggerActionsClick();
                break;
            }
            case R.id.empty_tab_hint: {
                final Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, CustomTabsFragment.class.getName());
                intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.tabs);
                startActivityForResult(intent, REQUEST_SETTINGS);
                break;
            }
        }
    }

    @Override
    public void onClosed() {
        updateDrawerPercentOpen(0, true);
    }

    @Override
    public boolean onLongClick(final View v) {
        switch (v.getId()) {
            case R.id.action_buttons: {
                showMenuItemToast(v, v.getContentDescription(), true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onOpened() {
        updateDrawerPercentOpen(1, true);
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(final int position) {
        if (mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.showContent();
        }
        updateSlidingMenuTouchMode();
        updateActionsButton();
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        setControlBarVisibleAnimate(true);
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) {
            updateDrawerPercentOpen(1, false);
        } else {
            updateDrawerPercentOpen(0, false);
        }
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, QuickSearchBarActivity.class));
        return true;
    }

    public void openSearchView(final ParcelableAccount account) {
        mSelectedAccountToSearch = account;
        onSearchRequested();
    }

    public void setSystemWindowInsets(Rect insets) {
        final Fragment fragment = getLeftDrawerFragment();
        if (fragment instanceof AccountsDashboardFragment) {
            ((AccountsDashboardFragment) fragment).setStatusBarHeight(insets.top);
        }
        mColorStatusFrameLayout.setStatusBarHeight(insets.top);
    }

    public void updateUnreadCount() {
        if (mTabIndicator == null || mUpdateUnreadCountTask != null
                && mUpdateUnreadCountTask.getStatus() == AsyncTask.Status.RUNNING) return;
        mUpdateUnreadCountTask = new UpdateUnreadCountTask(this, mReadStateManager, mTabIndicator,
                mPagerAdapter.getTabs());
        AsyncTaskUtils.executeTask(mUpdateUnreadCountTask);
        mTabIndicator.setDisplayBadge(mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SWIPEBACK_ACTIVITY: {
                // closeAccountsDrawer();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        final int tabPosition = handleIntent(intent, false);
        if (tabPosition >= 0) {
            mViewPager.setCurrentItem(MathUtils.clamp(tabPosition, mPagerAdapter.getCount(), 0));
        }
    }

    @Override
    protected void onDestroy() {
		stopStreamingService();
        // Delete unused items in databases.
        cleanDatabasesByItemLimit(this);
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONDESTROY));
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.showContent();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public float getControlBarOffset() {
        if (mTabColumns > 1) {
            final ViewGroup.LayoutParams lp = mActionsButton.getLayoutParams();
            float total;
            if (lp instanceof MarginLayoutParams) {
                total = ((MarginLayoutParams) lp).bottomMargin + mActionsButton.getHeight();
            } else {
                total = mActionsButton.getHeight();
            }
            return 1 - mActionsButton.getTranslationY() / total;
        }
        final float totalHeight = getControlBarHeight();
        return 1 + mTabsContainer.getTranslationY() / totalHeight;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
            if (mSlidingMenu != null) {
                mSlidingMenu.toggle(true);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mActionBar = (Toolbar) findViewById(R.id.actionbar);
        mTabIndicator = (TabPagerIndicator) findViewById(R.id.main_tabs);
        mSlidingMenu = (HomeSlidingMenu) findViewById(R.id.home_menu);
        mViewPager = (ExtendedViewPager) findViewById(R.id.main_pager);
        mEmptyTabHint = findViewById(R.id.empty_tab_hint);
        mActionsButton = findViewById(R.id.action_buttons);
        mTabsContainer = findViewById(R.id.tabs_container);
        mTabIndicator = (TabPagerIndicator) findViewById(R.id.main_tabs);
        mActionBarOverlay = findViewById(R.id.actionbar_overlay);
        mColorStatusFrameLayout = (TintedStatusFrameLayout) findViewById(R.id.home_content);
    }

    private Fragment getKeyboardShortcutRecipient() {
        if (mSlidingMenu.isMenuShowing()) {
            return getLeftDrawerFragment();
        } else if (mSlidingMenu.isSecondaryMenuShowing()) {
            return null;
        } else {
            return getCurrentVisibleFragment();
        }
    }

    @Override
    public int getControlBarHeight() {
        return mTabIndicator.getHeight() - mTabIndicator.getStripHeight();
    }

    private boolean handleFragmentKeyboardShortcutRepeat(int keyCode, int repeatCount, @NonNull KeyEvent event) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutRepeat(keyCode, repeatCount, event);
        }
        return false;
    }

    private boolean handleFragmentKeyboardShortcutSingle(int keyCode, @NonNull KeyEvent event) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutSingle(keyCode, event);
        }
        return false;
    }

	private int handleIntent(final Intent intent, final boolean firstCreate) {
		// use packge's class loader to prevent BadParcelException
		intent.setExtrasClassLoader(getClassLoader());
		// reset intent
		setIntent(new Intent(this, HomeActivity.class));
		final String action = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(action)) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			final Bundle appSearchData = intent.getBundleExtra(SearchManager.APP_DATA);
			final long accountId;
			if (appSearchData != null && appSearchData.containsKey(EXTRA_ACCOUNT_ID)) {
				accountId = appSearchData.getLong(EXTRA_ACCOUNT_ID, -1);
			} else {
				accountId = getDefaultAccountId(this);
			}
			openSearch(this, accountId, query);
			return -1;
		}
        final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false);
		final long[] refreshedIds = intent.getLongArrayExtra(EXTRA_REFRESH_IDS);
		if (refreshedIds != null) {
			mTwitterWrapper.refreshAll(refreshedIds);
		} else if (firstCreate && refreshOnStart) {
			mTwitterWrapper.refreshAll();
		}

        final Uri uri = intent.getData();
        final String tabType = uri != null ? Utils.matchTabType(uri) : null;
        int initialTab = -1;
        if (tabType != null) {
            final long accountId = ParseUtils.parseLong(uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID));
            for (int i = mPagerAdapter.getCount() - 1; i > -1; i--) {
                final SupportTabSpec tab = mPagerAdapter.getTab(i);
                if (tabType.equals(tab.type)) {
                    initialTab = i;
                    if (hasAccountId(tab.args, accountId)) {
                        break;
                    }
                }
            }
        }
		if (initialTab != -1 && mViewPager != null) {
			// clearNotification(initial_tab);
		}
		final Intent extraIntent = intent.getParcelableExtra(EXTRA_EXTRA_INTENT);
		if (extraIntent != null && firstCreate) {
			extraIntent.setExtrasClassLoader(getClassLoader());
            startActivity(extraIntent);
		}
		return initialTab;
	}
	
    private boolean hasAccountId(Bundle args, long accountId) {
        if (args == null) return false;
        if (args.containsKey(EXTRA_ACCOUNT_ID)) {
            return args.getLong(EXTRA_ACCOUNT_ID) == accountId;
        } else if (args.containsKey(EXTRA_ACCOUNT_IDS)) {
            return ArrayUtils.contains(args.getLongArray(EXTRA_ACCOUNT_IDS), accountId);
        }
        return false;
    }

	private void initUnreadCount() {
        for (int i = 0, j = mTabIndicator.getCount(); i < j; i++) {
            mTabIndicator.setBadge(i, 0);
		}
	}

	private boolean isPushRegistered() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		return preferences != null && preferences.getBoolean(KEY_PUSH_REGISTERED, false);
	}

	private void openAccountsDrawer() {
		if (mSlidingMenu == null) return;
		mSlidingMenu.showMenu();
	}

	private boolean openSettingsWizard() {
        if (mPreferences == null || mPreferences.getBoolean(KEY_SETTINGS_WIZARD_COMPLETED, false))
            return false;
		startActivity(new Intent(this, SettingsWizardActivity.class));
		return true;
	}

    private void setTabPosition(final int initialTab) {
        final boolean rememberPosition = mPreferences.getBoolean(KEY_REMEMBER_POSITION, true);
        if (initialTab >= 0) {
            mViewPager.setCurrentItem(MathUtils.clamp(initialTab, mPagerAdapter.getCount(), 0));
        } else if (rememberPosition) {
			final int position = mPreferences.getInt(KEY_SAVED_TAB_POSITION, 0);
			mViewPager.setCurrentItem(MathUtils.clamp(position, mPagerAdapter.getCount(), 0));
		}
	}

    private void setupBars() {
        final int themeColor = getThemeColor();
		final int actionBarColor = getActionBarColor();
        final int contrastColor = TwidereColorUtils.getContrastYIQ(actionBarColor, 192);
        final int themeResId = getCurrentThemeResourceId();
        final String backgroundOption = getCurrentThemeBackgroundOption();
        final boolean isTransparent = ThemeUtils.isTransparentBackground(backgroundOption);
        final int actionBarAlpha = isTransparent ? ThemeUtils.getUserThemeBackgroundAlpha(this) : 0xFF;
        final IHomeActionButton homeActionButton = (IHomeActionButton) mActionsButton;
        mTabIndicator.setItemContext(ThemeUtils.getActionBarContext(this));
        ViewUtils.setBackground(mActionBar, ThemeUtils.getActionBarBackground(this, themeResId, actionBarColor,
                backgroundOption, true));
        //No need to differentiate between dark and light theme due to custom action bar color preference
		homeActionButton.setButtonColor(actionBarColor);
		homeActionButton.setIconColor(contrastColor, Mode.SRC_ATOP);
		mTabIndicator.setStripColor(themeColor);
		mTabIndicator.setIconColor(contrastColor);
		mTabIndicator.setLabelColor(contrastColor);
		ActivityAccessor.setTaskDescription(this, new TaskDescriptionCompat(null, null, actionBarColor));
		mColorStatusFrameLayout.setDrawColor(true);
		mColorStatusFrameLayout.setDrawShadow(false);
		mColorStatusFrameLayout.setColor(actionBarColor, actionBarAlpha);
		mColorStatusFrameLayout.setFactor(1);
        mTabIndicator.setAlpha(actionBarAlpha / 255f);
        mActionsButton.setAlpha(actionBarAlpha / 255f);
        ViewUtils.setBackground(mActionBarOverlay, ThemeUtils.getWindowContentOverlay(this));
	}

    private void setupHomeTabs() {
        mPagerAdapter.clear();
        mPagerAdapter.addTabs(CustomTabUtils.getHomeTabs(this));
        final boolean hasNoTab = mPagerAdapter.getCount() == 0;
        mEmptyTabHint.setVisibility(hasNoTab ? View.VISIBLE : View.GONE);
        mViewPager.setVisibility(hasNoTab ? View.GONE : View.VISIBLE);
    }

	private void setupSlidingMenu() {
		if (mSlidingMenu == null) return;
        final Resources res = getResources();
        final int marginThreshold = res.getDimensionPixelSize(R.dimen.default_sliding_menu_margin_threshold);
        final boolean relativeBehindWidth = res.getBoolean(R.bool.relative_behind_width);
		mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		mSlidingMenu.setShadowWidthRes(R.dimen.default_sliding_menu_shadow_width);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow_left);
		mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_right);
        if (relativeBehindWidth) {
        	mSlidingMenu.setBehindOffsetRes(R.dimen.drawer_offset_home);
        } else {
            mSlidingMenu.setBehindWidthRes(R.dimen.drawer_width_home);
        }
		mSlidingMenu.setTouchmodeMarginThreshold(marginThreshold);
		mSlidingMenu.setFadeDegree(0.5f);
		mSlidingMenu.setMenu(R.layout.drawer_home_accounts);
		mSlidingMenu.setSecondaryMenu(R.layout.drawer_home_quick_menu);
		mSlidingMenu.setOnOpenedListener(this);
		mSlidingMenu.setOnClosedListener(this);
		mLeftDrawerContainer = (LeftDrawerFrameLayout) mSlidingMenu.getMenu().findViewById(R.id.left_drawer_container);
		mRightDrawerContainer = (RightDrawerFrameLayout) mSlidingMenu.getSecondaryMenu().findViewById(
				R.id.right_drawer_container);
        final boolean isTransparentBackground = ThemeUtils.isTransparentBackground(getCurrentThemeBackgroundOption());
		mLeftDrawerContainer.setClipEnabled(isTransparentBackground);
		mLeftDrawerContainer.setScrollScale(mSlidingMenu.getBehindScrollScale());
		mRightDrawerContainer.setClipEnabled(isTransparentBackground);
		mRightDrawerContainer.setScrollScale(mSlidingMenu.getBehindScrollScale());
		mSlidingMenu.setBehindCanvasTransformer(new ListenerCanvasTransformer(this));
        final Window window = getWindow();
        ThemeUtils.applyWindowBackground(this, mSlidingMenu.getContent(),
                getCurrentThemeResourceId(), getThemeBackgroundOption(),
                getCurrentThemeBackgroundAlpha());
        window.setBackgroundDrawable(new EmptyDrawable());
	}

    private void triggerActionsClick() {
        if (mViewPager == null || mPagerAdapter == null) return;
        final int position = mViewPager.getCurrentItem();
        final SupportTabSpec tab = mPagerAdapter.getTab(position);
        if (tab == null) {
            startActivity(new Intent(INTENT_ACTION_COMPOSE));
        } else {
            if (classEquals(DirectMessagesFragment.class, tab.cls)) {
                openMessageConversation(this, -1, -1);
            } else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
                openSearchView(null);
            } else {
                startActivity(new Intent(INTENT_ACTION_COMPOSE));
            }
        }
    }

	private void updateActionsButton() {
		if (mViewPager == null || mPagerAdapter == null) return;
		final int icon, title;
		final int position = mViewPager.getCurrentItem();
		final SupportTabSpec tab = mPagerAdapter.getTab(position);
		if (tab == null) {
			title = R.string.compose;
			icon = R.drawable.ic_action_status_compose;
		} else {
			if (classEquals(DirectMessagesFragment.class, tab.cls)) {
                icon = R.drawable.ic_action_add;
                title = R.string.new_direct_message;
			} else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
				icon = R.drawable.ic_action_search;
				title = android.R.string.search_go;
			} else {
				icon = R.drawable.ic_action_status_compose;
				title = R.string.compose;
			}
		}
        if (mActionsButton instanceof IHomeActionButton) {
            final IHomeActionButton hab = (IHomeActionButton) mActionsButton;
            hab.setIcon(icon);
            hab.setTitle(title);
        }
    }

	private void updateActionsButtonStyle() {
		final boolean leftsideComposeButton = mPreferences.getBoolean(KEY_LEFTSIDE_COMPOSE_BUTTON, false);
        final FrameLayout.LayoutParams lp = (LayoutParams) mActionsButton.getLayoutParams();
        lp.gravity = Gravity.BOTTOM | (leftsideComposeButton ? Gravity.LEFT : Gravity.RIGHT);
        mActionsButton.setLayoutParams(lp);
	}

	private void updateDrawerPercentOpen(final float percentOpen, final boolean horizontalScroll) {
		if (mLeftDrawerContainer == null || mRightDrawerContainer == null) return;
		mLeftDrawerContainer.setPercentOpen(percentOpen);
		mRightDrawerContainer.setPercentOpen(percentOpen);
	}

	private void updateSlidingMenuTouchMode() {
		if (mViewPager == null || mSlidingMenu == null) return;
		final int position = mViewPager.getCurrentItem();
		final boolean pagingEnabled = mViewPager.isEnabled();
		final boolean atFirstOrLast = position == 0 || position == mPagerAdapter.getCount() - 1;
		final int mode = !pagingEnabled || atFirstOrLast ? SlidingMenu.TOUCHMODE_FULLSCREEN
				: SlidingMenu.TOUCHMODE_MARGIN;
		mSlidingMenu.setTouchModeAbove(mode);
	}

	private void startStreamingService() {
		if (!isStreamingServiceRunning) {
			final Intent serviceIntent = new Intent(this, StreamingService.class);
			startService(serviceIntent);
			isStreamingServiceRunning = true;
		}
		sendBroadcast(new Intent(BROADCAST_REFRESH_STREAMING_SERVICE));
	}

	private void stopStreamingService() {
		if (isStreamingServiceRunning) {
			final Intent serviceIntent = new Intent(this, StreamingService.class);
			stopService(serviceIntent);
			isStreamingServiceRunning = false;
		}
	}

	private static final class AccountChangeObserver extends ContentObserver {
		private final HomeActivity mActivity;

		public AccountChangeObserver(final HomeActivity activity, final Handler handler) {
			super(handler);
			mActivity = activity;
		}

		@Override
		public void onChange(final boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(final boolean selfChange, final Uri uri) {
			mActivity.notifyAccountsChanged();
			mActivity.updateUnreadCount();
		}
	}

	private static class ListenerCanvasTransformer implements CanvasTransformer {
		private final HomeActivity mHomeActivity;

		public ListenerCanvasTransformer(final HomeActivity homeActivity) {
			mHomeActivity = homeActivity;
		}

		@Override
		public void transformCanvas(final Canvas canvas, final float percentOpen) {
			mHomeActivity.updateDrawerPercentOpen(percentOpen, true);
		}

	}

    private static class UpdateUnreadCountTask extends AsyncTask<Object, Object, Map<SupportTabSpec, Integer>> {
		private final Context mContext;
        private final ReadStateManager mReadStateManager;
		private final TabPagerIndicator mIndicator;
        private final List<SupportTabSpec> mTabs;

        UpdateUnreadCountTask(final Context context, final ReadStateManager manager, final TabPagerIndicator indicator, final List<SupportTabSpec> tabs) {
            mContext = context;
            mReadStateManager = manager;
			mIndicator = indicator;
            mTabs = Collections.unmodifiableList(tabs);
		}

		@Override
        protected Map<SupportTabSpec, Integer> doInBackground(final Object... params) {
            final Map<SupportTabSpec, Integer> result = new HashMap<>();
            for (SupportTabSpec spec : mTabs) {
                switch (spec.type) {
                    case TAB_TYPE_HOME_TIMELINE: {
                        final long[] accountIds = Utils.getAccountIds(spec.args);
                        final String tagWithAccounts = Utils.getReadPositionTagWithAccounts(mContext, true, spec.tag, accountIds);
                        final long position = mReadStateManager.getPosition(tagWithAccounts);
                        result.put(spec, Utils.getStatusesCount(mContext, Statuses.CONTENT_URI, position, accountIds));
                        break;
                    }
                    case TAB_TYPE_MENTIONS_TIMELINE: {
                        final long[] accountIds = Utils.getAccountIds(spec.args);
                        final String tagWithAccounts = Utils.getReadPositionTagWithAccounts(mContext, true, spec.tag, accountIds);
                        final long position = mReadStateManager.getPosition(tagWithAccounts);
                        result.put(spec, Utils.getStatusesCount(mContext, Mentions.CONTENT_URI, position, accountIds));
                        break;
                    }
                    case TAB_TYPE_DIRECT_MESSAGES: {
                        break;
                    }
                }
			}
			return result;
		}

		@Override
        protected void onPostExecute(final Map<SupportTabSpec, Integer> result) {
            mIndicator.clearBadge();
            for (Entry<SupportTabSpec, Integer> entry : result.entrySet()) {
                final SupportTabSpec key = entry.getKey();
                mIndicator.setBadge(key.position, entry.getValue());
			}
		}

    }


}