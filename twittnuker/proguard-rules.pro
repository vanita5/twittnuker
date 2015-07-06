-dontobfuscate

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keepclassmembers class android.support.v7.internal.app.WindowDecorActionBar {
    private android.support.v7.internal.widget.ActionBarContextView mContextView;
    private android.support.v7.internal.widget.DecorToolbar mDecorToolbar;
}

-keepclassmembers class android.support.v7.internal.widget.ActionBarOverlayLayout {
    private android.graphics.drawable.Drawable mWindowContentOverlay;
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep the BuildConfig
-keep class com.example.BuildConfig { *; }

# Otto http://square.github.io/otto/
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# Retrofit
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
#-dontwarn retrofit.**
#-dontwarn okio.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-keep class java.util.regex.Pattern { *; }
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.**
-dontwarn sun.net.spi.**
-dontwarn org.codehaus.mojo.**
-dontwarn com.twitter.Autolink
-dontwarn com.google.appengine.api.urlfetch.*


# dnsjava
-dontnote org.xbill.DNS.spi.DNSJavaNameServiceDescriptor
-dontwarn org.xbill.DNS.spi.DNSJavaNameServiceDescriptor

# Tile Image View
-dontwarn com.jakewharton.**

# snakeyaml
-keep class org.yaml.snakeyaml.** { public protected private *; }
-keep class org.yaml.snakeyaml.** { public protected private *; }
-dontwarn org.yaml.snakeyaml.**

# twitter4
-keep class twitter4j.** { public protected private *; }
-keep class de.vanita5.twittnuker.** { public protected private *; }
-keep class org.mariotaku.** { public protected private *; }

#android-gif-drawable
-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}

# https://github.com/bluelinelabs/LoganSquare
-keep class com.bluelinelabs.logansquare.** { *; }
-keep @com.bluelinelabs.logansquare.annotation.JsonObject class *
-keep class **$$JsonObjectMapper { *; }

#jackson fasterxml
-keepnames class org.codehaus.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-dontwarn com.squareup.picasso.**