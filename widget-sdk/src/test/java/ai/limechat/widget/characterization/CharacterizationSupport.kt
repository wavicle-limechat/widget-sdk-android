package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.WidgetCallback
import ai.limechat.widget.WidgetError
import android.net.Uri
import android.os.Looper
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebView
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowWebView
import java.time.Duration

/**
 * Test-only helpers for the phase a characterization tests.
 * Nothing here ships: it only reads sdk internals (private webView child,
 * static token cache) so the tests can pin today's behaviour.
 */

/** ShadowWebView only keeps the last evaluated script. This one keeps all of them. */
@Implements(WebView::class)
class RecordingShadowWebView : ShadowWebView() {
    val scripts = mutableListOf<String>()

    @Implementation
    override fun evaluateJavascript(script: String, callback: ValueCallback<String>?) {
        scripts += script
        super.evaluateJavascript(script, callback)
    }
}

fun LimechatWidgetView.webView(): WebView = getChildAt(0) as WebView

fun LimechatWidgetView.recorder(): RecordingShadowWebView = Shadow.extract(webView())

fun LimechatWidgetView.lastLoadedUrl(): String? = shadowOf(webView()).lastLoadedUrl

/** Calls window.LimechatAndroid.<method>(payload) the way the page does, then runs the main looper. */
fun LimechatWidgetView.postFromJs(payload: String, method: String = "postMessage") {
    val bridge = shadowOf(webView()).getJavascriptInterface("LimechatAndroid")
        ?: error("LimechatAndroid js interface not registered")
    val m = bridge.javaClass.getMethod(method, String::class.java)
    m.isAccessible = true
    m.invoke(bridge, payload)
    idleMain()
}

fun idleMain() = shadowOf(Looper.getMainLooper()).idle()

fun idleMainFor(ms: Long) = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ms))

/** Runs the main looper until [condition] is true. Needed when real IO threads post back to main. */
fun waitOnMain(timeoutMs: Long = 5000, condition: () -> Boolean) {
    val end = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < end) {
        idleMain()
        if (condition()) return
        Thread.sleep(10)
    }
    idleMain()
    if (!condition()) throw AssertionError("condition not met within ${timeoutMs}ms")
}

/** LimechatWidgetView keeps conversation tokens in a private static map that outlives a test. */
@Suppress("UNCHECKED_CAST")
fun tokenMemoryCache(): MutableMap<String, String> {
    val f = LimechatWidgetView::class.java.getDeclaredField("inMemoryTokenCache")
    f.isAccessible = true
    return f.get(null) as MutableMap<String, String>
}

class FakeRequest(
    private val uri: String,
    private val mainFrame: Boolean = true,
    private val gesture: Boolean = true
) : WebResourceRequest {
    override fun getUrl(): Uri = Uri.parse(uri)
    override fun isForMainFrame(): Boolean = mainFrame
    override fun isRedirect(): Boolean = false
    override fun hasGesture(): Boolean = gesture
    override fun getMethod(): String = "GET"
    override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()
}

class RecordingCallback(
    private val throwOn: String? = null
) : WidgetCallback {
    val events = mutableListOf<String>()
    val messages = mutableListOf<Map<String, Any?>>()
    val errors = mutableListOf<WidgetError>()
    val tokens = mutableListOf<String>()

    override fun onLoaded() = record("onLoaded")
    override fun onClose() = record("onClose")
    override fun onError(error: WidgetError) {
        errors += error
        events += "onError"
    }
    override fun onMessage(message: Map<String, Any?>) {
        messages += message
        record("onMessage")
    }
    override fun onConversationTokenChange(token: String) {
        tokens += token
        record("onConversationTokenChange")
    }

    private fun record(name: String) {
        events += name
        if (name == throwOn) throw IllegalStateException("boom from $name")
    }
}
