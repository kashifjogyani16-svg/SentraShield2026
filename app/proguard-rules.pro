# Keep Room entities & DAOs
-keep class com.sentra.shield.data.db.** { *; }
-keep class com.sentra.shield.data.model.** { *; }

# Retrofit / OkHttp / Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Compose
-keep class androidx.compose.** { *; }
