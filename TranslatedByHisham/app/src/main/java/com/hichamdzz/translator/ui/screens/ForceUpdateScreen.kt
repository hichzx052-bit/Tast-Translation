package com.hichamdzz.translator.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hichamdzz.translator.repository.VersionInfo
import com.hichamdzz.translator.ui.theme.*

@Composable
fun ForceUpdateScreen(updateInfo: VersionInfo?) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.SystemUpdate, null, modifier = Modifier.size(80.dp), tint = Primary)
            Spacer(Modifier.height(24.dp))
            Text("يتوفر تحديث جديد", color = TextPrimary, fontSize = 24.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text("الإصدار ${updateInfo?.versionName ?: "جديد"}", color = TextSecondary, fontSize = 16.sp)
            if (updateInfo?.changelog?.isNotEmpty() == true) {
                Spacer(Modifier.height(8.dp))
                Text(updateInfo.changelog, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { updateInfo?.updateUrl?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("تحديث الآن", fontSize = 18.sp) }
        }
    }
}
