# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/zainahmad/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools-proguard.html

# Add any custom rules below this line.

# Keep generic signatures to allow Gson to deserialize generic collections
-keepattributes Signature

# Keep TypeToken and its subclasses to prevent R8 from erasing generic type arguments
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep fields annotated with SerializedName
-keepattributes *Annotation*

# Keep compose-emoji-picker library classes and members to prevent ClassCastException during JSON deserialization
-keep class dev.alexdametto.compose_emoji_picker.** { *; }
-keep class dev.alexdametto.compose_emoji_picker.data.model.** { *; }

# Keep Glance ActionCallback implementations and their parameterless constructors
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    <init>();
}


