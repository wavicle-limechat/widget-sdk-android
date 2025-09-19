package ai.limechat.widget.utils

import ai.limechat.widget.models.WidgetConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlBuilderTest {

    private fun config(
        websiteToken: String = "test-token",
        locale: String = "en",
        colorScheme: WidgetConfig.ColorScheme = WidgetConfig.ColorScheme.LIGHT,
        customAttributes: Map<String, Any>? = null,
        baseUrl: String = "https://app.limechat.ai",
        conversationToken: String? = null,
        showLegacyBackIcon: Boolean = false
    ): WidgetConfig {
        return WidgetConfig(
            websiteToken = websiteToken,
            locale = locale,
            colorScheme = colorScheme,
            customAttributes = customAttributes,
            baseUrl = baseUrl,
            conversationToken = conversationToken,
            showBackButtonOnLegacyView = showLegacyBackIcon
        )
    }

    @Test
    fun `build widget url with minimal config uses defaults`() {
        val url = UrlBuilder.buildWidgetUrl(config())

        assertTrue(url.startsWith("https://app.limechat.ai/widget?"))
        assertTrue(url.contains("website_token=test-token"))
        assertTrue(url.contains("locale=en"))
        assertTrue(url.contains("color_scheme=light"))
    }

    @Test
    fun `build widget url with all parameters`() {
        val customAttributes = mapOf(
            "department" to "support",
            "priority" to "high"
        )

        val url = UrlBuilder.buildWidgetUrl(
            config(
                locale = "es",
                colorScheme = WidgetConfig.ColorScheme.DARK,
                customAttributes = customAttributes,
                baseUrl = "https://custom.example.com"
            )
        )

        assertTrue(url.startsWith("https://custom.example.com/widget?"))
        assertTrue(url.contains("website_token=test-token"))
        assertTrue(url.contains("locale=es"))
        assertTrue(url.contains("color_scheme=dark"))
        assertTrue(url.contains("custom_attributes=%7B%22department%22%3A%22support%22%2C%22priority%22%3A%22high%22%7D"))
    }

    @Test
    fun `build widget url handles special characters`() {
        val url = UrlBuilder.buildWidgetUrl(
            config(
                websiteToken = "test+token=special",
                locale = "pt-BR"
            )
        )

        assertTrue(url.contains("website_token=test%2Btoken%3Dspecial"))
        assertTrue(url.contains("locale=pt-BR"))
    }

    @Test
    fun `color scheme mapping is preserved`() {
        val lightUrl = UrlBuilder.buildWidgetUrl(config(colorScheme = WidgetConfig.ColorScheme.LIGHT))
        val darkUrl = UrlBuilder.buildWidgetUrl(config(colorScheme = WidgetConfig.ColorScheme.DARK))
        val autoUrl = UrlBuilder.buildWidgetUrl(config(colorScheme = WidgetConfig.ColorScheme.AUTO))

        assertTrue(lightUrl.contains("color_scheme=light"))
        assertTrue(darkUrl.contains("color_scheme=dark"))
        assertTrue(autoUrl.contains("color_scheme=auto"))
    }

    @Test
    fun `base url trims trailing slash`() {
        val url = UrlBuilder.buildWidgetUrl(config(baseUrl = "https://example.com/"))

        assertTrue(url.startsWith("https://example.com/widget?"))
        assertFalse(url.contains("//widget"))
    }

    @Test
    fun `includes conversation token when provided`() {
        val url = UrlBuilder.buildWidgetUrl(config(conversationToken = "abc123"))

        assertTrue(url.contains("cw_conversation=abc123"))
    }

    @Test
    fun `omits conversation token when blank`() {
        val url = UrlBuilder.buildWidgetUrl(config(conversationToken = " "))

        assertFalse(url.contains("cw_conversation"))
    }

    @Test
    fun `appends legacy back icon flag when enabled`() {
        val url = UrlBuilder.buildWidgetUrl(config(showLegacyBackIcon = true))

        assertTrue(url.contains("show_legacy_back_icon=true"))
    }

    @Test
    fun `appendQueryParameters adds encoded params`() {
        val baseUrl = UrlBuilder.buildWidgetUrl(config())
        val updatedUrl = UrlBuilder.appendQueryParameters(baseUrl, mapOf("lc_open_message" to "hello world"))

        assertTrue(updatedUrl.contains("lc_open_message=hello%20world"))
        assertTrue(updatedUrl.startsWith("https://app.limechat.ai"))
    }
}
