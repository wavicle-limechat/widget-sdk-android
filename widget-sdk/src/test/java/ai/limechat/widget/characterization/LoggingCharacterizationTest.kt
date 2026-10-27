package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.models.WidgetConfig
import android.util.Log
import android.webkit.ConsoleMessage
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLog

/**
 * Pins what LimechatWidgetView writes to logcat today (develop @ 3456551), incl. the
 * full conversation token, raw bridge payloads, open-with-message text and js console lines.
 * Phase c trims these logs; these tests are expected to change then.
 */
@RunWith(RobolectricTestRunner::class)
class LoggingCharacterizationTest {

    private val prod = "https://app.limechat.ai/widget"
    private val q = "?website_token=tok&locale=en&color_scheme=light"

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
        ShadowLog.clear()
    }

    private fun debugLogs(): List<String> =
        ShadowLog.getLogsForTag("LimechatWidgetView").filter { it.type == Log.DEBUG }.map { it.msg }

    private fun assertLogged(line: String) =
        assertTrue("missing log line: $line\nactual:\n${debugLogs().joinToString("\n")}", debugLogs().contains(line))

    @Test
    fun `widget url is logged on load with cw_conversation redacted`() {
        tokenMemoryCache()["default"] = "secret-conversation-token-123"
        LimechatWidgetView(RuntimeEnvironment.getApplication()).init(WidgetConfig.builder("tok").build())
        assertLogged("Widget instance ID: default")
        assertLogged("Instance default - Loaded conversation token: found")
        assertLogged("Loading widget URL: $prod$q&cw_conversation=<redacted>")
    }

    @Test
    fun `bridge messages log only the event name, never the payload or token`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        val payload = """{"event":"set-cw-conversation","cw_conversation":"secret-conversation-token-123"}"""
        v.postFromJs(payload)
        assertLogged("JS->Android postMessage received")
        assertLogged("Widget event: set-cw-conversation")
        assertLogged("Conversation token updated")
        assertLogged("Instance default - Saved conversation token")
    }

    @Test
    fun `external token mode logs without the token`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").setExternalTokenManagement("external-token-xyz") {}.build())
        assertLogged("Using external conversation token")
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"another-token-abc"}""")
        assertLogged("External token management - calling onConversationTokenChange")
    }

    @Test
    fun `pins current behaviour - open with message and payload text is logged`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build(), null, initialMessage = "my order 42")
        assertLogged("Loading widget URL: $prod$q&lc_open_message=my+order+42")
        v.open("refund please")
        assertLogged("Loading widget URL: $prod$q&lc_open_message=refund+please")
        v.open(linkedMapOf<String, Any>("content" to "hi"))
        assertLogged("Loading widget URL: $prod$q&lc_open_payload=%7B%22content%22%3A%22hi%22%7D")
    }

    @Test
    fun `pins current behaviour - js console messages are mirrored to logcat`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        shadowOf(v.webView()).webChromeClient.onConsoleMessage(
            ConsoleMessage("user email is a@b.com", "x.js", 1, ConsoleMessage.MessageLevel.LOG)
        )
        assertLogged("JS Console: user email is a@b.com")
    }
}
