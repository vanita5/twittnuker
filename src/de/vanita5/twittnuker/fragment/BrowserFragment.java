package de.vanita5.twittnuker.fragment;

import android.os.Bundle;
import android.webkit.WebView;

import de.vanita5.twittnuker.util.ParseUtils;


public class BrowserFragment extends BaseWebViewFragment {

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		final Object uri = args != null ? args.get(EXTRA_URI) : null;
		final WebView view = getWebView();
		view.loadUrl(ParseUtils.parseString(uri, "about:blank"));
	}
}
