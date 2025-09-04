package ai.limechat.widget.button

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
 * Handles the visual rendering and styling of the widget button
 */
class WidgetButtonRenderer(private val context: Context) {
    
    companion object {
        private const val TAG = "WidgetButtonRenderer"
        private const val DEFAULT_ICON_SIZE_DP = 56
        private const val UNREAD_BADGE_SIZE_DP = 20
        private const val UNREAD_BADGE_MARGIN_DP = 4
        
        // Colors
        private const val DEFAULT_BACKGROUND_COLOR = 0xFF6366F1.toInt() // Purple
        private const val WHITE_COLOR = 0xFFFFFFFF.toInt()
        private const val BADGE_COLOR = 0xFFEF4444.toInt() // Red
        private const val SHADOW_COLOR = 0x40000000 // Semi-transparent black
    }

    private val configFetcher = WidgetConfigFetcher()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Create a custom button view
     */
    fun createCustomButton(customView: View, parent: FrameLayout): View {
        val buttonLayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        parent.addView(customView, buttonLayoutParams)
        return customView
    }

    /**
     * Create the default widget button
     */
    fun createDefaultButton(
        config: WidgetConfig,
        parent: FrameLayout,
        onConfigLoaded: (AppCompatImageButton) -> Unit
    ): AppCompatImageButton {
        val button = AppCompatImageButton(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                dpToPx(DEFAULT_ICON_SIZE_DP),
                dpToPx(DEFAULT_ICON_SIZE_DP)
            )
            
            background = createCircularBackground()
            setPadding(0, 0, 0, 0)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        
        parent.addView(button)
        loadButtonIcon(config, button, onConfigLoaded)
        return button
    }

    /**
     * Create and setup the unread badge
     */
    fun createUnreadBadge(parent: FrameLayout): TextView {
        return TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
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
        }.also { badge ->
            parent.addView(badge)
        }
    }

    /**
     * Update the unread badge with count
     */
    fun updateUnreadBadge(badge: TextView, count: Int) {
        badge.apply {
            if (count > 0) {
                text = if (count > 99) "99+" else count.toString()
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        coroutineScope.cancel()
    }

    // Private implementation methods

    private fun loadButtonIcon(
        config: WidgetConfig,
        button: AppCompatImageButton,
        onConfigLoaded: (AppCompatImageButton) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val response = configFetcher.fetchConfig(config.baseUrl, config.websiteToken)
                withContext(Dispatchers.Main) {
                    setupButtonIcon(button, response)
                    onConfigLoaded(button)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch widget config", e)
                withContext(Dispatchers.Main) {
                    loadDefaultIcon(button)
                    onConfigLoaded(button)
                }
            }
        }
    }

    private fun setupButtonIcon(button: AppCompatImageButton, configResponse: WidgetConfigResponse?) {
        val iconUrl = configResponse?.config?.widgetIconMobile 
            ?: configResponse?.config?.widgetIconDesktop
        
        if (!iconUrl.isNullOrEmpty()) {
            loadIconFromUrl(button, iconUrl)
        } else {
            loadDefaultIcon(button)
        }
    }

    private fun loadIconFromUrl(button: AppCompatImageButton, url: String) {
        coroutineScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val connection = URL(url).openConnection().apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                    }
                    connection.getInputStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    button.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load icon from URL: $url", e)
                withContext(Dispatchers.Main) {
                    loadDefaultIcon(button)
                }
            }
        }
    }

    private fun loadDefaultIcon(button: AppCompatImageButton) {
        try {
            val iconResId = context.resources.getIdentifier(
                "ic_limechat_default",
                "drawable",
                context.packageName
            )
            
            if (iconResId != 0) {
                button.setImageResource(iconResId)
            } else {
                button.setImageDrawable(createDefaultIconDrawable())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load default icon", e)
            button.setImageDrawable(createDefaultIconDrawable())
        }
    }

    private fun createDefaultIconDrawable(): Drawable {
        val size = dpToPx(DEFAULT_ICON_SIZE_DP)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw background circle
        val backgroundPaint = Paint().apply {
            color = DEFAULT_BACKGROUND_COLOR
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint)
        
        // Draw "LC" text
        val textPaint = Paint().apply {
            color = WHITE_COLOR
            textSize = size * 0.3f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("LC", size / 2f, size / 2f + textPaint.textSize / 3, textPaint)
        
        return BitmapDrawable(context.resources, bitmap)
    }

    private fun createCircularBackground(): Drawable {
        return object : Drawable() {
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
                val radius = minOf(bounds.width(), bounds.height()) / 2f
                
                // Draw shadow
                canvas.drawCircle(centerX, centerY + 2, radius, shadowPaint)
                
                // Draw main circle
                canvas.drawCircle(centerX, centerY, radius, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: ColorFilter?) {
                paint.colorFilter = colorFilter
            }

            override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
        }
    }

    private fun createBadgeBackground(): Drawable {
        return object : Drawable() {
            private val paint = Paint().apply {
                color = BADGE_COLOR
                isAntiAlias = true
            }

            override fun draw(canvas: Canvas) {
                val bounds = bounds
                val radius = minOf(bounds.width(), bounds.height()) / 2f
                canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), radius, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: ColorFilter?) {
                paint.colorFilter = colorFilter
            }

            override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}