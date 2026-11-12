package ai.limechat.widget.internal

import ai.limechat.widget.WidgetFilePicker
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

internal class WidgetChromeClient(
    private val filePicker: () -> WidgetFilePicker?
) : WebChromeClient() {

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        val picker = filePicker()
        if (picker == null) {
            // No picker attached: cancel so the page's file input isn't stuck
            filePathCallback.onReceiveValue(null)
            return true
        }
        picker.showFileChooser(filePathCallback, fileChooserParams)
        return true
    }
}
