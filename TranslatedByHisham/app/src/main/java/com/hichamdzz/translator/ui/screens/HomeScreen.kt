package com.hichamdzz.translator.ui.screens

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hichamdzz.translator.service.FloatingWidgetService
import com.hichamdzz.translator.ui.navigation.Routes
import com.hichamdzz.translator.ui.theme.*
import com.hichamdzz.translator.viewmodel.MainViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val sourceLang by viewModel.sourceLang.collectAsState()
    val targetLang by viewModel.targetLang.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val context = LocalContext.current

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f, targetValue = 1.15f, label = "scale",
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Translated by Hisham", style = MaterialTheme.typography.titleLarge, color = Primary)
                IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(sourceLang.flag, fontSize = 32.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(sourceLang.nativeName, color = TextPrimary, fontSize = 14.sp)
                    }
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = Accent, modifier = Modifier.size(32.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(targetLang.flag, fontSize = 32.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(targetLang.nativeName, color = TextPrimary, fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                if (isTranslating) {
                    Box(modifier = Modifier.size(160.dp).scale(scale).clip(CircleShape).background(Primary.copy(alpha = 0.2f)))
                }
                Box(
                    modifier = Modifier.size(120.dp).clip(CircleShape)
                        .background(Brush.linearGradient(if (isTranslating) listOf(ErrorRed, ErrorRed) else listOf(GradientStart, GradientEnd)))
                        .clickable { viewModel.toggleTranslation() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isTranslating) Icons.Default.Stop else Icons.Default.Mic, contentDescription = "Translate", modifier = Modifier.size(48.dp), tint = TextPrimary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(if (isTranslating) "جارٍ الترجمة..." else "اضغط للترجمة", color = TextSecondary, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                HomeBottomAction(Icons.Default.LiveTv, "لايف") { navController.navigate(Routes.LIVE_MODE) }
                HomeBottomAction(Icons.Default.VideoLibrary, "فيديو") { navController.navigate(Routes.VIDEO_TRANSLATION) }
                HomeBottomAction(Icons.Default.RecordVoiceOver, "الأصوات") { navController.navigate(Routes.VOICE_SELECTION) }
                HomeBottomAction(Icons.Default.Layers, "عائم") {
                    context.startService(Intent(context, FloatingWidgetService::class.java))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomeBottomAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceDark), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = Accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
