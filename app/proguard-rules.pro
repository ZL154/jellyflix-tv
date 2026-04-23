# Jellyflix TV proguard rules.

# Kotlin serialization keeps
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Jellyfin SDK models use reflection.
-keep class org.jellyfin.sdk.model.** { *; }

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Plugin API: keep so third-party plugin DEX can resolve symbols.
-keep class com.jellyflix.plugin.** { *; }
-keep interface com.jellyflix.plugin.** { *; }

# Timber, OkHttp
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okhttp3.internal.platform.ConscryptPlatform
