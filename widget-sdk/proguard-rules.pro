# Limechat Widget SDK ProGuard Rules

# Keep public API classes
-keep public class ai.limechat.widget.LimechatWidgetView {
    public <methods>;
}

-keep public class ai.limechat.widget.WidgetFilePicker {
    public <methods>;
}

-keep public interface ai.limechat.widget.WidgetCallback {
    *;
}

-keep public class ai.limechat.widget.WidgetError {
    *;
}

-keep public class ai.limechat.widget.models.WidgetConfig {
    *;
}

-keep public class ai.limechat.widget.models.WidgetConfig$User {
    *;
}

-keep public enum ai.limechat.widget.models.WidgetConfig$ColorScheme {
    *;
}

# Keep JavascriptInterface methods
-keep class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView related classes
-keep class android.webkit.** { *; }
-keep class androidx.webkit.** { *; }

# Keep JSON serialization
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.json.* *;
}

# Keep Kotlin metadata for data classes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Activity Result Contracts
-keep class androidx.activity.result.** { *; }