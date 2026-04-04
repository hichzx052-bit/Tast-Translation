package com.hichamdzz.translator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hichamdzz.translator.ui.theme.*
import com.hichamdzz.translator.util.Constants
import com.hichamdzz.translator.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(navController: NavController, viewModel: MainViewModel) {
    var whisperKey by remember { mutableStateOf("") }
    var deeplKey by remember { mutableStateOf("") }
    var elevenKey by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        whisperKey = viewModel.getApiKey(Constants.PREFS_WHISPER_KEY)
        deeplKey = viewModel.getApiKey(Constants.PREFS_DEEPL_KEY)
        elevenKey = viewModel.getApiKey(Constants.PREFS_ELEVENLABS_KEY)
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState())) {
        TopAppBar(title = { Text("لوحة المطور 🔧", color = TextPrimary) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark))

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Developer: Hichamdzz", color = Primary, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))

            ApiKeyField("OpenAI Whisper API Key", whisperKey) { whisperKey = it }
            Spacer(Modifier.height(16.dp))
            ApiKeyField("DeepL API Key", deeplKey) { deeplKey = it }
            Spacer(Modifier.height(16.dp))
            ApiKeyField("ElevenLabs API Key", elevenKey) { elevenKey = it }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveApiKey(Constants.PREFS_WHISPER_KEY, whisperKey)
                    viewModel.saveApiKey(Constants.PREFS_DEEPL_KEY, deeplKey)
                    viewModel.saveApiKey(Constants.PREFS_ELEVENLABS_KEY, elevenKey)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("حفظ المفاتيح") }

            if (saved) { Spacer(Modifier.height(8.dp)); Text("✅ تم الحفظ!", color = SuccessGreen) }
        }
    }
}

@Composable
fun ApiKeyField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary, unfocusedBorderColor = TextSecondary,
            focusedLabelColor = Primary, cursorColor = Primary,
            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
        ),
        singleLine = true
    )
}
