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
 * Pins what LimechatWidgetView writes to logcat: the widget url without its query string,
 * and no tokens, payloads or js console lines.
 */
@RunWith(RobolectricTestRunner::class)
class LoggingCharacterizationTest {

    private val prod = "https://app.limechat.ai/widget"

    @Before
    fun setUp() {
        resetTokenMemory()
        ShadowLog.clear()
    }

    private fun debugLogs(): List<String> =
        ShadowLog.getLogsForTag("LimechatWidgetView").filter { it.type == Log.DEBUG }.map { it.msg }

    private fun assertLogged(line: String) =
        assertTrue("missing log line: $line\nactual:\n${debugLogs().joinToString("\n")}", debugLogs().contains(line))

    @Test
    fun `widget url is logged on load without its query string`() {
        tokenMemoryCache()["default"] = "secret-conversation-token-123"
        LimechatWidgetView(RuntimeEnvironment.getApplication()).init(WidgetConfig.builder("tok").build())
        assertLogged("Loading widget URL: $prod")
    }

    @Test
    fun `bridge messages never log the payload or token`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"secret-conversation-token-123"}""")
        assertTrue(ShadowLog.getLogs().none { it.msg.contains("secret-conversation-token-123") })
    }

    @Test
    fun `open with message logs only the url without its query`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build(), null, initialMessage = "my order 42")
        assertLogged("Loading widget URL: $prod")
        assertTrue(ShadowLog.getLogs().none { it.msg.contains("my+order+42") || it.msg.contains("my order 42") || it.msg.contains("tok") })
        v.open("refund please")
        assertLogged("Loading widget URL: $prod")
        v.open(linkedMapOf<String, Any>("content" to "hi"))
        assertLogged("Loading widget URL: $prod")
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
