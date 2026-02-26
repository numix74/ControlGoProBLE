# ── Logs ──────────────────────────────────────────────────────────────────────
# Supprime tous les appels Log.v/d/i en release (Log.w/e conservés pour crashs)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ── Protobuf Lite ──────────────────────────────────────────────────────────────
-keep class com.google.protobuf.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# ── Nordic BLE ────────────────────────────────────────────────────────────────
-keep class no.nordicsemi.android.ble.** { *; }

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── App classes ───────────────────────────────────────────────────────────────
-keep class com.ximun.gopropro.proto.** { *; }
