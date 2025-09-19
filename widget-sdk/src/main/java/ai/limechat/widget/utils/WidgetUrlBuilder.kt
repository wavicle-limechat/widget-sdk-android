package ai.limechat.widget.utils

import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.models.WidgetMessage
import kotlin.jvm.Volatile

/**
 * Responsible for building widget URLs with proper parameters
 */
class WidgetUrlBuilder(private val config: WidgetConfig) {

    @Volatile
    private var conversationToken: String? = config.conversationToken?.takeUnless { it.isBlank() }

    /** Update the conversation token used when generating URLs. */
    fun updateConversationToken(token: String?) {
        conversationToken = token?.takeUnless { it.isBlank() }
    }

    /** Build the base widget URL including configuration parameters. */
    fun buildBaseUrl(): String {
        return UrlBuilder.buildWidgetUrl(config.copy(conversationToken = conversationToken))
    }

    /** Build URL with initial message payload appended. */
    fun buildUrlWithMessage(message: WidgetMessage): String {
        val baseUrl = buildBaseUrl()
        val params = message.toQueryParameters()
        return UrlBuilder.appendQueryParameters(baseUrl, params)
    }

    /** Build URL with additional query parameters appended. */
    fun buildUrlWithParams(params: Map<String, String>): String {
        val sanitized = params.filterValues { it.isNotBlank() }
        if (sanitized.isEmpty()) return buildBaseUrl()
        return UrlBuilder.appendQueryParameters(buildBaseUrl(), sanitized)
    }
}
