package de.vanita5.twittnuker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MenuInflater;

import org.mariotaku.menucomponent.widget.MenuBar;

import de.vanita5.twittnuker.menu.TwidereMenuInflater;

public class TwidereMenuBar extends MenuBar {
	public TwidereMenuBar(Context context) {
		super(context);
	}

	public TwidereMenuBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public MenuInflater getMenuInflater() {
		return new TwidereMenuInflater(getContext());
	}
}