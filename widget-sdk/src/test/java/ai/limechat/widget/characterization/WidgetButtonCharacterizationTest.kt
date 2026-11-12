package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.utils.WidgetConfigFetchException
import ai.limechat.widget.utils.WidgetConfigFetcher
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.Base64
import java.util.Collections

/**
 * Pins LimechatWidgetButton + WidgetConfigFetcher on develop @ 3456551:
 * GET {baseUrl}/widget_config?website_token=..., icon = widgetIconMobile ?: widgetIconDesktop,
 * fallback drawable looked up by name `ic_limechat_default`, unread badge setter.
 * Uses a local ServerSocket-based server (TinyHttpServer), no extra dependency.
 */
@RunWith(RobolectricTestRunner::class)
class WidgetButtonCharacterizationTest {

    data class Req(val path: String, val query: String?, val accept: String?, val userAgent: String?)

    private lateinit var server: TinyHttpServer
    private val requests: MutableList<Req> = Collections.synchronizedList(mutableListOf())
    private var configStatus = 200
    private var configBody = "{}"
    private var iconStatus = 200
    private lateinit var base: String

    private val png: ByteArray = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
    )

    @Before
    fun setUp() {
        server = TinyHttpServer { r ->
            requests += Req(r.path, r.query, r.headers["accept"], r.headers["user-agent"])
            when {
                r.path.endsWith("widget_config") -> TinyHttpServer.Response(configStatus, configBody.toByteArray())
                r.path.endsWith(".png") -> TinyHttpServer.Response(iconStatus, if (iconStatus == 200) png else "nope".toByteArray(), "image/png")
                else -> TinyHttpServer.Response(404, "not found".toByteArray(), "text/plain")
            }
        }
        base = "http://127.0.0.1:${server.port}"
    }

    @After
    fun tearDown() {
        server.stop()
    }

    /** config is a json string inside json, like the real /widget_config. */
    private fun configWith(vararg fields: Pair<String, String>): String {
        val inner = org.json.JSONObject(fields.toMap()).toString()
        return org.json.JSONObject(mapOf("config" to inner)).toString()
    }

    private fun button(token: String = "tok", baseUrl: String = base): LimechatWidgetButton {
        val b = LimechatWidgetButton(RuntimeEnvironment.getApplication())
        b.init(WidgetConfig.builder(token).setBaseUrl(baseUrl).build())
        return b
    }

    private fun LimechatWidgetButton.imageButton(): AppCompatImageButton? =
        (0 until childCount).map { getChildAt(it) }.filterIsInstance<AppCompatImageButton>().firstOrNull()

    private fun LimechatWidgetButton.badge(): TextView? =
        (0 until childCount).map { getChildAt(it) }.filterIsInstance<TextView>().firstOrNull()

    @Test
    fun `pins current behaviour - SDK_VERSION constant matches gradle version 0_2_0-beta_1`() {
        assertEquals("0.2.0-beta.1", LimechatWidgetButton.SDK_VERSION)
        assertEquals("0.2.0-beta.1", LimechatWidgetButton.getSDKVersion())
    }

    @Test
    fun `fetches widget_config with encoded token, accept and user agent, then loads the mobile icon`() {
        configBody = configWith("widgetIconMobile" to "$base/mobile.png", "widgetIconDesktop" to "$base/desktop.png")
        val b = button(token = "to k+1")
        waitOnMain { b.imageButton()?.drawable is BitmapDrawable }

        val cfg = requests.first()
        assertEquals("/widget_config", cfg.path)
        assertEquals("website_token=to+k%2B1", cfg.query)
        assertEquals("application/json", cfg.accept)
        assertEquals("LimechatWidget/Android", cfg.userAgent)
        assertEquals(listOf("/widget_config", "/mobile.png"), requests.map { it.path })
    }

    @Test
    fun `desktop icon is used when mobile icon is missing`() {
        configBody = configWith("widgetIconDesktop" to "$base/desktop.png")
        val b = button()
        waitOnMain { b.imageButton()?.drawable is BitmapDrawable }
        assertEquals(listOf("/widget_config", "/desktop.png"), requests.map { it.path })
    }

    @Test
    fun `config as a json object (not a string) is also read`() {
        configBody = """{"config":{"widgetIconMobile":"$base/obj.png"}}"""
        val b = button()
        waitOnMain { b.imageButton()?.drawable is BitmapDrawable }
        assertEquals(listOf("/widget_config", "/obj.png"), requests.map { it.path })
    }

    private fun assertDefaultIcon(b: LimechatWidgetButton) {
        waitOnMain { b.imageButton()?.drawable != null }
        // ic_limechat_default is a <vector>; the "LC" painted fallback would be a BitmapDrawable
        assertTrue(
            "expected ic_limechat_default, got ${b.imageButton()?.drawable}",
            b.imageButton()!!.drawable is VectorDrawable
        )
    }

    @Test
    fun `no icon fields - falls back to drawable named ic_limechat_default`() {
        configBody = configWith("welcomeTitle" to "hi")
        val b = button()
        assertDefaultIcon(b)
        assertEquals(listOf("/widget_config"), requests.map { it.path })
    }

    @Test
    fun `widget_config 500 - falls back to ic_limechat_default`() {
        configStatus = 500
        assertDefaultIcon(button())
    }

    @Test
    fun `icon url 404 - falls back to ic_limechat_default`() {
        iconStatus = 404
        configBody = configWith("widgetIconMobile" to "$base/missing.png")
        val b = button()
        waitOnMain { requests.any { it.path == "/missing.png" } }
        assertDefaultIcon(b)
    }

    @Test
    fun `pins current behaviour - baseUrl trailing slash gives a double slash widget_config path`() {
        configBody = configWith()
        val b = button(baseUrl = "$base/")
        assertDefaultIcon(b)
        assertEquals("//widget_config", requests.first().path)
    }

    @Test
    fun `default button is 56dp and forwards clicks to the host listener`() {
        configBody = configWith()
        val b = button()
        var clicks = 0
        b.setOnClickListener { clicks++ }
        assertDefaultIcon(b)
        val ib = b.imageButton()!!
        val px = (56 * RuntimeEnvironment.getApplication().resources.displayMetrics.density).toInt()
        assertEquals(px, ib.layoutParams.width)
        assertEquals(px, ib.layoutParams.height)
        ib.performClick()
        assertEquals(1, clicks)
    }

    // ---- custom button + unread badge ----

    @Test
    fun `custom button skips widget_config and forwards clicks`() {
        val b = LimechatWidgetButton(RuntimeEnvironment.getApplication())
        val custom = View(RuntimeEnvironment.getApplication())
        var clicks = 0
        b.setOnClickListener { clicks++ }
        b.init(WidgetConfig.builder("tok").setBaseUrl(base).build(), custom)
        custom.performClick()
        assertEquals(1, clicks)
        assertEquals(custom, b.getChildAt(0))
        Thread.sleep(100)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun `unread badge - hidden at 0 and negative, number up to 99, 99+ above`() {
        val b = LimechatWidgetButton(RuntimeEnvironment.getApplication())
        b.init(WidgetConfig.builder("tok").build(), View(RuntimeEnvironment.getApplication()))
        val badge = b.badge()!!
        assertEquals(View.GONE, badge.visibility)
        b.updateUnreadCount(5)
        assertEquals(View.VISIBLE, badge.visibility)
        assertEquals("5", badge.text.toString())
        b.updateUnreadCount(99)
        assertEquals("99", badge.text.toString())
        b.updateUnreadCount(100)
        assertEquals("99+", badge.text.toString())
        b.updateUnreadCount(0)
        assertEquals(View.GONE, badge.visibility)
        b.updateUnreadCount(-3)
        assertEquals(View.GONE, badge.visibility)
    }

    @Test
    fun `unread count set before init shows once the badge exists`() {
        val b = LimechatWidgetButton(RuntimeEnvironment.getApplication())
        b.updateUnreadCount(7)
        assertNull(b.badge())
        b.init(WidgetConfig.builder("tok").build(), View(RuntimeEnvironment.getApplication()))
        assertEquals(View.VISIBLE, b.badge()!!.visibility)
        assertEquals("7", b.badge()!!.text.toString())
    }

    @Test
    fun `open without setWidgetView is a logged no-op`() {
        val b = LimechatWidgetButton(RuntimeEnvironment.getApplication())
        b.open("hi")
        b.open(mapOf("content" to "hi"))
    }

    // ---- WidgetConfigFetcher directly ----

    @Test
    fun `fetcher parses config string`() = runBlocking {
        configBody = configWith("widgetIconMobile" to "m.png", "welcomeTitle" to "Hello")
        val r = WidgetConfigFetcher().fetchConfig(base, "tok")
        assertEquals("m.png", r.config!!.widgetIconMobile)
        assertEquals("Hello", r.config!!.welcomeTitle)
        assertNull(r.config!!.widgetIconDesktop)
    }

    @Test
    fun `fetcher non-200 throws with http code and error body`() = runBlocking {
        configStatus = 500
        configBody = "boom"
        try {
            WidgetConfigFetcher().fetchConfig(base, "tok")
            fail("expected exception")
        } catch (e: WidgetConfigFetchException) {
            assertEquals(500, e.httpCode)
            assertEquals("Failed to fetch widget config. HTTP 500: boom", e.message)
        }
    }

    @Test
    fun `fetcher network error throws with http code -1`() = runBlocking {
        server.stop()
        try {
            WidgetConfigFetcher().fetchConfig(base, "tok")
            fail("expected exception")
        } catch (e: WidgetConfigFetchException) {
            assertEquals(-1, e.httpCode)
            assertTrue(e.message!!.startsWith("Network error while fetching widget config:"))
        }
    }

    @Test
    fun `fetcher body without config gives null config`() = runBlocking {
        configBody = """{"other":1}"""
        assertNull(WidgetConfigFetcher().fetchConfig(base, "tok").config)
    }
}
