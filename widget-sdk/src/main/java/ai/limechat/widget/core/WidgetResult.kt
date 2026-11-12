package ai.limechat.widget.core

/**
 * Result wrapper for widget operations
 * Provides type-safe error handling following functional programming principles
 * 
 * Usage:
 * ```
 * when (val result = widgetOperation()) {
 *     is WidgetResult.Success -> handleSuccess(result.data)
 *     is WidgetResult.Error -> handleError(result.exception)
 * }
 * ```
 */
@Deprecated("Unused by the SDK. Will be removed in the next major version.")
sealed class WidgetResult<out T> {
    
    /**
     * Successful operation result
     */
    data class Success<T>(val data: T) : WidgetResult<T>()
    
    /**
     * Failed operation result
     */
    data class Error(val exception: WidgetException) : WidgetResult<Nothing>()
    
    /**
     * Check if result is successful
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Check if result is error
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Get data if successful, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    /**
     * Get data if successful, throw exception otherwise
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }
    
    /**
     * Transform successful result
     */
    inline fun <R> map(transform: (T) -> R): WidgetResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    /**
     * Execute action on successful result
     */
    inline fun onSuccess(action: (T) -> Unit): WidgetResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Execute action on error result
     */
    inline fun onError(action: (WidgetException) -> Unit): WidgetResult<T> {
        if (this is Error) action(exception)
        return this
    }
    
    companion object {
        /**
         * Create successful result
         */
        fun <T> success(data: T): WidgetResult<T> = Success(data)
        
        /**
         * Create error result
         */
        fun <T> error(exception: WidgetException): WidgetResult<T> = Error(exception)
        
        /**
         * Create error result from throwable
         */
        fun <T> error(throwable: Throwable): WidgetResult<T> = Error(
            when (throwable) {
                is WidgetException -> throwable
                else -> WidgetException.UnknownError("Unexpected error", throwable)
            }
        )
        
        /**
         * Wrap operation in try-catch and return result
         */
        inline fun <T> catching(operation: () -> T): WidgetResult<T> {
            return try {
                success(operation())
            } catch (e: Exception) {
                error(e)
            }
        }
    }
}
