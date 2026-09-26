package com.sentra.shield.util

import com.sentra.shield.data.api.ApiClient
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiAnalyzer {
    suspend fun analyzeApp(packageName: String, permissions: List<String>): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiKey = "YOUR_GEMINI_API_KEY" // Replace with actual key
            val prompt = "Analyze this Android app for security risks. Package: $packageName, Permissions: ${permissions.joinToString()}. Is it suspicious? Answer in 2 lines."
            
            val body = JsonObject().apply {
                add("contents", com.google.gson.JsonArray().apply {
                    add(JsonObject().apply {
                        add("parts", com.google.gson.JsonArray().apply {
                            add(JsonObject().apply {
                                addProperty("text", prompt)
                            })
                        })
                    })
                })
            }
            
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
