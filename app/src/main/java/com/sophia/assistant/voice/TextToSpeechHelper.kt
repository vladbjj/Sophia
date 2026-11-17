package com.sophia.assistant.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TextToSpeechHelper(
    private val context: Context,
    private val onInitComplete: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ptBr = Locale("pt", "BR")
            val langResult = tts?.setLanguage(ptBr)

            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = false
                onInitComplete(false)
                return
            }

            // Seleciona voz feminina em pt-BR se disponível
            configureFemaleVoice(ptBr)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })

            isInitialized = true
            onInitComplete(true)
        } else {
            isInitialized = false
            onInitComplete(false)
        }
    }

    private fun configureFemaleVoice(ptBr: Locale) {
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                // Procura voz feminina em português (ex: Google pt-br-x-afs, pt-br-x-sfg ou com tag female)
                val femaleVoice: Voice? = voices.firstOrNull { voice ->
                    val isPt = voice.locale.language.equals(ptBr.language, ignoreCase = true)
                    val isFemale = voice.name.contains("female", ignoreCase = true) ||
                            voice.name.contains("pt-br-x-afs", ignoreCase = true) ||
                            voice.name.contains("pt-br-x-sfg", ignoreCase = true) ||
                            voice.name.contains("pt-br-x-jab", ignoreCase = true)
                    isPt && isFemale && !voice.isNetworkConnectionRequired
                } ?: voices.firstOrNull { voice ->
                    voice.locale.language.equals(ptBr.language, ignoreCase = true) &&
                            voice.locale.country.equals(ptBr.country, ignoreCase = true)
                }

                if (femaleVoice != null) {
                    tts?.voice = femaleVoice
                }
            }
        } catch (_: Exception) {}
    }

    fun speak(text: String, pitch: Float = 1.05f, rate: Float = 1.02f) {
        if (!isInitialized || tts == null) return
        stop()

        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOPHIA_SPEECH_${System.currentTimeMillis()}")
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "SOPHIA_SPEECH_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
