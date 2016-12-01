package de.vanita5.twittnuker.model.tab.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.UserListTimelineFragment;
import de.vanita5.twittnuker.model.ParcelableUserList;
import de.vanita5.twittnuker.model.Tab;
import de.vanita5.twittnuker.model.tab.TabConfiguration;
import de.vanita5.twittnuker.model.tab.DrawableHolder;
import de.vanita5.twittnuker.model.tab.StringHolder;
import de.vanita5.twittnuker.model.tab.argument.UserListArguments;
import de.vanita5.twittnuker.model.tab.conf.UserListExtraConfiguration;

import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER_LIST;

public class UserListTimelineTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.list_timeline);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.LIST;
    }

    @AccountFlags
    @Override
    public int getAccountFlags() {
        return FLAG_HAS_ACCOUNT | FLAG_ACCOUNT_REQUIRED;
    }

    @Nullable
    @Override
    public ExtraConfiguration[] getExtraConfigurations(Context context) {
        return new ExtraConfiguration[]{
                new UserListExtraConfiguration(EXTRA_USER_LIST).title(R.string.user_list).headerTitle(R.string.user_list)
        };
    }

    @Override
    public boolean applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        UserListArguments arguments = (UserListArguments) tab.getArguments();
        assert arguments != null;
        switch (extraConf.getKey()) {
            case EXTRA_USER_LIST: {
                final ParcelableUserList userList = ((UserListExtraConfiguration) extraConf).getValue();
                if (userList == null) return false;
                arguments.setListId(userList.id);
                break;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return UserListTimelineFragment.class;
    }
}