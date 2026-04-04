package com.hichamdzz.translator.api

import retrofit2.Response
import retrofit2.http.*

interface DeepLApi {
    @POST("translate")
    @FormUrlEncoded
    suspend fun translate(
        @Field("text") text: String,
        @Field("target_lang") targetLang: String,
        @Field("source_lang") sourceLang: String? = null,
        @Header("Authorization") auth: String
    ): Response<DeepLResponse>

    @GET("languages")
    suspend fun getLanguages(
        @Header("Authorization") auth: String
    ): Response<List<DeepLLanguage>>
}

data class DeepLResponse(val translations: List<DeepLTranslation>)
data class DeepLTranslation(val text: String, val detected_source_language: String?)
data class DeepLLanguage(val language: String, val name: String)
