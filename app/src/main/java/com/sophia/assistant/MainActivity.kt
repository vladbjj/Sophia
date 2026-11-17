package com.sophia.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.sophia.assistant.data.Message
import com.sophia.assistant.data.PreferencesHelper
import com.sophia.assistant.network.GeminiApiClient
import com.sophia.assistant.ui.components.AssistantState
import com.sophia.assistant.ui.components.SettingsDialog
import com.sophia.assistant.ui.screens.MainScreen
import com.sophia.assistant.ui.theme.SophiaAssistantTheme
import com.sophia.assistant.voice.SpeechRecognizerHelper
import com.sophia.assistant.voice.TextToSpeechHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var geminiClient: GeminiApiClient
    private var speechRecognizerHelper: SpeechRecognizerHelper? = null
    private var ttsHelper: TextToSpeechHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesHelper = PreferencesHelper(this)
        geminiClient = GeminiApiClient()

        setContent {
            SophiaAssistantTheme {
                SophiaApp(
                    preferencesHelper = preferencesHelper,
                    geminiClient = geminiClient,
                    createSpeechHelper = { onResult, onError ->
                        SpeechRecognizerHelper(this, onResult, onError).also {
                            speechRecognizerHelper = it
                        }
                    },
                    createTtsHelper = { onInit ->
                        TextToSpeechHelper(this, onInit).also {
                            ttsHelper = it
                        }
                    },
                    hasRecordAudioPermission = {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizerHelper?.destroy()
        ttsHelper?.destroy()
    }
}

@Composable
fun SophiaApp(
    preferencesHelper: PreferencesHelper,
    geminiClient: GeminiApiClient,
    createSpeechHelper: (onResult: (String) -> Unit, onError: (String) -> Unit) -> SpeechRecognizerHelper,
    createTtsHelper: (onInit: (Boolean) -> Unit) -> TextToSpeechHelper,
    hasRecordAudioPermission: () -> Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val messages = remember {
        mutableStateListOf(
            Message(
                text = "Olá! Eu sou a Sophia, sua assistente pessoal inteligente. Como posso ajudar você hoje?",
                isUser = false
            )
        )
    }

    var isThinking by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    var processPromptRef by remember { mutableStateOf<(String) -> Unit>({}) }

    // Helpers
    val speechHelper = remember {
        createSpeechHelper(
            { recognizedText ->
                processPromptRef(recognizedText)
            },
            { errorMessage ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(errorMessage)
                }
            }
        )
    }

    val ttsHelper = remember {
        createTtsHelper { success ->
            if (!success) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Aviso: Síntese de voz pt-BR não disponível.")
                }
            }
        }
    }

    val isListening by speechHelper.isListening.collectAsState()
    val rmsLevel by speechHelper.rmsLevel.collectAsState()
    val isSpeaking by ttsHelper.isSpeaking.collectAsState()

    // Estado geral da assistente
    val assistantState = when {
        isSpeaking -> AssistantState.SPEAKING
        isThinking -> AssistantState.THINKING
        isListening -> AssistantState.LISTENING
        else -> AssistantState.IDLE
    }

    // Função para processar mensagem e consultar IA
    fun processPrompt(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) return

        // Interrompe fala anterior se houver
        ttsHelper.stop()

        // Adiciona mensagem do usuário
        messages.add(Message(text = cleanPrompt, isUser = true))
        isThinking = true

        coroutineScope.launch {
            val apiKey = preferencesHelper.apiKey
            if (apiKey.isBlank()) {
                isThinking = false
                val errorMsg = "Por favor, adicione sua Gemini API Key nas configurações (ícone de engrenagem) para conversar comigo."
                messages.add(Message(text = errorMsg, isUser = false))
                ttsHelper.speak(
                    text = "Por favor, adicione sua chave da API Gemini nas configurações.",
                    pitch = preferencesHelper.speechPitch,
                    rate = preferencesHelper.speechRate
                )
                showSettings = true
                return@launch
            }

            val result = geminiClient.getSophiaResponse(
                apiKey = apiKey,
                history = messages.dropLast(1),
                newPrompt = cleanPrompt
            )

            isThinking = false

            result.fold(
                onSuccess = { reply ->
                    messages.add(Message(text = reply, isUser = false))
                    ttsHelper.speak(
                        text = reply,
                        pitch = preferencesHelper.speechPitch,
                        rate = preferencesHelper.speechRate
                    )
                },
                onFailure = { error ->
                    val errorText = "Desculpe, ocorreu um erro: ${error.localizedMessage ?: "Tente novamente."}"
                    messages.add(Message(text = errorText, isUser = false))
                    snackbarHostState.showSnackbar(errorText)
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        processPromptRef = { text -> processPrompt(text) }
    }

    DisposableEffect(speechHelper) {
        onDispose {
            speechHelper.destroy()
            ttsHelper.destroy()
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ttsHelper.stop()
            speechHelper.startListening()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("A permissão de microfone é necessária para falar com a Sophia.")
            }
        }
    }

    // Ao clicar no microfone
    val onMicClick: () -> Unit = {
        if (isListening) {
            speechHelper.stopListening()
        } else {
            ttsHelper.stop()
            if (hasRecordAudioPermission()) {
                speechHelper.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    MainScreen(
        messages = messages,
        assistantState = assistantState,
        rmsLevel = rmsLevel,
        onMicClick = onMicClick,
        onSendMessage = { text -> processPrompt(text) },
        onStopSpeech = { ttsHelper.stop() },
        onReplaySpeech = { text ->
            ttsHelper.speak(
                text = text,
                pitch = preferencesHelper.speechPitch,
                rate = preferencesHelper.speechRate
            )
        },
        onClearHistory = {
            ttsHelper.stop()
            speechHelper.stopListening()
            messages.clear()
            messages.add(
                Message(
                    text = "Histórico reiniciado. Como posso te ajudar agora?",
                    isUser = false
                )
            )
        },
        onOpenSettings = { showSettings = true },
        snackbarHostState = snackbarHostState
    )

    if (showSettings) {
        SettingsDialog(
            currentApiKey = preferencesHelper.apiKey,
            currentPitch = preferencesHelper.speechPitch,
            currentRate = preferencesHelper.speechRate,
            onDismiss = { showSettings = false },
            onSave = { key, pitch, rate ->
                preferencesHelper.apiKey = key
                preferencesHelper.speechPitch = pitch
                preferencesHelper.speechRate = rate
                showSettings = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Configurações da Sophia salvas com sucesso!")
                }
            }
        )
    }
}
