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

package de.vanita5.twittnuker.fragment.support;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.ActionMenuView.OnMenuItemClickListener;
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mariotaku.querybuilder.Expression;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.FiltersActivity;
import de.vanita5.twittnuker.activity.SettingsActivity;
import de.vanita5.twittnuker.activity.iface.IThemedActivity;
import de.vanita5.twittnuker.activity.support.AccountsManagerActivity;
import de.vanita5.twittnuker.activity.support.ComposeActivity;
import de.vanita5.twittnuker.activity.support.DraftsActivity;
import de.vanita5.twittnuker.activity.support.HomeActivity;
import de.vanita5.twittnuker.activity.support.QuickSearchBarActivity;
import de.vanita5.twittnuker.activity.support.UserProfileEditorActivity;
import de.vanita5.twittnuker.adapter.ArrayAdapter;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.constant.SharedPreferenceConstants;
import de.vanita5.twittnuker.menu.support.AccountToggleProvider;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.util.CompareUtils;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import de.vanita5.twittnuker.util.ListViewUtils;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.TransitionUtils;
import de.vanita5.twittnuker.util.UserColorNameManager;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.content.SupportFragmentReloadCursorObserver;
import de.vanita5.twittnuker.view.ShapedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.vanita5.twittnuker.util.Utils.openUserFavorites;
import static de.vanita5.twittnuker.util.Utils.openUserLists;
import static de.vanita5.twittnuker.util.Utils.openUserProfile;

public class AccountsDashboardFragment extends BaseSupportFragment implements LoaderCallbacks<Cursor>,
        OnSharedPreferenceChangeListener, ImageLoadingListener, OnClickListener, KeyboardShortcutCallback, AdapterView.OnItemClickListener {

	private final SupportFragmentReloadCursorObserver mReloadContentObserver = new SupportFragmentReloadCursorObserver(
			this, 0, this);

	private ContentResolver mResolver;
	private SharedPreferences mPreferences;
	private MergeAdapter mAdapter;

    private AccountSelectorAdapter mAccountsAdapter;
	private AccountOptionsAdapter mAccountOptionsAdapter;
	private AppMenuAdapter mAppMenuAdapter;

    private ListView mListView;
    private TextView mAppMenuSectionView;
    private View mAccountSelectorView;
    private RecyclerView mAccountsSelector;
    private ImageView mAccountProfileBannerView;
    private ImageView mFloatingProfileImageSnapshotView;
    private ShapedImageView mAccountProfileImageView;
    private TextView mAccountProfileNameView, mAccountProfileScreenNameView;
    private ActionMenuView mAccountsToggleMenu;
    private View mAccountProfileContainer;

	private Context mThemedContext;
    private MediaLoaderWrapper mImageLoader;
    private AccountToggleProvider mAccountActionProvider;

    private boolean mSwitchAccountAnimationPlaying;

    public long[] getActivatedAccountIds() {
        if (mAccountActionProvider != null) {
            return mAccountActionProvider.getActivatedAccountIds();
        }
		return Utils.getActivatedAccountIds(getActivity());
	}

	@Override
    public boolean handleKeyboardShortcutSingle(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, @NonNull final KeyEvent event) {
        return false;
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, final int repeatCount,
                                                @NonNull final KeyEvent event) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event);
        if (action == null) return false;
        final int offset;
        switch (action) {
            case ACTION_NAVIGATION_PREVIOUS: {
                offset = -1;
                break;
            }
            case ACTION_NAVIGATION_NEXT: {
                offset = 1;
                break;
            }
            default: {
                return false;
            }
        }
        final int firstVisiblePosition = ListViewUtils.getFirstFullyVisiblePosition(mListView);
        final int selectedItem = mListView.getSelectedItemPosition();
        final int count = mListView.getCount();
        int resultPosition;
        if (!mListView.isFocused() || selectedItem == ListView.INVALID_POSITION) {
            resultPosition = firstVisiblePosition;
        } else {
            resultPosition = selectedItem + offset;
            while (resultPosition >= 0 && resultPosition < count && !mAdapter.isEnabled(resultPosition)) {
                resultPosition += offset;
            }
        }
        final View focusedChild = mListView.getFocusedChild();
        if (focusedChild == null) {
            mListView.requestChildFocus(mListView.getChildAt(0), null);
        }
        if (resultPosition >= 0 && resultPosition < count) {
            mListView.setSelection(resultPosition);
        }
        return true;
    }

    @Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_SETTINGS: {
				if (data == null) return;
				final FragmentActivity activity = getActivity();
                if (data.getBooleanExtra(EXTRA_CHANGED, false) && activity instanceof IThemedActivity) {
					((IThemedActivity) activity).restart();
		}
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
    public void onResume() {
        super.onResume();
        updateDefaultAccountState();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_container: {
                final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
                if (account == null) return;
                final FragmentActivity activity = getActivity();
                final Bundle activityOption = Utils.makeSceneTransitionOption(activity,
                        new Pair<View, String>(mAccountProfileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE));
                openUserProfile(activity, account.account_id, account.account_id,
                        account.screen_name, activityOption);
                break;
            }
        }
    }

    @Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new CursorLoader(getActivity(), Accounts.CONTENT_URI, Accounts.COLUMNS, null, null, Accounts.SORT_POSITION);
	}

	@Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        final Menu menu = mAccountsToggleMenu.getMenu();
        mAccountActionProvider = (AccountToggleProvider) MenuItemCompat.getActionProvider(menu.findItem(MENU_SELECT_ACCOUNT));
        mAccountActionProvider.setExclusive(false);
        final ParcelableAccount[] accounts = ParcelableAccount.getAccounts(data);
		long defaultId = -1;
        for (ParcelableAccount account : accounts) {
			if (account.is_activated) {
				defaultId = account.account_id;
                break;
			}
		}
        mAccountsAdapter.setAccounts(accounts);
		mAccountsAdapter.setSelectedAccountId(mPreferences.getLong(KEY_DEFAULT_ACCOUNT_ID, defaultId));
        mAccountOptionsAdapter.setSelectedAccount(mAccountsAdapter.getSelectedAccount());
        mAccountActionProvider.setAccounts(accounts);

        initAccountActionsAdapter(accounts);
        updateAccountOptionsSeparatorLabel(null);
        updateDefaultAccountState();
    }

	@Override
    public void onLoaderReset(final Loader<Cursor> loader) {
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View v, final int position, final long id) {
		final ListAdapter adapter = mAdapter.getAdapter(position);
		final Object item = mAdapter.getItem(position);
        if (adapter instanceof AccountOptionsAdapter) {
            final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
			if (account == null || !(item instanceof OptionItem)) return;
			final OptionItem option = (OptionItem) item;
			switch (option.id) {
				case MENU_SEARCH: {
                    final Intent intent = new Intent(getActivity(), QuickSearchBarActivity.class);
                    intent.putExtra(EXTRA_ACCOUNT_ID, account.account_id);
                    startActivity(intent);
					closeAccountsDrawer();
					break;
				}
                case MENU_COMPOSE: {
                    final Intent composeIntent = new Intent(INTENT_ACTION_COMPOSE);
                    composeIntent.setClass(getActivity(), ComposeActivity.class);
                    composeIntent.putExtra(EXTRA_ACCOUNT_IDS, new long[]{account.account_id});
                    startActivity(composeIntent);
					break;
				}
				case MENU_FAVORITES: {
					openUserFavorites(getActivity(), account.account_id, account.account_id, account.screen_name);
					break;
				}
				case MENU_LISTS: {
					openUserLists(getActivity(), account.account_id, account.account_id, account.screen_name);
					break;
				}
				case MENU_EDIT: {
					final Bundle bundle = new Bundle();
					bundle.putLong(EXTRA_ACCOUNT_ID, account.account_id);
					final Intent intent = new Intent(INTENT_ACTION_EDIT_USER_PROFILE);
					intent.setClass(getActivity(), UserProfileEditorActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
			}
		} else if (adapter instanceof AppMenuAdapter) {
			if (!(item instanceof OptionItem)) return;
			final OptionItem option = (OptionItem) item;
			switch (option.id) {
                case MENU_ACCOUNTS: {
                    final Intent intent = new Intent(getActivity(), AccountsManagerActivity.class);
					startActivity(intent);
					break;
				}
				case MENU_DRAFTS: {
					final Intent intent = new Intent(INTENT_ACTION_DRAFTS);
					intent.setClass(getActivity(), DraftsActivity.class);
					startActivity(intent);
					break;
				}
				case MENU_FILTERS: {
					final Intent intent = new Intent(getActivity(), FiltersActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
					break;
				}
				case MENU_SETTINGS: {
					final Intent intent = new Intent(getActivity(), SettingsActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivityForResult(intent, REQUEST_SETTINGS);
					break;
				}
			}
			closeAccountsDrawer();
		}
	}

	@Override
    public void onLoadingStarted(String imageUri, View view) {

	}

	@Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

		}

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        view.setTag(imageUri);
	}

	@Override
    public void onLoadingCancelled(String imageUri, View view) {

	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (KEY_DEFAULT_ACCOUNT_ID.equals(key)) {
			updateDefaultAccountState();
		}
	}

    public void setStatusBarHeight(int height) {
        mAccountProfileContainer.setPadding(0, height, 0, 0);
    }

	@Override
    protected void fitSystemWindows(Rect insets) {
        // No-op
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mResolver = getContentResolver();
        final View view = getView();
        if (view == null) throw new AssertionError();
        final Context context = view.getContext();
        final TwittnukerApplication application = TwittnukerApplication.getInstance(context);
        mImageLoader = application.getMediaLoaderWrapper();
        mListView.setItemsCanFocus(true);
        mListView.setHorizontalScrollBarEnabled(false);
        mListView.setVerticalScrollBarEnabled(false);
        mAdapter = new MergeAdapter();
        final LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        mAccountsAdapter = new AccountSelectorAdapter(context, inflater, this);
        mAccountOptionsAdapter = new AccountOptionsAdapter(context);
        mAppMenuAdapter = new AppMenuAdapter(context);
        mAppMenuSectionView = Utils.newSectionView(context, R.string.more);
        mAccountSelectorView = inflater.inflate(R.layout.header_drawer_account_selector, mListView, false);
        mAccountsSelector = (RecyclerView) mAccountSelectorView.findViewById(R.id.other_accounts_list);
        final LinearLayoutManager layoutManager = new FixedLinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setStackFromEnd(true);
        mAccountsSelector.setLayoutManager(layoutManager);
        mAccountsSelector.setAdapter(mAccountsAdapter);
        mAccountsSelector.setItemAnimator(null);
        mAccountProfileContainer = mAccountSelectorView.findViewById(R.id.profile_container);
        mAccountProfileImageView = (ShapedImageView) mAccountSelectorView.findViewById(R.id.profile_image);
        mAccountProfileBannerView = (ImageView) mAccountSelectorView.findViewById(R.id.account_profile_banner);
        mFloatingProfileImageSnapshotView = (ImageView) mAccountSelectorView.findViewById(R.id.floating_profile_image_snapshot);
        mAccountProfileNameView = (TextView) mAccountSelectorView.findViewById(R.id.name);
        mAccountProfileScreenNameView = (TextView) mAccountSelectorView.findViewById(R.id.screen_name);
        mAccountsToggleMenu = (ActionMenuView) mAccountSelectorView.findViewById(R.id.toggle_menu);
        final SupportMenuInflater menuInflater = new SupportMenuInflater(context);
        menuInflater.inflate(R.menu.action_dashboard_timeline_toggle, mAccountsToggleMenu.getMenu());
        mAccountsToggleMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getGroupId() != AccountToggleProvider.MENU_GROUP) return false;
                final ParcelableAccount[] accounts = mAccountActionProvider.getAccounts();
                final ParcelableAccount account = accounts[item.getOrder()];
                final ContentValues values = new ContentValues();
                final boolean newActivated = !account.is_activated;
                mAccountActionProvider.setAccountActivated(account.account_id, newActivated);
                values.put(Accounts.IS_ACTIVATED, newActivated);
                final String where = Expression.equals(Accounts.ACCOUNT_ID, account.account_id).getSQL();
                mResolver.update(Accounts.CONTENT_URI, values, where, null);
                return true;
            }
        });

        mAccountProfileContainer.setOnClickListener(this);

        mAdapter.addView(mAccountSelectorView, true);
        mAdapter.addAdapter(mAccountOptionsAdapter);
        mAdapter.addView(mAppMenuSectionView, false);
        mAdapter.addAdapter(mAppMenuAdapter);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts_dashboard, container, false);
    }

    @Override
    public void onBaseViewCreated(View view, Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(android.R.id.list);
    }

    @Override
	public void onStart() {
		super.onStart();
		final ContentResolver resolver = getContentResolver();
		resolver.registerContentObserver(Accounts.CONTENT_URI, true, mReloadContentObserver);
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public void onStop() {
		final ContentResolver resolver = getContentResolver();
		resolver.unregisterContentObserver(mReloadContentObserver);
		super.onStop();
	}

    void initAccountActionsAdapter(ParcelableAccount[] accounts) {
        mAccountOptionsAdapter.clear();
        mAccountOptionsAdapter.add(new OptionItem(android.R.string.search_go, R.drawable.ic_action_search, MENU_SEARCH));
        if (accounts.length > 1) {
            mAccountOptionsAdapter.add(new OptionItem(R.string.compose, R.drawable.ic_action_status_compose, MENU_COMPOSE));
        }
        mAccountOptionsAdapter.add(new OptionItem(R.string.favorites, R.drawable.ic_action_star, MENU_FAVORITES));
        mAccountOptionsAdapter.add(new OptionItem(R.string.lists, R.drawable.ic_action_list, MENU_LISTS));
    }

	private void closeAccountsDrawer() {
		final Activity activity = getActivity();
		if (activity instanceof HomeActivity) {
			((HomeActivity) activity).closeAccountsDrawer();
		}
	}

    private void getLocationOnScreen(View view, RectF rectF) {
        final int[] location = new int[2];
        view.getLocationOnScreen(location);
        rectF.set(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
    }

    @Override
    public Context getThemedContext() {
		if (mThemedContext != null) return mThemedContext;
		final Context context = getActivity();
		final int themeResource = ThemeUtils.getDrawerThemeResource(context);
        return mThemedContext = new ContextThemeWrapper(context, themeResource);
	}

    private void onAccountSelected(AccountProfileImageViewHolder holder, final ParcelableAccount account) {
        if (mSwitchAccountAnimationPlaying) return;
        final ImageView snapshotView = mFloatingProfileImageSnapshotView;
        final ShapedImageView profileImageView = mAccountProfileImageView;
        final ShapedImageView clickedImageView = holder.getIconView();

        // Reset snapshot view position
        snapshotView.setPivotX(0);
        snapshotView.setPivotY(0);
        snapshotView.setTranslationX(0);
        snapshotView.setTranslationY(0);

        final Matrix matrix = new Matrix();
        final RectF sourceBounds = new RectF(), destBounds = new RectF(), snapshotBounds = new RectF();
        getLocationOnScreen(clickedImageView, sourceBounds);
        getLocationOnScreen(profileImageView, destBounds);
        getLocationOnScreen(snapshotView, snapshotBounds);
        final float finalScale = destBounds.width() / sourceBounds.width();
        final Bitmap snapshotBitmap = TransitionUtils.createViewBitmap(clickedImageView, matrix,
                new RectF(0, 0, sourceBounds.width(), sourceBounds.height()));
        final ViewGroup.LayoutParams lp = snapshotView.getLayoutParams();
        lp.width = clickedImageView.getWidth();
        lp.height = clickedImageView.getHeight();
        snapshotView.setLayoutParams(lp);
        // Copied from MaterialNavigationDrawer: https://github.com/madcyph3r/AdvancedMaterialDrawer/
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(snapshotView, View.TRANSLATION_X,
                sourceBounds.left - snapshotBounds.left, destBounds.left - snapshotBounds.left))
                .with(ObjectAnimator.ofFloat(snapshotView, View.TRANSLATION_Y,
                        sourceBounds.top - snapshotBounds.top, destBounds.top - snapshotBounds.top))
                .with(ObjectAnimator.ofFloat(snapshotView, View.SCALE_X, 1, finalScale))
                .with(ObjectAnimator.ofFloat(snapshotView, View.SCALE_Y, 1, finalScale))
                .with(ObjectAnimator.ofFloat(profileImageView, View.ALPHA, 1, 0))
                .with(ObjectAnimator.ofFloat(clickedImageView, View.SCALE_X, 0, 1))
                .with(ObjectAnimator.ofFloat(clickedImageView, View.SCALE_Y, 0, 1));
        final long animationTransition = 400;
        set.setDuration(animationTransition);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListener() {

            private Drawable clickedDrawable;
            private int[] clickedColors;

            @Override
            public void onAnimationStart(Animator animation) {
                snapshotView.setVisibility(View.VISIBLE);
                snapshotView.setImageBitmap(snapshotBitmap);
                final Drawable profileDrawable = profileImageView.getDrawable();
                clickedDrawable = clickedImageView.getDrawable();
                clickedColors = clickedImageView.getBorderColors();
                final ParcelableAccount oldSelectedAccount = mAccountsAdapter.getSelectedAccount();
                mImageLoader.displayDashboardProfileImage(clickedImageView,
                        oldSelectedAccount.profile_image_url, profileDrawable);
//                mImageLoader.displayDashboardProfileImage(profileImageView,
//                        account.profile_image_url, clickedDrawable);
                clickedImageView.setBorderColors(profileImageView.getBorderColors());
                mSwitchAccountAnimationPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                finishAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                finishAnimation();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            private void finishAnimation() {
                final Editor editor = mPreferences.edit();
                editor.putLong(KEY_DEFAULT_ACCOUNT_ID, account.account_id);
                editor.apply();
        		mAccountsAdapter.setSelectedAccountId(account.account_id);
                mAccountOptionsAdapter.setSelectedAccount(account);
                updateAccountOptionsSeparatorLabel(clickedDrawable);
                snapshotView.setVisibility(View.INVISIBLE);
                snapshotView.setImageDrawable(null);
                profileImageView.setImageDrawable(clickedDrawable);
                profileImageView.setBorderColors(clickedColors);
                profileImageView.setAlpha(1f);
                clickedImageView.setScaleX(1);
                clickedImageView.setScaleY(1);
                clickedImageView.setAlpha(1f);
                mSwitchAccountAnimationPlaying = false;
    		}
        });
        set.start();
    }

    private void updateAccountOptionsSeparatorLabel(Drawable profileImageSnapshot) {
        final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
        if (account == null) {
            return;
		}
        mAccountProfileNameView.setText(account.name);
        mAccountProfileScreenNameView.setText("@" + account.screen_name);
        mImageLoader.displayDashboardProfileImage(mAccountProfileImageView,
                account.profile_image_url, profileImageSnapshot);
        mAccountProfileImageView.setBorderColors(account.color);
        final int bannerWidth = mAccountProfileBannerView.getWidth();
        final Resources res = getResources();
        final int defWidth = res.getDisplayMetrics().widthPixels;
        final int width = bannerWidth > 0 ? bannerWidth : defWidth;
        final String bannerUrl = Utils.getBestBannerUrl(account.profile_banner_url, width);
        final ImageView bannerView = mAccountProfileBannerView;
        if (bannerView.getDrawable() == null || !CompareUtils.objectEquals(bannerUrl, bannerView.getTag())) {
            mImageLoader.displayProfileBanner(mAccountProfileBannerView, bannerUrl, this);
        } else {
            mImageLoader.cancelDisplayTask(mAccountProfileBannerView);
	    }
    }

	private void updateDefaultAccountState() {
	}

	private static final class AccountOptionsAdapter extends OptionItemsAdapter {

        private final boolean mNameFirst;
        private final UserColorNameManager mUserColorNameManager;
        private ParcelableAccount mSelectedAccount;

		public AccountOptionsAdapter(final Context context) {
			super(context);
            mUserColorNameManager = UserColorNameManager.getInstance(context);
            mNameFirst = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME,
                    Context.MODE_PRIVATE, SharedPreferenceConstants.class).getBoolean(KEY_NAME_FIRST);
		}

        public void setSelectedAccount(ParcelableAccount account) {
            mSelectedAccount = account;
            notifyDataSetChanged();
        }

        @Override
        protected String getTitle(int position, OptionItem option) {
            final ParcelableAccount account = mSelectedAccount;
            if (account != null && option.id == MENU_COMPOSE) {
                final Context context = getContext();
                final String displayName = mUserColorNameManager.getDisplayName(-1, account.name,
                        account.screen_name, mNameFirst, false);
                return context.getString(R.string.tweet_from_name, displayName);
            }
            return super.getTitle(position, option);
        }
	}

    static class AccountProfileImageViewHolder extends ViewHolder implements OnClickListener {

        private final AccountSelectorAdapter adapter;
        private final ShapedImageView icon;

        public AccountProfileImageViewHolder(AccountSelectorAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
            icon = (ShapedImageView) itemView.findViewById(android.R.id.icon);
		}

        public ShapedImageView getIconView() {
            return icon;
        }

		@Override
        public void onClick(View v) {
            adapter.dispatchItemSelected(this);
		}
    }

    private static class AccountSelectorAdapter extends Adapter<AccountProfileImageViewHolder> {

        private final LayoutInflater mInflater;
        private final MediaLoaderWrapper mImageLoader;
        private final AccountsDashboardFragment mFragment;
        private final LongSparseArray<Long> positionMap = new LongSparseArray<>();
        private ParcelableAccount[] mInternalAccounts;

        AccountSelectorAdapter(Context context, LayoutInflater inflater, AccountsDashboardFragment fragment) {
            mInflater = inflater;
            mImageLoader = TwittnukerApplication.getInstance(context).getMediaLoaderWrapper();
            mFragment = fragment;
            setHasStableIds(true);
		}

        public ParcelableAccount getAdapterAccount(int adapterPosition) {
            if (mInternalAccounts == null || mInternalAccounts.length < 1) {
                return null;
			}
            return mInternalAccounts[adapterPosition + 1];
		}

        @Nullable
        public ParcelableAccount getSelectedAccount() {
            if (mInternalAccounts == null || mInternalAccounts.length == 0) {
            	return null;
        	}
            return mInternalAccounts[0];
        }

        public long getSelectedAccountId() {
            final ParcelableAccount selectedAccount = getSelectedAccount();
            if (selectedAccount == null) return -1;
            return selectedAccount.account_id;
        }

        public void setSelectedAccountId(long accountId) {
            final ParcelableAccount selectedAccount = getSelectedAccount();
            if (selectedAccount == null) return;
            swap(accountId, selectedAccount.account_id);
        }

		@Override
        public AccountProfileImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.adapter_item_dashboard_account, parent, false);
            return new AccountProfileImageViewHolder(this, view);
		}

        @Override
        public void onBindViewHolder(AccountProfileImageViewHolder holder, int position) {
//            holder.itemView.setAlpha(c.getInt(mIndices.is_activated) == 1 ? 1 : 0.5f);
            final ParcelableAccount account = getAdapterAccount(position);
            mImageLoader.cancelDisplayTask(holder.icon);
//            holder.icon.setImageDrawable(null);
            mImageLoader.displayDashboardProfileImage(holder.icon, account.profile_image_url, null);
            holder.icon.setBorderColor(account.color);
		}

		@Override
        public long getItemId(int position) {
            return getAdapterAccount(position).account_id;
        }

        @Override
        public int getItemCount() {
            if (mInternalAccounts == null || mInternalAccounts.length == 0) return 0;
            return mInternalAccounts.length - 1;
		}

        public void setAccounts(ParcelableAccount[] accounts) {
            if (accounts != null) {
                final ParcelableAccount[] previousAccounts = mInternalAccounts;
                mInternalAccounts = new ParcelableAccount[accounts.length];
                int tempIdx = 0;
                final List<ParcelableAccount> tempList = new ArrayList<>();
                Collections.addAll(tempList, accounts);
                if (previousAccounts != null) {
                    for (ParcelableAccount previousAccount : previousAccounts) {
                        final int idx = indexOfAccount(tempList, previousAccount.account_id);
                        if (idx >= 0) {
                            mInternalAccounts[tempIdx++] = tempList.remove(idx);
                        }
                    }
                }
                for (ParcelableAccount account : tempList) {
                    mInternalAccounts[tempIdx++] = account;
                }
            } else {
                mInternalAccounts = null;
            }
            notifyDataSetChanged();
        }

        private void dispatchItemSelected(AccountProfileImageViewHolder holder) {
            mFragment.onAccountSelected(holder, getAdapterAccount(holder.getAdapterPosition()));
        }

        private static int indexOfAccount(List<ParcelableAccount> accounts, long accountId) {
            for (int i = 0, j = accounts.size(); i < j; i++) {
                if (accounts.get(i).account_id == accountId) return i;
            }
            return -1;
        }

        private void swap(long fromId, long toId) {
            int fromIdx = -1, toIdx = -1;
            for (int i = 0, j = mInternalAccounts.length; i < j; i++) {
                final ParcelableAccount account = mInternalAccounts[i];
                if (account.account_id == fromId) {
                    fromIdx = i;
                }
                if (account.account_id == toId) {
                    toIdx = i;
                }
            }
            if (fromIdx < 0 || toIdx < 0) return;
            final ParcelableAccount temp = mInternalAccounts[toIdx];
            mInternalAccounts[toIdx] = mInternalAccounts[fromIdx];
            mInternalAccounts[fromIdx] = temp;
            notifyDataSetChanged();
        }
    }

    private static final class AppMenuAdapter extends OptionItemsAdapter {

        public AppMenuAdapter(final Context context) {
            super(context);
            add(new OptionItem(R.string.accounts, R.drawable.ic_action_accounts, MENU_ACCOUNTS));
            add(new OptionItem(R.string.drafts, R.drawable.ic_action_draft, MENU_DRAFTS));
            add(new OptionItem(R.string.filters, R.drawable.ic_action_speaker_muted, MENU_FILTERS));
            add(new OptionItem(R.string.settings, R.drawable.ic_action_settings, MENU_SETTINGS));
        }

    }

	private static class OptionItem {

		private final int name, icon, id;

		OptionItem(final int name, final int icon, final int id) {
			this.name = name;
			this.icon = icon;
			this.id = id;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof OptionItem)) return false;
			final OptionItem other = (OptionItem) obj;
			if (icon != other.icon) return false;
			if (id != other.id) return false;
			if (name != other.name) return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + icon;
			result = prime * result + id;
			result = prime * result + name;
			return result;
		}

		@Override
		public String toString() {
			return "AccountOption{name=" + name + ", icon=" + icon + ", id=" + id + "}";
		}

	}

	private static abstract class OptionItemsAdapter extends ArrayAdapter<OptionItem> {

        private final int mActionIconColor;

		public OptionItemsAdapter(final Context context) {
			super(context, R.layout.list_item_menu);
            mActionIconColor = ThemeUtils.getThemeForegroundColor(context);
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final OptionItem option = getItem(position);
			final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            text1.setText(getTitle(position, option));
            icon.setImageDrawable(ResourcesCompat.getDrawable(icon.getResources(), option.icon, null));
            icon.setColorFilter(mActionIconColor, Mode.SRC_ATOP);
			return view;
		}

        protected String getTitle(int position, OptionItem option) {
            return getContext().getString(option.name);
        }

	}
}