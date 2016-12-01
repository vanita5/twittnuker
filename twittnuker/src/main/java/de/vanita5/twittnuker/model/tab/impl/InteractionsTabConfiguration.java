package de.vanita5.twittnuker.model.tab.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.InteractionsTimelineFragment;
import de.vanita5.twittnuker.model.Tab;
import de.vanita5.twittnuker.model.tab.DrawableHolder;
import de.vanita5.twittnuker.model.tab.StringHolder;
import de.vanita5.twittnuker.model.tab.TabConfiguration;
import de.vanita5.twittnuker.model.tab.extra.InteractionsTabExtras;

import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_MENTIONS_ONLY;
import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_MY_FOLLOWING_ONLY;

public class InteractionsTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.interactions);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.NOTIFICATIONS;
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
                new BooleanExtraConfiguration(EXTRA_MY_FOLLOWING_ONLY, false).title(R.string.following_only),
                new BooleanExtraConfiguration(EXTRA_MENTIONS_ONLY, false).title(R.string.mentions_only),
        };
    }

    @Override
    public void applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final InteractionsTabExtras extras = (InteractionsTabExtras) tab.getExtras();
        assert extras != null;
        switch (extraConf.getKey()) {
            case EXTRA_MY_FOLLOWING_ONLY: {
                extras.setMyFollowingOnly(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
            case EXTRA_MENTIONS_ONLY: {
                extras.setMentionsOnly(((BooleanExtraConfiguration) extraConf).getValue());
                break;
            }
        }
    }

    @Override
    public void readExtraConfigurationFrom(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final InteractionsTabExtras extras = (InteractionsTabExtras) tab.getExtras();
        if (extras == null) return;
        switch (extraConf.getKey()) {
            case EXTRA_MY_FOLLOWING_ONLY: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isMyFollowingOnly());
                break;
            }
            case EXTRA_MENTIONS_ONLY: {
                ((BooleanExtraConfiguration) extraConf).setValue(extras.isMentionsOnly());
                break;
            }
        }
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return InteractionsTimelineFragment.class;
    }
}