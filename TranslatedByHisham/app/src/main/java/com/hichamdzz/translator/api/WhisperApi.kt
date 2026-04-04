package com.hichamdzz.translator.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface WhisperApi {
    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model") model: okhttp3.RequestBody,
        @Part("language") language: okhttp3.RequestBody? = null,
        @Header("Authorization") auth: String
    ): Response<TranscriptionResponse>

    @Multipart
    @POST("audio/translations")
    suspend fun translate(
        @Part file: MultipartBody.Part,
        @Part("model") model: okhttp3.RequestBody,
        @Header("Authorization") auth: String
    ): Response<TranscriptionResponse>
}

data class TranscriptionResponse(
    val text: String,
    val language: String? = null
)
