package com.sentra.shield.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sentra.shield.data.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiAnalyzer {
    suspend fun analyzeApp(packageName: String, permissions: List<String>): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiKey = "YOUR_GEMINI_API_KEY"
            val prompt = "Analyze this Android app: $packageName with permissions: ${permissions.joinToString()}. Is it suspicious?"

            val partsArray = JsonArray().apply {
                add(JsonObject().apply { addProperty("text", prompt) })
            }
            val contentObj = JsonObject().apply { add("parts", partsArray) }
            val contentsArray = JsonArray().apply { add(contentObj) }
            val body = JsonObject().apply { add("contents", contentsArray) }

            val response = ApiClient.gemini.generateContent(apiKey, body)
            if (response.isSuccessful) {
                "Gemini: ${response.body()}"
            } else {
                "Gemini Error: ${response.code()}"
            }
        } catch (e: Exception) {
            "Gemini Error: ${e.message}"
        }
    }
}
