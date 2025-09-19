package ai.limechat.widget.utils

import ai.limechat.widget.models.WidgetConfig
import org.junit.Assert.*
import org.junit.Test

class UrlBuilderTest {

    @Test
    fun `test build widget url with minimal config`() {
        val config = WidgetConfig.builder("test-token").build()
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

        val config = WidgetConfig.builder("test-token")
            .setLocale("es")
            .setColorScheme(WidgetConfig.ColorScheme.DARK)
            .setCustomAttributes(customAttributes)
            .setBaseUrl("https://custom.example.com")
            .build()

        val url = UrlBuilder.buildWidgetUrl(config)
        
        assertTrue(url.startsWith("https://custom.example.com/widget?"))
        assertTrue(url.contains("website_token=test-token"))
        assertTrue(url.contains("locale=es"))
        assertTrue(url.contains("color_scheme=dark"))
        assertTrue(url.contains("custom_attributes="))
    }

    @Test
    fun `test build widget url with special characters`() {
        val config = WidgetConfig.builder("test+token=special")
            .setLocale("pt-BR")
            .build()

        val url = UrlBuilder.buildWidgetUrl(config)
        
        assertTrue(url.contains("website_token=test%2Btoken%3Dspecial"))
        assertTrue(url.contains("locale=pt-BR"))
    }

    @Test
    fun `test color scheme mapping`() {
        val lightConfig = WidgetConfig.builder("test")
            .setColorScheme(WidgetConfig.ColorScheme.LIGHT)
            .build()
        val darkConfig = WidgetConfig.builder("test")
            .setColorScheme(WidgetConfig.ColorScheme.DARK)
            .build()
        val autoConfig = WidgetConfig.builder("test")
            .setColorScheme(WidgetConfig.ColorScheme.AUTO)
            .build()

        assertTrue(UrlBuilder.buildWidgetUrl(lightConfig).contains("color_scheme=light"))
        assertTrue(UrlBuilder.buildWidgetUrl(darkConfig).contains("color_scheme=dark"))
        assertTrue(UrlBuilder.buildWidgetUrl(autoConfig).contains("color_scheme=auto"))
    }

    @Test
    fun `test base url without trailing slash`() {
        val config = WidgetConfig.builder("test")
            .setBaseUrl("https://example.com/")
            .build()

        val url = UrlBuilder.buildWidgetUrl(config)
        assertTrue(url.startsWith("https://example.com/widget?"))
        assertFalse(url.contains("//widget"))
    }

    @Test
    fun `test show legacy back icon parameter`() {
        val configWithLegacyIcon = WidgetConfig.builder("test")
            .setShowLegacyBackIcon(true)
            .build()
        val configWithoutLegacyIcon = WidgetConfig.builder("test")
            .setShowLegacyBackIcon(false)
            .build()

        val urlWithLegacy = UrlBuilder.buildWidgetUrl(configWithLegacyIcon)
        val urlWithoutLegacy = UrlBuilder.buildWidgetUrl(configWithoutLegacyIcon)

        assertTrue(urlWithLegacy.contains("show_legacy_back_icon=true"))
        assertFalse(urlWithoutLegacy.contains("show_legacy_back_icon"))
    }
}