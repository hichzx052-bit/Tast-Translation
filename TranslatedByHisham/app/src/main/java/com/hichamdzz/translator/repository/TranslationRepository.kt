package com.hichamdzz.translator.repository

import android.content.Context
import com.hichamdzz.translator.api.*
import com.hichamdzz.translator.model.TranslationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepository @Inject constructor(
    private val whisperApi: WhisperApi,
    private val deepLApi: DeepLApi,
    private val elevenLabsApi: ElevenLabsApi,
    @ApplicationContext private val context: Context
) {
    suspend fun speechToText(audioFile: File, apiKey: String, language: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val filePart = MultipartBody.Part.createFormData("file", audioFile.name, audioFile.asRequestBody("audio/wav".toMediaType()))
            val modelPart = "whisper-1".toRequestBody("text/plain".toMediaType())
            val langPart = language?.toRequestBody("text/plain".toMediaType())
            val response = whisperApi.transcribe(filePart, modelPart, langPart, "Bearer $apiKey")
            if (response.isSuccessful) Result.success(response.body()?.text ?: "")
            else Result.failure(Exception("Whisper error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun translateText(text: String, targetLang: String, sourceLang: String? = null, apiKey: String): Result<TranslationResult> = withContext(Dispatchers.IO) {
        try {
            val response = deepLApi.translate(text, targetLang.uppercase(), sourceLang?.uppercase(), "DeepL-Auth-Key $apiKey")
            if (response.isSuccessful) {
                val translation = response.body()?.translations?.firstOrNull()
                Result.success(TranslationResult(
                    originalText = text, translatedText = translation?.text ?: "",
                    sourceLanguage = translation?.detected_source_language ?: sourceLang ?: "auto",
                    targetLanguage = targetLang
                ))
            } else Result.failure(Exception("DeepL error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun textToSpeech(text: String, voiceId: String, apiKey: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val response = elevenLabsApi.textToSpeech(voiceId, TTSRequest(text = text), apiKey)
            if (response.isSuccessful) Result.success(response.body()?.bytes() ?: byteArrayOf())
            else Result.failure(Exception("ElevenLabs error: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun fullTranslation(audioFile: File, targetLang: String, voiceId: String, whisperKey: String, deeplKey: String, elevenKey: String): Result<Pair<TranslationResult, ByteArray>> {
        val text = speechToText(audioFile, whisperKey).getOrElse { return Result.failure(it) }
        val translation = translateText(text, targetLang, apiKey = deeplKey).getOrElse { return Result.failure(it) }
        val audio = textToSpeech(translation.translatedText, voiceId, elevenKey).getOrElse { return Result.failure(it) }
        return Result.success(Pair(translation, audio))
    }
}
