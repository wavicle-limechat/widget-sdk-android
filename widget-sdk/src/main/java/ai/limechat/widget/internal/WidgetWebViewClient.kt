package ai.limechat.widget.internal

import ai.limechat.widget.WidgetCallback
import ai.limechat.widget.WidgetError
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

internal class WidgetWebViewClient(
    private val context: Context,
    private val baseUrl: () -> String?,
    private val callback: () -> WidgetCallback?,
    private val onPageLoaded: () -> Unit
) : WebViewClient() {

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onPageLoaded()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        super.onReceivedError(view, request, error)
        callback()?.onError(
            WidgetError(
                WidgetError.ErrorCode.WEBVIEW_ERROR,
                "WebView error: ${error.description}",
                context = mapOf("errorCode" to error.errorCode)
            )
        )
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()

        // Only allow loading the widget URL (same scheme + host + port as baseUrl)
        val baseUrl = baseUrl() ?: return false
        if (!isSameOrigin(request.url, baseUrl)) {
            // User link taps open outside the widget, everything else stays blocked
            if (request.isForMainFrame && request.hasGesture()) {
                openExternally(url)
            }
            return true // Block loading
        }

        return false
    }

    /**
     * True if [uri] has the same scheme, host and port as [baseUrl]
     */
    private fun isSameOrigin(uri: Uri, baseUrl: String): Boolean {
        val base = Uri.parse(baseUrl.trimEnd('/'))
        return uri.scheme.equals(base.scheme, ignoreCase = true) &&
            uri.host != null &&
            uri.host.equals(base.host, ignoreCase = true) &&
            effectivePort(uri) == effectivePort(base)
    }

    private fun effectivePort(uri: Uri): Int {
        if (uri.port != -1) return uri.port
        return when (uri.scheme?.lowercase()) {
            "http" -> 80
            "https" -> 443
            else -> -1
        }
    }

    /**
     * Open a url outside the widget (system browser, mail, dialer)
     */
    private fun openExternally(url: String) {
        val uri = Uri.parse(url)
        val intent = when (uri.scheme?.lowercase()) {
            "http", "https", "mailto", "tel" -> Intent(Intent.ACTION_VIEW, uri)
            "intent" -> parseIntentUrl(url) ?: return
            else -> return
        }
        if (context !is android.app.Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val fallback = intent.getStringExtra("browser_fallback_url")
            val fallbackUri = fallback?.let { Uri.parse(it) }
            val fallbackScheme = fallbackUri?.scheme?.lowercase()
            if (uri.scheme.equals("intent", ignoreCase = true) && (fallbackScheme == "http" || fallbackScheme == "https")) {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
                if (context !is android.app.Activity) {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(fallbackIntent)
                    return
                } catch (fallbackError: ActivityNotFoundException) {
                }
            }
            callback()?.onError(
                WidgetError(
                    WidgetError.ErrorCode.WEBVIEW_ERROR,
                    "No app found to open external link",
                    e,
                    mapOf("scheme" to (uri.scheme ?: ""))
                )
            )
        } catch (e: SecurityException) {
            callback()?.onError(
                WidgetError(
                    WidgetError.ErrorCode.WEBVIEW_ERROR,
                    "Not allowed to open external link",
                    e,
                    mapOf("scheme" to (uri.scheme ?: ""))
                )
            )
        }
    }

    /**
     * Parse an `intent:` url into a browsable intent, or null if it can't be parsed
     */
    private fun parseIntentUrl(url: String): Intent? {
        return try {
            Intent.parseUri(url, Intent.URI_INTENT_SCHEME).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                component = null
                selector = null
            }
        } catch (e: Exception) {
            null
        }
    }
}
