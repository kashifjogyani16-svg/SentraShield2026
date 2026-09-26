package com.sentra.shield.util

import android.content.Context

object ApiKeyManager {
    private const val PREFS = "sentra_prefs"
    private const val KEY_VT = "virustotal_api_key"
    private const val KEY_GEMINI = "gemini_api_key"

    fun saveVirusTotalKey(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_VT, key).apply()
    }

    fun getVirusTotalKey(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_VT, "") ?: ""
    }

    fun saveGeminiKey(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_GEMINI, key).apply()
    }

    fun getGeminiKey(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_GEMINI, "") ?: ""
    }
}
