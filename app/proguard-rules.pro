# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in show.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# GSON
-dontwarn com.google.**
-dontwarn sun.misc.**
-dontwarn okio.**

-keepattributes *Annotation*
-keep class com.google.**
-keep class com.google.gson.Gson {*;}

-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# MPAndroidChart
-keep class com.github.mikephil.charting.** {*;}

# ButterKnife
-keep class butterknife.*
-keep class * implements butterknife.Unbinder { public <init>(**, android.view.View); }
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# Retrofit
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions*

-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keepclasseswithmembernames  interface * {
    @retrofit2.* <methods>;
}

-if interface * { @retrofit2.http.* <methods>; }

-keep,allowobfuscation interface <1>

 # Warden
-keep class com.aurora.warden.data.** {*;}
-keep class com.aurora.warden.retro.** {*;}
-keepclassmembers class com.aurora.warden.* { <fields>; }
-keepclassmembers enum * { *; }