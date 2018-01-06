package de.vanita5.twittnuker.model.tab;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import org.mariotaku.kpreferences.SharedPreferencesExtensionsKt;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.constant.PreferenceKeysKt;
import de.vanita5.twittnuker.util.dagger.DependencyHolder;

import java.util.ArrayList;
import java.util.List;

import kotlin.text.StringsKt;

public abstract class DrawableHolder {

    private String name;

    @Nullable
    public static DrawableHolder builtin(@NonNull String key) {
        switch (key) {
            // Default built-in icons map, don't remove icons below
            case "accounts":
                return Builtin.ACCOUNTS;
            case "hashtag":
                return Builtin.HASHTAG;
            case "heart":
                return Builtin.HEART;
            case "home":
                return Builtin.HOME;
            case "list":
                return Builtin.LIST;
            case "mention":
                return Builtin.MENTION;
            case "notifications":
                return Builtin.NOTIFICATIONS;
            case "gallery":
                return Builtin.GALLERY;
            case "message":
                return Builtin.MESSAGE;
            case "quote":
                return Builtin.QUOTE;
            case "search":
                return Builtin.SEARCH;
            case "staggered":
                return Builtin.STAGGERED;
            case "star":
                return Builtin.STAR;
            case "trends":
                return Builtin.TRENDS;
            case "twittnuker":
                return Builtin.TWITTNUKER;
            case "twitter":
                return Builtin.TWITTER;
            case "user":
                return Builtin.USER;
            // End of default built-in icons
            case "favorite":
                return Builtin.FAVORITE;
            case "web":
                return Builtin.WEB;
            // Twittnuker built-ins
            case "comp":
                return Builtin.COMP;
            case "cthulhu":
                return Builtin.CTHULHU;
            case "harvey":
                return Builtin.HARVEY;
            case "majora":
                return Builtin.MAJORA;
            case "metroid":
                return Builtin.METROID;
            case "nyarlathorepirycx":
                return Builtin.NYARLATHOREPIRYCX;
            case "pokeball":
                return Builtin.POKEBALL;
            case "triforce":
                return Builtin.TRIFORCE;
        }
        return null;
    }

    @NonNull
    public static DrawableHolder resource(int resId) {
        return new Resource(resId);
    }

    @NonNull
    public abstract String getPersistentKey();

    @NonNull
    public abstract Drawable createDrawable(Context context);

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static DrawableHolder parse(String str) {
        DrawableHolder icon = builtin(str);
        if (icon != null) {
            return icon;
        }
        return null;
    }

    public static List<DrawableHolder> builtins() {
        String[] keys = {
                // Default built-in icons map, don't remove icons below
                "accounts",
                "hashtag",
                "heart",
                "home",
                "list",
                "mention",
                "notifications",
                "gallery",
                "message",
                "quote",
                "search",
                "staggered",
                "star",
                "trends",
                "twittnuker",
                "twitter",
                "user",
                // End of default built-in icons
                "favorite",
                "web",
                // Twittnuker built-ins
                "comp",
                "cthulhu",
                "harvey",
                "majora",
                "metroid",
                "nyarlathorepirycx",
                "pokeball",
                "triforce",
        };
        List<DrawableHolder> list = new ArrayList<>();
        for (String key : keys) {
            list.add(builtin(key));
        }
        return list;
    }

    private static class Resource extends DrawableHolder {
        private int resId;

        public Resource(int resId) {
            this.resId = resId;
        }

        @Override
        @NonNull
        public String getPersistentKey() {
            return String.valueOf(resId);
        }

        @NonNull
        @Override
        public Drawable createDrawable(Context context) {
            return ContextCompat.getDrawable(context, resId);
        }
    }

    public static class Builtin extends DrawableHolder {
        @NonNull
        public static final DrawableHolder HOME = new Builtin("home", R.drawable.ic_action_home);
        @NonNull
        public static final DrawableHolder HEART = new Builtin("heart", R.drawable.ic_action_heart);
        @NonNull
        public static final DrawableHolder HASHTAG = new Builtin("hashtag", R.drawable.ic_action_hashtag);
        @NonNull
        public static final DrawableHolder ACCOUNTS = new Builtin("accounts", R.drawable.ic_action_accounts);
        @NonNull
        public static final DrawableHolder LIST = new Builtin("list", R.drawable.ic_action_list);
        @NonNull
        public static final DrawableHolder MENTION = new Builtin("mention", R.drawable.ic_action_at);
        @NonNull
        public static final DrawableHolder NOTIFICATIONS = new Builtin("notifications", R.drawable.ic_action_notification);
        @NonNull
        public static final DrawableHolder GALLERY = new Builtin("gallery", R.drawable.ic_action_gallery);
        @NonNull
        public static final DrawableHolder MESSAGE = new Builtin("message", R.drawable.ic_action_message);
        @NonNull
        public static final DrawableHolder QUOTE = new Builtin("quote", R.drawable.ic_action_quote);
        @NonNull
        public static final DrawableHolder SEARCH = new Builtin("search", R.drawable.ic_action_search);
        @NonNull
        public static final DrawableHolder STAGGERED = new Builtin("staggered", R.drawable.ic_action_view_quilt);
        @NonNull
        public static final DrawableHolder STAR = new Builtin("star", R.drawable.ic_action_star);
        @NonNull
        public static final DrawableHolder TRENDS = new Builtin("trends", R.drawable.ic_action_trends);
        @NonNull
        public static final DrawableHolder TWITTNUKER = new Builtin("twittnuker", R.drawable.ic_action_twitter);
        @NonNull
        public static final DrawableHolder TWITTER = new Builtin("twitter", R.drawable.ic_action_twitter);
        @NonNull
        public static final DrawableHolder USER = new Builtin("user", R.drawable.ic_action_user);
        @NonNull
        public static final DrawableHolder FAVORITE = new DrawableHolder() {
            @NonNull
            @Override
            public String getPersistentKey() {
                return "favorite";
            }

            @Nullable
            @Override
            public String getName() {
                return "Favorite/Like";
            }

            @NonNull
            @Override
            public Drawable createDrawable(Context context) {
                final DependencyHolder holder = DependencyHolder.Companion.get(context);
                if (SharedPreferencesExtensionsKt.get(holder.preferences, PreferenceKeysKt.getIWantMyStarsBackKey())) {
                    return ContextCompat.getDrawable(context, R.drawable.ic_action_star);
                }
                return ContextCompat.getDrawable(context, R.drawable.ic_action_heart);
            }
        };
        @NonNull
        public static final DrawableHolder WEB = new Builtin("web", R.drawable.ic_action_web);
        @NonNull
        public static final DrawableHolder COMP = new Builtin("comp", R.drawable.ic_action_comp);
        @NonNull
        public static final DrawableHolder CTHULHU = new Builtin("cthulhu", R.drawable.ic_action_cthulhu);
        @NonNull
        public static final DrawableHolder HARVEY = new Builtin("harvey", R.drawable.ic_action_harvey);
        @NonNull
        public static final DrawableHolder MAJORA = new Builtin("majora", R.drawable.ic_action_heart_z);
        @NonNull
        public static final DrawableHolder METROID = new Builtin("metroid", R.drawable.ic_action_metroid);
        @NonNull
        public static final DrawableHolder NYARLATHOREPIRYCX = new Builtin("nyarlathorepirycx", R.drawable.ic_action_nyarlathorepirycx);
        @NonNull
        public static final DrawableHolder POKEBALL = new Builtin("pokeball", R.drawable.ic_action_pokeball);
        @NonNull
        public static final DrawableHolder TRIFORCE = new Builtin("triforce", R.drawable.ic_action_triforce);

        private final String key;
        private final int resId;

        private Builtin(String key, int resId) {
            this.key = key;
            this.resId = resId;
            setName(StringsKt.capitalize(key));
        }

        @Override
        @NonNull
        public String getPersistentKey() {
            return key;
        }

        @NonNull
        @Override
        public Drawable createDrawable(Context context) {
            return ContextCompat.getDrawable(context, resId);
        }
    }
}