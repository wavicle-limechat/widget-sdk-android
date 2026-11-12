package ai.limechat.widget.utils

import ai.limechat.widget.models.WidgetConfig
import org.junit.Assert.*
import org.junit.Test

@Suppress("DEPRECATION")
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
    fun `test show legacy back icon is not a url parameter`() {
        val configWithLegacyIcon = WidgetConfig.builder("test")
            .setShowLegacyBackIcon(true)
            .build()
        val configWithoutLegacyIcon = WidgetConfig.builder("test")
            .setShowLegacyBackIcon(false)
            .build()

        val urlWithLegacy = UrlBuilder.buildWidgetUrl(configWithLegacyIcon)
        val urlWithoutLegacy = UrlBuilder.buildWidgetUrl(configWithoutLegacyIcon)

        assertFalse(urlWithLegacy.contains("show_legacy_back_icon"))
        assertFalse(urlWithoutLegacy.contains("show_legacy_back_icon"))
    }

    @Test
    fun `test hide file picker parameter`() {
        val hidden = UrlBuilder.buildWidgetUrl(WidgetConfig.builder("test").setHideFilePicker(true).build())
        val default = UrlBuilder.buildWidgetUrl(WidgetConfig.builder("test").build())

        assertTrue(hidden.contains("hide_file_picker=true"))
        assertFalse(default.contains("hide_file_picker"))
    }

    @Test
    fun `lc_open_message survives the widget double decode`() {
        val config = WidgetConfig.builder("test-token").build()
        for (message in listOf("a%20b", "50% off")) {
            val url = UrlBuilder.buildWidgetUrl(config, openMessage = message)
            val raw = url.substringAfter("lc_open_message=").substringBefore('&')
            val once = java.net.URLDecoder.decode(raw, "UTF-8")
            assertEquals(message, java.net.URLDecoder.decode(once, "UTF-8"))
        }
    }

    @Test
    fun `the two argument buildWidgetUrl exists for java`() {
        val m = UrlBuilder::class.java.getMethod("buildWidgetUrl", WidgetConfig::class.java, String::class.java)
        val url = m.invoke(UrlBuilder, WidgetConfig.builder("test").build(), "tokX") as String
        assertTrue(url.contains("cw_conversation=tokX"))
    }
}
