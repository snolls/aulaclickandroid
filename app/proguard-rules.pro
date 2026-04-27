# ── Retrofit ──────────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# ── OkHttp ────────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ── Gson ──────────────────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── Modelos de red (DTOs) ─────────────────────────────────────────────────────
-keep class com.aulaclick.app.network.models.** { *; }

# ── Glide ─────────────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-dontwarn com.bumptech.glide.**

# ── Cloudinary ────────────────────────────────────────────────────────────────
-keep class com.cloudinary.** { *; }
-dontwarn com.cloudinary.**

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ── Material / AndroidX ───────────────────────────────────────────────────────
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ── Mantener atributos de anotaciones (necesario para Retrofit) ───────────────
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
