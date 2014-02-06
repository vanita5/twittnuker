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

package de.vanita5.twittnuker.activity;

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

import de.vanita5.twittnuker.activity.support.BaseSupportActivity;
import de.vanita5.twittnuker.util.Utils;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.readystatesoftware.viewbadger.BadgeView;

import de.vanita5.twittnuker.activity.support.SignInActivity;
import de.vanita5.twittnuker.adapter.support.SupportTabsAdapter;
import de.vanita5.twittnuker.app.TwittnukerApplication;
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
import de.vanita5.twittnuker.provider.TweetStore.DirectMessages;
import de.vanita5.twittnuker.provider.TweetStore.Mentions;
import de.vanita5.twittnuker.provider.TweetStore.Statuses;
import de.vanita5.twittnuker.task.AsyncTask;
import de.vanita5.twittnuker.util.ArrayUtils;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.MathUtils;
import de.vanita5.twittnuker.util.MultiSelectEventHandler;
import de.vanita5.twittnuker.util.SmartBarUtils;
import de.vanita5.twittnuker.util.SwipebackActivityUtils;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.UnreadCountUtils;
import de.vanita5.twittnuker.util.accessor.ViewAccessor;
import de.vanita5.twittnuker.view.ExtendedViewPager;
import de.vanita5.twittnuker.view.HomeActionsActionView;
import de.vanita5.twittnuker.view.LeftDrawerFrameLayout;
import de.vanita5.twittnuker.view.TabPageIndicator;
import de.vanita5.twittnuker.R;

public class HomeActivity extends BaseSupportActivity implements OnClickListener, OnPageChangeListener,
		SupportFragmentCallback, SlidingMenu.OnOpenedListener, SlidingMenu.OnClosedListener, OnLongClickListener {

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				updateActionsButton();
                updateSmartBar();
			} else if (BROADCAST_UNREAD_COUNT_UPDATED.equals(action)) {
				updateUnreadCount();
			} else if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
				if (!mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false) && !intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
					closeStream();
				}
			} else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
				if(mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false)) {
					return;
				}
				
				SupplicantState supplicantState;
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				supplicantState = wifiInfo.getSupplicantState();
				
				if (SupplicantState.COMPLETED.equals(supplicantState)) {
					connectToStream();
				} else {
					closeStream();
				}
			}
		}

	};
	
	private static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI };
	private static final Uri[] MESSAGES_URIS = new Uri[] { DirectMessages.Inbox.CONTENT_URI, DirectMessages.Outbox.CONTENT_URI };

	private final Handler mHandler = new Handler();

	private final ContentObserver mAccountChangeObserver = new AccountChangeObserver(this, mHandler);

	private final ArrayList<SupportTabSpec> mCustomTabs = new ArrayList<SupportTabSpec>();
	private final SparseArray<Fragment> mAttachedFragments = new SparseArray<Fragment>();

	private Account mSelectedAccountToSearch;

	private SharedPreferences mPreferences;

	private AsyncTwitterWrapper mTwitterWrapper;

	private MultiSelectEventHandler mMultiSelectHandler;
	private ActionBar mActionBar;
	private SupportTabsAdapter mPagerAdapter;
	private ExtendedViewPager mViewPager;

	private TabPageIndicator mIndicator;
	private SlidingMenu mSlidingMenu;

	private View mEmptyTabHint;
    private ProgressBar mSmartBarProgress;
    private HomeActionsActionView mActionsButton, mBottomActionsButton;

	private LeftDrawerFrameLayout mLeftDrawerContainer;
	private Fragment mCurrentVisibleFragment;

	private UpdateUnreadCountTask mUpdateUnreadCountTask;

	private final Rect mRect = new Rect();
	
	private boolean mBottomComposeButton;

    private int mTabDisplayOption;
	private boolean mStreaming;
	protected boolean hasStreamLoaded;
	
	protected TwitterStream twitterStream;

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

	public void notifyAccountsChanged() {
		if (mPreferences == null) return;
		final long[] account_ids = getAccountIds(this);
		final long default_id = mPreferences.getLong(KEY_DEFAULT_ACCOUNT_ID, -1);
		if (account_ids == null || account_ids.length == 0) {
			finish();
		} else if (account_ids.length > 0 && !ArrayUtils.contains(account_ids, default_id)) {
			mPreferences.edit().putLong(KEY_DEFAULT_ACCOUNT_ID, account_ids[0]).commit();
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

//	@Override
//	public void onBackStackChanged() {
//		super.onBackStackChanged();
//		if (!isDualPaneMode()) return;
//		final FragmentManager fm = getSupportFragmentManager();
//		setPagingEnabled(!isLeftPaneUsed());
//		final int count = fm.getBackStackEntryCount();
//		if (count == 0) {
//			showLeftPane();
//		}
//		updateActionsButton();
//		updateActionsButtonStyle();
//		updateSlidingMenuTouchMode();
//	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
            case R.id.actions:
			case R.id.actions_button:
			case R.id.actions_button_bottom: {
                triggerActionsClick();
				break;
			}
		}
	}

	@Override
	public void onClosed() {
		updatePullToRefreshLayoutScroll(0, true);
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mViewPager = (ExtendedViewPager) findViewById(R.id.main_pager);
		mEmptyTabHint = findViewById(R.id.empty_tab_hint);
		mBottomActionsButton = (HomeActionsActionView) findViewById(R.id.actions_button_bottom);
		if (mSlidingMenu == null) {
			mSlidingMenu = new SlidingMenu(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_home, menu);
        final MenuItem itemProgress = menu.findItem(MENU_PROGRESS);
        mSmartBarProgress = (ProgressBar) itemProgress.getActionView().findViewById(android.R.id.progress);
		updateActionsButton();
        updateSmartBar();
		return true;
	}

	@Override
	public void onDetachFragment(final Fragment fragment) {
		if (fragment instanceof IBaseFragment && ((IBaseFragment) fragment).getTabPosition() != -1) {
			mAttachedFragments.remove(((IBaseFragment) fragment).getTabPosition());
		}
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
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
	public boolean onLongClick(final View v) {
		switch (v.getId()) {
			case R.id.actions:
			case R.id.actions_button: {
				showMenuItemToast(v, v.getContentDescription());
				return true;
			}
			case R.id.actions_button_bottom: {
				showMenuItemToast(v, v.getContentDescription(), true);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onOpened() {
		updatePullToRefreshLayoutScroll(1, true);
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
				} else if (count == 0 && !mSlidingMenu.isMenuShowing()) {
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
        final boolean useBottomActionItems = SmartBarUtils.hasSmartBar() && isBottomComposeButton();
        setMenuItemAvailability(menu, MENU_ACTIONS, useBottomActionItems);
        setMenuItemAvailability(menu, MENU_PROGRESS, useBottomActionItems);
        if (useBottomActionItems) {
            final int icon, title;
            final int position = mViewPager.getCurrentItem();
            final SupportTabSpec tab = mPagerAdapter.getTab(position);
            if (tab == null) {
                title = R.string.compose;
                icon = R.drawable.ic_iconic_action_new_message;
            } else {
                if (classEquals(DirectMessagesFragment.class, tab.cls)) {
                    icon = R.drawable.ic_iconic_action_new_message;
                    title = R.string.compose;
                } else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
                    icon = R.drawable.ic_iconic_action_search;
                    title = android.R.string.search_go;
                } else {
                    icon = R.drawable.ic_iconic_action_new_message;
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
			updatePullToRefreshLayoutScroll(1, false);
		} else {
			updatePullToRefreshLayoutScroll(0, false);
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
	public boolean triggerRefresh(final int position) {
		final Fragment f = mAttachedFragments.get(position);
		return f instanceof RefreshScrollTopInterface && !f.isDetached()
				&& ((RefreshScrollTopInterface) f).triggerRefresh();
	}

	public void updateUnreadCount() {
		if (mIndicator == null || mUpdateUnreadCountTask != null
				&& mUpdateUnreadCountTask.getStatus() == AsyncTask.Status.RUNNING) return;
		mUpdateUnreadCountTask = new UpdateUnreadCountTask(mIndicator, mPreferences.getBoolean(
				KEY_UNREAD_COUNT, true));
		mUpdateUnreadCountTask.execute();
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

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		mBottomComposeButton = isBottomComposeButton();
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
		mMultiSelectHandler.dispatchOnCreate();
		final Resources res = getResources();
		final boolean displayIcon = res.getBoolean(R.bool.home_display_icon);
		final long[] accountIds = getAccountIds(this);
		if (accountIds.length == 0) {
			final Intent sign_in_intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
			sign_in_intent.setClass(this, SignInActivity.class);
			startActivity(sign_in_intent);
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
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONCREATE));
		final boolean refreshOnStart = mPreferences.getBoolean(KEY_REFRESH_ON_START, false);
        mTabDisplayOption = getTabDisplayOptionInt(this);
		final int initialTabPosition = handleIntent(intent, savedInstanceState == null);
		mActionBar = getActionBar();
		mActionBar.setCustomView(R.layout.home_tabs);
		
		mStreaming = mPreferences.getBoolean(KEY_STREAMING_ENABLED, false);

		final View view = mActionBar.getCustomView();
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		mActionsButton = (HomeActionsActionView) view.findViewById(R.id.actions_button);
		ThemeUtils.applyBackground(mIndicator);
		mPagerAdapter = new SupportTabsAdapter(this, getSupportFragmentManager(), mIndicator, 1);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setOnPageChangeListener(this);
        if (mTabDisplayOption != 0) {
            mIndicator.setDisplayLabel((mTabDisplayOption & VALUE_TAB_DIPLAY_OPTION_CODE_LABEL) != 0);
            mIndicator.setDisplayIcon((mTabDisplayOption & VALUE_TAB_DIPLAY_OPTION_CODE_ICON) != 0);
        } else {
            mIndicator.setDisplayLabel(false);
            mIndicator.setDisplayIcon(true);
        }
		mActionsButton.setOnClickListener(this);
		mActionsButton.setOnLongClickListener(this);
		mBottomActionsButton.setOnClickListener(this);
		mBottomActionsButton.setOnLongClickListener(this);
		initTabs();
		final boolean tabsNotEmpty = mPagerAdapter.getCount() > 0;
		mEmptyTabHint.setVisibility(tabsNotEmpty ? View.GONE : View.VISIBLE);
		mViewPager.setVisibility(tabsNotEmpty ? View.VISIBLE : View.GONE);
		mActionBar.setDisplayShowHomeEnabled(displayIcon || !tabsNotEmpty);
		mActionBar.setHomeButtonEnabled(displayIcon || !tabsNotEmpty);
		mActionBar.setDisplayShowTitleEnabled(!tabsNotEmpty);
		mActionBar.setDisplayShowCustomEnabled(tabsNotEmpty);
		setTabPosition(initialTabPosition);
		setupSlidingMenu();
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
	}

	@Override
	protected void onDestroy() {
		closeStream();
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
	protected void onResume() {
		super.onResume();
		mViewPager.setEnabled(!mPreferences.getBoolean(KEY_DISABLE_TAB_SWIPE, false));
		invalidateOptionsMenu();
		updateActionsButtonStyle();
		updateActionsButton();
        updateSmartBar();
		updateSlidingMenuTouchMode();
		
		if(!hasStreamLoaded && mPreferences.getBoolean(KEY_STREAMING_ENABLED, false)) {
			hasStreamLoaded = false;
			connectToStream();
		} else {
			closeStream();
		}
	}
	
	public void connectToStream() {
		if(mPreferences.getBoolean(KEY_STREAMING_ENABLED, false)) {
			if(twitterStream != null) {
				SupplicantState supplicantState;
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				supplicantState = wifiInfo.getSupplicantState();
				
				if (mPreferences.getBoolean(KEY_STREAMING_ON_MOBILE, false)
						|| SupplicantState.COMPLETED.equals(supplicantState)) {
					hasStreamLoaded = true;
					twitterStream.user(); //Initialize stream
					if (mPreferences.getBoolean(KEY_STREAMING_NOTIFICATION, false)) {
						NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
						final Intent intent = new Intent(this, HomeActivity.class);
						final Notification.Builder builder = new Notification.Builder(this);
						builder.setOngoing(true);
						builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
						builder.setSmallIcon(R.drawable.ic_stat_twittnuker);
						builder.setContentTitle(getString(R.string.app_name));
						builder.setContentText(getString(R.string.streaming_service_running));
						builder.setTicker(getString(R.string.streaming_service_running));
						notificationManager.notify(NOTIFICATION_ID_STREAMING, builder.build());
					}
				}
			}
		}
	}
	
	public void closeStream() {
		if (twitterStream != null) {
			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(NOTIFICATION_ID_STREAMING);
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... voids) {
					twitterStream.shutdown();
					return null;
				}
			}.execute();
		}
	}
	
	public boolean isStreaming() {
		if(twitterStream != null) { //TODO check this
			return true;
		}
		return false;
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
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		registerReceiver(mStateReceiver, filter);
		if (isTabsChanged(getHomeTabs(this)) || mBottomComposeButton != isBottomComposeButton()
				|| getTabDisplayOptionInt(this) != mTabDisplayOption) {
			restart();
		}
		updateUnreadCount();
		
		if (mPreferences.getBoolean(KEY_STREAMING_ENABLED, false)) {
			Twitter twitter = Utils.getDefaultTwitterInstance(getApplicationContext(), true);
			twitterStream = new TwitterStreamFactory().getInstance(twitter.getAuthorization());
			twitterStream.addListener(new UserStreamListenerImpl(getApplicationContext(), mPreferences.getLong(KEY_DEFAULT_ACCOUNT_ID, -1)));
		}
	}

	@Override
	protected void onStop() {
		mMultiSelectHandler.dispatchOnStop();
		unregisterReceiver(mStateReceiver);
		final ContentResolver resolver = getContentResolver();
		resolver.unregisterContentObserver(mAccountChangeObserver);
		mPreferences.edit().putInt(KEY_SAVED_TAB_POSITION, mViewPager.getCurrentItem()).commit();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTOP));

		super.onStop();
	}

	protected void setPagingEnabled(final boolean enabled) {
		if (mIndicator != null && mViewPager != null) {
			mViewPager.setEnabled(!mPreferences.getBoolean(KEY_DISABLE_TAB_SWIPE, false));
			mIndicator.setSwitchingEnabled(enabled);
			mIndicator.setEnabled(enabled);
		}
	}

    @Override
    protected boolean shouldSetWindowBackground() {
        return false;
    }

	private LeftDrawerFrameLayout getLeftDrawerContainer() {
		return mLeftDrawerContainer;
	}

	private View getPullToRefreshHeaderView(final Fragment f) {
		return null;
		// if (f.getActivity() == null || !(f instanceof
		// IBasePullToRefreshFragment)) return null;
		// final IBasePullToRefreshFragment ptrf = (IBasePullToRefreshFragment)
		// f;
		// final PullToRefreshLayout l = ptrf.getPullToRefreshLayout();
		// if (l == null) return null;
		// return l.getHeaderView();
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
			SwipebackActivityUtils.startSwipebackActivity(this, extraIntent);
		}
		return initialTab;
	}
	
	private boolean isBottomComposeButton() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		return preferences != null && preferences.getBoolean(KEY_BOTTOM_COMPOSE_BUTTON, false);
	}
	
	private void setUiOptions(final Window window) {
		if (SmartBarUtils.hasSmartBar()) {
			if (mBottomComposeButton) {
				window.setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
			} else {
				window.setUiOptions(0);
			}
		}
	}

	private boolean hasActivatedTask() {
		if (mTwitterWrapper == null) return false;
		return mTwitterWrapper.hasActivatedTask();
	}

	private void initTabs() {
		final List<SupportTabSpec> tabs = getHomeTabs(this);
		mCustomTabs.clear();
		mCustomTabs.addAll(tabs);
		mPagerAdapter.clear();
		mPagerAdapter.addTabs(tabs);
	}

	private void initUnreadCount() {
		for (int i = 0, j = mIndicator.getTabCount(); i < j; i++) {
			final BadgeView badge = new BadgeView(this, mIndicator.getTabItem(i).findViewById(R.id.tab_item_content));
			badge.setId(R.id.unread_count);
			badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
			badge.setTextSize(getResources().getInteger(R.integer.unread_count_text_size));
			badge.hide();
		}
	}

	private boolean isTabsChanged(final List<SupportTabSpec> tabs) {
		if (mCustomTabs.isEmpty() && tabs == null) return false;
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
		if (mPreferences == null || mPreferences.getBoolean(KEY_SETTINGS_WIZARD_COMPLETED, false))
			return false;
		startActivity(new Intent(this, SettingsWizardActivity.class));
		return true;
	}

	private void setPullToRefreshLayoutScroll(final WindowManager wm, final Fragment f, final int scrollX,
			final int statusBarHeight, final boolean horizontalScroll) {
		if (f == null || f.isDetached() || f.getActivity() == null) return;
		final View headerView = getPullToRefreshHeaderView(f);
		if (headerView != null) {
			headerView.setScrollX(scrollX);
			if (!horizontalScroll) {
				updatePullToRefreshY(wm, headerView, statusBarHeight);
			}
		}
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

	private void setupSlidingMenu() {
		if (mSlidingMenu == null) return;
		final int marginThreshold = getResources().getDimensionPixelSize(R.dimen.default_sliding_menu_margin_threshold);
		mSlidingMenu.setMode(SlidingMenu.LEFT);
		mSlidingMenu.setShadowWidthRes(R.dimen.default_sliding_menu_shadow_width);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow_holo);
		mSlidingMenu.setBehindWidthRes(R.dimen.left_drawer_width);
		mSlidingMenu.setTouchmodeMarginThreshold(marginThreshold);
		mSlidingMenu.setFadeDegree(0.5f);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		mSlidingMenu.setMenu(R.layout.home_left_drawer_container);
		mSlidingMenu.setOnOpenedListener(this);
		mSlidingMenu.setOnClosedListener(this);
		mLeftDrawerContainer = (LeftDrawerFrameLayout) mSlidingMenu.getMenu().findViewById(R.id.left_drawer_container);
		final boolean isTransparentBackground = ThemeUtils.isTransparentBackground(this);
		mLeftDrawerContainer.setClipEnabled(isTransparentBackground);
		mLeftDrawerContainer.setScrollScale(mSlidingMenu.getBehindScrollScale());
		mSlidingMenu.setBehindCanvasTransformer(new ListenerCanvasTransformer(this));
        final Window window = getWindow();
		if (isTransparentBackground) {
            final Drawable windowBackground = ThemeUtils.getWindowBackground(this, getCurrentThemeResourceId());
            ViewAccessor.setBackground(mSlidingMenu.getContent(), windowBackground);
            window.setBackgroundDrawable(new EmptyDrawable());
        } else {
            window.setBackgroundDrawable(null);
		}
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
			icon = R.drawable.ic_iconic_action_compose;
		} else {
			if (classEquals(DirectMessagesFragment.class, tab.cls)) {
				icon = R.drawable.ic_iconic_action_new_message;
				title = R.string.compose;
			} else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
				icon = R.drawable.ic_iconic_action_search;
				title = android.R.string.search_go;
			} else {
				icon = R.drawable.ic_iconic_action_compose;
				title = R.string.compose;
			}
		}
		final boolean hasActivatedTask = hasActivatedTask();
		if (mActionsButton != null) {
			mActionsButton.setIcon(icon);
			mActionsButton.setTitle(title);
			mActionsButton.setShowProgress(hasActivatedTask);
		}
		if (mBottomActionsButton != null) {
			mBottomActionsButton.setIcon(icon);
			mBottomActionsButton.setTitle(title);
			mBottomActionsButton.setShowProgress(hasActivatedTask);
        }
        if (mSmartBarProgress != null) {
            mSmartBarProgress.setVisibility(hasActivatedTask ? View.VISIBLE : View.INVISIBLE);
        }
	}

	private void updateActionsButtonStyle() {
		if (mActionsButton == null || mBottomActionsButton == null) return;
		final boolean isBottomActionsButton = isBottomComposeButton();
		final boolean showBottomActionsButton = !SmartBarUtils.hasSmartBar() && isBottomActionsButton;
		final boolean leftsideComposeButton = mPreferences.getBoolean(KEY_LEFTSIDE_COMPOSE_BUTTON, false);
		mActionsButton.setVisibility(isBottomActionsButton ? View.GONE : View.VISIBLE);
		mBottomActionsButton.setVisibility(showBottomActionsButton ? View.VISIBLE : View.GONE);
		final FrameLayout.LayoutParams compose_lp = (LayoutParams) mBottomActionsButton.getLayoutParams();
		compose_lp.gravity = Gravity.BOTTOM | (leftsideComposeButton ? Gravity.LEFT : Gravity.RIGHT);
		mBottomActionsButton.setLayoutParams(compose_lp);
	}

	private void updatePullToRefreshLayoutScroll(final float percentOpen, final boolean horizontalScroll) {
		final Window w = getWindow();
		final WindowManager wm = getWindowManager();
		final View decorView = w.getDecorView();
		decorView.getWindowVisibleDisplayFrame(mRect);
		final int statusBarHeight = mRect.top;
		final LeftDrawerFrameLayout ld = mLeftDrawerContainer;
		if (ld == null) return;
		final int scrollX = -Math.round(percentOpen * ld.getMeasuredWidth());
		ld.setPercentOpen(percentOpen);
		for (int i = 0, j = mAttachedFragments.size(); i < j; i++) {
			final Fragment f = mAttachedFragments.valueAt(i);
			setPullToRefreshLayoutScroll(wm, f, scrollX, statusBarHeight, horizontalScroll);
		}
	}

	private void updateSlidingMenuTouchMode() {
		if (mViewPager == null || mSlidingMenu == null) return;
		final int position = mViewPager.getCurrentItem();
		final int mode = !mViewPager.isEnabled() || position == 0 ? SlidingMenu.TOUCHMODE_FULLSCREEN
				: SlidingMenu.TOUCHMODE_MARGIN;
		mSlidingMenu.setTouchModeAbove(mode);
	}

    private void updateSmartBar() {
        final boolean useBottomActionItems = SmartBarUtils.hasSmartBar() && isBottomComposeButton();
        if (useBottomActionItems) {
            invalidateOptionsMenu();
        }
    }

	private static void updatePullToRefreshY(final WindowManager wm, final View view, final int y) {
		if (view == null || wm == null || view.getWindowToken() == null) return;
		final ViewGroup.LayoutParams lp = view.getLayoutParams();
		if (!(lp instanceof WindowManager.LayoutParams)) return;
		final WindowManager.LayoutParams wlp = (WindowManager.LayoutParams) lp;
		wlp.y = y;
		wm.updateViewLayout(view, wlp);
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
			mHomeActivity.updatePullToRefreshLayoutScroll(percentOpen, true);
		}

	}

	private static class UpdateUnreadCountTask extends AsyncTask<Void, Void, int[]> {
		private final Context mContext;
		private final TabPageIndicator mIndicator;
		private final boolean mEnabled;

		UpdateUnreadCountTask(final TabPageIndicator indicator, final boolean enabled) {
			mIndicator = indicator;
			mContext = indicator.getContext();
			mEnabled = enabled;
		}

		@Override
		protected int[] doInBackground(final Void... params) {
			final int tab_count = mIndicator.getTabCount();
			final int[] result = new int[tab_count];
			for (int i = 0, j = tab_count; i < j; i++) {
				result[i] = UnreadCountUtils.getUnreadCount(mContext, i);
			}
			return result;
		}

		@Override
		protected void onPostExecute(final int[] result) {
			final int tab_count = mIndicator.getTabCount();
			if (result == null || result.length != tab_count) return;
			for (int i = 0, j = tab_count; i < j; i++) {
				final BadgeView badge = (BadgeView) mIndicator.getTabItem(i).findViewById(R.id.unread_count);
				if (!mEnabled) {
					badge.setCount(0);
					badge.hide();
					continue;
				}
				final int count = result[i];
				if (count > 0) {
					badge.setCount(count);
					badge.show();
				} else if (count == 0) {
					badge.setCount(0);
					badge.hide();
				} else {
					badge.setText("\u0387");
					badge.show();
				}
			}
		}

	}
	
	static class UserStreamListenerImpl implements UserStreamListener {
		
		private final long account_id;
		private final String screen_name;
		private final ContentResolver resolver;
		private final boolean large_profile_image;
		
		public UserStreamListenerImpl(final Context context, final long account_id) {
			this.account_id = account_id;
			large_profile_image = context.getResources().getBoolean(R.bool.hires_profile_image);
			resolver = context.getContentResolver();
			screen_name = Utils.getAccountScreenName(context, account_id);
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			final long status_id = statusDeletionNotice.getStatusId();
			final String where = Statuses.STATUS_ID + " = " + status_id;
			for(final Uri uri : STATUSES_URIS) {
				resolver.delete(uri, where, null);
			}
		}

		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
			final String where = Statuses.USER_ID + " = " + userId + " AND " + Statuses.STATUS_ID + " >= " + upToStatusId;
			final ContentValues values = new ContentValues();
			values.putNull(Statuses.LOCATION);
			for(final Uri uri : STATUSES_URIS) {
				resolver.update(uri, values, where, null);
			}
		}

		@Override
		public void onStallWarning(StallWarning warning) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatus(Status status) {
			final ContentValues values = Utils.makeStatusContentValues(status, account_id, large_profile_image);
			if (values != null) {
				final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.STATUS_ID + " = " + status.getId();
				resolver.delete(Statuses.CONTENT_URI, where, null);
				resolver.delete(Mentions.CONTENT_URI, where, null);
				final Uri.Builder status_uri_builder = Statuses.CONTENT_URI.buildUpon();
				status_uri_builder.appendQueryParameter(TwittnukerApplication.QUERY_PARAM_NOTIFY, "true");
				final Uri status_uri = status_uri_builder.build();
				resolver.insert(status_uri, values);
				final Status rt = status.getRetweetedStatus();
				if (rt != null && rt.getText().contains("@" + screen_name) || rt == null && status.getText().contains("@" + screen_name)) {
					final Uri.Builder mention_uri_builder = Mentions.CONTENT_URI.buildUpon();
					mention_uri_builder.appendQueryParameter(TwittnukerApplication.QUERY_PARAM_NOTIFY, "true");
					final Uri mention_uri = mention_uri_builder.build();
					resolver.insert(mention_uri, values);
				}
			}
		}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onException(Exception ex) {
			if (Utils.isDebugBuild()) {
				Log.w(LOGTAG, ex);
			}
		}

		@Override
		public void onBlock(User source, User blockedUser) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDeletionNotice(long directMessageId, long userId) {
			final String where = DirectMessages.MESSAGE_ID + " = " + directMessageId;
			for(final Uri uri : MESSAGES_URIS) {
				resolver.delete(uri, where, null);
			}
		}

		@Override
		public void onDirectMessage(DirectMessage directMessage) {
			if(directMessage == null || directMessage.getId() <= 0) return;
			
			for(final Uri uri : MESSAGES_URIS) {
				final String where = DirectMessages.ACCOUNT_ID + " = " + account_id + " AND " + DirectMessages.MESSAGE_ID + " = " + directMessage.getId();
				resolver.delete(uri, where, null);
			}
			final User sender = directMessage.getSender();
			final User recipient = directMessage.getRecipient();
			
			if(sender.getId() == account_id) {
				final ContentValues values = Utils.makeDirectMessageContentValues(directMessage, account_id, true, large_profile_image);
				if(values != null) {
					resolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
				}
			}
			if(recipient.getId() == account_id) {
				final ContentValues values = Utils.makeDirectMessageContentValues(directMessage, account_id, false, large_profile_image);
				final Uri.Builder builder = DirectMessages.Inbox.CONTENT_URI.buildUpon();
				builder.appendQueryParameter(TwittnukerApplication.QUERY_PARAM_NOTIFY, "true");
				if(values != null) {
					resolver.insert(builder.build(), values);
				}
			}
		}

		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFollow(User source, User followedUser) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFriendList(long[] friendIds) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUnblock(User source, User unblockedUser) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUnfavorite(User source, User target,
				Status unfavoritedStatus) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUserListCreation(User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUserListDeletion(User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner,
				UserList list) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUserListMemberDeletion(User deletedMember,
				User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUserListSubscription(User subscriber, User listOwner,
				UserList list) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUserListUnsubscription(User subscriber, User listOwner,
				UserList list) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUserListUpdate(User listOwner, UserList list) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUserProfileUpdate(User updatedUser) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
