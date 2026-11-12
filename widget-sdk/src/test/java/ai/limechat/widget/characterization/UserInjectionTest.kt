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
 * Pins how WidgetConfig.User reaches the widget: through `window.chatwootWebChannel.rnUser`
 * after every page load. Nothing about the user goes into the url.
 */
@RunWith(RobolectricTestRunner::class)
class UserInjectionTest {

    @Before
    fun setUp() {
        resetTokenMemory()
    }

    private fun LimechatWidgetView.userScripts() = recorder().scripts.filter { it.contains("rnUser") }

    private fun loaded(config: WidgetConfig): LimechatWidgetView {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(config)
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        return v
    }

    @Test
    fun `user is injected into rnUser after the page finishes`() {
        val v = loaded(
            WidgetConfig.builder("tok")
                .setUser(WidgetConfig.User(name = "O'Brien", email = "a@b.c", phoneNumber = "+1"))
                .build()
        )
        val script = v.userScripts().single()
        assertTrue(script.contains("window.chatwootWebChannel.rnUser = "))
        assertTrue(script.contains("\"name\":\"O'Brien\""))
        assertTrue(script.contains("\"email\":\"a@b.c\""))
        assertTrue(script.contains("\"phone_number\":\"+1\""))
    }

    @Test
    fun `no user gives no rnUser script`() {
        assertEquals(emptyList<String>(), loaded(WidgetConfig.builder("tok").build()).userScripts())
    }

    @Test
    fun `a user with all nulls gives no rnUser script`() {
        val v = loaded(WidgetConfig.builder("tok").setUser(WidgetConfig.User()).build())
        assertEquals(emptyList<String>(), v.userScripts())
    }

    @Test
    fun `a second page finish injects again`() {
        val v = loaded(WidgetConfig.builder("tok").setUser(WidgetConfig.User(name = "A")).build())
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        assertEquals(2, v.userScripts().size)
    }

    private fun LimechatWidgetView.setUserScripts() = recorder().scripts.filter { it.contains("'chatwoot-widget:'") && it.contains("\"set-user\"") }

    @Test
    fun `identifier and hash together send one set-user`() {
        val v = loaded(
            WidgetConfig.builder("tok")
                .setUser(WidgetConfig.User(name = "N", identifier = "id-1", identifierHash = "h"))
                .build()
        )
        v.widgetReady()
        val script = v.setUserScripts().single()
        assertTrue(script.contains("\"identifier\":\"id-1\""))
        assertTrue(script.contains("\"identifier_hash\":\"h\""))
    }

    @Test
    fun `hash without identifier sends no set-user`() {
        val v = loaded(WidgetConfig.builder("tok").setUser(WidgetConfig.User(identifierHash = "h")).build())
        assertEquals(emptyList<String>(), v.setUserScripts())
    }

    @Test
    fun `identifier without hash sends no set-user`() {
        val v = loaded(WidgetConfig.builder("tok").setUser(WidgetConfig.User(identifier = "id-1")).build())
        assertEquals(emptyList<String>(), v.setUserScripts())
    }

    private fun userConfig() = WidgetConfig.builder("tok").setUser(WidgetConfig.User(name = "N")).build()

    private fun LimechatWidgetView.openScripts() = recorder().scripts.filter { it.contains("\"open-with-message\"") }

    @Test
    fun `with a user, open(message) sends the message after rnUser and keeps it out of the url`() {
        val v = loaded(userConfig())
        v.open("hi")
        assertTrue(!v.lastLoadedUrl()!!.contains("lc_open_"))
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "x", null)
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        v.widgetReady()
        val scripts = v.recorder().scripts
        val rn = scripts.indexOfLast { it.contains("rnUser") }
        val open = scripts.indexOfFirst { it.contains("\"open-with-message\"") }
        assertTrue(open > rn && rn >= 0)
        assertTrue(scripts[open].contains("\"openMessage\":\"hi\""))
    }

    @Test
    fun `with a user, init with initial message and open(messageData) wait for rnUser`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(userConfig(), null, "first")
        assertTrue(!v.lastLoadedUrl()!!.contains("lc_open_"))
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "x", null)
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        v.widgetReady()
        assertTrue(v.openScripts().single().contains("first"))
        v.open(mapOf("content" to "data"))
        assertTrue(!v.lastLoadedUrl()!!.contains("lc_open_"))
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "x", null)
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        v.widgetReady()
        assertTrue(v.openScripts().last().contains("\"content\":\"data\""))
        assertEquals(2, v.openScripts().size)
    }

    @Test
    fun `with no user, open(message) holds the message and keeps it out of the url`() {
        val v = loaded(WidgetConfig.builder("tok").build())
        v.open("hi")
        assertTrue(!v.lastLoadedUrl()!!.contains("lc_open_"))
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        assertEquals(emptyList<String>(), v.openScripts())
        v.widgetReady()
        assertTrue(v.openScripts().single().contains("\"openMessage\":\"hi\""))
    }

    @Test
    fun `re-init drops a held open message`() {
        val v = loaded(userConfig())
        v.open("from A")
        v.init(WidgetConfig.builder("tok2").setUser(WidgetConfig.User(name = "B")).build())
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        assertEquals(emptyList<String>(), v.openScripts())
    }

    @Test
    fun `re-init with no user drops a held open message`() {
        val v = loaded(userConfig())
        v.open("from A")
        v.init(WidgetConfig.builder("tok2").build())
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        assertEquals(emptyList<String>(), v.openScripts())
    }

    @Test
    fun `a held open message waits for the new page to start`() {
        val v = loaded(userConfig())
        v.open("hi")
        // the old page finishing must not take the message
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "old")
        assertEquals(emptyList<String>(), v.openScripts())
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "new", null)
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "new")
        v.widgetReady()
        assertTrue(v.openScripts().single().contains("\"openMessage\":\"hi\""))
    }

    @Test
    fun `a fragment onPageFinished with no onPageStarted does not send the held message`() {
        val v = loaded(userConfig())
        v.open("hi")
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "x", null)
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x#/")
        assertEquals(emptyList<String>(), v.openScripts())
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "x")
        assertEquals(emptyList<String>(), v.openScripts())
    }

    @Test
    fun `set-cw-conversation sends the held message once, after rnUser`() {
        val v = loaded(userConfig())
        v.open("hi")
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "x", null)
        v.widgetReady()
        val scripts = v.recorder().scripts
        val open = scripts.indexOfFirst { it.contains("\"open-with-message\"") }
        assertTrue(open > scripts.indexOfLast { it.contains("rnUser") })
        assertTrue(scripts[open].contains("\"openMessage\":\"hi\""))
        assertEquals(1, v.openScripts().size)
    }

    @Test
    fun `a second set-cw-conversation does not resend the held message`() {
        val v = loaded(userConfig())
        v.open("hi")
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "x", null)
        v.widgetReady()
        v.widgetReady()
        assertEquals(1, v.openScripts().size)
    }

    @Test
    fun `set-cw-conversation from the old page does not take a message held for the next load`() {
        val v = loaded(userConfig())
        v.open("hi")
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"t"}""", currentPage = false)
        assertEquals(emptyList<String>(), v.openScripts())
    }

    @Test
    fun `identifier and hash send set-user once at ready and not on page finish`() {
        val v = loaded(
            WidgetConfig.builder("tok")
                .setUser(WidgetConfig.User(name = "N", identifier = "id-1", identifierHash = "h"))
                .build()
        )
        assertEquals(emptyList<String>(), v.setUserScripts())
        v.widgetReady()
        v.postFromJs("""{"event":"set-cw-conversation"}""")
        assertEquals(1, v.setUserScripts().size)
    }

    @Test
    fun `a new page sends set-user again`() {
        val v = loaded(
            WidgetConfig.builder("tok")
                .setUser(WidgetConfig.User(name = "N", identifier = "id-1", identifierHash = "h"))
                .build()
        )
        v.widgetReady()
        v.widgetReady()
        assertEquals(2, v.setUserScripts().size)
    }

    private fun LimechatWidgetView.openMessage(): org.json.JSONObject {
        val script = recorder().scripts.single { it.contains("open-with-message") }
        val json = script.substringAfter("JSON.stringify(").substringBefore("), '*')")
        return org.json.JSONObject(json).getJSONObject("openMessage")
    }

    private fun readyView(): LimechatWidgetView {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        v.widgetReady()
        return v
    }

    @Test
    fun `open with submitted_values and no content sends Hi`() {
        val v = readyView()
        v.open(mapOf("submitted_values" to listOf("a")))
        val m = v.openMessage()
        assertEquals("Hi", m.getString("content"))
        assertTrue(m.has("submitted_values"))
    }

    @Test
    fun `open with empty content and other keys sends Hi`() {
        val v = readyView()
        v.open(mapOf("content" to "", "n" to 1))
        assertEquals("Hi", v.openMessage().getString("content"))
    }

    @Test
    fun `open with existing content keeps it`() {
        val v = readyView()
        v.open(mapOf("content" to "yo", "n" to 1))
        assertEquals("yo", v.openMessage().getString("content"))
    }

    @Test
    fun `open with only empty content is unchanged`() {
        val v = readyView()
        v.open(mapOf("content" to ""))
        assertEquals("", v.openMessage().getString("content"))
    }

    @Test
    fun `init initialMessageData without content sends Hi at ready`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build(), null, mapOf<String, Any>("submitted_values" to "x"))
        v.widgetReady()
        assertEquals("Hi", v.openMessage().getString("content"))
    }
}
