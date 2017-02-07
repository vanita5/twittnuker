package de.vanita5.twittnuker.model.tab.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.TrendsSuggestionsFragment;
import de.vanita5.twittnuker.model.Tab;
import de.vanita5.twittnuker.model.tab.DrawableHolder;
import de.vanita5.twittnuker.model.tab.StringHolder;
import de.vanita5.twittnuker.model.tab.TabConfiguration;
import de.vanita5.twittnuker.model.tab.conf.TrendsLocationExtraConfiguration;
import de.vanita5.twittnuker.model.tab.extra.TrendsTabExtras;

import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_PLACE;

public class TrendsTabConfiguration extends TabConfiguration {
    @NonNull
    @Override
    public StringHolder getName() {
        return StringHolder.resource(R.string.trends);
    }

    @NonNull
    @Override
    public DrawableHolder getIcon() {
        return DrawableHolder.Builtin.HASHTAG;
    }

    @AccountFlags
    @Override
    public int getAccountFlags() {
        return FLAG_HAS_ACCOUNT | FLAG_ACCOUNT_REQUIRED | FLAG_ACCOUNT_MUTABLE;
    }

    @Nullable
    @Override
    public ExtraConfiguration[] getExtraConfigurations(Context context) {
        return new ExtraConfiguration[]{
                new TrendsLocationExtraConfiguration(EXTRA_PLACE).title(R.string.trends_location).mutable(true),
        };
    }

    @Override
    public boolean applyExtraConfigurationTo(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final TrendsTabExtras extras = (TrendsTabExtras) tab.getExtras();
        assert extras != null;
        switch (extraConf.getKey()) {
            case EXTRA_PLACE: {
                TrendsLocationExtraConfiguration conf = (TrendsLocationExtraConfiguration) extraConf;
                TrendsLocationExtraConfiguration.Place place = conf.getValue();
                if (place != null) {
                    extras.setWoeId(place.getWoeId());
                    extras.setPlaceName(place.getName());
                } else {
                    return false;
                }
                break;
            }
        }
        return true;
    }

    @Override
    public boolean readExtraConfigurationFrom(@NonNull Tab tab, @NonNull ExtraConfiguration extraConf) {
        final TrendsTabExtras extras = (TrendsTabExtras) tab.getExtras();
        if (extras == null) return false;
        switch (extraConf.getKey()) {
            case EXTRA_PLACE: {
                final int woeId = extras.getWoeId();
                final String name = extras.getPlaceName();
                if (name != null) {
                    TrendsLocationExtraConfiguration.Place place = new TrendsLocationExtraConfiguration.Place(woeId, name);
                    ((TrendsLocationExtraConfiguration) extraConf).setValue(place);
                } else {
                    ((TrendsLocationExtraConfiguration) extraConf).setValue(null);
                }
                break;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> getFragmentClass() {
        return TrendsSuggestionsFragment.class;
    }
}