package com.sentra.shield.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Minimal wrapper around the free-tier Gemini API (Google AI Studio key) used to
 * turn a raw threat-score breakdown into a short plain-English explanation.
 * Get a free key at https://aistudio.google.com/app/apikey
 */
interface GeminiApi {

    @POST
    suspend fun generateContent(
        @Url url: String = "$BASE_URL/v1beta/models/gemini-1.5-flash:generateContent",
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): Response<GeminiResponse>

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com"
    }
}

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)
