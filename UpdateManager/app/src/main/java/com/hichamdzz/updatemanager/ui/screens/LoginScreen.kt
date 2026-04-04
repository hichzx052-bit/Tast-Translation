package com.hichamdzz.updatemanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hichamdzz.updatemanager.BuildConfig
import com.hichamdzz.updatemanager.ui.theme.*

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.padding(32.dp)) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(64.dp), tint = OrangePrimary)
                Spacer(Modifier.height(16.dp))
                Text("Update Manager", color = TextWhite, fontSize = 24.sp)
                Text("أدخل كود المطور", color = TextGray, fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = code, onValueChange = { code = it; error = false },
                    label = { Text("كود المطور") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = error, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, cursorColor = OrangePrimary, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                if (error) Text("كود خاطئ!", color = ErrorRed, fontSize = 12.sp)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { if (code == BuildConfig.DEVELOPER_CODE) onLogin() else error = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) { Text("دخول", fontSize = 16.sp) }
            }
        }
    }
}
