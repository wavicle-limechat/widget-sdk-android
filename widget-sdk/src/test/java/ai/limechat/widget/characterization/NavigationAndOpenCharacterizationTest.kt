package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.WidgetError
import ai.limechat.widget.WidgetFilePicker
import ai.limechat.widget.models.WidgetConfig
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceResponse
import android.webkit.FakeWebResourceError
import androidx.fragment.app.FragmentActivity
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

/**
 * Pins navigation (shouldOverrideUrlLoading :324-340), onReceivedError (:307-322),
 * the file chooser (:346-353), open()/open(msg)/open(map) (:149-221) and onBackPressed
 * on develop @ 3456551.
 */
@RunWith(RobolectricTestRunner::class)
class NavigationAndOpenCharacterizationTest {

    private val prod = "https://app.limechat.ai/widget"
    private val q = "?website_token=tok&locale=en&color_scheme=light"
    private lateinit var cb: RecordingCallback

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
        cb = RecordingCallback()
    }

    private fun view(config: WidgetConfig = WidgetConfig.builder("tok").build()) =
        LimechatWidgetView(RuntimeEnvironment.getApplication()).also { it.init(config, cb) }

    private fun override(v: LimechatWidgetView, url: String): Boolean =
        shadowOf(v.webView()).webViewClient.shouldOverrideUrlLoading(v.webView(), FakeRequest(url))

    // ---- url allowlist ----

    @Test
    fun `urls starting with baseUrl are allowed`() {
        val v = view()
        assertFalse(override(v, "https://app.limechat.ai/widget?x=1"))
        assertFalse(override(v, "https://app.limechat.ai"))
    }

    @Test
    fun `allowlist compares scheme host and port, so lookalike hosts are blocked`() {
        val v = view()
        assertTrue(override(v, "https://app.limechat.ai.evil.com/phish"))
        assertTrue(override(v, "https://app.limechat.aixyz/"))
    }

    @Test
    fun `external link taps are blocked in the webview and opened outside with ACTION_VIEW`() {
        val v = view()
        val app = shadowOf(RuntimeEnvironment.getApplication())
        listOf(
            "https://google.com/",
            "http://app.limechat.ai/widget",
            "mailto:a@b.com",
            "tel:+123"
        ).forEach {
            assertTrue(it, override(v, it))
            val started = app.nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, started.action)
            assertEquals(Uri.parse(it), started.data)
            assertTrue(started.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        }
        assertTrue(override(v, "intent://x#Intent;end"))
        assertEquals(Intent.ACTION_VIEW, app.nextStartedActivity.action)
        assertNull(app.nextStartedActivity)
        assertEquals(emptyList<String>(), cb.events)
    }

    @Test
    fun `allowlist trims the trailing slash of baseUrl`() {
        val v = view(WidgetConfig.builder("tok").setBaseUrl("https://example.com/").build())
        assertFalse(override(v, "https://example.com/widget"))
        assertFalse(override(v, "https://example.com"))
    }

    @Test
    fun `pins current behaviour - before init every url is allowed`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        assertFalse(override(v, "https://google.com/"))
    }

    // ---- errors ----

    @Test
    fun `pins current behaviour - onReceivedError reports WEBVIEW_ERROR for any request, subresources included`() {
        val v = view()
        val error = FakeWebResourceError(-2, "net::ERR_FAILED")
        shadowOf(v.webView()).webViewClient.onReceivedError(
            v.webView(), FakeRequest("https://cdn.example.com/x.png", mainFrame = false), error
        )
        val e = cb.errors.single()
        assertEquals(WidgetError.ErrorCode.WEBVIEW_ERROR, e.code)
        assertEquals("WebView error: net::ERR_FAILED", e.message)
        assertEquals(mapOf("errorCode" to -2), e.context)
    }

    @Test
    fun `main frame http error reports WEBVIEW_ERROR with the status`() {
        val v = view()
        val response = WebResourceResponse("text/html", "utf-8", 503, "x", emptyMap(), null)
        shadowOf(v.webView()).webViewClient.onReceivedHttpError(
            v.webView(), FakeRequest("$prod$q"), response
        )
        val e = cb.errors.single()
        assertEquals(WidgetError.ErrorCode.WEBVIEW_ERROR, e.code)
        assertEquals(mapOf("statusCode" to 503), e.context)
    }

    @Test
    fun `subresource http error reports nothing`() {
        val v = view()
        val response = WebResourceResponse("text/html", "utf-8", 404, "x", emptyMap(), null)
        shadowOf(v.webView()).webViewClient.onReceivedHttpError(
            v.webView(), FakeRequest("https://cdn.example.com/x.png", mainFrame = false), response
        )
        assertTrue(cb.errors.isEmpty())
    }

    @Test
    fun `onPageFinished alone does not call onLoaded`() {
        val v = view()
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "$prod$q")
        assertTrue(cb.events.isEmpty())
    }

    @Test
    fun `onLoaded fires once when the widget is ready, even after a second page finish`() {
        val v = view()
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "$prod$q")
        v.widgetReady()
        shadowOf(v.webView()).webViewClient.onPageFinished(v.webView(), "$prod$q")
        v.postFromJs("""{"event":"set-cw-conversation"}""")
        assertEquals(listOf("onLoaded"), cb.events)
    }

    @Test
    fun `onLoaded fires after the token callback`() {
        val v = view()
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "x", null)
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"tokA"}""")
        assertEquals(listOf("onConversationTokenChange", "onLoaded"), cb.events)
    }

    @Test
    fun `a new page fires onLoaded again`() {
        val v = view()
        v.widgetReady()
        v.widgetReady()
        assertEquals(listOf("onLoaded", "onLoaded"), cb.events)
    }

    // ---- file chooser ----

    private class Params(private val accept: Array<String>, private val m: Int = MODE_OPEN) :
        WebChromeClient.FileChooserParams() {
        override fun getMode(): Int = m
        override fun getAcceptTypes(): Array<String> = accept
        override fun isCaptureEnabled(): Boolean = false
        override fun getTitle(): CharSequence? = null
        override fun getFilenameHint(): String? = null
        override fun createIntent(): Intent = Intent(Intent.ACTION_GET_CONTENT)
    }

    private class RecordingValueCallback : ValueCallback<Array<Uri>> {
        val calls = mutableListOf<Array<Uri>?>()
        override fun onReceiveValue(value: Array<Uri>?) {
            calls += value
        }
    }

    /** WidgetFilePicker must be created before the activity is STARTED (registerForActivityResult). */
    private fun pickerActivityView(): Pair<FragmentActivity, LimechatWidgetView> {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).create()
        val activity = controller.get()
        val picker = WidgetFilePicker(activity)
        controller.start().resume()
        val v = LimechatWidgetView(activity)
        v.init(WidgetConfig.builder("tok").build(), cb)
        v.attachFilePicker(picker)
        return activity to v
    }

    @Test
    fun `file picker created after the activity is started throws`() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        try {
            WidgetFilePicker(activity)
            org.junit.Assert.fail("expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // registerForActivityResult after STARTED
        }
    }

    @Test
    fun `file chooser without attachFilePicker cancels with null so the page is not stuck`() {
        val v = view()
        val vc = RecordingValueCallback()
        val handled = shadowOf(v.webView()).webChromeClient
            .onShowFileChooser(v.webView(), vc, Params(arrayOf("image/*")))
        assertTrue(handled)
        idleMain()
        assertEquals(1, vc.calls.size)
        assertNull(vc.calls.single())
    }

    @Test
    fun `file chooser with picker launches a chooser for GET_CONTENT and cancel returns null`() {
        val (activity, v) = pickerActivityView()

        val vc = RecordingValueCallback()
        assertTrue(
            shadowOf(v.webView()).webChromeClient
                .onShowFileChooser(v.webView(), vc, Params(arrayOf("image/*", "application/pdf"), WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE))
        )

        val started = shadowOf(activity).nextStartedActivityForResult
        val chooser = started.intent
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION")
        val inner = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_GET_CONTENT, inner.action)
        assertEquals("*/*", inner.type)
        assertTrue(inner.categories.contains(Intent.CATEGORY_OPENABLE))
        assertTrue(inner.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
        assertEquals(listOf("image/*", "application/pdf"), inner.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)!!.toList())

        shadowOf(activity).receiveResult(chooser, Activity.RESULT_CANCELED, null)
        idleMain()
        assertEquals(1, vc.calls.size)
        assertNull(vc.calls.single())
    }

    @Test
    fun `file chooser with picker and a single accept type sets it as the intent type`() {
        val (activity, v) = pickerActivityView()
        shadowOf(v.webView()).webChromeClient
            .onShowFileChooser(v.webView(), RecordingValueCallback(), Params(arrayOf("image/*", "")))
        @Suppress("DEPRECATION")
        val inner = shadowOf(activity).nextStartedActivityForResult.intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals("image/*", inner.type)
        assertFalse(inner.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, true))
    }

    @Test
    fun `file chooser with picker returns the picked uri`() {
        val (activity, v) = pickerActivityView()
        val vc = RecordingValueCallback()
        shadowOf(v.webView()).webChromeClient.onShowFileChooser(v.webView(), vc, Params(arrayOf()))
        val chooser = shadowOf(activity).nextStartedActivityForResult.intent
        shadowOf(activity).receiveResult(chooser, Activity.RESULT_OK, Intent().setData(Uri.parse("content://x/1")))
        idleMain()
        assertEquals(listOf(Uri.parse("content://x/1")), vc.calls.single()!!.toList())
    }

    // ---- open ----

    @Test
    fun `open() without message does nothing`() {
        val v = view()
        v.recorder().scripts.clear()
        v.open()
        assertEquals("$prod$q", v.lastLoadedUrl())
        assertEquals(emptyList<String>(), v.recorder().scripts)
    }

    @Test
    fun `open(message) holds the message and keeps it out of the url`() {
        val v = view()
        v.open("hi there")
        assertEquals("$prod$q", v.lastLoadedUrl())
        v.widgetReady()
        assertTrue(v.recorder().scripts.single { it.contains("\"open-with-message\"") }.contains("\"openMessage\":\"hi there\""))
    }

    @Test
    fun `open with message keeps the stored cw_conversation in internal mode`() {
        tokenMemoryCache()["default"] = "stored1"
        val v = view()
        assertEquals("$prod$q&cw_conversation=stored1", v.lastLoadedUrl())
        v.open("hi")
        assertEquals("$prod$q&cw_conversation=stored1", v.lastLoadedUrl())
    }

    @Test
    fun `open with message keeps a token received via set-cw-conversation`() {
        val v = view()
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"fresh"}""")
        v.open("hi")
        assertEquals("$prod$q&cw_conversation=fresh", v.lastLoadedUrl())
    }

    @Test
    fun `open with message uses the live token under external token management`() {
        val v = view(WidgetConfig.builder("tok").setExternalTokenManagement("ext1") {}.build())
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"newer"}""")
        v.open("hi")
        assertEquals("$prod$q&cw_conversation=newer", v.lastLoadedUrl())
    }

    @Test
    fun `open(map) holds the payload and keeps cw_conversation in the url`() {
        tokenMemoryCache()["default"] = "stored1"
        val v = view()
        v.open(linkedMapOf<String, Any>("content" to "hi", "n" to 1))
        assertEquals("$prod$q&cw_conversation=stored1", v.lastLoadedUrl())
        v.widgetReady()
        assertTrue(v.recorder().scripts.single { it.contains("\"open-with-message\"") }.contains("\"openMessage\":{\"content\":\"hi\",\"n\":1}"))
    }

    @Test
    fun `open while ready posts the message and does not reload`() {
        val v = view()
        v.widgetReady()
        val url = v.lastLoadedUrl()
        v.recorder().scripts.clear()
        v.open("hi")
        assertEquals(url, v.lastLoadedUrl())
        assertEquals(1, v.recorder().scripts.count { it.contains("\"open-with-message\"") })
    }

    @Test
    fun `init with an initial message and no user holds it until ready`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").build(), null, "first")
        assertEquals("$prod$q", v.lastLoadedUrl())
        assertTrue(v.recorder().scripts.none { it.contains("open-with-message") })
        v.widgetReady()
        assertTrue(v.recorder().scripts.single { it.contains("open-with-message") }.contains("first"))
    }

    @Test
    fun `open with an empty string or empty map does nothing`() {
        val v = view(WidgetConfig.builder("tok").setUser(WidgetConfig.User(name = "N")).build())
        v.widgetReady()
        v.recorder().scripts.clear()
        v.open("")
        v.open(emptyMap<String, Any>())
        assertTrue(v.recorder().scripts.isEmpty())
        v.widgetReady()
        assertTrue(v.recorder().scripts.none { it.contains("open-with-message") })
    }

    @Test
    fun `back press does not resend an open message`() {
        val v = view()
        v.open("hi")
        v.widgetReady()
        shadowOf(v.webView()).webViewClient.onPageStarted(v.webView(), "x", null)
        v.postFromJs("""{"event":"set-cw-conversation"}""")
        assertEquals(1, v.recorder().scripts.count { it.contains("open-with-message") })
        assertTrue(v.recorder().loadedUrls.none { it.contains("lc_open_") })
    }

    @Test
    fun `a message with percent, plus and unicode reaches the post unchanged`() {
        val v = view()
        val text = "50% off + free \u00e9 \ud83d\ude0a"
        v.open(text)
        v.widgetReady()
        val script = v.recorder().scripts.single { it.contains("open-with-message") }
        val json = script.substringAfter("JSON.stringify(").substringBefore("), '*')")
        assertEquals(text, org.json.JSONObject(json).getString("openMessage"))
    }

    @Test
    fun `open before init does nothing`() {
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.open("hi")
        v.open(mapOf("content" to "hi"))
        assertNull(v.lastLoadedUrl())
    }

    // ---- back ----

    @Test
    fun `onBackPressed returns false when the webview has no history`() {
        val v = view()
        shadowOf(v.webView()).setCanGoBack(false)
        assertFalse(v.onBackPressed())
    }

    @Test
    fun `onBackPressed goes back in the webview when it can`() {
        val v = view()
        shadowOf(v.webView()).setCanGoBack(true)
        assertTrue(v.onBackPressed())
        assertEquals(1, shadowOf(v.webView()).goBackInvocations)
    }
}
