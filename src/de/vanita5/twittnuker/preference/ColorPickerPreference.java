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

package de.vanita5.twittnuker.preference;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.view.ColorPickerPresetsView;
import de.vanita5.twittnuker.view.ColorPickerView;
import de.vanita5.twittnuker.view.ColorPickerPresetsView.OnColorClickListener;
import de.vanita5.twittnuker.view.ColorPickerView.OnColorChangedListener;

public class ColorPickerPreference extends DialogPreference implements DialogInterface.OnClickListener,
		OnColorChangedListener, OnColorClickListener {

	private View mView;
	protected int mDefaultValue = Color.WHITE;
	private final float mDensity;
	private boolean mAlphaSliderEnabled = false;

	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	private static final String ATTR_DEFAULTVALUE = "defaultValue";
	private static final String ATTR_ALPHASLIDER = "alphaSlider";

	private ColorPickerView mColorPicker;
	private ColorPickerPresetsView mColorPresets;

	public ColorPickerPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ColorPickerPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mDensity = context.getResources().getDisplayMetrics().density;
		init(context, attrs);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final int color = mColorPicker.getColor();
				if (isPersistent()) {
					persistInt(color);
				}
				setPreviewColor();
				final OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
				if (listener != null) {
					listener.onPreferenceChange(this, color);
				}
				break;
		}
	}

	@Override
	public void onColorChanged(final int color) {
		final AlertDialog dialog = (AlertDialog) getDialog();
		if (dialog == null) return;
		final Context context = getContext();
		dialog.setIcon(new BitmapDrawable(context.getResources(), ColorPickerView.getColorPreviewBitmap(context, color)));
	}

	@Override
	public void onColorClick(final int color) {
		if (mColorPicker == null) return;
		mColorPicker.setColor(color, true);
	}

	@Override
	public void setDefaultValue(final Object value) {
		if (!(value instanceof Integer)) return;
		mDefaultValue = (Integer) value;
	}

	protected void init(final Context context, final AttributeSet attrs) {
		if (attrs != null) {
			final String defaultValue = attrs.getAttributeValue(ANDROID_NS, ATTR_DEFAULTVALUE);
			if (defaultValue != null && defaultValue.startsWith("#")) {
				try {
					setDefaultValue(Color.parseColor(defaultValue));
				} catch (final IllegalArgumentException e) {
					Log.e("ColorPickerPreference", "Wrong color: " + defaultValue);
					setDefaultValue(Color.WHITE);
				}
			} else {
				final int colorResourceId = attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DEFAULTVALUE, 0);
				if (colorResourceId != 0) {
					setDefaultValue(context.getResources().getColor(colorResourceId));
				}
			}
			mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, ATTR_ALPHASLIDER, false);
		}
	}

	@Override
	protected void onBindView(final View view) {
		super.onBindView(view);
		mView = view;
		setPreviewColor();
	}

	@Override
	protected void onPrepareDialogBuilder(final Builder builder) {
		super.onPrepareDialogBuilder(builder);
		final Context context = getContext();
		final LayoutInflater inflater = LayoutInflater.from(getContext());
		final View view = inflater.inflate(R.layout.color_picker, null);

		final int val = getValue();

		mColorPicker = (ColorPickerView) view.findViewById(R.id.color_picker);
		mColorPresets = (ColorPickerPresetsView) view.findViewById(R.id.color_presets);
		mColorPicker.setOnColorChangedListener(this);
		mColorPresets.setOnColorClickListener(this);

		mColorPicker.setColor(val, true);
		mColorPicker.setAlphaSliderVisible(mAlphaSliderEnabled);
		builder.setView(view);
		builder.setIcon(new BitmapDrawable(context.getResources(), ColorPickerView.getColorPreviewBitmap(context, val)));
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
		if (isPersistent() && defaultValue instanceof Integer) {
			persistInt(restoreValue ? getValue() : (Integer) defaultValue);
		}
	}

	private int getValue() {
		try {
			if (isPersistent()) return getPersistedInt(mDefaultValue);
		} catch (final ClassCastException e) {
			e.printStackTrace();
		}
		return mDefaultValue;
	}

	private void setPreviewColor() {
		if (mView == null) return;
		final View widget_frame_view = mView.findViewById(android.R.id.widget_frame);
		if (!(widget_frame_view instanceof ViewGroup)) return;
		final ViewGroup widget_frame = (ViewGroup) widget_frame_view;
		widget_frame.setVisibility(View.VISIBLE);
		widget_frame.setPadding(widget_frame.getPaddingLeft(), widget_frame.getPaddingTop(), (int) (mDensity * 8),
				widget_frame.getPaddingBottom());
		// remove preview image that is already created
		widget_frame.removeAllViews();
		widget_frame.setAlpha(isEnabled() ? 1 : 0.25f);
		final ImageView imageView = new ImageView(getContext());
		widget_frame.addView(imageView);
		imageView.setImageBitmap(ColorPickerView.getColorPreviewBitmap(getContext(), getValue()));
	}

}
