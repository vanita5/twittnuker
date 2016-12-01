package de.vanita5.twittnuker.model.tab.impl;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.DirectMessagesFragment;
import de.vanita5.twittnuker.model.tab.TabConfiguration;
import de.vanita5.twittnuker.model.tab.DrawableHolder;
import de.vanita5.twittnuker.model.tab.StringHolder;

public class MessagesTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.direct_messages);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.MESSAGE;
    }

    @AccountRequirement
    @Override
    public int getAccountRequirement() {
        return FLAG_HAS_ACCOUNT | FLAG_ACCOUNT_MULTIPLE;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return DirectMessagesFragment.class;
    }
}