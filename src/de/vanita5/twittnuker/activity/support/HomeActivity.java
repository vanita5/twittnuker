/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import static de.vanita5.twittnuker.util.CompareUtils.classEquals;
import static de.vanita5.twittnuker.util.CustomTabUtils.getAddedTabPosition;
import static de.vanita5.twittnuker.util.CustomTabUtils.getHomeTabs;
import static de.vanita5.twittnuker.util.Utils.cleanDatabasesByItemLimit;
import static de.vanita5.twittnuker.util.Utils.getAccountIds;
import static de.vanita5.twittnuker.util.Utils.getDefaultAccountId;
import static de.vanita5.twittnuker.util.Utils.isDatabaseReady;
import static de.vanita5.twittnuker.util.Utils.openDirectMessagesConversation;
import static de.vanita5.twittnuker.util.Utils.openSearch;
import static de.vanita5.twittnuker.util.Utils.showMenuItemToast;
import static de.vanita5.twittnuker.util.Utils.getTabDisplayOptionInt;
import static de.vanita5.twittnuker.util.Utils.setMenuItemAvailability;

import java.util.ArrayList;
import java.util.List;

import de.vanita5.twittnuker.activity.SettingsWizardActivity;
import de.vanita5.twittnuker.service.StreamingService;
import de.vanita5.twittnuker.util.ActivityAccessor;
import de.vanita5.twittnuker.util.ActivityAccessor.TaskDescriptionCompat;
import de.vanita5.twittnuker.util.FlymeUtils;
import de.vanita5.twittnuker.util.HotKeyHandler;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.HomeSlidingMenu;
import de.vanita5.twittnuker.view.RightDrawerFrameLayout;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;

import de.vanita5.twittnuker.adapter.support.SupportTabsAdapter;
import de.vanita5.twittnuker.fragment.iface.IBaseFragment;
import de.vanita5.twittnuker.fragment.iface.IBasePullToRefreshFragment;
import de.vanita5.twittnuker.fragment.iface.RefreshScrollTopInterface;
import de.vanita5.twittnuker.fragment.iface.SupportFragmentCallback;
import de.vanita5.twittnuker.fragment.support.DirectMessagesFragment;
import de.vanita5.twittnuker.fragment.support.TrendsSuggectionsFragment;
import de.vanita5.twittnuker.graphic.EmptyDrawable;
import de.vanita5.twittnuker.model.Account;
import de.vanita5.twittnuker.model.SupportTabSpec;
import de.vanita5.twittnuker.provider.TweetStore.Accounts;
import de.vanita5.twittnuker.task.AsyncTask;
import de.vanita5.twittnuker.util.ArrayUtils;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.MathUtils;
import de.vanita5.twittnuker.util.MultiSelectEventHandler;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.UnreadCountUtils;
import de.vanita5.twittnuker.util.accessor.ViewAccessor;
import de.vanita5.twittnuker.view.ExtendedViewPager;
import de.vanita5.twittnuker.view.LeftDrawerFrameLayout;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.view.TabPagerIndicator;
import de.vanita5.twittnuker.view.iface.IHomeActionButton;

public class HomeActivity extends BaseSupportActivity implements OnClickListener, OnPageChangeListener,
		SupportFragmentCallback, SlidingMenu.OnOpenedListener, SlidingMenu.OnClosedListener,
        OnLongClickListener {

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				updateActionsButton();
                updateSmartBar();
			} else if (BROADCAST_UNREAD_COUNT_UPDATED.equals(action)) {
				updateUnreadCount();
			}
		}

	};


	private final Handler mHandler = new Handler();

	private final ContentObserver mAccountChangeObserver = new AccountChangeObserver(this, mHandler);

	private final ArrayList<SupportTabSpec> mCustomTabs = new ArrayList<SupportTabSpec>();

	private final SparseArray<Fragment> mAttachedFragments = new SparseArray<Fragment>();
	private Account mSelectedAccountToSearch;

	private SharedPreferences mPreferences;

	private AsyncTwitterWrapper mTwitterWrapper;

	private MultiSelectEventHandler mMultiSelectHandler;
	private HotKeyHandler mHotKeyHandler;

	private SupportTabsAdapter mPagerAdapter;

	private ExtendedViewPager mViewPager;
    private TabPagerIndicator mTabIndicator;
	private HomeSlidingMenu mSlidingMenu;
	private View mEmptyTab;
	private View mEmptyTabHint;
    private ProgressBar mSmartBarProgress;
    private View mActionsButton;
    private View mTabsContainer;
    private View mActionBarOverlay;
	private LeftDrawerFrameLayout mLeftDrawerContainer;
	private RightDrawerFrameLayout mRightDrawerContainer;

	private Fragment mCurrentVisibleFragment;
	private UpdateUnreadCountTask mUpdateUnreadCountTask;

	private int mTabDisplayOption;
	private boolean isStreamingServiceRunning = false;

	private float mPagerPosition;

	public void closeAccountsDrawer() {
		if (mSlidingMenu == null) return;
		mSlidingMenu.showContent();
	}

	@Override
	public Fragment getCurrentVisibleFragment() {
		return mCurrentVisibleFragment;
	}

	public SlidingMenu getSlidingMenu() {
		return mSlidingMenu;
	}

	public ViewPager getViewPager() {
		return mViewPager;
	}

	@Override
	public void setControlBarOffset(float offset) {
        mTabsContainer.setTranslationY(getControlBarHeight() * (offset - 1));
        mActionsButton.setTranslationY(mActionsButton.getHeight() * (1 - offset));
        notifyControlBarOffsetChanged();
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

	@Override
	public void onAttachFragment(final Fragment fragment) {
		if (fragment instanceof IBaseFragment && ((IBaseFragment) fragment).getTabPosition() != -1) {
			mAttachedFragments.put(((IBaseFragment) fragment).getTabPosition(), fragment);
		}
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
	public void onClick(final View v) {
		switch (v.getId()) {
            case R.id.actions_button: {
                triggerActionsClick();
				break;
			}
		}
	}

	@Override
	public void onClosed() {
		updateDrawerPercentOpen(0, true);
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mSlidingMenu = (HomeSlidingMenu) findViewById(R.id.home_menu);
		mViewPager = (ExtendedViewPager) findViewById(R.id.main_pager);
		mEmptyTab = findViewById(R.id.empty_tab);
		mEmptyTabHint = findViewById(R.id.empty_tab_hint);
        mActionsButton = findViewById(R.id.actions_button);
        mTabsContainer = findViewById(R.id.tabs_container);
        mTabIndicator = (TabPagerIndicator) findViewById(R.id.main_tabs);
        mActionBarOverlay = findViewById(R.id.actionbar_overlay);
	}

	@Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        final MenuItem itemProgress = menu.findItem(MENU_PROGRESS);
        mSmartBarProgress = (ProgressBar) itemProgress.getActionView().findViewById(android.R.id.progress);
		updateActionsButton();
		return true;
	}

	@Override
	public void onDetachFragment(final Fragment fragment) {
		if (fragment instanceof IBaseFragment && ((IBaseFragment) fragment).getTabPosition() != -1) {
			mAttachedFragments.remove(((IBaseFragment) fragment).getTabPosition());
		}
	}

	@Override
	public boolean getSystemWindowsInsets(Rect insets) {
        final int height = mTabIndicator != null ? mTabIndicator.getHeight() : 0;
		insets.top = height != 0 ? height : Utils.getActionBarHeight(this);
		return true;
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
			default: {
				if (mHotKeyHandler.handleKey(keyCode, event)) return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onLongClick(final View v) {
		switch (v.getId()) {
			case R.id.actions_button: {
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
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
		final float pagerPosition = position + positionOffset;
		if (!Float.isNaN(mPagerPosition)) {
			setControlBarOffset(MathUtils.clamp(getControlBarOffset() + Math.abs(pagerPosition - mPagerPosition), 1, 0));
		}
		mPagerPosition = pagerPosition;
	}

	@Override
	public void onPageScrollStateChanged(final int state) {

	}

	@Override
	public void onPageSelected(final int position) {
		if (mSlidingMenu.isMenuShowing()) {
			mSlidingMenu.showContent();
		}
		updateSlidingMenuTouchMode();
		updateActionsButton();
        updateSmartBar();
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
        if (mViewPager == null || mPagerAdapter == null) return false;
		final boolean useBottomActionItems = FlymeUtils.hasSmartBar();
        setMenuItemAvailability(menu, MENU_ACTIONS, useBottomActionItems);
        setMenuItemAvailability(menu, MENU_PROGRESS, useBottomActionItems);
        if (useBottomActionItems) {
            final int icon, title;
            final int position = mViewPager.getCurrentItem();
            final SupportTabSpec tab = mPagerAdapter.getTab(position);
            if (tab == null) {
                title = R.string.compose;
				icon = R.drawable.ic_action_status_compose;
            } else {
                if (classEquals(DirectMessagesFragment.class, tab.cls)) {
					icon = R.drawable.ic_action_new_message;
                    title = R.string.new_direct_message;
                } else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
					icon = R.drawable.ic_action_search;
                    title = android.R.string.search_go;
                } else {
					icon = R.drawable.ic_action_status_compose;
                    title = R.string.compose;
                }
            }
            final MenuItem actionsItem = menu.findItem(MENU_ACTIONS);
            actionsItem.setIcon(icon);
            actionsItem.setTitle(title);
        }
		return true;
	}

	@Override
	public boolean onSearchRequested() {
		final Bundle appSearchData = new Bundle();
		if (mSelectedAccountToSearch != null) {
			appSearchData.putLong(EXTRA_ACCOUNT_ID, mSelectedAccountToSearch.account_id);
		}
		startSearch(null, false, appSearchData, false);
		return true;
	}

	@Override
	public void onSetUserVisibleHint(final Fragment fragment, final boolean isVisibleToUser) {
		if (isVisibleToUser) {
			mCurrentVisibleFragment = fragment;
		}
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

	public void openSearchView(final Account account) {
		mSelectedAccountToSearch = account;
		onSearchRequested();
	}

	public void setHomeProgressBarIndeterminateVisibility(final boolean visible) {
		
	}

	@Override
	public boolean shouldOverrideActivityAnimation() {
		return false;
	}

	@Override
	public float getControlBarOffset() {
		final float totalHeight = getControlBarHeight();
        return 1 + mTabsContainer.getTranslationY() / totalHeight;
	}

	@Override
	public int getControlBarHeight() {
        return mTabIndicator.getHeight() - mTabIndicator.getStripHeight();
	}

	@Override
	public boolean triggerRefresh(final int position) {
		final Fragment f = mAttachedFragments.get(position);
		return f instanceof RefreshScrollTopInterface && !f.isDetached()
				&& ((RefreshScrollTopInterface) f).triggerRefresh();
	}

	public void updateUnreadCount() {
        if (mTabIndicator == null || mUpdateUnreadCountTask != null
				&& mUpdateUnreadCountTask.getStatus() == AsyncTask.Status.RUNNING) return;
        mUpdateUnreadCountTask = new UpdateUnreadCountTask(mTabIndicator);
		mUpdateUnreadCountTask.execute();
		mTabIndicator.setDisplayBadge(mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
	}

	@Override
	protected IBasePullToRefreshFragment getCurrentPullToRefreshFragment() {
		if (mCurrentVisibleFragment instanceof IBasePullToRefreshFragment)
			return (IBasePullToRefreshFragment) mCurrentVisibleFragment;
		else if (mCurrentVisibleFragment instanceof SupportFragmentCallback) {
			final Fragment curr = ((SupportFragmentCallback) mCurrentVisibleFragment).getCurrentVisibleFragment();
			if (curr instanceof IBasePullToRefreshFragment) return (IBasePullToRefreshFragment) curr;
		}
		return null;
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

    /**
     * Called when the activity is first created.
     */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setUiOptions(getWindow());
		super.onCreate(savedInstanceState);
		if (!isDatabaseReady(this)) {
			Toast.makeText(this, R.string.preparing_database_toast, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mTwitterWrapper = getTwitterWrapper();
		mMultiSelectHandler = new MultiSelectEventHandler(this);
		mHotKeyHandler = new HotKeyHandler(this);
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
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
		setContentView(R.layout.activity_home);
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONCREATE));
//		final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false);
		final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false); //FIXME workaround
        mTabDisplayOption = getTabDisplayOptionInt(this);
		final int initialTabPosition = handleIntent(intent, savedInstanceState == null);

        ThemeUtils.applyBackground(mTabIndicator);
        mPagerAdapter = new SupportTabsAdapter(this, getSupportFragmentManager(), mTabIndicator, 1);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
        mTabIndicator.setViewPager(mViewPager);
        mTabIndicator.setOnPageChangeListener(this);
        if (mTabDisplayOption != 0) {
            mTabIndicator.setDisplayLabel((mTabDisplayOption & VALUE_TAB_DIPLAY_OPTION_CODE_LABEL) != 0);
            mTabIndicator.setDisplayIcon((mTabDisplayOption & VALUE_TAB_DIPLAY_OPTION_CODE_ICON) != 0);
        } else {
            mTabIndicator.setDisplayLabel(false);
            mTabIndicator.setDisplayIcon(true);
        }
//        mTabIndicator.setDisplayBadge(mPreferences.getBoolean(KEY_UNREAD_COUNT, true));
        mActionsButton.setOnClickListener(this);
        mActionsButton.setOnLongClickListener(this);
		setTabPosition(initialTabPosition);
		setupSlidingMenu();
        setupBars();
		initUnreadCount();
		updateActionsButton();
        updateSmartBar();
		updateSlidingMenuTouchMode();

		if (savedInstanceState == null) {
			if (refreshOnStart) {
				mTwitterWrapper.refreshAll();
			}
			if (intent.getBooleanExtra(EXTRA_OPEN_ACCOUNTS_DRAWER, false)) {
				openAccountsDrawer();
			}
		}
		mPagerPosition = Float.NaN;
        setupHomeTabs();
    }

    private void setupBars() {
        final int themeColor = getThemeColor();
        final int actionBarColor = getActionBarColor();
		final int themeResId = getCurrentThemeResourceId();
        final boolean isTransparent = ThemeUtils.isTransparentBackground(themeResId);
        final int actionBarAlpha = isTransparent ? ThemeUtils.getUserThemeBackgroundAlpha(this) : 0xFF;
        final IHomeActionButton homeActionButton = (IHomeActionButton) mActionsButton;
        mTabIndicator.setItemContext(ThemeUtils.getActionBarContext(this));
        if (ThemeUtils.isColoredActionBar(themeResId)) {
            final int contrastColor = Utils.getContrastYIQ(actionBarColor, 192);
            ViewAccessor.setBackground(mTabIndicator, new ColorDrawable(actionBarColor));
            homeActionButton.setButtonColor(actionBarColor);
            homeActionButton.setIconColor(contrastColor, Mode.SRC_ATOP);
            mTabIndicator.setStripColor(themeColor);
            mTabIndicator.setIconColor(contrastColor);
            ActivityAccessor.setTaskDescription(this, new TaskDescriptionCompat(null, null, actionBarColor));
        } else {
            final int backgroundColor = ThemeUtils.getThemeBackgroundColor(mTabIndicator.getItemContext());
            final int foregroundColor = ThemeUtils.getThemeForegroundColor(mTabIndicator.getItemContext());
            ViewAccessor.setBackground(mTabIndicator, ThemeUtils.getActionBarBackground(this, themeResId));
            homeActionButton.setButtonColor(backgroundColor);
            homeActionButton.setIconColor(foregroundColor, Mode.SRC_ATOP);
            mTabIndicator.setStripColor(themeColor);
            mTabIndicator.setIconColor(foregroundColor);
	    }
        mTabIndicator.setAlpha(actionBarAlpha / 255f);
        mActionsButton.setAlpha(actionBarAlpha / 255f);
        ViewAccessor.setBackground(mActionBarOverlay, ThemeUtils.getWindowContentOverlay(this));
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
	protected void onNewIntent(final Intent intent) {
		final int tab_position = handleIntent(intent, false);
		if (tab_position >= 0) {
			mViewPager.setCurrentItem(MathUtils.clamp(tab_position, mPagerAdapter.getCount(), 0));
		}
	}

    @Override
    protected void onPause() {
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONPAUSE));
        super.onPause();
    }

	@Override
	protected void onResume() {
		super.onResume();
        sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONRESUME));
		mViewPager.setEnabled(!mPreferences.getBoolean(KEY_DISABLE_TAB_SWIPE, false));
		invalidateOptionsMenu();
		updateActionsButtonStyle();
		updateActionsButton();
        updateSmartBar();
		updateSlidingMenuTouchMode();

		if (mPreferences.getBoolean(KEY_STREAMING_ENABLED, true)) {
			startStreamingService();
		} else {
			stopStreamingService();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mMultiSelectHandler.dispatchOnStart();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTART));
		final ContentResolver resolver = getContentResolver();
		resolver.registerContentObserver(Accounts.CONTENT_URI, true, mAccountChangeObserver);
		final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
		filter.addAction(BROADCAST_UNREAD_COUNT_UPDATED);
		registerReceiver(mStateReceiver, filter);
		if (isTabsChanged(getHomeTabs(this)) || getTabDisplayOptionInt(this) != mTabDisplayOption) {
			restart();
		}
		updateUnreadCount();
		if (mPreferences.getBoolean(KEY_STREAMING_ENABLED, true)) {
			startStreamingService();
		} else {
			stopStreamingService();
		}
	}

	@Override
	protected void onStop() {
		mMultiSelectHandler.dispatchOnStop();
		unregisterReceiver(mStateReceiver);
		final ContentResolver resolver = getContentResolver();
		resolver.unregisterContentObserver(mAccountChangeObserver);
		mPreferences.edit().putInt(KEY_SAVED_TAB_POSITION, mViewPager.getCurrentItem()).apply();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTOP));

		super.onStop();
	}

	protected void setPagingEnabled(final boolean enabled) {
        if (mTabIndicator != null && mViewPager != null) {
			mViewPager.setEnabled(!mPreferences.getBoolean(KEY_DISABLE_TAB_SWIPE, false));
            mTabIndicator.setEnabled(enabled);
		}
	}

    @Override
    protected boolean shouldSetWindowBackground() {
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
//		final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false); FIXME workaround
		final boolean refreshOnStart = false;
		final long[] refreshedIds = intent.getLongArrayExtra(EXTRA_IDS);
		if (refreshedIds != null) {
			mTwitterWrapper.refreshAll(refreshedIds);
		} else if (firstCreate && refreshOnStart) {
			mTwitterWrapper.refreshAll();
		}

		final int tab = intent.getIntExtra(EXTRA_INITIAL_TAB, -1);
		final int initialTab = tab != -1 ? tab : getAddedTabPosition(this, intent.getStringExtra(EXTRA_TAB_TYPE));
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
	
	private boolean hasActivatedTask() {
		if (mTwitterWrapper == null) return false;
		return mTwitterWrapper.hasActivatedTask();
	}

    private void setupHomeTabs() {
		final List<SupportTabSpec> tabs = getHomeTabs(this);
		mCustomTabs.clear();
		mCustomTabs.addAll(tabs);
		mPagerAdapter.clear();
		mPagerAdapter.addTabs(tabs);
		mEmptyTab.setVisibility(tabs.isEmpty() ? View.VISIBLE : View.GONE);
        mEmptyTabHint.setVisibility(tabs.isEmpty() ? View.VISIBLE : View.GONE);
        mViewPager.setVisibility(tabs.isEmpty() ? View.GONE : View.VISIBLE);
	}

	private void initUnreadCount() {
        for (int i = 0, j = mTabIndicator.getCount(); i < j; i++) {
            mTabIndicator.setBadge(i, 0);
		}
	}

	private boolean isTabsChanged(final List<SupportTabSpec> tabs) {
		if (mCustomTabs.size() == 0 && tabs == null) return false;
		if (mCustomTabs.size() != tabs.size()) return true;
		for (int i = 0, size = mCustomTabs.size(); i < size; i++) {
			if (!mCustomTabs.get(i).equals(tabs.get(i))) return true;
		}
		return false;
	}

	private void openAccountsDrawer() {
		if (mSlidingMenu == null) return;
		mSlidingMenu.showMenu();
	}

	private boolean openSettingsWizard() {
		if (mPreferences == null || mPreferences.getBoolean(KEY_SETTINGS_WIZARD_COMPLETED, false)) return false;
		startActivity(new Intent(this, SettingsWizardActivity.class));
		return true;
	}

	private void setTabPosition(final int initial_tab) {
		final boolean remember_position = mPreferences.getBoolean(KEY_REMEMBER_POSITION, true);
		if (initial_tab >= 0) {
			mViewPager.setCurrentItem(MathUtils.clamp(initial_tab, mPagerAdapter.getCount(), 0));
		} else if (remember_position) {
			final int position = mPreferences.getInt(KEY_SAVED_TAB_POSITION, 0);
			mViewPager.setCurrentItem(MathUtils.clamp(position, mPagerAdapter.getCount(), 0));
		}
	}

	private void setUiOptions(final Window window) {
		if (FlymeUtils.hasSmartBar()) {
			window.setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
		}
	}

	private void setupSlidingMenu() {
		if (mSlidingMenu == null) return;
		final int marginThreshold = getResources().getDimensionPixelSize(R.dimen.default_sliding_menu_margin_threshold);
		mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		mSlidingMenu.setShadowWidthRes(R.dimen.default_sliding_menu_shadow_width);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow_left);
		mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_right);
		mSlidingMenu.setBehindWidthRes(R.dimen.drawer_width_home);
		mSlidingMenu.setTouchmodeMarginThreshold(marginThreshold);
		mSlidingMenu.setFadeDegree(0.5f);
		mSlidingMenu.setMenu(R.layout.drawer_home_accounts);
		mSlidingMenu.setSecondaryMenu(R.layout.drawer_home_quick_menu);
		mSlidingMenu.setOnOpenedListener(this);
		mSlidingMenu.setOnClosedListener(this);
		mLeftDrawerContainer = (LeftDrawerFrameLayout) mSlidingMenu.getMenu().findViewById(R.id.left_drawer_container);
		mRightDrawerContainer = (RightDrawerFrameLayout) mSlidingMenu.getSecondaryMenu().findViewById(
				R.id.right_drawer_container);
		final boolean isTransparentBackground = ThemeUtils.isTransparentBackground(this);
		mLeftDrawerContainer.setClipEnabled(isTransparentBackground);
		mLeftDrawerContainer.setScrollScale(mSlidingMenu.getBehindScrollScale());
		mRightDrawerContainer.setClipEnabled(isTransparentBackground);
		mRightDrawerContainer.setScrollScale(mSlidingMenu.getBehindScrollScale());
		mSlidingMenu.setBehindCanvasTransformer(new ListenerCanvasTransformer(this));
        final Window window = getWindow();
		final Drawable windowBackground = ThemeUtils.getWindowBackground(this, getCurrentThemeResourceId());
		ViewAccessor.setBackground(mSlidingMenu.getContent(), windowBackground);
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
                openDirectMessagesConversation(this, -1, -1);
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
				icon = R.drawable.ic_action_new_message;
                title = R.string.new_direct_message;
			} else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
				icon = R.drawable.ic_action_search;
				title = android.R.string.search_go;
			} else {
				icon = R.drawable.ic_action_status_compose;
				title = R.string.compose;
			}
		}
		final boolean hasActivatedTask = hasActivatedTask();
        if (mActionsButton instanceof IHomeActionButton) {
            final IHomeActionButton hab = (IHomeActionButton) mActionsButton;
            hab.setIcon(icon);
            hab.setTitle(title);
            hab.setShowProgress(hasActivatedTask);
        }
        if (mSmartBarProgress != null) {
            mSmartBarProgress.setVisibility(hasActivatedTask ? View.VISIBLE : View.INVISIBLE);
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

    private void updateSmartBar() {
		final boolean useBottomActionItems = FlymeUtils.hasSmartBar();
        if (useBottomActionItems) {
            invalidateOptionsMenu();
        }
    }

    public void moveControlBarBy(float delta) {
		final int min = -getControlBarHeight(), max = 0;
		mTabsContainer.setTranslationY(MathUtils.clamp(mTabsContainer.getTranslationY() + delta, max, min));
		final ViewGroup.LayoutParams ablp = mActionsButton.getLayoutParams();
		final int totalHeight;
		if (ablp instanceof MarginLayoutParams) {
			final MarginLayoutParams mlp = (MarginLayoutParams) ablp;
			totalHeight = mActionsButton.getHeight() + mlp.topMargin + mlp.bottomMargin;
		} else {
			totalHeight = mActionsButton.getHeight();
		}
		mActionsButton.setTranslationY(MathUtils.clamp(mActionsButton.getTranslationY() - delta, totalHeight, 0));
		notifyControlBarOffsetChanged();
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

	private static class UpdateUnreadCountTask extends AsyncTask<Void, Void, int[]> {
		private final Context mContext;
		private final TabPagerIndicator mIndicator;

        UpdateUnreadCountTask(final TabPagerIndicator indicator) {
			mIndicator = indicator;
			mContext = indicator.getContext();
		}

		@Override
		protected int[] doInBackground(final Void... params) {
            final int tabCount = mIndicator.getCount();
            final int[] result = new int[tabCount];
            for (int i = 0, j = tabCount; i < j; i++) {
				result[i] = UnreadCountUtils.getUnreadCount(mContext, i);
			}
			return result;
		}

		@Override
		protected void onPostExecute(final int[] result) {
			final int tabCount = mIndicator.getCount();
			if (result == null || result.length != tabCount) return;
			for (int i = 0; i < tabCount; i++) {
				mIndicator.setBadge(i, result[i]);
			}
		}

	}

}
