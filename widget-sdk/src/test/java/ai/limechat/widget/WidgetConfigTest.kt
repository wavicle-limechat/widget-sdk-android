package ai.limechat.widget

import ai.limechat.widget.models.WidgetConfig
import org.junit.Assert.*
import org.junit.Test

class WidgetConfigTest {

    @Test
    fun `test widget config creation with required fields`() {
        val config = WidgetConfig.builder("test-token").build()
        
        assertEquals("test-token", config.websiteToken)
        assertEquals("en", config.locale)
        assertEquals(WidgetConfig.ColorScheme.LIGHT, config.colorScheme)
        assertNull(config.user)
        assertNull(config.customAttributes)
        assertEquals("https://app.limechat.ai", config.baseUrl)
        assertFalse(config.showLegacyBackIcon)
    }

    @Test
    fun `test widget config creation with all fields`() {
        val user = WidgetConfig.User(
            name = "John Doe",
            email = "john@example.com",
            phoneNumber = "+1234567890",
            identifierHash = "hash123"
        )

        val customAttributes = mapOf(
            "department" to "support",
            "priority" to "high"
        )

        val config = WidgetConfig.builder("test-token")
            .setLocale("es")
            .setColorScheme(WidgetConfig.ColorScheme.DARK)
            .setUser(user)
            .setCustomAttributes(customAttributes)
            .setBaseUrl("https://custom.example.com")
            .build()

        assertEquals("test-token", config.websiteToken)
        assertEquals("es", config.locale)
        assertEquals(WidgetConfig.ColorScheme.DARK, config.colorScheme)
        assertEquals(user, config.user)
        assertEquals(customAttributes, config.customAttributes)
        assertEquals("https://custom.example.com", config.baseUrl)
    }

    @Test(expected = ai.limechat.widget.core.WidgetException.ConfigurationError::class)
    fun `test widget config with empty token throws exception`() {
        WidgetConfig.builder("").build()
    }

    @Test(expected = ai.limechat.widget.core.WidgetException.ConfigurationError::class)
    fun `test widget config with blank token throws exception`() {
        WidgetConfig.builder("   ").build()
    }

    @Test
    fun `test user creation with partial fields`() {
        val user = WidgetConfig.User(
            name = "Jane Doe",
            email = "jane@example.com"
        )

        assertEquals("Jane Doe", user.name)
        assertEquals("jane@example.com", user.email)
        assertNull(user.phoneNumber)
        assertNull(user.identifierHash)
    }

    @Test
    fun `test color scheme enum values`() {
        assertEquals(3, WidgetConfig.ColorScheme.values().size)
        assertTrue(WidgetConfig.ColorScheme.values().contains(WidgetConfig.ColorScheme.LIGHT))
        assertTrue(WidgetConfig.ColorScheme.values().contains(WidgetConfig.ColorScheme.DARK))
        assertTrue(WidgetConfig.ColorScheme.values().contains(WidgetConfig.ColorScheme.AUTO))
    }
}