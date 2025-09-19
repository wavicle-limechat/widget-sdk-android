package com.example.localsdkdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity

// Import from LOCAL widget-sdk module
import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.WidgetCallback
import ai.limechat.widget.WidgetError
import ai.limechat.widget.WidgetFilePicker
import ai.limechat.widget.models.WidgetConfig

/**
 * Full-screen widget activity using LOCAL SDK
 */
class WidgetActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "LocalWidgetActivity"
        private const val PREFS_NAME = "limechat_local_sdk_demo"
        private const val PREF_KEY_CONVERSATION_PREFIX = "cw_conversation_"

        const val EXTRA_INSTANCE_ID = "conversation_instance_id"
        const val EXTRA_MANAGE_TOKEN = "manage_conversation_token"

        const val INSTANCE_ID_PRIMARY = "primary_fullscreen"
        const val INSTANCE_ID_FLOATING = "floating_fullscreen"

        private val WIDGET_VIEW_ID = View.generateViewId()
    }
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    private val conversationPrefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "🚀 Opening widget with LOCAL SDK")
        
        // Create widget view
        widgetView = LimechatWidgetView(this).apply {
            id = WIDGET_VIEW_ID
        }
        setContentView(widgetView)
        
        // Set up file picker for file uploads
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // Get configuration from intent
        val websiteToken = intent.getStringExtra("website_token") ?: "MEFFACy4xaovJayhLjSt836h"
        val userName = intent.getStringExtra("user_name") ?: "Demo User"
        val userEmail = intent.getStringExtra("user_email") ?: "demo@local.com"
        val instanceId = intent.getStringExtra(EXTRA_INSTANCE_ID) ?: INSTANCE_ID_PRIMARY
        val shouldManageConversationToken = intent.getBooleanExtra(EXTRA_MANAGE_TOKEN, true)
        
        // Create configuration
        val config = WidgetConfig(
            websiteToken = websiteToken,
            baseUrl = "https://app.limechat.ai",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = WidgetConfig.User(
                name = userName,
                email = userEmail,
                phoneNumber = "+1234567890"
            ),
            customAttributes = mapOf(
                "source" to "local_sdk_demo",
                "type" to "local_module_dependency"
            )
        )
        
        // Check for custom messages and initialize accordingly
        val customMessage = intent.getStringExtra("custom_message")
        @Suppress("UNCHECKED_CAST")
        val customMessageData = intent.getSerializableExtra("custom_message_data") as? Map<String, Any>
        
        val storedConversationToken = if (shouldManageConversationToken) {
            getStoredConversationToken(websiteToken, instanceId).also {
                Log.d(TAG, "Current stored cw_conversation token for $instanceId: ${previewToken(it)}")
            }
        } else {
            Log.d(TAG, "Widget launched without host-managed conversation token for $instanceId")
            null
        }

        val conversationOptions = if (shouldManageConversationToken) {
            LimechatWidgetView.ConversationOptions(
                token = storedConversationToken,
                onTokenChange = LimechatWidgetView.ConversationTokenListener { newToken ->
                    Log.d(TAG, "🔄 Received cw_conversation token update for $instanceId: ${previewToken(newToken)}")
                    storeConversationToken(websiteToken, instanceId, newToken)
                }
            )
        } else {
            null
        }

        val initOptions = when {
            customMessage != null -> {
                Log.d(TAG, "🗨️ Initializing widget with custom string message: $customMessage")
                LimechatWidgetView.InitOptions(
                    initialMessage = customMessage,
                    conversationOptions = conversationOptions,
                    conversationInstanceId = instanceId
                )
            }
            customMessageData != null -> {
                Log.d(TAG, "🗨️ Initializing widget with custom message data: $customMessageData")
                LimechatWidgetView.InitOptions(
                    initialMessageData = customMessageData,
                    conversationOptions = conversationOptions,
                    conversationInstanceId = instanceId
                )
            }
            else -> {
                Log.d(TAG, "Initializing widget without custom message")
                LimechatWidgetView.InitOptions(
                    conversationOptions = conversationOptions,
                    conversationInstanceId = instanceId
                )
            }
        }

        widgetView.init(config, createWidgetCallback(), initOptions)

        Log.d(TAG, "✅ Widget initialized with local SDK")
    }
    
    private fun createWidgetCallback(): WidgetCallback {
        return object : WidgetCallback {
            override fun onLoaded() {
                Log.d(TAG, "✅ Local SDK widget loaded successfully")
                // No need to handle custom messages here anymore - they're in the URL
            }
            
            override fun onClose() {
                Log.d(TAG, "🚪 Widget close requested")
                finish()
            }
            
            override fun onError(error: WidgetError) {
                Log.e(TAG, "❌ Widget error: ${error.message}")
            }
            
            override fun onMessage(message: Map<String, Any?>) {
                Log.d(TAG, "📨 Widget message: $message")
            }
        }
    }
    
    
    override fun onBackPressed() {
        // Let widget handle back first
        if (!widgetView.onBackPressed()) {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (this::widgetView.isInitialized) {
            widgetView.destroy()
        }
        Log.d(TAG, "🧹 Local SDK widget cleaned up")
    }

    private fun getStoredConversationToken(websiteToken: String, instanceId: String): String? {
        return conversationPrefs.getString(conversationKey(websiteToken, instanceId), null)
    }

    private fun storeConversationToken(websiteToken: String, instanceId: String, token: String) {
        conversationPrefs.edit().putString(conversationKey(websiteToken, instanceId), token).apply()
    }

    private fun conversationKey(websiteToken: String, instanceId: String): String =
        "$PREF_KEY_CONVERSATION_PREFIX${instanceId}_$websiteToken"

    private fun previewToken(token: String?): String = token?.take(8)?.let { "$it..." } ?: "none"
}
