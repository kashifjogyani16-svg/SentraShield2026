package com.sentra.shield.data.api

import com.sentra.shield.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central place that builds Retrofit clients for optional third-party integrations.
 * Both [virusTotal] and [gemini] work without a key configured; callers should
 * check [hasVirusTotalKey] / [hasGeminiKey] first and skip the call gracefully
 * if the user hasn't set one up (see README "Setup").
 */
object ApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val hasVirusTotalKey: Boolean
        get() = BuildConfig.VIRUSTOTAL_API_KEY.isNotBlank()

    val hasGeminiKey: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    val virusTotal: VirusTotalApi by lazy {
        Retrofit.Builder()
            .baseUrl(VirusTotalApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VirusTotalApi::class.java)
    }

    val gemini: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(GeminiApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}
