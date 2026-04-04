package com.hichamdzz.translator.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hichamdzz.translator.ui.theme.*
import com.hichamdzz.translator.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveModeScreen(navController: NavController, viewModel: MainViewModel) {
    val sourceLang by viewModel.sourceLang.collectAsState()
    val targetLang by viewModel.targetLang.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val hearMy by viewModel.hearMyLanguage.collectAsState()
    val hearTheir by viewModel.hearTheirLanguage.collectAsState()
    val recognized by viewModel.recognizedText.collectAsState()
    val translated by viewModel.translatedText.collectAsState()
    val status by viewModel.statusMessage.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        TopAppBar(title = { Text("وضع اللايف", color = TextPrimary) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark))

        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Live indicator
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isTranslating) SuccessGreen else ErrorRed))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isTranslating) "مباشر — جارٍ الترجمة" else "متوقف", color = TextPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Translation output
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎤 الكلام الأصلي:", color = TextSecondary, fontSize = 12.sp)
                    Text(recognized.ifEmpty { "..." }, color = TextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    Divider(color = TextSecondary.copy(alpha = 0.2f))
                    Spacer(Modifier.height(16.dp))
                    Text("🌐 الترجمة:", color = Accent, fontSize = 12.sp)
                    Text(translated.ifEmpty { "..." }, color = SuccessGreen, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    if (status.isNotEmpty()) Text(status, color = Accent, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Language info
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("يسمع صوتي بـ: ${targetLang.flag} ${targetLang.nativeName}", color = TextPrimary)
                    Text("أسمع صوتهم بـ: ${sourceLang.flag} ${sourceLang.nativeName}", color = TextPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Toggles
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("اسمع لغتي", color = TextPrimary, modifier = Modifier.weight(1f))
                        Switch(hearMy, { viewModel.toggleHearMyLanguage() }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("اسمع لغتهم", color = TextPrimary, modifier = Modifier.weight(1f))
                        Switch(hearTheir, { viewModel.toggleHearTheirLanguage() }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.toggleTranslation() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isTranslating) ErrorRed else Primary)
            ) {
                Icon(if (isTranslating) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isTranslating) "إيقاف" else "بدء الترجمة المباشرة", fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
