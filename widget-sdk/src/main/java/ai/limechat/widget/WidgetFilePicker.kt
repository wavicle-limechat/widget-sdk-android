package ai.limechat.widget

import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

/**
 * Helper class for handling file uploads in the widget
 */
class WidgetFilePicker(private val activity: FragmentActivity) {
    
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    
    init {
        setupFilePicker()
    }
    
    /**
     * Show file chooser for file uploads
     */
    fun showFileChooser(
        callback: ValueCallback<Array<Uri>>,
        params: WebChromeClient.FileChooserParams
    ) {
        filePathCallback = callback
        
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE)
            
            // Set accepted MIME types if available
            if (params.acceptTypes.isNotEmpty()) {
                val acceptTypes = params.acceptTypes.filter { it.isNotBlank() }
                if (acceptTypes.isNotEmpty()) {
                    if (acceptTypes.size == 1) {
                        type = acceptTypes.first()
                    } else {
                        type = "*/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes.toTypedArray())
                    }
                }
            }
            
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        
        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Select Files"))
        } catch (e: Exception) {
            // If we can't launch the file picker, return null to the callback
            callback.onReceiveValue(null)
        }
    }
    
    private fun setupFilePicker() {
        filePickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val callback = filePathCallback
            filePathCallback = null
            
            if (result.resultCode == FragmentActivity.RESULT_OK) {
                val data = result.data
                val uris = mutableListOf<Uri>()
                
                when {
                    // Multiple files selected
                    data?.clipData != null -> {
                        val clipData = data.clipData!!
                        for (i in 0 until clipData.itemCount) {
                            clipData.getItemAt(i)?.uri?.let { uris.add(it) }
                        }
                    }
                    // Single file selected
                    data?.data != null -> {
                        uris.add(data.data!!)
                    }
                }
                
                callback?.onReceiveValue(uris.toTypedArray())
            } else {
                // User cancelled
                callback?.onReceiveValue(null)
            }
        }
    }
}