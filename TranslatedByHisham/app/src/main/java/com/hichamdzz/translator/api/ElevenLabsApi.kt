package com.hichamdzz.translator.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ElevenLabsApi {
    @POST("text-to-speech/{voice_id}")
    suspend fun textToSpeech(
        @Path("voice_id") voiceId: String,
        @Body request: TTSRequest,
        @Header("xi-api-key") apiKey: String
    ): Response<ResponseBody>

    @GET("voices")
    suspend fun getVoices(
        @Header("xi-api-key") apiKey: String
    ): Response<VoicesResponse>
}

data class TTSRequest(
    val text: String,
    val model_id: String = "eleven_multilingual_v2",
    val voice_settings: VoiceSettings = VoiceSettings()
)

data class VoiceSettings(
    val stability: Float = 0.5f,
    val similarity_boost: Float = 0.75f,
    val style: Float = 0.0f,
    val use_speaker_boost: Boolean = true
)

data class VoicesResponse(val voices: List<VoiceItem>)
data class VoiceItem(
    val voice_id: String,
    val name: String,
    val category: String?,
    val labels: Map<String, String>?
)
