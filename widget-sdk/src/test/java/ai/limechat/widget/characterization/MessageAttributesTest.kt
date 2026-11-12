package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.models.WidgetConfig
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class MessageAttributesTest {

    @Before
    fun setUp() {
        resetTokenMemory()
    }

    private fun config(tok: String = "tok") = WidgetConfig.builder(tok).build()

    private fun view(): LimechatWidgetView =
        LimechatWidgetView(RuntimeEnvironment.getApplication()).also { it.init(config()) }

    private fun LimechatWidgetView.attrScripts() =
        recorder().scripts.filter { it.contains("\"set-message-attributes\"") }

    private fun String.json(): JSONObject =
        JSONObject(substringAfter("JSON.stringify(").substringBefore("), '*')"))

    @Test
    fun `returns false for null, empty and NaN, and sends nothing`() {
        val v = view()
        v.widgetReady()
        assertFalse(v.setMessageAttributes(null))
        assertFalse(v.setMessageAttributes(emptyMap()))
        assertFalse(v.setMessageAttributes(mapOf("a" to Double.NaN)))
        assertTrue(v.attrScripts().isEmpty())
    }

    @Test
    fun `returns false for a Date and an unknown object, also nested`() {
        val v = view()
        v.widgetReady()
        assertFalse(v.setMessageAttributes(mapOf("d" to java.util.Date())))
        assertFalse(v.setMessageAttributes(mapOf("o" to Any())))
        assertFalse(v.setMessageAttributes(mapOf("n" to mapOf("d" to java.util.Date()))))
        assertFalse(v.setMessageAttributes(mapOf("l" to listOf(1, Any()))))
        assertTrue(v.attrScripts().isEmpty())
    }

    @Test
    fun `before ready it sends nothing`() {
        val v = view()
        assertTrue(v.setMessageAttributes(mapOf("a" to 1)))
        assertTrue(v.attrScripts().isEmpty())
    }

    @Test
    fun `at ready it sends the merged buffer once, later keys win`() {
        val v = view()
        v.setMessageAttributes(mapOf("a" to 1, "b" to 2))
        v.setMessageAttributes(mapOf("b" to 3))
        v.widgetReady()
        v.postFromJs("""{"event":"set-cw-conversation"}""")
        val attrs = v.attrScripts().single().json().getJSONObject("attributes")
        assertEquals(1, attrs.getInt("a"))
        assertEquals(3, attrs.getInt("b"))
    }

    @Test
    fun `attributes go before a held open message`() {
        val v = view()
        v.open("hi")
        v.setMessageAttributes(mapOf("a" to 1))
        v.widgetReady()
        val scripts = v.recorder().scripts
        val attr = scripts.indexOfFirst { it.contains("\"set-message-attributes\"") }
        val open = scripts.indexOfFirst { it.contains("\"open-with-message\"") }
        assertTrue(attr >= 0 && open > attr)
    }

    @Test
    fun `attributes go after set-user and before open`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").setUser(WidgetConfig.User(name = "N", identifier = "i", identifierHash = "h")).build())
        v.open("hi")
        v.setMessageAttributes(mapOf("a" to 1))
        v.widgetReady()
        val scripts = v.recorder().scripts
        val user = scripts.indexOfFirst { it.contains("\"set-user\"") }
        val attr = scripts.indexOfFirst { it.contains("\"set-message-attributes\"") }
        val open = scripts.indexOfFirst { it.contains("\"open-with-message\"") }
        assertTrue(user in 0 until attr && attr < open)
    }

    @Test
    fun `after ready it sends at once`() {
        val v = view()
        v.widgetReady()
        assertTrue(v.setMessageAttributes(mapOf("a" to 1)))
        assertEquals(1, v.attrScripts().size)
    }

    @Test
    fun `after a flush a new page does not resend`() {
        val v = view()
        v.setMessageAttributes(mapOf("a" to 1))
        v.widgetReady()
        v.widgetReady()
        assertEquals(1, v.attrScripts().size)
    }

    @Test
    fun `unflushed attributes survive a reload`() {
        val v = view()
        v.setMessageAttributes(mapOf("a" to 1))
        v.open("hi")
        assertTrue(v.attrScripts().isEmpty())
        v.widgetReady()
        assertEquals(1, v.attrScripts().size)
    }

    @Test
    fun `re-init clears unflushed attributes`() {
        val v = view()
        v.setMessageAttributes(mapOf("a" to 1))
        v.init(config("tok2"))
        v.widgetReady()
        assertTrue(v.attrScripts().isEmpty())
    }

    @Test
    fun `attributes set before the first init survive it`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        assertTrue(v.setMessageAttributes(mapOf("a" to 1)))
        v.init(config())
        v.widgetReady()
        assertEquals(1, v.attrScripts().size)
    }

    @Test
    fun `apostrophe, quote, backslash, newline and closing script tag stay in valid json`() {
        val v = view()
        v.widgetReady()
        val text = "it's \"q\" \\ line\nnext </script>"
        v.setMessageAttributes(mapOf("k" to text))
        assertEquals(text, v.attrScripts().single().json().getJSONObject("attributes").getString("k"))
    }

    @Test
    fun `nested maps and null values serialise as json`() {
        val v = view()
        v.widgetReady()
        v.setMessageAttributes(mapOf("n" to mapOf("x" to 1), "z" to null))
        val attrs = v.attrScripts().single().json().getJSONObject("attributes")
        assertEquals(1, attrs.getJSONObject("n").getInt("x"))
        assertTrue(attrs.has("z"))
        assertTrue(attrs.isNull("z"))
    }

    @Test
    fun `button forwards to the widget view`() {
        val v = view()
        v.widgetReady()
        val b = LimechatWidgetButton(RuntimeEnvironment.getApplication())
        b.setWidgetView(v)
        assertTrue(b.setMessageAttributes(mapOf("a" to 1)))
        assertEquals(1, v.attrScripts().size)
    }

    @Test
    fun `button returns false with no widget view`() {
        val b = LimechatWidgetButton(RuntimeEnvironment.getApplication())
        assertFalse(b.setMessageAttributes(mapOf("a" to 1)))
    }
}
