# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ---- Retrofit ----
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ---- OkHttp ----
-dontwarn okhttp3.**
-dontwarn okio.**

# ---- Gson ----
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class com.englishfriendai.app.data.remote.dto.** { <fields>; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class sun.misc.Unsafe { *; }

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---- Hilt ----
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# ---- Kotlin coroutines / socket.io ----
-dontwarn kotlinx.coroutines.**
-dontwarn io.socket.**
-keep class io.socket.** { *; }

# ---- Domain / data models kept for reflection-based (de)serialization ----
-keep class com.englishfriendai.app.domain.model.** { *; }
