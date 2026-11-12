package ai.limechat.widget.internal

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

internal object WebViewFactory {
    private const val USER_AGENT_PREFIX = "LimechatWidget/Android"

    @SuppressLint("SetJavaScriptEnabled")
    fun create(context: Context, client: WebViewClient, chromeClient: WebChromeClient): WebView {
        return WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true

                allowFileAccess = false
                allowContentAccess = false
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false

                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = true
                }

                userAgentString = "$USER_AGENT_PREFIX $userAgentString"

                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }

            webViewClient = client
            webChromeClient = chromeClient
        }
    }
}
