package de.vanita5.twittnuker.view.holder;

import android.content.Context;
import android.view.View;

import de.vanita5.twittnuker.Constants;


public class ViewHolder implements Constants {

	public View view;

	public ViewHolder(final View view) {
		if (view == null) throw new NullPointerException();
		this.view = view;
	}

	protected View findViewById(final int id) {
		return view.findViewById(id);
	}

	protected Context getContext() {
		return view.getContext();
	}

	protected String getString(final int resId, final Object... formatArgs) {
		return getContext().getString(resId, formatArgs);
	}

}
