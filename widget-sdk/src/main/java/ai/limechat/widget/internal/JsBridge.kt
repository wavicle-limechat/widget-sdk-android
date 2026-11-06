package ai.limechat.widget.internal

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView

/**
 * Native side of the page bridge. The page detects android by `window.LimechatAndroid`
 * and calls `window.LimechatAndroid.postMessage(json)`, so the name and method must stay.
 */
internal class JsBridge(
    private val webView: WebView,
    private val onMessage: (String) -> Unit
) {
    private companion object {
        const val JS_INTERFACE_NAME = "LimechatAndroid"
    }

    private var jsInterfaceAdded = false

    /** Must run before the first load. The page reads the interface at mount. */
    @SuppressLint("JavascriptInterface")
    fun setup() {
        if (jsInterfaceAdded) return
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun postMessage(message: String) {
                onMessage(message)
            }
        }, JS_INTERFACE_NAME)
        jsInterfaceAdded = true
    }
}
