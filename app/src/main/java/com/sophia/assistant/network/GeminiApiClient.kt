package com.sophia.assistant.network

import com.sophia.assistant.data.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val systemPersona = """
        Você é SOPHIA, uma assistente pessoal com inteligência artificial.
        Sua personalidade é acolhedora, altamente inteligente, ágil, educada e prestativa.
        Você se comunica em português do Brasil de forma fluida, natural, concisa e agradável aos ouvidos.
        IMPORTANTE: Suas respostas serão lidas e faladas diretamente por um sintetizador de voz (Text-To-Speech).
        Portanto:
        - Não use formatações pesadas de markdown como tabelas, asteriscos repetidos ou caracteres gráficos estranhos.
        - Seja objetiva, clara e simpática. Responda em frases bem pontuadas para que a voz soe suave e expressiva.
    """.trimIndent()

    suspend fun getSophiaResponse(
        apiKey: String,
        history: List<Message>,
        newPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Por favor, configure sua chave da Gemini API tocando no ícone de engrenagem no topo.")
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        try {
            val rootJson = JSONObject()

            // System instruction para a persona Sophia
            val systemInstruction = JSONObject()
            val systemParts = JSONArray()
            systemParts.put(JSONObject().put("text", systemPersona))
            systemInstruction.put("parts", systemParts)
            rootJson.put("system_instruction", systemInstruction)

            // Histórico recente (últimas 6 mensagens para manter contexto sem sobrecarregar)
            val contentsArray = JSONArray()
            val recentHistory = history.takeLast(6)
            for (msg in recentHistory) {
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.isUser) "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }

            // Nova mensagem do usuário
            val userContent = JSONObject()
            userContent.put("role", "user")
            val userParts = JSONArray()
            userParts.put(JSONObject().put("text", newPrompt))
            userContent.put("parts", userParts)
            contentsArray.put(userContent)

            rootJson.put("contents", contentsArray)

            // Configuração de geração
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 800)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody)
                        val errorObj = errorJson.optJSONObject("error")
                        errorObj?.optString("message") ?: "Erro HTTP ${response.code}"
                    } catch (e: Exception) {
                        "Erro HTTP ${response.code}: $responseBody"
                    }
                    return@withContext Result.failure(IOException("Erro na API Gemini: $errorMessage"))
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val replyText = parts.getJSONObject(0).optString("text", "")
                        if (replyText.isNotBlank()) {
                            // Limpa pontuações de markdown que atrapalham a fala
                            val sanitized = cleanTextForSpeech(replyText)
                            return@withContext Result.success(sanitized)
                        }
                    }
                }

                Result.failure(IOException("Resposta vazia da Sophia."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cleanTextForSpeech(text: String): String {
        return text
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // remove **negrito**
            .replace(Regex("\\*(.*?)\\*"), "$1")       // remove *itálico*
            .replace(Regex("`{1,3}.*?`{1,3}"), "")     // remove blocos de código
            .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "") // remove títulos #
            .trim()
    }
}
