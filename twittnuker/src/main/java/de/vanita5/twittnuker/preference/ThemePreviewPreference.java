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

package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.widget.ActionMenuView;
import android.text.Html;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.util.ColorUtils;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.TwidereLinkify;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.accessor.ViewAccessor;
import de.vanita5.twittnuker.view.iface.IExtendedView;
import de.vanita5.twittnuker.view.iface.IExtendedView.TouchInterceptor;

import java.lang.reflect.InvocationTargetException;

import static de.vanita5.twittnuker.util.HtmlEscapeHelper.toPlainText;
import static de.vanita5.twittnuker.util.Utils.formatToLongTimeString;
import static de.vanita5.twittnuker.util.Utils.getDefaultTextSize;

public class ThemePreviewPreference extends Preference implements Constants, OnSharedPreferenceChangeListener {

	public ThemePreviewPreference(final Context context) {
		this(context, null);
	}

	public ThemePreviewPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ThemePreviewPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (KEY_THEME.equals(key) || KEY_THEME_BACKGROUND.equals(key) || KEY_THEME_COLOR.equals(key)
				|| KEY_ACTION_BAR_COLOR.equals(key)) {
			notifyChanged();
		}
	}

	@Override
	protected View onCreateView(final ViewGroup parent) {
		final Context context = getContext();
		final int themeResource = ThemeUtils.getThemeResource(context);
        final Context theme = new ContextThemeWrapper(context, themeResource);
		final LayoutInflater inflater = LayoutInflater.from(theme);
        try {
            final View view = inflater.inflate(R.layout.theme_preview, parent, false);
            setPreviewView(theme, view.findViewById(R.id.theme_preview_content), themeResource);
            return view;
        } catch (InflateException e) {
            if (e.getCause() instanceof InvocationTargetException) {
                e.getCause().getCause().printStackTrace();
	        }
            throw e;
        }
    }

	private static void setPreviewView(final Context context, final View view, final int themeRes) {
		if (view instanceof IExtendedView) {
			((IExtendedView) view).setTouchInterceptor(new DummyTouchInterceptor());
		}
		final View windowBackgroundView = view.findViewById(R.id.theme_preview_window_background);
		final View windowContentOverlayView = view.findViewById(R.id.theme_preview_window_content_overlay);
        final View actionBarOverlay = view.findViewById(R.id.actionbar_overlay);
        final Toolbar actionBarView = (Toolbar) view.findViewById(R.id.actionbar);
        final ActionMenuView menuBar = (ActionMenuView) view.findViewById(R.id.menu_bar);
		final View statusContentView = view.findViewById(R.id.theme_preview_status_content);
		final TextView retweetsCountView = (TextView) view.findViewById(R.id.retweets_count);
		final TextView favoritesCountView = (TextView) view.findViewById(R.id.favorites_count);
		final TextView replyCountsView = (TextView) view.findViewById(R.id.replies_count);
		final CardView cardView = (CardView) view.findViewById(R.id.card);

		final int defaultTextSize = getDefaultTextSize(context);
        final int cardBackgroundColor = ThemeUtils.getCardBackgroundColor(context);
        final int accentColor = ThemeUtils.getUserAccentColor(context);

		ViewAccessor.setBackground(windowBackgroundView, ThemeUtils.getWindowBackground(context));
//        ViewAccessor.setBackground(windowContentOverlayView, ThemeUtils.getWindowContentOverlay(context));
        ViewAccessor.setBackground(actionBarView, ThemeUtils.getActionBarBackground(context, themeRes, accentColor, true));
        ViewAccessor.setBackground(actionBarOverlay, ThemeUtils.getWindowContentOverlay(context));
        cardView.setCardBackgroundColor(cardBackgroundColor);

		final int highlightOption = Utils.getLinkHighlightingStyle(context);
		TwidereLinkify linkify = new TwidereLinkify(null);
		linkify.setHighlightOption(highlightOption);


		actionBarView.setTitle(R.string.app_name);
		actionBarView.setTitleTextColor(ColorUtils.getContrastYIQ(accentColor, 192));
        menuBar.setEnabled(false);
        final MenuInflater inflater = new SupportMenuInflater(context);
        inflater.inflate(R.menu.menu_status, menuBar.getMenu());
        ThemeUtils.wrapMenuIcon(menuBar, MENU_GROUP_STATUS_SHARE);
		if (statusContentView != null) {
			ViewAccessor.setBackground(statusContentView, ThemeUtils.getWindowBackground(context));

			final View profileView = statusContentView.findViewById(R.id.profile_container);
            final ImageView profileImageView = (ImageView) statusContentView.findViewById(R.id.profile_image);
			final TextView nameView = (TextView) statusContentView.findViewById(R.id.name);
			final TextView screenNameView = (TextView) statusContentView.findViewById(R.id.screen_name);
			final TextView textView = (TextView) statusContentView.findViewById(R.id.text);
			final TextView timeSourceView = (TextView) statusContentView.findViewById(R.id.time_source);
            final View retweetedByContainer = statusContentView.findViewById(R.id.retweeted_by_container);

            retweetedByContainer.setVisibility(View.GONE);

			nameView.setTextSize(defaultTextSize * 1.25f);
			textView.setTextSize(defaultTextSize * 1.25f);
			screenNameView.setTextSize(defaultTextSize * 0.85f);
			timeSourceView.setTextSize(defaultTextSize * 0.85f);

			profileView.setBackgroundResource(0);
			textView.setTextIsSelectable(false);

            profileImageView.setImageResource(R.mipmap.ic_launcher);
			nameView.setText(TWIDERE_PREVIEW_NAME);
			screenNameView.setText("@" + TWIDERE_PREVIEW_SCREEN_NAME);

			if (highlightOption != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
				textView.setText(Html.fromHtml(TWIDERE_PREVIEW_TEXT_HTML));
				linkify.applyAllLinks(textView, 0, false);
			} else {
				textView.setText(toPlainText(TWIDERE_PREVIEW_TEXT_HTML));
			}

			final String time = formatToLongTimeString(context, System.currentTimeMillis());
			timeSourceView.setText(toPlainText(context.getString(R.string.time_source, time, TWIDERE_PREVIEW_SOURCE)));
		}
		if (retweetsCountView != null) {
			retweetsCountView.setText("2");
		}
		if (favoritesCountView != null) {
			favoritesCountView.setText("4");
		}
		if (replyCountsView != null) {
			replyCountsView.setText("1");
		}
	}

	private static class DummyTouchInterceptor implements TouchInterceptor {

		@Override
		public boolean dispatchTouchEvent(final View view, final MotionEvent event) {
			return false;
		}

		@Override
		public boolean onInterceptTouchEvent(final View view, final MotionEvent event) {
			return true;
		}

		@Override
		public boolean onTouchEvent(final View view, final MotionEvent event) {
			return false;
		}

	}

}