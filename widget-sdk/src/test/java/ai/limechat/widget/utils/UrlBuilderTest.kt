package ai.limechat.widget.utils

import ai.limechat.widget.models.WidgetConfig
import org.junit.Assert.*
import org.junit.Test

class UrlBuilderTest {

    @Test
    fun `test build widget url with minimal config`() {
        val config = WidgetConfig(websiteToken = "test-token")
        val url = UrlBuilder.buildWidgetUrl(config)
        
        assertTrue(url.startsWith("https://app.limechat.ai/widget?"))
        assertTrue(url.contains("website_token=test-token"))
        assertTrue(url.contains("locale=en"))
        assertTrue(url.contains("color_scheme=light"))
    }

    @Test
    fun `test build widget url with all parameters`() {
        val customAttributes = mapOf(
            "department" to "support",
            "priority" to "high"
        )

        val config = WidgetConfig(
            websiteToken = "test-token",
            locale = "es",
            colorScheme = WidgetConfig.ColorScheme.DARK,
            customAttributes = customAttributes,
            baseUrl = "https://custom.example.com"
        )

        val url = UrlBuilder.buildWidgetUrl(config)
        
        assertTrue(url.startsWith("https://custom.example.com/widget?"))
        assertTrue(url.contains("website_token=test-token"))
        assertTrue(url.contains("locale=es"))
        assertTrue(url.contains("color_scheme=dark"))
        assertTrue(url.contains("custom_attributes="))
    }

    @Test
    fun `test build widget url with special characters`() {
        val config = WidgetConfig(
            websiteToken = "test+token=special",
            locale = "pt-BR"
        )

        val url = UrlBuilder.buildWidgetUrl(config)
        
        assertTrue(url.contains("website_token=test%2Btoken%3Dspecial"))
        assertTrue(url.contains("locale=pt-BR"))
    }

    @Test
    fun `test color scheme mapping`() {
        val lightConfig = WidgetConfig(
            websiteToken = "test",
            colorScheme = WidgetConfig.ColorScheme.LIGHT
        )
        val darkConfig = WidgetConfig(
            websiteToken = "test",
            colorScheme = WidgetConfig.ColorScheme.DARK
        )
        val autoConfig = WidgetConfig(
            websiteToken = "test",
            colorScheme = WidgetConfig.ColorScheme.AUTO
        )

        assertTrue(UrlBuilder.buildWidgetUrl(lightConfig).contains("color_scheme=light"))
        assertTrue(UrlBuilder.buildWidgetUrl(darkConfig).contains("color_scheme=dark"))
        assertTrue(UrlBuilder.buildWidgetUrl(autoConfig).contains("color_scheme=auto"))
    }

    @Test
    fun `test base url without trailing slash`() {
        val config = WidgetConfig(
            websiteToken = "test",
            baseUrl = "https://example.com/"
        )

        val url = UrlBuilder.buildWidgetUrl(config)
        assertTrue(url.startsWith("https://example.com/widget?"))
        assertFalse(url.contains("//widget"))
    }
}