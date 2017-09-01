#-dontobfuscate

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

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
# -keep class com.example.BuildConfig { *; }

# Otto http://square.github.io/otto/
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

-keep class * extends android.support.v4.view.ActionProvider
-keepclassmembers class * extends android.support.v4.view.ActionProvider {
    <init>(android.content.Context);
}

# Retrofit
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions
-dontwarn javax.annotation.**

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn com.squareup.haha.**
-dontwarn com.google.android.gms.**
-dontwarn com.makeramen.roundedimageview.**
-dontwarn jnamed**
-dontwarn org.xbill.DNS.**
-dontwarn com.bluelinelabs.logansquare.**
-dontwarn okio.**
-dontwarn android.support.**
-dontwarn com.afollestad.**
-dontwarn com.facebook.stetho.**
-dontwarn com.google.android.**
-dontwarn okhttp3.**
-dontwarn sun.net.spi.**
-dontwarn sun.misc.**
-dontwarn sun.nio.**
-dontwarn java.nio.file.**

-dontwarn com.twitter.Autolink
-dontwarn com.google.appengine.api.urlfetch.*

-dontwarn InnerClasses

-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes SourceFile
-keepattributes LineNumberTable
-keepattributes Signature
-keepattributes InnerClasses


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

-keep class de.vanita5.microblog.library.annotation.NoObfuscate
-keep @de.vanita5.microblog.library.annotation.NoObfuscate class *

# https://github.com/mariotaku/RestFu
-keep class org.mariotaku.restfu.annotation.** { *; }

# http://square.github.io/otto/
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

-keep class * extends android.support.v4.view.ActionProvider
-keepclassmembers class * extends android.support.v4.view.ActionProvider {
    <init>(android.content.Context);
}

# Essential components
-keep class * extends de.vanita5.twittnuker.util.Analyzer
-keep class * extends de.vanita5.twittnuker.util.MapFragmentFactory
-keep class * extends de.vanita5.twittnuker.util.twitter.card.TwitterCardViewFactory

# Extra feature service
-keep class * extends de.vanita5.twittnuker.util.premium.ExtraFeaturesService

# Extra feature component factories
-keep class * extends de.vanita5.twittnuker.util.gifshare.GifShareProvider.Factory
-keep class * extends de.vanita5.twittnuker.util.schedule.StatusScheduleProvider.Factory
-keep class * extends de.vanita5.twittnuker.util.sync.DataSyncProvider.Factory
-keep class * extends de.vanita5.twittnuker.util.sync.TimelineSyncManager.Factory

# View components
-keep class * extends de.vanita5.twittnuker.util.view.AppBarChildBehavior.ChildTransformation

#jackson fasterxml
-keepnames class org.codehaus.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-dontwarn com.squareup.picasso.**

# Osmdroid
-dontwarn org.osmdroid.**

# Android 6 (API 23)
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn retrofit.client.ApacheClient$GenericEntityHttpRequest
-dontwarn retrofit.client.ApacheClient$GenericHttpRequest
-dontwarn retrofit.client.ApacheClient$TypedOutputEntity

# slf4j
-keep class org.slf4j.** { *; }
-keep enum org.slf4j.** { *; }
-keep interface org.slf4j.** { *; }
-dontwarn org.slf4j.**

#JacksonXML
-dontwarn java.beans.**

-keep public class com.google.android.gms.* { public *; }
-dontwarn com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView


-dontwarn rx.**
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-dontwarn com.damnhandy.uri.template.jackson.datatype.**

# leakcanary
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }
-keep class com.squareup.haha.** { *; }
-dontwarn com.squareup.leakcanary.**

# Marshmallow removed Notification.setLatestEventInfo()
-dontwarn android.app.Notification

-dontwarn de.vanita5.microblog.library.twitter.model.TwitterResponse$AccessLevel

# app-theme-engine
-dontwarn com.afollestad.appthemeengine.**

-keepclassmembers class * {
    private <fields>;
}

-keepclassmembers class de.vanita5.twittnuker.activity.BrowserSignInActivity$InjectorJavaScriptInterface {
    public *;
}

# junit
-keep class org.junit.** { *; }
-dontwarn org.junit.**

-keep class junit.** { *; }
-dontwarn junit.**

# Kotlin
-keep class de.vanita5.twittnuker.** { *; }
-dontwarn de.vanita5.twittnuker.**
-dontwarn de.vanita5.twittnuker.adapter.iface.**
-dontwarn de.vanita5.twittnuker.fragment.AbsStatusesFragment
-dontwarn de.vanita5.twittnuker.fragment.CursorStatusesFragment
-dontwarn de.vanita5.twittnuker.fragment.ItemsListFragment

-keep class com.afollestad.** { *; }

# Dropbox
-dontwarn javax.servlet.**

-keep class com.android.vending.billing.**
-dontwarn com.anjlab.android.iab.v3.BillingProcessor

-keep class com.jayway.jsonpath.spi.** { *; }
-dontwarn com.jayway.jsonpath.spi.**