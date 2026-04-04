package com.hichamdzz.translator.repository

import com.hichamdzz.translator.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class VersionInfo(val versionCode: Int, val versionName: String, val minRequired: String, val updateUrl: String, val changelog: String)

@Singleton
class UpdateRepository @Inject constructor(private val client: OkHttpClient) {
    suspend fun checkForUpdate(): Result<VersionInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(BuildConfig.VERSION_CHECK_URL).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                val info = VersionInfo(
                    versionCode = json.optInt("versionCode", 1),
                    versionName = json.optString("versionName", "1.0.0"),
                    minRequired = json.optString("minRequiredVersion", "1.0.0"),
                    updateUrl = json.optString("updateUrl", ""),
                    changelog = json.optString("changelog", "")
                )
                if (info.versionCode > BuildConfig.VERSION_CODE) Result.success(info)
                else Result.success(null)
            } else Result.failure(Exception("Update check failed: ${response.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
