# Consumer ProGuard rules for Limechat Widget SDK

# Keep public API
-keep public class ai.limechat.widget.** { *; }

# Keep JavaScript interface methods
-keep class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView related classes that might be accessed
-keep class android.webkit.WebView { *; }
-keep class android.webkit.WebViewClient { *; }
-keep class android.webkit.WebChromeClient { *; }