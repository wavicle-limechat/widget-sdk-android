package ai.limechat.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.models.WidgetConfigResponse
import ai.limechat.widget.utils.WidgetConfigFetcher
import kotlinx.coroutines.*
import java.net.URL

/**
 * Widget button that displays either a custom button or the default widget icon
 * fetched from the widget_config API
 */
class LimechatWidgetButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "LimechatWidgetButton"
        private const val DEFAULT_ICON_SIZE_DP = 56
        private const val UNREAD_BADGE_SIZE_DP = 20
        private const val UNREAD_BADGE_MARGIN_DP = 4
        
        // SDK Version - this helps distinguish between local and published versions
        const val SDK_VERSION = "0.0.2-published"
        
        // Colors
        private const val DEFAULT_BACKGROUND_COLOR = 0xFF6366F1.toInt() // Purple
        private const val WHITE_COLOR = 0xFFFFFFFF.toInt()
        private const val BADGE_COLOR = 0xFFEF4444.toInt() // Red
        private const val SHADOW_COLOR = 0x40000000 // Semi-transparent black
        
        /**
         * Get the current SDK version
         */
        fun getSDKVersion(): String = SDK_VERSION
    }

    private var config: WidgetConfig? = null
    private var customButton: View? = null
    private var defaultButton: AppCompatImageButton? = null
    private var unreadBadge: TextView? = null
    private var onClickListener: OnClickListener? = null
    private var unreadCount = 0
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val configFetcher = WidgetConfigFetcher()

    init {
        // Set default layout params
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
        }
    }

    /**
     * Initialize the widget button with configuration
     */
    fun init(config: WidgetConfig, customButton: View? = null) {
        Log.d(TAG, "🚀 LimeChat Widget SDK v$SDK_VERSION initializing...")
        this.config = config
        this.customButton = customButton
        
        if (customButton != null) {
            setupCustomButton(customButton)
        } else {
            fetchWidgetConfigAndSetupDefaultButton()
        }
    }

    /**
     * Set the click listener for the widget button
     */
    override fun setOnClickListener(listener: OnClickListener?) {
        this.onClickListener = listener
        // Apply to current button if exists
        customButton?.setOnClickListener(listener)
        defaultButton?.setOnClickListener(listener)
    }

    /**
     * Update the unread count badge
     */
    fun updateUnreadCount(count: Int) {
        unreadCount = count
        updateUnreadBadge()
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        coroutineScope.cancel()
    }

    private fun setupCustomButton(button: View) {
        // Remove any existing views
        removeAllViews()
        
        // Add the custom button
        val buttonLayoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        addView(button, buttonLayoutParams)
        
        // Set click listener
        button.setOnClickListener { view ->
            onClickListener?.onClick(view)
        }
        
        // Add unread badge
        setupUnreadBadge()
    }

    private fun fetchWidgetConfigAndSetupDefaultButton() {
        val config = this.config ?: return
        
        coroutineScope.launch {
            try {
                // Fetch widget config from API
                val response = configFetcher.fetchConfig(config.baseUrl, config.websiteToken)
                
                withContext(Dispatchers.Main) {
                    setupDefaultButton(response)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch widget config", e)
                withContext(Dispatchers.Main) {
                    // Use default icon on error
                    setupDefaultButton(null)
                }
            }
        }
    }

    private fun setupDefaultButton(configResponse: WidgetConfigResponse?) {
        // Remove any existing views
        removeAllViews()
        
        // Create default button
        defaultButton = AppCompatImageButton(context).apply {
            layoutParams = LayoutParams(
                dpToPx(DEFAULT_ICON_SIZE_DP),
                dpToPx(DEFAULT_ICON_SIZE_DP)
            )
            
            // Set circular background
            background = createCircularBackground()
            
            // Remove padding for image to fill the button
            setPadding(0, 0, 0, 0)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            
            // Set click listener
            setOnClickListener { view ->
                onClickListener?.onClick(view)
            }
        }
        
        // Load icon from config or use default
        val iconUrl = configResponse?.config?.widgetIconMobile 
            ?: configResponse?.config?.widgetIconDesktop
        
        if (!iconUrl.isNullOrEmpty()) {
            loadIconFromUrl(iconUrl)
        } else {
            loadDefaultIcon()
        }
        
        addView(defaultButton)
        
        // Add unread badge
        setupUnreadBadge()
    }

    private fun loadIconFromUrl(url: String) {
        coroutineScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val connection = URL(url).openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.getInputStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    defaultButton?.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load icon from URL: $url", e)
                withContext(Dispatchers.Main) {
                    loadDefaultIcon()
                }
            }
        }
    }

    private fun loadDefaultIcon() {
        // Use a default LimeChat icon
        // You'll need to add this icon to the drawable resources
        try {
            val iconResId = resources.getIdentifier(
                "ic_limechat_default",
                "drawable",
                context.packageName
            )
            if (iconResId != 0) {
                defaultButton?.setImageResource(iconResId)
            } else {
                // Fallback to a simple colored circle if no icon found
                defaultButton?.setImageDrawable(createDefaultIconDrawable())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load default icon", e)
            defaultButton?.setImageDrawable(createDefaultIconDrawable())
        }
    }

    private fun createDefaultIconDrawable(): Drawable {
        // Create a simple colored circle as fallback
        val size = dpToPx(DEFAULT_ICON_SIZE_DP)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = DEFAULT_BACKGROUND_COLOR
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        
        // Draw "LC" text
        val textPaint = Paint().apply {
            color = WHITE_COLOR
            textSize = size * 0.3f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("LC", size / 2f, size / 2f + textPaint.textSize / 3, textPaint)
        
        return BitmapDrawable(resources, bitmap)
    }

    private fun createCircularBackground(): Drawable {
        return object : android.graphics.drawable.Drawable() {
            private val paint = Paint().apply {
                color = DEFAULT_BACKGROUND_COLOR
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            private val shadowPaint = Paint().apply {
                color = SHADOW_COLOR
                isAntiAlias = true
                style = Paint.Style.FILL
            }

            override fun draw(canvas: Canvas) {
                val bounds = bounds
                val centerX = bounds.exactCenterX()
                val centerY = bounds.exactCenterY()
                val radius = Math.min(bounds.width(), bounds.height()) / 2f
                
                // Draw shadow
                canvas.drawCircle(centerX, centerY + 2, radius, shadowPaint)
                
                // Draw main circle
                canvas.drawCircle(centerX, centerY, radius, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paint.colorFilter = colorFilter
            }

            override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
        }
    }

    private fun setupUnreadBadge() {
        if (unreadBadge == null) {
            unreadBadge = TextView(context).apply {
                layoutParams = LayoutParams(
                    dpToPx(UNREAD_BADGE_SIZE_DP),
                    dpToPx(UNREAD_BADGE_SIZE_DP)
                ).apply {
                    gravity = Gravity.TOP or Gravity.END
                    setMargins(0, dpToPx(UNREAD_BADGE_MARGIN_DP), dpToPx(UNREAD_BADGE_MARGIN_DP), 0)
                }
                
                gravity = Gravity.CENTER
                setTextColor(WHITE_COLOR)
                textSize = 10f
                background = createBadgeBackground()
                visibility = View.GONE
            }
            addView(unreadBadge)
        }
        
        updateUnreadBadge()
    }

    private fun updateUnreadBadge() {
        unreadBadge?.apply {
            if (unreadCount > 0) {
                text = if (unreadCount > 99) "99+" else unreadCount.toString()
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun createBadgeBackground(): Drawable {
        return object : android.graphics.drawable.Drawable() {
            private val paint = Paint().apply {
                color = BADGE_COLOR
                isAntiAlias = true
            }

            override fun draw(canvas: Canvas) {
                val bounds = bounds
                val radius = Math.min(bounds.width(), bounds.height()) / 2f
                canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), radius, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paint.colorFilter = colorFilter
            }

            override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}