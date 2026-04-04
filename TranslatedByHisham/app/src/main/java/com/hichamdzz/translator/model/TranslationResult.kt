package com.hichamdzz.translator.model

data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val audioUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
