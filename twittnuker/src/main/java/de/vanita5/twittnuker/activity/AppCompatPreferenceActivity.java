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

package de.vanita5.twittnuker.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A {@link android.preference.PreferenceActivity} which implements and proxies the necessary calls
 * to be used with AppCompat.
 * <p/>
 * This technique can be used with an {@link android.app.Activity} class, not just
 * {@link android.preference.PreferenceActivity}.
 */
public abstract class AppCompatPreferenceActivity extends PreferenceActivity {
	private AppCompatDelegate mDelegate;

	public ActionBar getSupportActionBar() {
		return getDelegate().getSupportActionBar();
	}

	public void setSupportActionBar(@Nullable Toolbar toolbar) {
		getDelegate().setSupportActionBar(toolbar);
	}

	public boolean supportRequestWindowFeature(int featureId) {
		return getDelegate().requestWindowFeature(featureId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getDelegate().installViewFactory();
		getDelegate().onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStop() {
		super.onStop();
		getDelegate().onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getDelegate().onDestroy();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getDelegate().onPostCreate(savedInstanceState);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		getDelegate().onPostResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getDelegate().onConfigurationChanged(newConfig);
	}

	@Override
	public void setContentView(@LayoutRes int layoutResID) {
		getDelegate().setContentView(layoutResID);
	}

	@Override
	public void setContentView(View view) {
		getDelegate().setContentView(view);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		getDelegate().setContentView(view, params);
	}

	@Override
	public void addContentView(View view, ViewGroup.LayoutParams params) {
		getDelegate().addContentView(view, params);
	}

	@Override
	public void invalidateOptionsMenu() {
		getDelegate().invalidateOptionsMenu();
	}

	@NonNull
	@Override
	public MenuInflater getMenuInflater() {
		return getDelegate().getMenuInflater();
	}

	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		super.onTitleChanged(title, color);
		getDelegate().setTitle(title);
	}

	private AppCompatDelegate getDelegate() {
		if (mDelegate == null) {
			mDelegate = AppCompatDelegate.create(this, null);
		}
		return mDelegate;
	}
}