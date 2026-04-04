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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hichamdzz.translator.ui.navigation.Routes
import com.hichamdzz.translator.ui.theme.*
import com.hichamdzz.translator.util.Constants
import com.hichamdzz.translator.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel) {
    var devCodeInput by remember { mutableStateOf("") }
    var showDevDialog by remember { mutableStateOf(false) }
    val hearMy by viewModel.hearMyLanguage.collectAsState()
    val hearTheir by viewModel.hearTheirLanguage.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp).verticalScroll(rememberScrollState())) {
        TopAppBar(title = { Text("الإعدادات", color = TextPrimary) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark))

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection("الترجمة") {
            SettingsToggle("اسمع لغتي", "ترجم كلامي للطرف الآخر", hearMy) { viewModel.toggleHearMyLanguage() }
            SettingsToggle("اسمع لغتهم", "ترجم كلام الطرف الآخر لي", hearTheir) { viewModel.toggleHearTheirLanguage() }
        }

        SettingsSection("الصوت") {
            SettingsItem("اختيار الصوت", "تغيير صوت الترجمة") { navController.navigate(Routes.VOICE_SELECTION) }
        }

        SettingsSection("المطور") {
            SettingsItem("لوحة المطور", "أدخل كود المطور") { showDevDialog = true }
        }
    }

    if (showDevDialog) {
        AlertDialog(
            onDismissRequest = { showDevDialog = false },
            title = { Text("كود المطور", color = TextPrimary) },
            text = { OutlinedTextField(value = devCodeInput, onValueChange = { devCodeInput = it }, label = { Text("أدخل الكود") }) },
            confirmButton = {
                TextButton(onClick = { if (devCodeInput == Constants.DEVELOPER_CODE) { showDevDialog = false; navController.navigate(Routes.DEVELOPER) } }) { Text("دخول") }
            },
            dismissButton = { TextButton(onClick = { showDevDialog = false }) { Text("إلغاء") } },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = Primary, modifier = Modifier.padding(vertical = 12.dp))
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) { Text(title, color = TextPrimary); Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
        Switch(checked = checked, onCheckedChange = { onToggle() }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) { Text(title, color = TextPrimary); Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
        IconButton(onClick = onClick) { Icon(Icons.Default.ChevronRight, null, tint = TextSecondary) }
    }
}
