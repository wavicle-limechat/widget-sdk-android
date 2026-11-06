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
        tokenMemoryCache().clear()
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
}
