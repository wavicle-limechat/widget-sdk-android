package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.models.WidgetConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/**
 * Pins the bridge wrapper script and sendMessage. sendMessage posts `chatwoot-widget:` prefixed
 * json, the prefix the real page reads.
 */
@RunWith(RobolectricTestRunner::class)
class InjectedJsCharacterizationTest {

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
    }

    @Test
    fun `bridge wrapper script is evaluated before loadUrl and again on every onPageFinished`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        val bridgeScripts = { v.recorder().scripts.count { it.contains("window.__lcAndroidBridgeJSI") } }
        assertEquals(1, bridgeScripts())
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        assertEquals(2, bridgeScripts())
        assertTrue(v.recorder().scripts.first().contains("data.indexOf('limechat-widget:') === 0"))
    }

    @Test
    fun `sendMessage posts chatwoot-widget prefixed json the page reads`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        v.recorder().scripts.clear()
        v.sendMessage("custom-event", linkedMapOf("k" to "v", "n" to 1))
        assertEquals(
            listOf("window.postMessage('chatwoot-widget:' + JSON.stringify({\"event\":\"custom-event\",\"k\":\"v\",\"n\":1}), '*');"),
            v.recorder().scripts
        )
    }

    @Test
    fun `sendMessage keeps apostrophes inside the json`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        v.recorder().scripts.clear()
        v.sendMessage("x", mapOf("name" to "O'Brien"))
        val script = v.recorder().scripts.single()
        assertTrue(script.contains("JSON.stringify("))
        assertTrue(script.contains("\"name\":\"O'Brien\""))
    }

    @Test
    fun `sendMessage serializes nested maps as objects`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        v.recorder().scripts.clear()
        v.sendMessage("x", mapOf("user" to mapOf("email" to "a@b.c")))
        assertTrue(v.recorder().scripts.single().contains("\"user\":{\"email\":\"a@b.c\"}"))
    }
}
