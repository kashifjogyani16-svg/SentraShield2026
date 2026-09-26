package com.sentra.shield.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Minimal wrapper around the free VirusTotal Public API v3 `files/{hash}` endpoint.
 * Requires a free API key from https://www.virustotal.com/gui/join-us
 */
interface VirusTotalApi {

    @GET("files/{hash}")
    suspend fun getFileReport(
        @Path("hash") hash: String,
        @Header("x-apikey") apiKey: String
    ): Response<VirusTotalFileReport>

    companion object {
        const val BASE_URL = "https://www.virustotal.com/api/v3/"
    }
}

data class VirusTotalFileReport(
    val data: VirusTotalData?
)

data class VirusTotalData(
    val id: String?,
    val type: String?,
    val attributes: VirusTotalAttributes?
)

data class VirusTotalAttributes(
    val last_analysis_stats: VirusTotalStats?,
    val reputation: Int?,
    val meaningful_name: String?
)

data class VirusTotalStats(
    val malicious: Int = 0,
    val suspicious: Int = 0,
    val undetected: Int = 0,
    val harmless: Int = 0,
    val timeout: Int = 0
)
