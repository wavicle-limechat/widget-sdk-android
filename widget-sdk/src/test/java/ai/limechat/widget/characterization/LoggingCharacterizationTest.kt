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
 * Pins what LimechatWidgetView writes to logcat: the redacted widget url, and no tokens,
 * payloads or js console lines.
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
        assertLogged("Loading widget URL: $prod$q&cw_conversation=<redacted>")
    }

    @Test
    fun `bridge messages never log the payload or token`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"secret-conversation-token-123"}""")
        assertTrue(ShadowLog.getLogs().none { it.msg.contains("secret-conversation-token-123") })
    }

    @Test
    fun `open with message logs only the redacted url`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build(), null, initialMessage = "my order 42")
        assertLogged("Loading widget URL: $prod$q&lc_open_message=my+order+42")
        v.open("refund please")
        assertLogged("Loading widget URL: $prod$q&lc_open_message=refund+please")
        v.open(linkedMapOf<String, Any>("content" to "hi"))
        assertLogged("Loading widget URL: $prod$q&lc_open_payload=%7B%22content%22%3A%22hi%22%7D")
    }

    @Test
    fun `js console messages are not mirrored to logcat`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        shadowOf(v.webView()).webChromeClient.onConsoleMessage(
            ConsoleMessage("user email is a@b.com", "x.js", 1, ConsoleMessage.MessageLevel.LOG)
        )
        assertTrue(ShadowLog.getLogs().none { it.msg.contains("a@b.com") })
    }
}
