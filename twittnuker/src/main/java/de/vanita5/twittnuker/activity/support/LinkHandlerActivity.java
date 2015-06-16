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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.internal.widget.ActionBarContainer;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.meizu.flyme.reflect.StatusBarProxy;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.iface.IControlBarActivity;
import de.vanita5.twittnuker.fragment.iface.IBaseFragment;
import de.vanita5.twittnuker.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import de.vanita5.twittnuker.fragment.iface.SupportFragmentCallback;
import de.vanita5.twittnuker.fragment.support.SearchFragment;
import de.vanita5.twittnuker.fragment.support.UserFragment;
import de.vanita5.twittnuker.graphic.EmptyDrawable;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import de.vanita5.twittnuker.util.MultiSelectEventHandler;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.TwidereActionModeForChildListener;
import de.vanita5.twittnuker.util.TwidereColorUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.support.ActivitySupport;
import de.vanita5.twittnuker.util.support.ActivitySupport.TaskDescriptionCompat;
import de.vanita5.twittnuker.util.support.ViewSupport;
import de.vanita5.twittnuker.util.support.view.ViewOutlineProviderCompat;
import de.vanita5.twittnuker.view.TintedStatusFrameLayout;
import de.vanita5.twittnuker.view.TintedStatusNativeActionModeAwareLayout;

import static de.vanita5.twittnuker.util.Utils.createFragmentForIntent;
import static de.vanita5.twittnuker.util.Utils.matchLinkId;

public class LinkHandlerActivity extends BaseAppCompatActivity implements SystemWindowsInsetsCallback,
        IControlBarActivity, SupportFragmentCallback {

    private ControlBarShowHideHelper mControlBarShowHideHelper = new ControlBarShowHideHelper(this);
	private MultiSelectEventHandler mMultiSelectHandler;
    private TwidereActionModeForChildListener mTwidereActionModeForChildListener;
    private final View.OnLayoutChangeListener mLayoutChangeListener = new View.OnLayoutChangeListener() {

        private final Rect tempInsets = new Rect();
        private boolean compatCalled;

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (compatCalled) return;
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom && !ViewSupport.isInLayout(v)) {
                mMainContent.getSystemWindowsInsets(tempInsets);
                onFitSystemWindows(tempInsets);
                compatCalled = true;
            }
        }
    };

    private TintedStatusFrameLayout mMainContent;
    private View mActionBarWithOverlay;
    private ActionBarContainer mActionBarContainer;

	private boolean mFinishOnly;
    private int mActionBarHeight;


	@Override
    public Fragment getCurrentVisibleFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.content_fragment);
    }

    @Override
    public boolean triggerRefresh(int position) {
        return false;
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getDialogWhenLargeThemeResource(this);
    }

    @Override
    public void onBackPressed() {
        if (mTwidereActionModeForChildListener.finishExisting()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        final View actionBarContainer = findViewById(R.id.twidere_action_bar_container);
        if (actionBarContainer != null) {
            final ViewGroup.LayoutParams toolBarParams = actionBarContainer.getLayoutParams();
            boolean changed = false;
            if (toolBarParams instanceof ViewGroup.MarginLayoutParams) {
                final int topMargin = ((ViewGroup.MarginLayoutParams) toolBarParams).topMargin;
                changed = topMargin != insets.top;
                ((ViewGroup.MarginLayoutParams) toolBarParams).topMargin = insets.top;
            }
            if (changed) {
				actionBarContainer.setLayoutParams(toolBarParams);
			}
        }
        insets.top += ThemeUtils.getActionBarHeight(this);
        super.onFitSystemWindows(insets);
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof IBaseFragment) {
            ((IBaseFragment) fragment).requestFitSystemWindows();
        }
    }

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        return super.getSystemWindowsInsets(insets);
    }

    @Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				if (mFinishOnly) {
					finish();
				} else {
                    NavUtils.navigateUpFromSameTask(this);
				}
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event) {
        if (shouldFragmentTakeAllKeyboardShortcuts()) {
            return handleFragmentKeyboardShortcutSingle(handler, keyCode, event);
        }
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event)) return true;
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event);
        if (ACTION_NAVIGATION_BACK.equals(action)) {
            onBackPressed();
            return true;
        }
        return handler.handleKey(this, null, keyCode, event);
    }

    private boolean shouldFragmentTakeAllKeyboardShortcuts() {
        final Fragment fragment = getCurrentVisibleFragment();
        return fragment instanceof KeyboardShortcutsHandler.TakeAllKeyboardShortcut;
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event) {
        if (shouldFragmentTakeAllKeyboardShortcuts()) {
            handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event);
        }
        if (handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event)) return true;
        return super.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event);
    }

    @Override
	protected void onCreate(final Bundle savedInstanceState) {
		mMultiSelectHandler = new MultiSelectEventHandler(this);
		mMultiSelectHandler.dispatchOnCreate();
		final Intent intent = getIntent();
		final Uri data = intent.getData();
        final int linkId = matchLinkId(data);
        requestWindowFeatures(getWindow(), linkId, data);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_fragment);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));

        mTwidereActionModeForChildListener = new TwidereActionModeForChildListener(this, this, false);
        final TintedStatusNativeActionModeAwareLayout layout = (TintedStatusNativeActionModeAwareLayout) findViewById(R.id.main_content);
        layout.setActionModeForChildListener(mTwidereActionModeForChildListener);

        ThemeUtils.setCompatContentViewOverlay(this, new EmptyDrawable());
        final View actionBarContainer = findViewById(R.id.twidere_action_bar_container);
        ViewCompat.setElevation(actionBarContainer, ThemeUtils.getSupportActionBarElevation(this));
        ViewSupport.setOutlineProvider(actionBarContainer, ViewOutlineProviderCompat.BACKGROUND);
        final View windowOverlay = findViewById(R.id.window_overlay);
        ViewSupport.setBackground(windowOverlay, ThemeUtils.getNormalWindowContentOverlay(this, getCurrentThemeResourceId()));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setActionBarTheme(actionBar, linkId, data);
        }
        mMainContent.setOnFitSystemWindowsListener(this);
        setStatusBarColor(linkId, data);
        setTaskInfo(linkId, data);
        if (!showFragment(linkId, data)) {
			finish();
		}
	}


	@Override
    protected void onStart() {
        mMainContent.addOnLayoutChangeListener(mLayoutChangeListener);
        super.onStart();
        mMultiSelectHandler.dispatchOnStart();
    }

    @Override
    protected void onStop() {
        mMultiSelectHandler.dispatchOnStop();
        super.onStop();
        mMainContent.removeOnLayoutChangeListener(mLayoutChangeListener);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        final boolean result = super.onPreparePanel(featureId, view, menu);
        if (shouldSetActionItemColor()) {
            setupToolbarMenuItemColor();
            mActionBarContainer.post(new Runnable() {
                @Override
                public void run() {
                    setupToolbarMenuItemColor();
                }
            });
        }
        return result;
    }

    private void setupToolbarMenuItemColor() {
        final Toolbar toolbar = peekActionBarToolbar();
        if (toolbar != null) {
            final int actionBarColor = getCurrentActionBarColor();
            final int themeId = getCurrentThemeResourceId();
            final int itemColor = ThemeUtils.getContrastForegroundColor(this, themeId, actionBarColor);
            ThemeUtils.wrapToolbarMenuIcon(ViewSupport.findViewByType(toolbar, ActionMenuView.class), itemColor, itemColor);
        }
    }

    public final void setSubtitle(CharSequence subtitle) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setSubtitle(subtitle);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mMainContent = (TintedStatusFrameLayout) findViewById(R.id.main_content);
        mActionBarWithOverlay = findViewById(R.id.twidere_action_bar_with_overlay);
        mActionBarContainer = (ActionBarContainer) findViewById(R.id.twidere_action_bar_container);
    }

    protected boolean shouldSetActionItemColor() {
        return !(getCurrentVisibleFragment() instanceof UserFragment);
    }

    private boolean handleFragmentKeyboardShortcutRepeat(KeyboardShortcutsHandler handler, int keyCode,
                                                         int repeatCount, @NonNull KeyEvent event) {
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutRepeat(handler, keyCode,
                    repeatCount, event);
        }
        return false;
    }

    private boolean handleFragmentKeyboardShortcutSingle(KeyboardShortcutsHandler handler, int keyCode,
                                                         @NonNull KeyEvent event) {
        final Fragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof KeyboardShortcutCallback) {
            if (((KeyboardShortcutCallback) fragment).handleKeyboardShortcutSingle(handler, keyCode, event)) {
                return true;
            }
        }
        return false;
    }

    private void requestWindowFeatures(Window window, int linkId, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        final int transitionRes;
        switch (linkId) {
            case LINK_ID_USER: {
                transitionRes = R.transition.transition_user;
                break;
            }
//            case LINK_ID_STATUS: {
//                transitionRes = R.transition.transition_status;
//                break;
//            }
            default: {
                transitionRes = 0;
                break;
            }
        }
        if (transitionRes != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !ThemeUtils.isTransparentBackground(getCurrentThemeBackgroundOption())) {
            Utils.setSharedElementTransition(this, window, transitionRes);
        }
    }

    @SuppressLint("AppCompatMethod")
    private void setActionBarTheme(ActionBar actionBar, int linkId, Uri data) {
        final int actionBarColor = getActionBarColor();
        final int themeId = getCurrentThemeResourceId();
        final String option = getThemeBackgroundOption();
        int actionBarItemsColor = ThemeUtils.getContrastForegroundColor(this, themeId, actionBarColor);
        final ActionBarContainer actionBarContainer = (ActionBarContainer) findViewById(R.id.twidere_action_bar_container);
        switch (linkId) {
            case LINK_ID_SEARCH:
            case LINK_ID_USER_LISTS:
            case LINK_ID_USER_LIST:
            case LINK_ID_FILTERS: {
                ThemeUtils.applyActionBarBackground(actionBarContainer, this, themeId, actionBarColor, option, false);
                break;
            }
            default: {
                ThemeUtils.applyActionBarBackground(actionBarContainer, this, themeId, actionBarColor, option, true);
                break;
            }
        }
        final boolean isTransparent = ThemeUtils.isTransparentBackground(option);
        final int actionBarAlpha = isTransparent ? ThemeUtils.getActionBarAlpha(ThemeUtils.getUserThemeBackgroundAlpha(this)) : 0xFF;
        mActionBarWithOverlay.setAlpha(actionBarAlpha / 255f);
    }

    private void setStatusBarColor(int linkId, Uri uri) {
        switch (linkId) {
            case LINK_ID_USER: {
                mMainContent.setShadowColor(0xA0000000);
                // Fall through
            }
            default: {
                mMainContent.setDrawShadow(false);
                mMainContent.setDrawColor(true);
                mMainContent.setFactor(1);
                final int alpha = ThemeUtils.isTransparentBackground(getThemeBackgroundOption())
                        ? ThemeUtils.getActionBarAlpha(getCurrentThemeBackgroundAlpha()) : 0xFF;
                final int statusBarColor = ThemeUtils.getActionBarColor(this);
                mMainContent.setColor(statusBarColor, alpha);
                StatusBarProxy.setStatusBarDarkIcon(getWindow(), TwidereColorUtils.getYIQLuminance(statusBarColor) > ThemeUtils.ACCENT_COLOR_THRESHOLD);
                break;
            }
        }
    }

    private void setTaskInfo(int linkId, Uri uri) {
        switch (linkId) {
//            case LINK_ID_USER: {
//                break;
//            }
            default: {
                if (ThemeUtils.isColoredActionBar(getCurrentThemeResourceId())) {
                    ActivitySupport.setTaskDescription(this, new TaskDescriptionCompat(null, null,
                            getCurrentThemeColor()));
                }
                break;
            }
        }
    }

    private boolean showFragment(final int linkId, final Uri uri) {
		final Intent intent = getIntent();
		intent.setExtrasClassLoader(getClassLoader());
        final Fragment fragment = createFragmentForIntent(this, linkId, intent);
		if (uri == null || fragment == null) return false;
        setSubtitle(null);
        switch (linkId) {
			case LINK_ID_STATUS: {
				setTitle(R.string.status);
				break;
			}
			case LINK_ID_USER: {
				setTitle(R.string.user);
				break;
			}
			case LINK_ID_USER_TIMELINE: {
				setTitle(R.string.statuses);
				break;
			}
			case LINK_ID_USER_FAVORITES: {
				setTitle(R.string.favorites);
				break;
			}
			case LINK_ID_USER_FOLLOWERS: {
				setTitle(R.string.followers);
				break;
			}
			case LINK_ID_USER_FRIENDS: {
                setTitle(R.string.following);
				break;
			}
			case LINK_ID_USER_BLOCKS: {
				setTitle(R.string.blocked_users);
				break;
			}
			case LINK_ID_MUTES_USERS: {
				setTitle(R.string.twitter_muted_users);
				break;
			}
			case LINK_ID_DIRECT_MESSAGES_CONVERSATION: {
				setTitle(R.string.direct_messages);
				break;
			}
			case LINK_ID_USER_LIST: {
				setTitle(R.string.user_list);
				break;
			}
			case LINK_ID_USER_LISTS: {
				setTitle(R.string.user_lists);
				break;
			}
			case LINK_ID_USER_LIST_TIMELINE: {
				setTitle(R.string.list_timeline);
				break;
			}
			case LINK_ID_USER_LIST_MEMBERS: {
				setTitle(R.string.list_members);
				break;
			}
			case LINK_ID_USER_LIST_SUBSCRIBERS: {
				setTitle(R.string.list_subscribers);
				break;
			}
			case LINK_ID_USER_LIST_MEMBERSHIPS: {
				setTitle(R.string.lists_following_user);
				break;
			}
			case LINK_ID_SAVED_SEARCHES: {
				setTitle(R.string.saved_searches);
				break;
			}
			case LINK_ID_USER_MENTIONS: {
				setTitle(R.string.user_mentions);
				break;
			}
			case LINK_ID_INCOMING_FRIENDSHIPS: {
				setTitle(R.string.incoming_friendships);
				break;
			}
			case LINK_ID_USERS: {
				setTitle(R.string.users);
				break;
			}
			case LINK_ID_STATUSES: {
				setTitle(R.string.statuses);
				break;
			}
            case LINK_ID_USER_MEDIA_TIMELINE: {
                setTitle(R.string.media);
                break;
            }
			case LINK_ID_STATUS_RETWEETERS: {
				setTitle(R.string.users_retweeted_this);
				break;
			}
			case LINK_ID_STATUS_FAVORITERS: {
				setTitle(R.string.users_retweeted_this);
				break;
			}
			case LINK_ID_STATUS_REPLIES: {
                setTitle(R.string.view_replies);
				break;
			}
			case LINK_ID_SEARCH: {
				setTitle(android.R.string.search_go);
                setSubtitle(uri.getQueryParameter(QUERY_PARAM_QUERY));
				break;
			}
            case LINK_ID_ACCOUNTS: {
                setTitle(R.string.accounts);
                break;
            }
            case LINK_ID_DRAFTS: {
                setTitle(R.string.drafts);
                break;
            }
            case LINK_ID_FILTERS: {
                setTitle(R.string.filters);
                break;
            }
            case LINK_ID_MAP: {
                setTitle(R.string.view_map);
                break;
            }
            case LINK_ID_PROFILE_EDITOR: {
                setTitle(R.string.edit_profile);
                break;
            }
            default: {
                setTitle(getString(R.string.app_name));
                break;
            }
		}
		mFinishOnly = Boolean.parseBoolean(uri.getQueryParameter(QUERY_PARAM_FINISH_ONLY));
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_fragment, fragment);
		ft.commit();
		return true;
	}

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {
        // Currently only search page needs this pattern, so we only enable this feature for it.
        if (!(getCurrentVisibleFragment() instanceof SearchFragment)) return;
        mControlBarShowHideHelper.setControlBarVisibleAnimate(visible);
    }

    @Override
    public void setControlBarOffset(float offset) {
        mActionBarContainer.setTranslationY(-Math.round((1 - offset) * getControlBarHeight()));
        notifyControlBarOffsetChanged();
    }

    @Override
    public float getControlBarOffset() {
        return 1 + mActionBarContainer.getTranslationY() / (float) getControlBarHeight();
    }

    @Override
    public int getControlBarHeight() {
        if (mActionBarHeight != 0) return mActionBarHeight;
        return mActionBarHeight = ThemeUtils.getActionBarHeight(this);
    }

    public ActionBarContainer getActionBarContainer() {
        return mActionBarContainer;
    }
}