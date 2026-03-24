# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase { abstract *; }

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep data model classes
-keep class com.offlineledger.data.model.** { *; }
