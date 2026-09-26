package com.sentra.shield.data.api

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface VirusTotalApi {
    @GET("files/{hash}")
    suspend fun getFileReport(
        @Path("hash") hash: String,
        @Header("x-apikey") apiKey: String
    ): Response<JsonObject>
}
