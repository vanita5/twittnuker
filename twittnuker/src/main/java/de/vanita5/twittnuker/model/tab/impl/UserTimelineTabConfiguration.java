package de.vanita5.twittnuker.model.tab.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.UserTimelineFragment;
import de.vanita5.twittnuker.model.tab.DrawableHolder;
import de.vanita5.twittnuker.model.tab.StringHolder;
import de.vanita5.twittnuker.model.tab.TabConfiguration;
import de.vanita5.twittnuker.model.tab.conf.UserExtraConfiguration;

import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER;

public class UserTimelineTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.users_statuses);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.USER;
    }

    @AccountRequirement
    @Override
    public int getAccountRequirement() {
        return FLAG_HAS_ACCOUNT | FLAG_ACCOUNT_REQUIRED;
    }

    @Nullable
    @Override
    public ExtraConfiguration[] getExtraConfigurations(Context context) {
        return new ExtraConfiguration[]{
                new UserExtraConfiguration(EXTRA_USER).title(R.string.user).headerTitle(R.string.user)
        };
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return UserTimelineFragment.class;
    }
}