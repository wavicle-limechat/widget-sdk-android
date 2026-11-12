package ai.limechat.widget.internal

import ai.limechat.widget.WidgetCallback
import android.app.Activity
import android.content.Context
import android.util.Log

/**
 * Same as v0.0.9: close always calls onClose when a callback is set. Only when there is no callback
 * and the legacy back icon is on do we fall back to finishing the host activity.
 */
internal object CloseHandler {
    fun handle(context: Context, showLegacyBackIcon: Boolean, callback: WidgetCallback?) {
        if (callback != null || !showLegacyBackIcon) {
            callback?.onClose()
            return
        }
        try {
            (context as? Activity)?.finish()
        } catch (e: Exception) {
            Log.e("LimechatWidgetView", "Failed to auto-close activity: ${e.message}")
        }
    }
}
