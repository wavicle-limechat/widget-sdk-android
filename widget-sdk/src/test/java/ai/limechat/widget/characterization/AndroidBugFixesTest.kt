package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.WidgetError
import ai.limechat.widget.models.WidgetConfig
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLog

/**
 * Android bugs fixed in this pass (plan section 8): a1 file chooser, a2 open(msg) token,
 * a3 external links, prefix allowlist, token/payload logging.
 */
@RunWith(RobolectricTestRunner::class)
class AndroidBugFixesTest {

    private val prod = "https://app.limechat.ai/widget"
    private val q = "?website_token=tok&platform=android&locale=en&color_scheme=light"
    private lateinit var cb: RecordingCallback

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
        ShadowLog.clear()
        cb = RecordingCallback()
    }

    private fun view(config: WidgetConfig = WidgetConfig.builder("tok").build()) =
        LimechatWidgetView(RuntimeEnvironment.getApplication()).also { it.init(config, cb) }

    private fun override(v: LimechatWidgetView, url: String, mainFrame: Boolean = true, gesture: Boolean = true): Boolean =
        shadowOf(v.webView()).webViewClient.shouldOverrideUrlLoading(v.webView(), FakeRequest(url, mainFrame, gesture))

    private val app get() = shadowOf(RuntimeEnvironment.getApplication())

    // ---- a2: open(msg) / open(map) keep the session ----

    @Test
    fun `open(message) uses the latest set-cw-conversation token over the stored one`() {
        tokenMemoryCache()["default"] = "stored1"
        val v = view()
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"fresh"}""")
        v.failMainFrameLoad()
        v.open("hi")
        assertEquals("$prod$q&cw_conversation=fresh", v.lastLoadedUrl())
    }

    @Test
    fun `open(map) keeps a token received via set-cw-conversation`() {
        val v = view()
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"fresh"}""")
        v.failMainFrameLoad()
        v.open(linkedMapOf<String, Any>("content" to "hi"))
        assertEquals("$prod$q&cw_conversation=fresh", v.lastLoadedUrl())
    }

    @Test
    fun `open(map) in external mode sends the config token`() {
        val v = view(WidgetConfig.builder("tok").setExternalTokenManagement("ext1") {}.build())
        v.open(linkedMapOf<String, Any>("content" to "hi"))
        assertEquals("$prod$q&cw_conversation=ext1", v.lastLoadedUrl())
    }

    @Test
    fun `open(message) without any token sends no cw_conversation`() {
        val v = view()
        v.open("hi")
        assertEquals("$prod$q", v.lastLoadedUrl())
    }

    // ---- a1: file chooser without a picker ----

    @Test
    fun `file chooser without a picker calls back exactly once with null and returns true`() {
        val v = view()
        val calls = mutableListOf<Array<Uri>?>()
        val params = object : WebChromeClient.FileChooserParams() {
            override fun getMode(): Int = MODE_OPEN
            override fun getAcceptTypes(): Array<String> = arrayOf()
            override fun isCaptureEnabled(): Boolean = false
            override fun getTitle(): CharSequence? = null
            override fun getFilenameHint(): String? = null
            override fun createIntent(): Intent = Intent(Intent.ACTION_GET_CONTENT)
        }
        val handled = shadowOf(v.webView()).webChromeClient
            .onShowFileChooser(v.webView(), ValueCallback { calls += it }, params)
        assertTrue(handled)
        assertEquals(1, calls.size)
        assertNull(calls.single())
        assertNull(app.nextStartedActivity)
    }

    // ---- allowlist: scheme + host + port ----

    @Test
    fun `lookalike hosts and userinfo tricks are external`() {
        val v = view()
        listOf(
            "https://app.limechat.ai.evil.com",
            "https://app.limechat.aievil.com/x",
            "https://app.limechat.ai@evil.com/x"
        ).forEach { assertTrue(it, override(v, it, gesture = false)) }
        assertNull(app.nextStartedActivity)
    }

    @Test
    fun `same scheme host and port are allowed, any path, case-insensitive, explicit default port`() {
        val v = view()
        listOf(
            "https://app.limechat.ai",
            "https://app.limechat.ai/",
            "https://app.limechat.ai/widget?website_token=tok",
            "https://app.limechat.ai/other/path",
            "https://APP.LimeChat.ai/widget",
            "https://app.limechat.ai:443/widget"
        ).forEach { assertFalse(it, override(v, it)) }
        assertNull(app.nextStartedActivity)
    }

    @Test
    fun `different scheme or port is external`() {
        val v = view()
        assertTrue(override(v, "http://app.limechat.ai/widget", gesture = false))
        assertTrue(override(v, "https://app.limechat.ai:8443/widget", gesture = false))
    }

    @Test
    fun `custom baseUrl with port matches on port`() {
        val v = view(WidgetConfig.builder("tok").setBaseUrl("http://10.0.2.2:8787/").build())
        assertFalse(override(v, "http://10.0.2.2:8787/widget"))
        assertTrue(override(v, "http://10.0.2.2/widget", gesture = false))
        assertTrue(override(v, "http://10.0.2.2:8788/widget", gesture = false))
    }

    // ---- a3: external links ----

    @Test
    fun `external link tap from an activity context opens ACTION_VIEW without NEW_TASK`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val v = LimechatWidgetView(activity).also { it.init(WidgetConfig.builder("tok").build(), cb) }
        assertTrue(override(v, "https://google.com/search?q=x"))
        val started = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals(Uri.parse("https://google.com/search?q=x"), started.data)
        assertEquals(0, started.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun `external loads without a gesture or outside the main frame stay blocked`() {
        val v = view()
        assertTrue(override(v, "https://google.com/", gesture = false))
        assertTrue(override(v, "https://google.com/", mainFrame = false))
        assertNull(app.nextStartedActivity)
        assertEquals(emptyList<String>(), cb.events)
    }

    @Test
    fun `unsupported schemes are blocked and not opened`() {
        val v = view()
        listOf("javascript:alert(1)", "file:///sdcard/x", "market://details?id=x", "sms:123")
            .forEach { assertTrue(it, override(v, it)) }
        assertNull(app.nextStartedActivity)
    }

    @Test
    fun `intent url is parsed into a browsable intent without an explicit component`() {
        val v = view()
        val url = "intent://scan/#Intent;scheme=zxing;package=com.example.scanner;component=com.example.scanner/.Evil;end"
        assertTrue(override(v, url))
        val started = app.nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals(Uri.parse("zxing://scan/"), started.data)
        assertEquals("com.example.scanner", started.`package`)
        assertNull(started.component)
        assertTrue(started.categories.contains(Intent.CATEGORY_BROWSABLE))
        assertTrue(started.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `intent url with no handler opens its browser_fallback_url`() {
        app.checkActivities(true)
        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/fallback"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        shadowOf(RuntimeEnvironment.getApplication().packageManager)
            .addResolveInfoForIntent(fallback, ResolveInfo().apply {
                activityInfo = ActivityInfo().apply {
                    packageName = "com.browser"
                    name = "Browser"
                }
            })
        val v = view()
        val url = "intent://scan/#Intent;scheme=zxing;package=com.example.scanner;" +
            "S.browser_fallback_url=https%3A%2F%2Fexample.com%2Ffallback;end"
        assertTrue(override(v, url))
        val started = app.nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals(Uri.parse("https://example.com/fallback"), started.data)
        assertEquals(emptyList<String>(), cb.events)
    }

    @Test
    fun `no app to open the link is reported via onError WEBVIEW_ERROR`() {
        app.checkActivities(true)
        val v = view()
        assertTrue(override(v, "mailto:a@b.com"))
        val err = cb.errors.single()
        assertEquals(WidgetError.ErrorCode.WEBVIEW_ERROR, err.code)
        assertEquals("No app found to open external link", err.message)
        assertEquals(mapOf("scheme" to "mailto"), err.context)
    }

    // ---- onLoaded still fires when the token callback throws ----

    @Test
    fun `calls onLoaded even when the token callback throws`() {
        val throwing = RecordingCallback(throwOn = "onConversationTokenChange")
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build(), throwing)
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "x", null)
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"tok-1"}""")

        assertEquals(listOf("onConversationTokenChange", "onLoaded", "onError"), throwing.events)
    }

    // ---- logs: no conversation token, no raw bridge payload ----

    @Test
    fun `no conversation token or raw bridge payload is logged in any flow`() {
        val stored = "Q7stored-aaaa1111"
        val fresh = "Q7fresh0-bbbb2222"
        val ext = "Q7extern-cccc3333"
        val newer = "Q7newer0-dddd4444"

        tokenMemoryCache()["default"] = stored
        val v = view()
        v.postFromJs("""{"event":"set-cw-conversation","type":"set-cw-conversation","cw_conversation":"$fresh"}""")
        v.postFromJs("""{"event":"custom","secret":"payload-value-zzz"}""")
        v.open("hi")
        v.open(linkedMapOf<String, Any>("content" to "hi"))

        val e = view(WidgetConfig.builder("tok").setExternalTokenManagement(ext) {}.build())
        e.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"$newer"}""")
        e.open("hi")

        val logs = ShadowLog.getLogs().map { "${it.tag}: ${it.msg}" }
        assertTrue(logs.isNotEmpty())
        listOf(stored, fresh, ext, newer).forEach { token ->
            val leaked = logs.filter { it.contains(token.take(8)) }
            assertEquals("token $token leaked in logs", emptyList<String>(), leaked)
        }
        assertEquals(emptyList<String>(), logs.filter { it.contains("payload-value-zzz") })
        assertEquals(emptyList<String>(), logs.filter { it.contains("cw_conversation=") })
    }
}
