package ai.limechat.widget.models

/**
 * Represents the current state of the widget
 */
data class WidgetState(
    val isInitialized: Boolean = false,
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val unreadCount: Int = 0,
    val isMessagingSetup: Boolean = false
) {
    val isReady: Boolean
        get() = isInitialized && isLoaded && !hasError

    fun loading(): WidgetState = copy(isLoading = true, hasError = false, errorMessage = null)
    
    fun loaded(): WidgetState = copy(isLoading = false, isLoaded = true, hasError = false, errorMessage = null)
    
    fun error(message: String): WidgetState = copy(isLoading = false, hasError = true, errorMessage = message)
    
    fun updateUnreadCount(count: Int): WidgetState = copy(unreadCount = count)
    
    fun messagingSetup(): WidgetState = copy(isMessagingSetup = true)
}