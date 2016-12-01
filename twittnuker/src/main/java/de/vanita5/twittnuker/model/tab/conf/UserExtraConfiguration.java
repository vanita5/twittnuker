package de.vanita5.twittnuker.model.tab.conf;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.UserListSelectorActivity;
import de.vanita5.twittnuker.model.tab.TabConfiguration;

import static de.vanita5.twittnuker.constant.IntentConstants.INTENT_ACTION_SELECT_USER;

public class UserExtraConfiguration extends TabConfiguration.ExtraConfiguration {
    public UserExtraConfiguration(String key) {
        super(key);
    }


    @NonNull
    @Override
    public View onCreateView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_simple_user, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull final Context context, @NonNull View view, @NonNull final DialogFragment fragment) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
                intent.setClass(context, UserListSelectorActivity.class);
                fragment.startActivity(intent);
            }
        });
    }
}