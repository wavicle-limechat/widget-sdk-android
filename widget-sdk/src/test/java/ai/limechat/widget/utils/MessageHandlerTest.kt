package ai.limechat.widget.utils

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MessageHandlerTest {

    private lateinit var messageHandler: MessageHandler

    @Before
    fun setup() {
        messageHandler = MessageHandler()
    }

    @Test
    fun `test process message with prefix`() {
        val message = """limechat-widget:{"event":"loaded","status":"success"}"""
        val result = messageHandler.processMessage(message)

        assertNotNull(result)
        assertEquals("loaded", result?.get("event"))
        assertEquals("success", result?.get("status"))
    }

    @Test
    fun `test process message without prefix`() {
        val message = """{"event":"close-widget","reason":"user"}"""
        val result = messageHandler.processMessage(message)

        assertNotNull(result)
        assertEquals("close-widget", result?.get("event"))
        assertEquals("user", result?.get("reason"))
    }

    @Test
    fun `test process invalid json message`() {
        val message = """limechat-widget:invalid-json"""
        val result = messageHandler.processMessage(message)

        assertNull(result)
    }

    @Test
    fun `test process empty message`() {
        val result = messageHandler.processMessage("")
        assertNull(result)
    }

    @Test
    fun `test process message with nested objects`() {
        val message = """limechat-widget:{"event":"set-user","user":{"name":"John","email":"john@example.com"},"metadata":{"timestamp":1234567890}}"""
        val result = messageHandler.processMessage(message)

        assertNotNull(result)
        assertEquals("set-user", result?.get("event"))
        
        val user = result?.get("user") as? Map<*, *>
        assertNotNull(user)
        assertEquals("John", user?.get("name"))
        assertEquals("john@example.com", user?.get("email"))

        val metadata = result?.get("metadata") as? Map<*, *>
        assertNotNull(metadata)
        assertEquals(1234567890, metadata?.get("timestamp"))
    }

    @Test
    fun `test process message with null values`() {
        val message = """limechat-widget:{"event":"test","value":null,"number":42}"""
        val result = messageHandler.processMessage(message)

        assertNotNull(result)
        assertEquals("test", result?.get("event"))
        assertNull(result?.get("value"))
        assertEquals(42, result?.get("number"))
    }

    @Test
    fun `test process message with special characters`() {
        val message = """limechat-widget:{"event":"message","text":"Hello \"World\"!","emoji":"😊"}"""
        val result = messageHandler.processMessage(message)

        assertNotNull(result)
        assertEquals("message", result?.get("event"))
        assertEquals("Hello \"World\"!", result?.get("text"))
        assertEquals("😊", result?.get("emoji"))
    }
}