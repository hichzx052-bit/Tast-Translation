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

    val waveAnim = rememberInfiniteTransition(label = "wave")
    val waveOffset by waveAnim.animateFloat(0f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Restart), label = "wo")

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        TopAppBar(title = { Text("وضع اللايف", color = TextPrimary) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark))

        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Live indicator
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isTranslating) SuccessGreen else ErrorRed))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTranslating) "مباشر — جارٍ الترجمة" else "متوقف", color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Waveform placeholder
            if (isTranslating) {
                Row(modifier = Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    repeat(20) { i ->
                        val h = (30 + 50 * Math.sin((i + waveOffset * 20) * 0.5)).dp
                        Box(modifier = Modifier.width(4.dp).height(h).padding(horizontal = 1.dp).clip(RoundedCornerShape(2.dp)).background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Language pair
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("يسمع صوتي بـ:", color = TextSecondary)
                    Text("${targetLang.flag} ${targetLang.nativeName}", color = TextPrimary, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("أسمع صوتهم بـ:", color = TextSecondary)
                    Text("${sourceLang.flag} ${sourceLang.nativeName}", color = TextPrimary, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggles
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("اسمع لغتي", color = TextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = hearMy, onCheckedChange = { viewModel.toggleHearMyLanguage() }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("اسمع لغتهم", color = TextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = hearTheir, onCheckedChange = { viewModel.toggleHearTheirLanguage() }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start/Stop button
            Button(
                onClick = { viewModel.toggleTranslation() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isTranslating) ErrorRed else Primary)
            ) {
                Icon(if (isTranslating) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isTranslating) "إيقاف" else "بدء الترجمة المباشرة", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
