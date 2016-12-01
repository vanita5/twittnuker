package de.vanita5.twittnuker.model.tab.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.HomeTimelineFragment;
import de.vanita5.twittnuker.model.Tab;
import de.vanita5.twittnuker.model.tab.DrawableHolder;
import de.vanita5.twittnuker.model.tab.StringHolder;
import de.vanita5.twittnuker.model.tab.TabConfiguration;
import de.vanita5.twittnuker.model.tab.extra.HomeTabExtras;

import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_HIDE_QUOTES;
import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_HIDE_REPLIES;
import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_HIDE_RETWEETS;

public class HomeTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.home);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.HOME;
    }

    @AccountRequirement
    @Override
    public int getAccountRequirement() {
        return FLAG_HAS_ACCOUNT | FLAG_ACCOUNT_MULTIPLE;
    }

    @Nullable
    @Override
    public ExtraConfiguration[] getExtraConfigurations(Context context) {
        return new ExtraConfiguration[]{
                new BooleanExtraConfiguration(EXTRA_HIDE_RETWEETS, false).title(R.string.hide_retweets),
                new BooleanExtraConfiguration(EXTRA_HIDE_QUOTES, false).title(R.string.hide_quotes),
                new BooleanExtraConfiguration(EXTRA_HIDE_REPLIES, false).title(R.string.hide_replies),
        };
    }

    @Override
    public void applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final HomeTabExtras extras = (HomeTabExtras) tab.getExtras();
        assert extras != null;
        switch (extraConf.getKey()) {
            case EXTRA_HIDE_RETWEETS: {
                extras.setHideRetweets(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
            case EXTRA_HIDE_QUOTES: {
                extras.setHideQuotes(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
            case EXTRA_HIDE_REPLIES: {
                extras.setHideReplies(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
        }
    }

    @Override
    public void readExtraConfigurationFrom(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final HomeTabExtras extras = (HomeTabExtras) tab.getExtras();
        if (extras == null) return;
        switch (extraConf.getKey()) {
            case EXTRA_HIDE_RETWEETS: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isHideRetweets());
                break;
            }
            case EXTRA_HIDE_QUOTES: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isHideQuotes());
                break;
            }
            case EXTRA_HIDE_REPLIES: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isHideReplies());
                break;
            }
        }
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return HomeTimelineFragment.class;
    }
}