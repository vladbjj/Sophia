package com.sophia.assistant.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sophia_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_PITCH = "tts_pitch"
        private const val KEY_RATE = "tts_rate"
    }

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var speechPitch: Float
        get() = prefs.getFloat(KEY_PITCH, 1.05f) // levemente mais agudo/suave feminino
        set(value) = prefs.edit().putFloat(KEY_PITCH, value).apply()

    var speechRate: Float
        get() = prefs.getFloat(KEY_RATE, 1.02f) // cadência natural
        set(value) = prefs.edit().putFloat(KEY_RATE, value).apply()
}
