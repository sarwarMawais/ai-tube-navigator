# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.aitube.navigator.data.remote.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes AnnotationDefault,RuntimeVisibleAnnotations

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
