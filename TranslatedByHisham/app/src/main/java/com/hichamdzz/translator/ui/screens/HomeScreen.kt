package com.hichamdzz.translator.ui.screens

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hichamdzz.translator.model.Language
import com.hichamdzz.translator.service.FloatingWidgetService
import com.hichamdzz.translator.ui.components.VideoBackground
import com.hichamdzz.translator.ui.navigation.Routes
import com.hichamdzz.translator.ui.theme.*
import com.hichamdzz.translator.viewmodel.MainViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val sourceLang by viewModel.sourceLang.collectAsState()
    val targetLang by viewModel.targetLang.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val translatedText by viewModel.translatedText.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val context = LocalContext.current
    var showLangPicker by remember { mutableStateOf(false) }
    var pickingSource by remember { mutableStateOf(true) }

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(1f, 1.15f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "s")

    Box(modifier = Modifier.fillMaxSize()) {
        // Video background
        VideoBackground(modifier = Modifier.fillMaxSize())

        // Dark overlay so text is readable
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

        // Content
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Translated by Hisham", style = MaterialTheme.typography.titleLarge, color = Primary)
                IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                    Icon(Icons.Default.Settings, null, tint = TextSecondary)
                }
            }
            Spacer(Modifier.height(20.dp))

            // Language selector
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.8f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { pickingSource = true; showLangPicker = true }) {
                        Text(sourceLang.flag, fontSize = 32.sp)
                        Text(sourceLang.nativeName, color = TextPrimary, fontSize = 14.sp)
                    }
                    IconButton(onClick = { val tmp = sourceLang; viewModel.setSourceLanguage(targetLang); viewModel.setTargetLanguage(tmp) }) {
                        Icon(Icons.Default.SwapHoriz, null, tint = Accent, modifier = Modifier.size(32.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { pickingSource = false; showLangPicker = true }) {
                        Text(targetLang.flag, fontSize = 32.sp)
                        Text(targetLang.nativeName, color = TextPrimary, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Results
            if (recognizedText.isNotEmpty() || translatedText.isNotEmpty()) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.85f)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (recognizedText.isNotEmpty()) {
                            Text("🎤 ${sourceLang.nativeName}:", color = TextSecondary, fontSize = 12.sp)
                            Text(recognizedText, color = TextPrimary, fontSize = 16.sp)
                            Spacer(Modifier.height(12.dp))
                        }
                        if (translatedText.isNotEmpty()) {
                            Text("🌐 ${targetLang.nativeName}:", color = Accent, fontSize = 12.sp)
                            Text(translatedText, color = SuccessGreen, fontSize = 18.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (statusMessage.isNotEmpty()) { Text(statusMessage, color = Accent, fontSize = 14.sp); Spacer(Modifier.height(8.dp)) }

            Spacer(modifier = Modifier.weight(1f))

            // Mic button
            Box(contentAlignment = Alignment.Center) {
                if (isTranslating) Box(modifier = Modifier.size(160.dp).scale(scale).clip(CircleShape).background(Primary.copy(alpha = 0.2f)))
                Box(
                    modifier = Modifier.size(120.dp).clip(CircleShape)
                        .background(Brush.linearGradient(if (isTranslating) listOf(ErrorRed, ErrorRed) else listOf(GradientStart, GradientEnd)))
                        .clickable { viewModel.toggleTranslation() },
                    contentAlignment = Alignment.Center
                ) { Icon(if (isTranslating) Icons.Default.Stop else Icons.Default.Mic, null, Modifier.size(48.dp), tint = TextPrimary) }
            }
            Spacer(Modifier.height(12.dp))
            Text(if (isTranslating) "جارٍ الاستماع..." else "اضغط للترجمة", color = TextPrimary, fontSize = 16.sp)

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                HomeAction(Icons.Default.LiveTv, "لايف") { navController.navigate(Routes.LIVE_MODE) }
                HomeAction(Icons.Default.VideoLibrary, "فيديو") { navController.navigate(Routes.VIDEO_TRANSLATION) }
                HomeAction(Icons.Default.RecordVoiceOver, "الأصوات") { navController.navigate(Routes.VOICE_SELECTION) }
                HomeAction(Icons.Default.Layers, "عائم") { context.startService(Intent(context, FloatingWidgetService::class.java)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    // Language picker
    if (showLangPicker) {
        AlertDialog(
            onDismissRequest = { showLangPicker = false },
            title = { Text(if (pickingSource) "لغة الكلام" else "لغة الترجمة", color = TextPrimary) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Language.SUPPORTED.forEach { lang ->
                        TextButton(onClick = {
                            if (pickingSource) viewModel.setSourceLanguage(lang) else viewModel.setTargetLanguage(lang)
                            showLangPicker = false
                        }, Modifier.fillMaxWidth()) { Text("${lang.flag} ${lang.nativeName} (${lang.name})", color = TextPrimary, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            },
            confirmButton = {}, containerColor = SurfaceDark
        )
    }
}

@Composable
private fun HomeAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(SurfaceDark.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = Accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextPrimary, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
