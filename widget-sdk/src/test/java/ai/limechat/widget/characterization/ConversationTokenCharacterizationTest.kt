package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.models.WidgetConfig
import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Pins conversation token persistence on develop @ 3456551
 * (LimechatWidgetView.init :101-121, loadConversationToken :648-661,
 * saveConversationToken :667-674, handleConversationTokenUpdate :676-699).
 *
 * Internal mode: prefs file `limechat_widget_conversations`, key = instanceId or "default",
 * plus a process-wide static memory map checked first.
 * External mode (setExternalTokenManagement): token comes from config, prefs never read/written.
 */
@RunWith(RobolectricTestRunner::class)
class ConversationTokenCharacterizationTest {

    private val ctx: Context get() = RuntimeEnvironment.getApplication()
    private val prefs: SharedPreferences
        get() = ctx.getSharedPreferences("limechat_widget_conversations", Context.MODE_PRIVATE)
    private val q = "?website_token=tok&platform=android&locale=en&color_scheme=light"
    private val prod = "https://app.limechat.ai/widget"

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
        prefs.edit().clear().commit()
    }

    private fun view(config: WidgetConfig, cb: RecordingCallback? = null) =
        LimechatWidgetView(ctx).also { it.init(config, cb) }

    private fun setToken(v: LimechatWidgetView, token: String) =
        v.postFromJs("""{"event":"set-cw-conversation","type":"set-cw-conversation","cw_conversation":"$token"}""")

    @Test
    fun `internal mode saves under key default in prefs and memory, and calls the callback`() {
        val cb = RecordingCallback()
        val v = view(WidgetConfig.builder("tok").build(), cb)
        setToken(v, "tokA")
        assertEquals("tokA", prefs.getString("default", null))
        assertEquals("tokA", tokenMemoryCache()["default"])
        assertEquals(listOf("tokA"), cb.tokens)
        assertEquals(setOf("default"), prefs.all.keys)
    }

    @Test
    fun `internal mode uses instanceId as the prefs key`() {
        val v = view(WidgetConfig.builder("tok").setInstanceId("shop-1").build())
        setToken(v, "tokB")
        assertEquals("tokB", prefs.getString("shop-1", null))
        assertNull(prefs.getString("default", null))
    }

    @Test
    fun `token update does not reload the page`() {
        val v = view(WidgetConfig.builder("tok").build())
        val before = v.lastLoadedUrl()
        setToken(v, "tokA")
        assertEquals(before, v.lastLoadedUrl())
        assertEquals("$prod$q", before)
    }

    @Test
    fun `calling init again on the same view uses the updated token`() {
        val v = view(WidgetConfig.builder("tok").build())
        setToken(v, "tokA")
        v.init(WidgetConfig.builder("tok").build())
        assertEquals("$prod$q&cw_conversation=tokA", v.lastLoadedUrl())
    }

    @Test
    fun `new view loads the token from memory`() {
        setToken(view(WidgetConfig.builder("tok").build()), "tokA")
        assertEquals("$prod$q&cw_conversation=tokA", view(WidgetConfig.builder("tok").build()).lastLoadedUrl())
    }

    @Test
    fun `new view loads the token from prefs after the memory map is gone (app restart) and caches it`() {
        prefs.edit().putString("default", "fromPrefs").commit()
        assertEquals("$prod$q&cw_conversation=fromPrefs", view(WidgetConfig.builder("tok").build()).lastLoadedUrl())
        assertEquals("fromPrefs", tokenMemoryCache()["default"])
    }

    @Test
    fun `memory map wins over prefs`() {
        prefs.edit().putString("default", "fromPrefs").commit()
        tokenMemoryCache()["default"] = "fromMemory"
        assertEquals("$prod$q&cw_conversation=fromMemory", view(WidgetConfig.builder("tok").build()).lastLoadedUrl())
    }

    @Test
    fun `instances are isolated by instanceId`() {
        setToken(view(WidgetConfig.builder("tok").setInstanceId("a").build()), "tokA")
        setToken(view(WidgetConfig.builder("tok").setInstanceId("b").build()), "tokB")
        assertTrue(view(WidgetConfig.builder("tok").setInstanceId("a").build()).lastLoadedUrl()!!.endsWith("cw_conversation=tokA"))
        assertTrue(view(WidgetConfig.builder("tok").setInstanceId("b").build()).lastLoadedUrl()!!.endsWith("cw_conversation=tokB"))
    }

    @Test
    fun `pins current behaviour - key default is shared across website tokens`() {
        setToken(view(WidgetConfig.builder("websiteA").build()), "tokFromA")
        assertEquals(
            "$prod?website_token=websiteB&platform=android&locale=en&color_scheme=light&cw_conversation=tokFromA",
            view(WidgetConfig.builder("websiteB").build()).lastLoadedUrl()
        )
    }

    @Test
    fun `empty, blank, missing and non-string tokens are ignored`() {
        val cb = RecordingCallback()
        val v = view(WidgetConfig.builder("tok").build(), cb)
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":""}""")
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":"   "}""")
        v.postFromJs("""{"event":"set-cw-conversation"}""")
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":123}""")
        v.postFromJs("""{"event":"set-cw-conversation","cw_conversation":null}""")
        assertEquals(emptyList<String>(), cb.events)
        assertTrue(prefs.all.isEmpty())
    }

    @Test
    fun `token is overwritten by a later update`() {
        val v = view(WidgetConfig.builder("tok").build())
        setToken(v, "one")
        setToken(v, "two")
        assertEquals("two", prefs.getString("default", null))
    }

    // ---- external mode ----

    @Test
    fun `external mode uses the config token in the url and ignores stored tokens`() {
        prefs.edit().putString("default", "stored").commit()
        tokenMemoryCache()["default"] = "stored"
        val v = view(WidgetConfig.builder("tok").setExternalTokenManagement("ext1") {}.build())
        assertEquals("$prod$q&cw_conversation=ext1", v.lastLoadedUrl())
    }

    @Test
    fun `external mode calls the config lambda and the callback, prefs and memory untouched`() {
        val got = mutableListOf<String>()
        val cb = RecordingCallback()
        val v = view(WidgetConfig.builder("tok").setExternalTokenManagement("ext1") { got += it }.build(), cb)
        setToken(v, "newTok")
        assertEquals(listOf("newTok"), got)
        assertEquals(listOf("newTok"), cb.tokens)
        assertTrue(prefs.all.isEmpty())
        assertTrue(tokenMemoryCache().isEmpty())
    }

    @Test
    fun `pins current behaviour - external mode with empty string token sends no cw_conversation`() {
        val v = view(WidgetConfig.builder("tok").setExternalTokenManagement("") {}.build())
        assertEquals("$prod$q", v.lastLoadedUrl())
    }

    @Test
    fun `pins current behaviour - external mode init again still uses the original config token after an update`() {
        val config = WidgetConfig.builder("tok").setExternalTokenManagement("ext1") {}.build()
        val v = view(config)
        setToken(v, "newTok")
        v.init(config)
        assertEquals("$prod$q&cw_conversation=ext1", v.lastLoadedUrl())
    }
}
