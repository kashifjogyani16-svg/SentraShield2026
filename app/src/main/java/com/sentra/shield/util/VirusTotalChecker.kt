package com.sentra.shield.util

import android.content.Context
import com.sentra.shield.data.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VirusTotalChecker {
    suspend fun checkFileHash(context: Context, hash: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiKey = ApiKeyManager.getVirusTotalKey(context)
            if (apiKey.isBlank()) {
                return@withContext "❌ VirusTotal API key set nahi hai. Settings mein jaakar daalein."
            }
            val response = ApiClient.virusTotal.getFileReport(hash, apiKey)
            if (response.isSuccessful) {
                "✅ File report: ${response.body()}"
            } else {
                "❌ Error: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            "❌ Error: ${e.message}"
        }
    }
}
