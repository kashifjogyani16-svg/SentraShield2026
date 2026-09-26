package com.sentra.shield.util

import com.sentra.shield.data.api.ApiClient
import com.sentra.shield.data.api.VirusTotalApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VirusTotalChecker {
    suspend fun checkFileHash(hash: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiKey = "YOUR_VIRUSTOTAL_API_KEY" // Replace with BuildConfig.VIRUSTOTAL_API_KEY if configured
            val response = ApiClient.virusTotal.getFileReport(hash, apiKey)
            if (response.isSuccessful) {
                "File report: ${response.body()}"
            } else {
                "Error: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
