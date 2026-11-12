package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.WidgetError
import ai.limechat.widget.models.WidgetConfig
import android.app.Activity
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/**
 * Pins what happens when the page calls window.LimechatAndroid.postMessage(payload)
 * on develop @ 3456551 (LimechatWidgetView.handleMessage, :611-642).
 *
 * Dispatch today: MessageHandler parses a json object (a prefix makes it unparseable), then switches
 * on `event ?: type` (only if they are strings):
 *   close-widget and widget-back -> onClose, set-cw-conversation -> token update,
 *   anything else (incl. loaded) -> onMessage(map).
 * Unparseable payloads are dropped silently. Exceptions thrown by the host callback
 * are caught and reported as onError(JAVASCRIPT_ERROR).
 */
@RunWith(RobolectricTestRunner::class)
class BridgeCharacterizationTest {

    private lateinit var cb: RecordingCallback
    private lateinit var view: LimechatWidgetView

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
        cb = RecordingCallback()
        view = newView(RuntimeEnvironment.getApplication(), WidgetConfig.builder("tok").build(), cb)
    }

    private fun newView(ctx: Context, config: WidgetConfig, callback: RecordingCallback): LimechatWidgetView =
        LimechatWidgetView(ctx).also { it.init(config, callback) }

    @Test
    fun `js interface is registered as LimechatAndroid before the first loadUrl`() {
        assertNotNull(shadowOf(view.webView()).getJavascriptInterface("LimechatAndroid"))
    }

    @Test
    fun `close-widget calls onClose`() {
        view.postFromJs("""{"event":"close-widget","type":"close-widget"}""")
        assertEquals(listOf("onClose"), cb.events)
    }

    @Test
    fun `close-widget with type only calls onClose`() {
        view.postFromJs("""{"type":"close-widget"}""")
        assertEquals(listOf("onClose"), cb.events)
    }

    @Test
    fun `limechat-widget prefixed payload is dropped silently`() {
        view.postFromJs("""limechat-widget:{"event":"close-widget"}""")
        assertEquals(emptyList<String>(), cb.events)
    }

    @Test
    fun `chatwoot-widget prefixed payload is dropped silently`() {
        view.postFromJs("""chatwoot-widget:{"event":"close-widget"}""")
        assertEquals(emptyList<String>(), cb.events)
    }

    @Test
    fun `loaded is an unknown event and goes to onMessage`() {
        view.postFromJs("""{"event":"loaded"}""")
        assertEquals(listOf("onMessage"), cb.events)
    }

    @Test
    fun `widget-back calls onClose, not onMessage`() {
        view.postFromJs("""{"event":"widget-back"}""")
        assertEquals(listOf("onClose"), cb.events)
    }

    @Test
    fun `set-cw-conversation calls onConversationTokenChange only, not onMessage`() {
        view.postFromJs("""{"event":"set-cw-conversation","type":"set-cw-conversation","cw_conversation":"tokA"}""")
        assertEquals(listOf("onConversationTokenChange"), cb.events)
        assertEquals(listOf("tokA"), cb.tokens)
    }

    @Test
    fun `unknown event goes to onMessage with the full parsed map`() {
        view.postFromJs("""{"event":"set-unread-count","count":3,"meta":{"a":null}}""")
        assertEquals(listOf("onMessage"), cb.events)
        assertEquals(
            mapOf("event" to "set-unread-count", "count" to 3, "meta" to mapOf("a" to null)),
            cb.messages.single()
        )
    }

    @Test
    fun `close and unread_count are unknown on android and go to onMessage`() {
        view.postFromJs("""{"event":"close"}""")
        view.postFromJs("""{"event":"unread_count","count":1}""")
        assertEquals(listOf("onMessage", "onMessage"), cb.events)
    }

    @Test
    fun `object without event or type goes to onMessage`() {
        view.postFromJs("""{"foo":"bar"}""")
        assertEquals(listOf("onMessage"), cb.events)
        assertEquals(mapOf("foo" to "bar"), cb.messages.single())
    }

    @Test
    fun `event wins over type`() {
        view.postFromJs("""{"event":"something","type":"close-widget"}""")
        assertEquals(listOf("onMessage"), cb.events)
    }

    @Test
    fun `non-string event falls through to type`() {
        view.postFromJs("""{"event":5,"type":"close-widget"}""")
        assertEquals(listOf("onClose"), cb.events)
    }

    @Test
    fun `malformed, empty, array and null payloads are dropped silently`() {
        listOf("not json", "", "[1,2]", "null", "limechat-widget:", "{\"event\":").forEach {
            view.postFromJs(it)
        }
        assertEquals(emptyList<String>(), cb.events)
    }

    @Test
    fun `only postMessage is exposed on the js interface`() {
        val bridge = shadowOf(view.webView()).getJavascriptInterface("LimechatAndroid")!!
        val annotated = bridge.javaClass.methods
            .filter { it.isAnnotationPresent(android.webkit.JavascriptInterface::class.java) }
            .map { it.name }
        assertEquals(listOf("postMessage"), annotated)
    }

    @Test
    fun `dispatch is async - nothing fires until the main looper runs`() {
        val bridge = shadowOf(view.webView()).getJavascriptInterface("LimechatAndroid")
        val m = bridge.javaClass.getMethod("postMessage", String::class.java)
        m.isAccessible = true
        m.invoke(bridge, """{"event":"close-widget"}""")
        assertEquals(emptyList<String>(), cb.events)
        idleMain()
        assertEquals(listOf("onClose"), cb.events)
    }

    @Test
    fun `pins current behaviour - exception thrown by host onClose is reported back as onError JAVASCRIPT_ERROR`() {
        val throwing = RecordingCallback(throwOn = "onClose")
        val v = newView(RuntimeEnvironment.getApplication(), WidgetConfig.builder("tok").build(), throwing)
        v.postFromJs("""{"event":"close-widget"}""")
        assertEquals(listOf("onClose", "onError"), throwing.events)
        val err = throwing.errors.single()
        assertEquals(WidgetError.ErrorCode.JAVASCRIPT_ERROR, err.code)
        assertEquals("Failed to process message: boom from onClose", err.message)
    }

    @Test
    fun `no callback - messages are processed without crashing`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        v.postFromJs("""{"event":"close-widget"}""")
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"t"}""")
        assertEquals("t", tokenMemoryCache()["default"])
    }

    @Test
    fun `before init the js interface is not registered yet`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        // createHardenedWebView does not add the bridge; setupMessaging runs in init/loadWidget
        assertEquals(null, shadowOf(v.webView()).getJavascriptInterface("LimechatAndroid"))
    }

    // ---- close: develop behaviour + ported v0.0.9-alpha legacy close (plan b0) ----

    @Test
    fun `limechat close url is blocked and does not close`() {
        val client = shadowOf(view.webView()).webViewClient
        assertTrue(client.shouldOverrideUrlLoading(view.webView(), FakeRequest("limechat://close")))
        assertEquals(emptyList<String>(), cb.events)
    }

    @Test
    fun `with setShowLegacyBackIcon(true) and an activity context, close calls onClose and does not finish the activity`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val acb = RecordingCallback()
        val v = newView(activity, WidgetConfig.builder("tok").setShowLegacyBackIcon(true).build(), acb)

        v.postFromJs("""{"event":"close-widget"}""")

        assertEquals(listOf("onClose"), acb.events)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun `with legacy back icon and no callback, close finishes the activity`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val v = LimechatWidgetView(activity)
        v.init(WidgetConfig.builder("tok").setShowLegacyBackIcon(true).build())
        v.postFromJs("""{"event":"close-widget"}""")
        assertTrue(activity.isFinishing)
    }
}
