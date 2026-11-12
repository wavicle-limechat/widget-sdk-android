package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.internal.ConversationTokenStore
import ai.limechat.widget.models.WidgetConfig
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import java.lang.reflect.Modifier

/** reset() clears the conversation: store, live token, a config token, held attributes and a held open message. */
@RunWith(RobolectricTestRunner::class)
class ResetTest {

    private val prod = "https://app.limechat.ai/widget"
    private val q = "?website_token=tok&platform=android&locale=en&color_scheme=light"
    private val ctx get() = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        resetTokenMemory()
        ctx.getSharedPreferences("limechat_widget_conversations", Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun prefToken(id: String = "default") =
        ctx.getSharedPreferences("limechat_widget_conversations", Context.MODE_PRIVATE).getString(id, null)

    private fun view(config: WidgetConfig = WidgetConfig.builder("tok").build()) =
        LimechatWidgetView(ctx).also { it.init(config, RecordingCallback()) }

    @Test
    fun `reset clears the stored token in prefs and memory and reloads without cw_conversation`() {
        val v = view()
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"tokA"}""")
        assertEquals("tokA", prefToken())
        val loads = v.recorder().loadedUrls.size
        v.reset()
        assertNull(prefToken())
        assertTrue(tokenMemoryCache().isEmpty())
        assertEquals(loads + 1, v.recorder().loadedUrls.size)
        assertEquals("$prod$q", v.lastLoadedUrl())
    }

    @Test
    fun `reset drops held attributes and a held open message`() {
        val v = view()
        assertTrue(v.setMessageAttributes(mapOf("a" to 1)))
        v.open("hi")
        v.reset()
        v.widgetReady()
        assertTrue(v.recorder().scripts.none { it.contains("set-message-attributes") })
        assertTrue(v.recorder().scripts.none { it.contains("open-with-message") })
    }

    @Test
    fun `reset in external mode loads without the config token until the next init`() {
        val config = WidgetConfig.builder("tok").setExternalTokenManagement("ext1") {}.build()
        val v = view(config)
        assertEquals("$prod$q&cw_conversation=ext1", v.lastLoadedUrl())
        v.reset()
        assertEquals("$prod$q", v.lastLoadedUrl())
        v.reload()
        assertEquals("$prod$q", v.lastLoadedUrl())
        v.init(config)
        assertEquals("$prod$q&cw_conversation=ext1", v.lastLoadedUrl())
    }

    @Test
    fun `reset before init does nothing`() {
        ConversationTokenStore.save(ctx, "default", "tokA")
        val v = LimechatWidgetView(ctx)
        v.reset()
        assertNull(v.lastLoadedUrl())
        assertEquals("tokA", prefToken())
    }

    @Test
    fun `static reset clears only the given instance`() {
        ConversationTokenStore.save(ctx, "a", "tokA")
        ConversationTokenStore.save(ctx, "b", "tokB")
        LimechatWidgetView.reset(ctx, "a")
        assertNull(prefToken("a"))
        assertEquals("tokB", prefToken("b"))
        assertFalse(tokenMemoryCache().containsKey("a"))
        assertEquals("tokB", tokenMemoryCache()["b"])
    }

    @Test
    fun `static reset with a null or blank instanceId clears default`() {
        ConversationTokenStore.save(ctx, "default", "tok1")
        LimechatWidgetView.reset(ctx)
        assertNull(prefToken())
        ConversationTokenStore.save(ctx, "default", "tok2")
        LimechatWidgetView.reset(ctx, "  ")
        assertNull(prefToken())
        assertTrue(tokenMemoryCache().isEmpty())
    }

    @Test
    fun `java sees static reset with a context and with a context and an instance id`() {
        val one = LimechatWidgetView::class.java.getMethod("reset", Context::class.java)
        val two = LimechatWidgetView::class.java.getMethod("reset", Context::class.java, String::class.java)
        assertTrue(Modifier.isStatic(one.modifiers))
        assertTrue(Modifier.isStatic(two.modifiers))
        ConversationTokenStore.save(ctx, "x", "tokX")
        two.invoke(null, ctx, "x")
        assertNull(prefToken("x"))
    }

    @Test
    fun `after reset the next token is stored`() {
        val v = view()
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"tokA"}""")
        v.reset()
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"tokB"}""")
        assertEquals("tokB", prefToken())
        assertEquals("tokB", tokenMemoryCache()["default"])
    }

    private fun userConfig() = WidgetConfig.builder("tok")
        .setUser(WidgetConfig.User(name = "N", identifier = "id-1", identifierHash = "h"))
        .build()

    private fun LimechatWidgetView.finishPage() {
        shadowOf(webView()).webViewClient.onPageFinished(webView(), "x")
    }

    @Test
    fun `reset with a user set sends no set-user and no rnUser on the reloaded page`() {
        val v = view(userConfig())
        v.finishPage()
        v.widgetReady()
        v.reset()
        val before = v.recorder().scripts.size
        v.finishPage()
        v.widgetReady()
        val after = v.recorder().scripts.drop(before)
        assertTrue(after.none { it.contains("rnUser") })
        assertTrue(after.none { it.contains("\"set-user\"") })
        assertNull(identityMemory()["default"])
    }

    @Test
    fun `init with a user after reset sends set-user again`() {
        val v = view(userConfig())
        v.widgetReady()
        v.reset()
        v.init(userConfig())
        val before = v.recorder().scripts.size
        v.finishPage()
        v.widgetReady()
        val after = v.recorder().scripts.drop(before)
        assertTrue(after.any { it.contains("rnUser") })
        assertEquals(1, after.count { it.contains("\"set-user\"") })
    }
}
