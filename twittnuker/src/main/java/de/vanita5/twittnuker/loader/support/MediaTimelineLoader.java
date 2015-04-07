package de.vanita5.twittnuker.loader.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.model.ParcelableStatus;

import java.util.List;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import static de.vanita5.twittnuker.util.Utils.getAccountId;
import static de.vanita5.twittnuker.util.Utils.isFiltered;

public class MediaTimelineLoader extends Twitter4JStatusesLoader {

	private final long mUserId;
	private final String mUserScreenName;
	private final boolean mIsMyTimeline;

	public MediaTimelineLoader(final Context context, final long accountId, final long userId, final String screenName,
                               final long sinceId, final long maxId, final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                               final int tabPosition, boolean fromUser) {
        super(context, accountId, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser);
		mUserId = userId;
		mUserScreenName = screenName;
		mIsMyTimeline = userId > 0 ? accountId == userId : accountId == getAccountId(context, screenName);
	}

    @NonNull
	@Override
    protected ResponseList<Status> getStatuses(@NonNull final Twitter twitter, final Paging paging) throws TwitterException {
		if (mUserId != -1)
			return twitter.getMediaTimeline(mUserId, paging);
		else if (mUserScreenName != null)
			return twitter.getMediaTimeline(mUserScreenName, paging);
        throw new TwitterException("Wrong user");
	}

	@Override
	protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
		return !mIsMyTimeline && isFiltered(database, -1, status.text_plain, status.text_html, status.source, -1);
	}
}