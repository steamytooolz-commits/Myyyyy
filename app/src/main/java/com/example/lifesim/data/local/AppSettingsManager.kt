package com.example.lifesim.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.lifesim.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lifesim_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LLM_API_KEY = "llm_api_key"
        private const val KEY_LLM_BASE_URL = "llm_base_url"
        private const val KEY_LLM_MODEL = "llm_model"
        private const val KEY_TEMPERATURE = "llm_temperature"
        private const val KEY_MAX_TOKENS = "llm_max_tokens"
        private const val KEY_USE_CUSTOM_API = "llm_use_custom_api"
    }

    fun getApiKey(): String {
        return prefs.getString(KEY_LLM_API_KEY, "") ?: ""
    }

    fun setApiKey(value: String) {
        prefs.edit().putString(KEY_LLM_API_KEY, value).apply()
    }

    fun getBaseUrl(): String {
        val url = prefs.getString(KEY_LLM_BASE_URL, Constants.LLM_API_BASE_URL) ?: Constants.LLM_API_BASE_URL
        return if (url.endsWith("/")) url else "$url/"
    }

    fun setBaseUrl(value: String) {
        var url = value.trim()
        if (url.isNotEmpty() && !url.endsWith("/")) {
            url = "$url/"
        }
        prefs.edit().putString(KEY_LLM_BASE_URL, url).apply()
    }

    fun getModel(): String {
        return prefs.getString(KEY_LLM_MODEL, Constants.LLM_MODEL) ?: Constants.LLM_MODEL
    }

    fun setModel(value: String) {
        prefs.edit().putString(KEY_LLM_MODEL, value.trim()).apply()
    }

    fun getTemperature(): Float {
        return prefs.getFloat(KEY_TEMPERATURE, 0.8f)
    }

    fun setTemperature(value: Float) {
        prefs.edit().putFloat(KEY_TEMPERATURE, value).apply()
    }

    fun getMaxTokens(): Int {
        return prefs.getInt(KEY_MAX_TOKENS, Constants.MAX_TOKENS)
    }

    fun setMaxTokens(value: Int) {
        prefs.edit().putInt(KEY_MAX_TOKENS, value).apply()
    }

    fun getUseCustomApi(): Boolean {
        return prefs.getBoolean(KEY_USE_CUSTOM_API, false)
    }

    fun setUseCustomApi(value: Boolean) {
        prefs.edit().putBoolean(KEY_USE_CUSTOM_API, value).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
