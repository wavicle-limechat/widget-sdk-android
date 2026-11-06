package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.utils.UrlBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Pins the exact widget url built today (develop @ 3456551).
 *
 * Runs under robolectric on purpose: the android runtime org.json keeps map insertion
 * order and escapes `/` as `\/`, the jvm org.json test dependency does not. The device
 * output is the android one, so that is what we pin.
 *
 * Encoding today: every key and value goes through java.net.URLEncoder once
 * (space -> `+`, `*` `-` `.` `_` unencoded, everything else %XX).
 * Param order today: website_token, locale, color_scheme, custom_attributes,
 * cw_conversation, then lc_open_message / lc_open_payload.
 */
@RunWith(RobolectricTestRunner::class)
class WidgetUrlCharacterizationTest {

    private val prod = "https://app.limechat.ai/widget"

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
    }

    private fun url(builder: WidgetConfig.Builder, cw: String? = null) =
        UrlBuilder.buildWidgetUrl(builder.build(), cw)

    // ---- UrlBuilder.buildWidgetUrl ----

    @Test
    fun `minimal config`() {
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light",
            url(WidgetConfig.builder("tok"))
        )
    }

    @Test
    fun `locale and color scheme dark and auto`() {
        assertEquals(
            "$prod?website_token=tok&locale=hi&color_scheme=dark",
            url(WidgetConfig.builder("tok").setLocale("hi").setColorScheme(WidgetConfig.ColorScheme.DARK))
        )
        assertEquals(
            "$prod?website_token=tok&locale=pt-BR&color_scheme=auto",
            url(WidgetConfig.builder("tok").setLocale("pt-BR").setColorScheme(WidgetConfig.ColorScheme.AUTO))
        )
    }

    @Test
    fun `website token with special chars and unicode is URLEncoder encoded`() {
        assertEquals(
            "$prod?website_token=a+b%2Bc%2F%C3%A9%26x%3Dy*%7E&locale=en&color_scheme=light",
            url(WidgetConfig.builder("a b+c/é&x=y*~"))
        )
    }

    @Test
    fun `custom attributes simple`() {
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light" +
                "&custom_attributes=%7B%22plan%22%3A%22pro%22%7D",
            url(WidgetConfig.builder("tok").setCustomAttributes(mapOf("plan" to "pro")))
        )
    }

    @Test
    fun `custom attributes keep insertion order and nest maps and lists`() {
        val attrs = linkedMapOf<String, Any>(
            "b" to 1,
            "a" to mapOf("x" to true),
            "list" to listOf(1, 2),
            "d" to 1.5
        )
        // {"b":1,"a":{"x":true},"list":[1,2],"d":1.5}
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light" +
                "&custom_attributes=%7B%22b%22%3A1%2C%22a%22%3A%7B%22x%22%3Atrue%7D%2C%22list%22%3A%5B1%2C2%5D%2C%22d%22%3A1.5%7D",
            url(WidgetConfig.builder("tok").setCustomAttributes(attrs))
        )
    }

    @Test
    fun `custom attributes with plus ampersand equals percent`() {
        // {"q":"a+b&c=d%e"}
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light" +
                "&custom_attributes=%7B%22q%22%3A%22a%2Bb%26c%3Dd%25e%22%7D",
            url(WidgetConfig.builder("tok").setCustomAttributes(mapOf("q" to "a+b&c=d%e")))
        )
    }

    @Test
    fun `pins current behaviour - custom attributes json escapes slash as backslash slash`() {
        // android org.json writes {"url":"https:\/\/x.com\/p"}
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light" +
                "&custom_attributes=%7B%22url%22%3A%22https%3A%5C%2F%5C%2Fx.com%5C%2Fp%22%7D",
            url(WidgetConfig.builder("tok").setCustomAttributes(mapOf("url" to "https://x.com/p")))
        )
    }

    @Test
    fun `custom attributes unicode and emoji are utf-8 percent encoded, not json-escaped`() {
        // {"name":"Zoë 😊"}
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light" +
                "&custom_attributes=%7B%22name%22%3A%22Zo%C3%AB+%F0%9F%98%8A%22%7D",
            url(WidgetConfig.builder("tok").setCustomAttributes(mapOf("name" to "Zoë 😊")))
        )
    }

    @Test
    fun `empty custom attributes map is omitted`() {
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light",
            url(WidgetConfig.builder("tok").setCustomAttributes(emptyMap()))
        )
    }

    @Test
    fun `cw_conversation from argument, blank argument omitted`() {
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light&cw_conversation=abc.def-1",
            url(WidgetConfig.builder("tok"), cw = "abc.def-1")
        )
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light",
            url(WidgetConfig.builder("tok"), cw = "  ")
        )
    }

    @Test
    fun `cw_conversation falls back to external config token, argument wins`() {
        val ext = WidgetConfig.builder("tok").setExternalTokenManagement("ext1") {}
        assertEquals("$prod?website_token=tok&locale=en&color_scheme=light&cw_conversation=ext1", url(ext))
        assertEquals("$prod?website_token=tok&locale=en&color_scheme=light&cw_conversation=arg", url(ext, cw = "arg"))
    }

    @Test
    fun `show_legacy_back_icon is not sent as a url param`() {
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light",
            url(WidgetConfig.builder("tok").setShowLegacyBackIcon(true))
        )
    }

    @Test
    fun `full config param order`() {
        val b = WidgetConfig.builder("tok")
            .setLocale("ar")
            .setColorScheme(WidgetConfig.ColorScheme.DARK)
            .setCustomAttributes(mapOf("k" to "v"))
            .setShowLegacyBackIcon(true)
            .setBaseUrl("https://staging.example.com")
        assertEquals(
            "https://staging.example.com/widget?website_token=tok&locale=ar&color_scheme=dark" +
                "&custom_attributes=%7B%22k%22%3A%22v%22%7D&cw_conversation=cw1",
            url(b, cw = "cw1")
        )
    }

    @Test
    fun `base url trailing slashes are all trimmed, path kept`() {
        val q = "?website_token=tok&locale=en&color_scheme=light"
        assertEquals("https://example.com/widget$q", url(WidgetConfig.builder("tok").setBaseUrl("https://example.com/")))
        assertEquals("https://example.com/widget$q", url(WidgetConfig.builder("tok").setBaseUrl("https://example.com//")))
        assertEquals("https://example.com/sub/widget$q", url(WidgetConfig.builder("tok").setBaseUrl("https://example.com/sub/")))
        assertEquals("http://10.0.2.2:8787/widget$q", url(WidgetConfig.builder("tok").setBaseUrl("http://10.0.2.2:8787")))
    }

    // ---- lc_open_message / lc_open_payload through the view (buildUrlWithParameters) ----

    private fun view() = LimechatWidgetView(RuntimeEnvironment.getApplication())

    @Test
    fun `pins current behaviour - lc_open_message is URLEncoder encoded once, web double-decodes and drops values with percent`() {
        // the web reads the param with URLSearchParams (decode #1) and then decodeURIComponent again
        // (decode #2). "50% off" survives decode #1 as "50% off", then decode #2 throws on "% o".
        val v = view()
        v.init(WidgetConfig.builder("tok").build(), null, initialMessage = "50% off + free")
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light&lc_open_message=50%25+off+%2B+free",
            v.lastLoadedUrl()
        )
    }

    @Test
    fun `lc_open_message unicode and quotes`() {
        val v = view()
        v.init(WidgetConfig.builder("tok").build(), null, initialMessage = "héllo \"you\" & 'me' 😊")
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light" +
                "&lc_open_message=h%C3%A9llo+%22you%22+%26+%27me%27+%F0%9F%98%8A",
            v.lastLoadedUrl()
        )
    }

    @Test
    fun `lc_open_payload is the android json of the map, encoded once`() {
        val v = view()
        val data = linkedMapOf<String, Any>(
            "content" to "hi 50%",
            "submitted_values" to linkedMapOf("k" to "v/w")
        )
        v.init(WidgetConfig.builder("tok").build(), null, initialMessageData = data)
        // {"content":"hi 50%","submitted_values":{"k":"v\/w"}}
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light" +
                "&lc_open_payload=%7B%22content%22%3A%22hi+50%25%22%2C%22submitted_values%22%3A%7B%22k%22%3A%22v%5C%2Fw%22%7D%7D",
            v.lastLoadedUrl()
        )
    }

    @Test
    fun `initial message is appended after cw_conversation`() {
        tokenMemoryCache()["default"] = "stored1"
        val v = view()
        v.init(WidgetConfig.builder("tok").setShowLegacyBackIcon(true).build(), null, initialMessage = "hello")
        assertEquals(
            "$prod?website_token=tok&locale=en&color_scheme=light&cw_conversation=stored1" +
                "&lc_open_message=hello",
            v.lastLoadedUrl()
        )
    }

    @Test
    fun `null initial message adds nothing`() {
        val v = view()
        v.init(WidgetConfig.builder("tok").build(), null, initialMessage = null)
        assertEquals("$prod?website_token=tok&locale=en&color_scheme=light", v.lastLoadedUrl())
    }
}
