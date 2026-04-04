package com.hichamdzz.translator.model

data class Voice(
    val id: String,
    val name: String,
    val language: String,
    val gender: String,
    val previewUrl: String? = null
) {
    companion object {
        val DEFAULT_VOICES = listOf(
            Voice("21m00Tcm4TlvDq8ikWAM", "Rachel", "en", "Female"),
            Voice("29vD33N1CtxCmqQRPOHJ", "Drew", "en", "Male"),
            Voice("EXAVITQu4vr4xnSDxMaL", "Bella", "en", "Female"),
            Voice("ErXwobaYiN019PkySvjV", "Antoni", "en", "Male"),
            Voice("MF3mGyEYCl7XYWbV9V6O", "Elli", "en", "Female"),
            Voice("TxGEqnHWrfWFTfGW9XjX", "Josh", "en", "Male"),
            Voice("VR6AewLTigWG4xSOukaG", "Arnold", "en", "Male"),
            Voice("pNInz6obpgDQGcFmaJgB", "Adam", "en", "Male"),
        )
    }
}
