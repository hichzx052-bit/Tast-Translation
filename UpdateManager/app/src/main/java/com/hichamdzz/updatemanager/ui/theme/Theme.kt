package com.hichamdzz.updatemanager.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OrangePrimary = Color(0xFFFF6B35)
val BackgroundDark = Color(0xFF0A0A1A)
val SurfaceDark = Color(0xFF141428)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFFB0B0C0)
val SuccessGreen = Color(0xFF2ED573)
val ErrorRed = Color(0xFFFF4757)

private val DarkColors = darkColorScheme(
    primary = OrangePrimary, background = BackgroundDark, surface = SurfaceDark,
    onPrimary = TextWhite, onBackground = TextWhite, onSurface = TextWhite, error = ErrorRed
)

@Composable
fun UpdateManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
