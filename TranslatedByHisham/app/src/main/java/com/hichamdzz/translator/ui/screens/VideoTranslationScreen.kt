package com.hichamdzz.translator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun VideoTranslationScreen(navController: NavController, viewModel: MainViewModel) {
    val targetLang by viewModel.targetLang.collectAsState()
    var videoUri by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        TopAppBar(title = { Text("ترجمة الفيديو", color = TextPrimary) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark))

        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Video preview area
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(16.dp)).background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                if (videoUri != null) {
                    Text("فيديو محمّل", color = TextPrimary)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VideoLibrary, null, modifier = Modifier.size(64.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("اختر فيديو", color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("اللغة المطلوبة", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("${targetLang.flag} ${targetLang.nativeName}", color = TextPrimary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = Primary))
                        Text("سبتايتل (ترجمة نصية)", color = TextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = Primary))
                        Text("دبلجة صوتية", color = TextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = Primary))
                        Text("ووترمارك AI — Translated by Hisham", color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            OutlinedButton(
                onClick = { /* pick video */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) { Icon(Icons.Default.FileOpen, null); Spacer(Modifier.width(8.dp)); Text("اختيار فيديو") }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { isProcessing = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = videoUri != null && !isProcessing
            ) { Text(if (isProcessing) "جارٍ المعالجة..." else "ترجمة الفيديو") }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
