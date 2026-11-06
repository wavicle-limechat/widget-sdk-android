package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.WidgetError
import ai.limechat.widget.models.WidgetConfig
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/**
 * What this proves: the client's onRenderProcessGone, when called by hand, returns true, swaps in a new
 * webview with the bridge, and follows the reload-once rule. Robolectric cannot kill a real renderer,
 * so it does NOT prove the system calls the method, that a real killed webview is safe to destroy,
 * or anything on api 23 to 25 where the callback does not exist. Those need the device check.
 */
@RunWith(RobolectricTestRunner::class)
class RenderProcessGoneTest {

    private lateinit var cb: RecordingCallback

    private val detail = object : RenderProcessGoneDetail() {
        override fun didCrash() = false
        override fun rendererPriorityAtExit() = 0
    }

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
        cb = RecordingCallback()
    }

    private fun view() = LimechatWidgetView(RuntimeEnvironment.getApplication())
        .also { it.init(WidgetConfig.builder("tok").build(), cb) }

    /** the webview, starting a page on it the way the system would */
    private fun LimechatWidgetView.ready(token: String? = null) {
        shadowOf(webView()).webViewClient.onPageStarted(webView(), "x", null)
        val extra = token?.let { ""","cw_conversation":"$it"""" } ?: ""
        postFromJs("""{"event":"set-cw-conversation"$extra}""")
    }

    private fun LimechatWidgetView.die(dead: WebView = webView()): Boolean =
        shadowOf(dead).webViewClient.onRenderProcessGone(dead, detail)

    private fun LimechatWidgetView.hasBridge() =
        shadowOf(webView()).getJavascriptInterface("LimechatAndroid") != null

    @Test
    fun `process death reloads once, then reports until ready`() {
        val v = view()
        v.ready(token = "live1")
        val first = v.webView()

        // first death: new webview, bridge on it, one load with the live token, no error
        assertTrue(v.die())
        val second = v.webView()
        assertNotSame(first, second)
        assertEquals(1, v.childCount)
        assertTrue(v.hasBridge())
        assertEquals(1, v.recorder().loadedUrls.size)
        assertTrue(v.recorder().loadedUrls[0].contains("live1"))
        assertTrue(cb.errors.isEmpty())

        // second death before ready: new webview, bridge, no load, one error
        assertTrue(v.die())
        assertNotSame(second, v.webView())
        assertEquals(1, v.childCount)
        assertTrue(v.hasBridge())
        assertTrue(v.recorder().loadedUrls.isEmpty())
        assertEquals(1, cb.errors.size)
        assertEquals(WidgetError.ErrorCode.WEBVIEW_ERROR, cb.errors[0].code)
        assertEquals("Widget web process died again before ready", cb.errors[0].message)

        // a later host reload() still loads, and does not reset the flag
        v.reload()
        assertEquals(1, v.recorder().loadedUrls.size)
        assertTrue(v.die())
        assertEquals(2, cb.errors.size)
        assertTrue(v.recorder().loadedUrls.isEmpty())

        // first ready resets it: the next death reloads again
        v.ready()
        assertTrue(v.die())
        assertEquals(2, cb.errors.size)
        assertEquals(1, v.recorder().loadedUrls.size)
    }

    @Test
    fun `held open and attributes go out at ready after a process death`() {
        val v = view()
        v.setMessageAttributes(mapOf("a" to "1"))
        v.open("hello")
        v.die()

        val rec = v.recorder()
        assertTrue(rec.scripts.none { it.contains("set-message-attributes") || it.contains("open-with-message") })
        v.ready()
        val attrs = rec.scripts.indexOfFirst { it.contains("set-message-attributes") }
        val open = rec.scripts.indexOfFirst { it.contains("open-with-message") }
        assertTrue(attrs >= 0)
        assertTrue(open > attrs)
    }

    @Test
    fun `a stale webview is ignored`() {
        val v = view()
        val first = v.webView()
        v.die()
        val second = v.webView()
        val loads = v.recorder().loadedUrls.size

        assertTrue(v.die(first))
        assertSame(second, v.webView())
        assertEquals(1, v.childCount)
        assertEquals(loads, v.recorder().loadedUrls.size)
        assertTrue(cb.errors.isEmpty())
        assertNotNull(second)
    }
}
