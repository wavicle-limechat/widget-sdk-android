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
 * Pins the js evaluated by injectUserData (:392-451) 500ms after onPageFinished, and
 * sendMessage (:132-143), on develop @ 3456551.
 *
 * Everything here is posted as bare json (no `chatwoot-widget:` prefix), so the real page
 * drops it. User values are interpolated into "..." and locale into '...' with no escaping.
 */
@RunWith(RobolectricTestRunner::class)
class InjectedJsCharacterizationTest {

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
    }

    /** Loads, fires onPageFinished, returns only the scripts injectUserData evaluates after 500ms. */
    private fun injected(config: WidgetConfig): List<String> {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(config)
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), v.lastLoadedUrl()!!)
        v.recorder().scripts.clear()
        idleMainFor(499)
        assertEquals("nothing injected before 500ms", emptyList<String>(), v.recorder().scripts)
        idleMainFor(1)
        return v.recorder().scripts.toList()
    }

    private fun userScript(name: String, email: String, phone: String, hash: String) =
        "window.LimechatWidget = window.LimechatWidget || {};\n" +
            "window.LimechatWidget.setUser = function(user) {\n" +
            "    window.postMessage(JSON.stringify({\n" +
            "        event: 'set-user',\n" +
            "        user: user,\n" +
            "        identifier: user.identifier_hash\n" +
            "    }), '*');\n" +
            "};\n" +
            "window.LimechatWidget.setUser({\n" +
            "    name: \"$name\",\n" +
            "    email: \"$email\",\n" +
            "    phone_number: \"$phone\",\n" +
            "    identifier_hash: \"$hash\"\n" +
            "});"

    private fun localeScript(locale: String) =
        "window.postMessage(JSON.stringify({\n" +
            "    event: 'set-locale',\n" +
            "    locale: '$locale'\n" +
            "}), '*');"

    private fun colorScript(dark: Boolean) =
        "window.postMessage(JSON.stringify({\n" +
            "    event: 'set-color-scheme',\n" +
            "    darkMode: $dark\n" +
            "}), '*');"

    private fun attrsScript(json: String) =
        "window.postMessage(JSON.stringify({\n" +
            "    event: 'set-custom-attributes',\n" +
            "    customAttributes: $json\n" +
            "}), '*');"

    @Test
    fun `no user - locale and color scheme only`() {
        assertEquals(
            listOf(localeScript("en"), colorScript(false)),
            injected(WidgetConfig.builder("tok").build())
        )
    }

    @Test
    fun `full user, dark, custom attributes - order is user, locale, color, attributes`() {
        val config = WidgetConfig.builder("tok")
            .setUser(WidgetConfig.User("Jane Doe", "jane@example.com", "+911234567890", "hash123"))
            .setLocale("hi")
            .setColorScheme(WidgetConfig.ColorScheme.DARK)
            .setCustomAttributes(linkedMapOf("plan" to "pro", "url" to "https://x.com/p"))
            .build()
        assertEquals(
            listOf(
                userScript("Jane Doe", "jane@example.com", "+911234567890", "hash123"),
                localeScript("hi"),
                colorScript(true),
                attrsScript("""{"plan":"pro","url":"https:\/\/x.com\/p"}""")
            ),
            injected(config)
        )
    }

    @Test
    fun `AUTO color scheme injects darkMode false`() {
        assertEquals(
            listOf(localeScript("en"), colorScript(false)),
            injected(WidgetConfig.builder("tok").setColorScheme(WidgetConfig.ColorScheme.AUTO).build())
        )
    }

    @Test
    fun `user with all null fields injects empty strings`() {
        assertEquals(
            userScript("", "", "", ""),
            injected(WidgetConfig.builder("tok").setUser(WidgetConfig.User()).build()).first()
        )
    }

    @Test
    fun `apostrophe in user name is fine because user values use double quotes`() {
        assertEquals(
            userScript("O'Brien", "o'brien@example.com", "", ""),
            injected(WidgetConfig.builder("tok").setUser(WidgetConfig.User("O'Brien", "o'brien@example.com")).build()).first()
        )
    }

    @Test
    fun `pins current behaviour - double quote and backslash in user name are not escaped (invalid js)`() {
        val script = injected(WidgetConfig.builder("tok").setUser(WidgetConfig.User("Say \"hi\" \\o/")).build()).first()
        assertEquals(userScript("Say \"hi\" \\o/", "", "", ""), script)
        assertTrue(script.contains("name: \"Say \"hi\" \\o/\","))
    }

    @Test
    fun `pins current behaviour - apostrophe in locale is not escaped (invalid js)`() {
        assertEquals(
            localeScript("e'n"),
            injected(WidgetConfig.builder("tok").setLocale("e'n").build()).first()
        )
    }

    @Test
    fun `pins current behaviour - User customAttributes are never injected`() {
        val scripts = injected(
            WidgetConfig.builder("tok")
                .setUser(WidgetConfig.User(name = "A", customAttributes = mapOf("vip" to true)))
                .build()
        )
        assertEquals(2 + 1, scripts.size)
        assertTrue(scripts.none { it.contains("vip") })
    }

    @Test
    fun `injection runs again on every onPageFinished`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        val client = shadowOf(v.webView()).webViewClient
        client.onPageFinished(v.webView(), "x")
        client.onPageFinished(v.webView(), "x")
        v.recorder().scripts.clear()
        idleMainFor(500)
        assertEquals(listOf(localeScript("en"), colorScript(false), localeScript("en"), colorScript(false)), v.recorder().scripts)
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
    fun `pins current behaviour - sendMessage posts limechat-widget prefixed json the page ignores`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build())
        v.recorder().scripts.clear()
        v.sendMessage("custom-event", linkedMapOf("k" to "v", "n" to 1))
        assertEquals(
            listOf("window.postMessage('limechat-widget:{\"event\":\"custom-event\",\"k\":\"v\",\"n\":1}', '*');"),
            v.recorder().scripts
        )
    }
}
