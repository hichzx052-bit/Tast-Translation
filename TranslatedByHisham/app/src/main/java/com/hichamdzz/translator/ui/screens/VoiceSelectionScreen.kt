package com.hichamdzz.translator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavController
import com.hichamdzz.translator.model.Voice
import com.hichamdzz.translator.ui.theme.*
import com.hichamdzz.translator.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSelectionScreen(navController: NavController, viewModel: MainViewModel) {
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val voices = Voice.DEFAULT_VOICES

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        TopAppBar(title = { Text("اختيار الصوت", color = TextPrimary) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark))

        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(voices) { voice ->
                val isSelected = voice.id == selectedVoice.id
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.setSelectedVoice(voice) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Primary.copy(alpha = 0.2f) else SurfaceDark),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GradientStart, GradientEnd))) else null
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isSelected) Primary else SurfaceDark), contentAlignment = Alignment.Center) {
                            Icon(if (voice.gender == "Male") Icons.Default.Male else Icons.Default.Female, null, tint = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(voice.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("${voice.gender} • ${voice.language}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen)
                    }
                }
            }
        }
    }
}
