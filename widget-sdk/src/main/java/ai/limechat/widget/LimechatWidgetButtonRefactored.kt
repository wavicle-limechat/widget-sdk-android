package ai.limechat.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import ai.limechat.widget.button.WidgetButtonRenderer
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.models.WidgetMessage

/**
 * Refactored widget button with improved architecture
 * 
 * Key improvements:
 * - Separation of rendering logic
 * - Better state management
 * - Cleaner API design
 * - Proper resource management
 * - Enhanced error handling
 */
class LimechatWidgetButtonRefactored @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "LimechatWidgetButton"
        const val SDK_VERSION = "1.0.0-refactored"
        
        fun getSDKVersion(): String = SDK_VERSION
    }

    // Core components
    private val renderer = WidgetButtonRenderer(context)
    
    // UI components
    private var currentButton: View? = null
    private var unreadBadge: TextView? = null
    
    // Configuration and state
    private var config: WidgetConfig? = null
    private var customButton: View? = null
    private var widgetView: LimechatWidgetViewRefactored? = null
    private var clickListener: OnClickListener? = null
    private var unreadCount = 0
    private var isInitialized = false

    init {
        setupDefaultLayoutParams()
    }

    /**
     * Initialize the widget button with configuration
     */
    fun initialize(config: WidgetConfig, customButton: View? = null) {
        if (isInitialized) {
            Log.w(TAG, "Button already initialized")
            return
        }

        try {
            this.config = config
            this.customButton = customButton
            
            setupButton()
            isInitialized = true
            
            Log.d(TAG, "Widget button initialized successfully with SDK v$SDK_VERSION")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize widget button", e)
        }
    }

    /**
     * Set the associated widget view for advanced interactions
     */
    fun setWidgetView(widgetView: LimechatWidgetViewRefactored) {
        this.widgetView = widgetView
    }

    /**
     * Set click listener for the button
     */
    override fun setOnClickListener(listener: OnClickListener?) {
        this.clickListener = listener
        currentButton?.setOnClickListener { view ->
            listener?.onClick(view)
        }
    }

    /**
     * Open the widget with an optional custom message
     */
    fun openWidget(message: WidgetMessage? = null) {
        val widgetView = this.widgetView
        if (widgetView == null) {
            Log.w(TAG, "WidgetView not set. Call setWidgetView() first for advanced functionality")
            return
        }

        if (!widgetView.isReady()) {
            Log.w(TAG, "Widget not ready, cannot open")
            return
        }

        try {
            if (message != null) {
                Log.d(TAG, "Opening widget with message: ${message.getMessageContent()}")
                widgetView.openWithMessage(message)
            } else {
                Log.d(TAG, "Opening widget without message")
                // Trigger regular click behavior
                clickListener?.onClick(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open widget", e)
        }
    }

    /**
     * Update the unread count badge
     */
    fun updateUnreadCount(count: Int) {
        if (count < 0) {
            Log.w(TAG, "Invalid unread count: $count")
            return
        }

        this.unreadCount = count
        unreadBadge?.let { badge ->
            renderer.updateUnreadBadge(badge, count)
        }
        
        Log.d(TAG, "Unread count updated to: $count")
    }

    /**
     * Get current unread count
     */
    fun getUnreadCount(): Int = unreadCount

    /**
     * Check if button is initialized
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            renderer.cleanup()
            
            // Clear references
            currentButton = null
            unreadBadge = null
            config = null
            customButton = null
            widgetView = null
            clickListener = null
            
            isInitialized = false
            Log.d(TAG, "Widget button cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    // Private implementation methods

    private fun setupDefaultLayoutParams() {
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
        }
    }

    private fun setupButton() {
        removeAllViews()
        
        if (customButton != null) {
            setupCustomButton()
        } else {
            setupDefaultButton()
        }
        
        setupUnreadBadge()
    }

    private fun setupCustomButton() {
        val customView = customButton ?: return
        
        currentButton = renderer.createCustomButton(customView, this)
        currentButton?.setOnClickListener { view ->
            clickListener?.onClick(view)
        }
    }

    private fun setupDefaultButton() {
        val config = this.config ?: return
        
        val button = renderer.createDefaultButton(config, this) { createdButton ->
            currentButton = createdButton
            currentButton?.setOnClickListener { view ->
                clickListener?.onClick(view)
            }
        }
        
        currentButton = button
    }

    private fun setupUnreadBadge() {
        unreadBadge = renderer.createUnreadBadge(this)
        
        // Apply current unread count if any
        if (unreadCount > 0) {
            updateUnreadCount(unreadCount)
        }
    }
}