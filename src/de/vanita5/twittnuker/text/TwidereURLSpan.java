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

package de.vanita5.twittnuker.text;

import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.util.TwidereLinkify.OnLinkClickListener;


public class TwidereURLSpan extends URLSpan implements Constants {

	private final int type, highlightStyle, highlightColor;
	private final long account_id;
	private final String url, orig;
	private final boolean sensitive;
	private final OnLinkClickListener listener;

	public TwidereURLSpan(final String url, final long account_id, final int type, final boolean sensitive,
			final OnLinkClickListener listener, final int highlightStyle, final int highlightColor) {
		this(url, null, account_id, type, sensitive, listener, highlightStyle, highlightColor);
	}

	public TwidereURLSpan(final String url, final String orig, final long account_id, final int type,
			final boolean sensitive, final OnLinkClickListener listener, final int highlightStyle,
			final int highlightColor) {
		super(url);
		this.url = url;
		this.orig = orig;
		this.account_id = account_id;
		this.type = type;
		this.sensitive = sensitive;
		this.listener = listener;
		this.highlightStyle = highlightStyle;
		this.highlightColor = highlightColor;
	}

	@Override
	public void onClick(final View widget) {
		if (listener != null) {
			listener.onLinkClick(url, orig, account_id, type, sensitive);
		}
	}

	@Override
	public void updateDrawState(final TextPaint ds) {
		switch (highlightStyle) {
			case LINK_HIGHLIGHT_OPTION_CODE_BOTH:
				ds.setUnderlineText(true);
				ds.setColor(highlightColor != 0 ? highlightColor : ds.linkColor);
				break;
			case LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE:
				ds.setUnderlineText(true);
				break;
			case LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT:
				ds.setColor(highlightColor != 0 ? highlightColor : ds.linkColor);
				break;
			default:
				break;
		}
	}
}