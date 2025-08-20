package ai.limechat.widget.utils

import android.util.Log
import ai.limechat.widget.models.WidgetConfigResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Fetches widget configuration from the API
 */
class WidgetConfigFetcher {
    
    companion object {
        private const val TAG = "WidgetConfigFetcher"
        private const val TIMEOUT_MS = 10000
    }

    /**
     * Fetch widget configuration from the API
     */
    suspend fun fetchConfig(baseUrl: String, websiteToken: String): WidgetConfigResponse {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            
            try {
                val encodedToken = URLEncoder.encode(websiteToken, "UTF-8")
                val url = URL("$baseUrl/widget_config?website_token=$encodedToken")
                
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "LimechatWidget/Android")
                }
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    
                    Log.d(TAG, "Widget config fetched successfully")
                    return@withContext WidgetConfigResponse.fromJson(json)
                } else {
                    val errorMessage = try {
                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    } catch (e: Exception) {
                        ""
                    }
                    
                    throw WidgetConfigFetchException(
                        "Failed to fetch widget config. HTTP $responseCode: $errorMessage",
                        responseCode
                    )
                }
            } catch (e: Exception) {
                when (e) {
                    is WidgetConfigFetchException -> throw e
                    else -> {
                        Log.e(TAG, "Error fetching widget config", e)
                        throw WidgetConfigFetchException(
                            "Network error while fetching widget config: ${e.message}",
                            -1,
                            e
                        )
                    }
                }
            } finally {
                connection?.disconnect()
            }
        }
    }
}

/**
 * Exception thrown when widget config fetch fails
 */
class WidgetConfigFetchException(
    message: String,
    val httpCode: Int,
    cause: Throwable? = null
) : Exception(message, cause)