package com.hichamdzz.translator.model

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String
) {
    companion object {
        val SUPPORTED = listOf(
            Language("ar", "Arabic", "العربية", "🇸🇦"),
            Language("en", "English", "English", "🇺🇸"),
            Language("fr", "French", "Français", "🇫🇷"),
            Language("es", "Spanish", "Español", "🇪🇸"),
            Language("de", "German", "Deutsch", "🇩🇪"),
            Language("it", "Italian", "Italiano", "🇮🇹"),
            Language("pt", "Portuguese", "Português", "🇵🇹"),
            Language("ru", "Russian", "Русский", "🇷🇺"),
            Language("zh", "Chinese", "中文", "🇨🇳"),
            Language("ja", "Japanese", "日本語", "🇯🇵"),
            Language("ko", "Korean", "한국어", "🇰🇷"),
            Language("tr", "Turkish", "Türkçe", "🇹🇷"),
            Language("hi", "Hindi", "हिन्दी", "🇮🇳"),
            Language("nl", "Dutch", "Nederlands", "🇳🇱"),
            Language("pl", "Polish", "Polski", "🇵🇱"),
            Language("sv", "Swedish", "Svenska", "🇸🇪"),
            Language("da", "Danish", "Dansk", "🇩🇰"),
            Language("fi", "Finnish", "Suomi", "🇫🇮"),
            Language("uk", "Ukrainian", "Українська", "🇺🇦"),
            Language("id", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
        )
        fun fromCode(code: String) = SUPPORTED.find { it.code == code } ?: SUPPORTED[1]
    }
}
