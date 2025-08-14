package ai.limechat.widget

import ai.limechat.widget.models.WidgetConfig
import org.junit.Assert.*
import org.junit.Test

class WidgetConfigTest {

    @Test
    fun `test widget config creation with required fields`() {
        val config = WidgetConfig(websiteToken = "test-token")
        
        assertEquals("test-token", config.websiteToken)
        assertEquals("en", config.locale)
        assertEquals(WidgetConfig.ColorScheme.LIGHT, config.colorScheme)
        assertNull(config.user)
        assertNull(config.customAttributes)
        assertEquals("https://app.limechat.ai", config.baseUrl)
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

        val config = WidgetConfig(
            websiteToken = "test-token",
            locale = "es",
            colorScheme = WidgetConfig.ColorScheme.DARK,
            user = user,
            customAttributes = customAttributes,
            baseUrl = "https://custom.example.com"
        )

        assertEquals("test-token", config.websiteToken)
        assertEquals("es", config.locale)
        assertEquals(WidgetConfig.ColorScheme.DARK, config.colorScheme)
        assertEquals(user, config.user)
        assertEquals(customAttributes, config.customAttributes)
        assertEquals("https://custom.example.com", config.baseUrl)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test widget config with empty token throws exception`() {
        WidgetConfig(websiteToken = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test widget config with blank token throws exception`() {
        WidgetConfig(websiteToken = "   ")
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