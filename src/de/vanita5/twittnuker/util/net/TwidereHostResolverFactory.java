package de.vanita5.twittnuker.util.net;

import de.vanita5.twittnuker.app.TwittnukerApplication;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HostAddressResolverFactory;
import twitter4j.http.HttpClientConfiguration;

public class TwidereHostResolverFactory implements HostAddressResolverFactory {

	private final TwittnukerApplication mApplication;

	public TwidereHostResolverFactory(final TwittnukerApplication application) {
		mApplication = application;
	}

	@Override
	public HostAddressResolver getInstance(final HttpClientConfiguration conf) {
		return mApplication.getHostAddressResolver();
	}

}