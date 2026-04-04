package com.hichamdzz.updatemanager.ui.screens

import android.util.Base64
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
import androidx.compose.ui.unit.sp
import com.hichamdzz.updatemanager.BuildConfig
import com.hichamdzz.updatemanager.ui.theme.*
import kotlinx.coroutines.launch
import com.hichamdzz.updatemanager.api.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.json.JSONObject

@Composable
fun DashboardScreen() {
    var versionName by remember { mutableStateOf("1.0.0") }
    var versionCode by remember { mutableStateOf("1") }
    var changelog by remember { mutableStateOf("") }
    var githubToken by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val owner = BuildConfig.GITHUB_REPO.split("/")[0]
    val repo = BuildConfig.GITHUB_REPO.split("/")[1]

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Update Manager", color = OrangePrimary, fontSize = 28.sp, modifier = Modifier.padding(vertical = 16.dp))
        Text("Developer: Hichamdzz", color = TextGray, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))

        // GitHub Token
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("GitHub Token", color = TextWhite)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = githubToken, onValueChange = { githubToken = it }, label = { Text("Personal Access Token") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, cursorColor = OrangePrimary, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Version Info
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("معلومات الإصدار", color = TextWhite, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = versionName, onValueChange = { versionName = it }, label = { Text("اسم الإصدار") },
                        modifier = Modifier.weight(1f), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                    OutlinedTextField(value = versionCode, onValueChange = { versionCode = it }, label = { Text("رقم الإصدار") },
                        modifier = Modifier.weight(1f), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = changelog, onValueChange = { changelog = it }, label = { Text("ملاحظات التحديث") },
                    modifier = Modifier.fillMaxWidth(), minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
            }
        }

        Spacer(Modifier.height(24.dp))

        // Push Update Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true; status = "جارٍ التحديث..."
                    try {
                        val api = Retrofit.Builder().baseUrl("https://api.github.com/")
                            .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }).build())
                            .addConverterFactory(GsonConverterFactory.create()).build().create(GitHubApi::class.java)
                        val auth = "token $githubToken"
                        val fileResp = api.getFileContent(owner, repo, "TranslatedByHisham/version.json", auth)
                        if (fileResp.isSuccessful) {
                            val sha = fileResp.body()!!.sha
                            val newJson = JSONObject().apply {
                                put("versionCode", versionCode.toIntOrNull() ?: 1)
                                put("versionName", versionName)
                                put("minRequiredVersion", versionName)
                                put("updateUrl", "https://github.com/$owner/$repo/releases/latest")
                                put("changelog", changelog)
                            }.toString(2)
                            val encoded = Base64.encodeToString(newJson.toByteArray(), Base64.NO_WRAP)
                            val updateResp = api.updateFile(owner, repo, "TranslatedByHisham/version.json",
                                UpdateFileRequest("Update version to $versionName", encoded, sha), auth)
                            status = if (updateResp.isSuccessful) "✅ تم تحديث version.json بنجاح!" else "❌ فشل: ${updateResp.code()}"
                        } else status = "❌ فشل قراءة الملف: ${fileResp.code()}"
                    } catch (e: Exception) { status = "❌ خطأ: ${e.message}" }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary), enabled = !isLoading && githubToken.isNotEmpty()
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextWhite)
            else { Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text("إرسال التحديث", fontSize = 16.sp) }
        }

        if (status.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(status, color = if (status.startsWith("✅")) SuccessGreen else if (status.startsWith("❌")) ErrorRed else TextGray)
        }

        Spacer(Modifier.height(32.dp))
    }
}
