package ai.limechat.widget.internal

import ai.limechat.widget.WidgetCallback
import android.app.Activity
import android.content.Context
import android.util.Log

/**
 * Close behaviour kept for hosts on v0.0.9-alpha: with the legacy back icon on and an Activity
 * context the activity finishes and onClose is NOT called. Otherwise (or if finish() throws) onClose is called.
 */
internal object CloseHandler {
    fun handle(context: Context, showLegacyBackIcon: Boolean, callback: WidgetCallback?) {
        if (!showLegacyBackIcon) {
            callback?.onClose()
            return
        }
        try {
            val activity = context as? Activity
            if (activity != null) {
                activity.finish()
            } else {
                callback?.onClose()
            }
        } catch (e: Exception) {
            Log.e("LimechatWidgetView", "Failed to auto-close activity: ${e.message}")
            callback?.onClose()
        }
    }
}
