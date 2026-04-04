package com.hichamdzz.updatemanager.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GitHubApi {
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Path("owner") owner: String, @Path("repo") repo: String, @Path("path") path: String,
        @Header("Authorization") auth: String
    ): Response<GitHubFileResponse>

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun updateFile(
        @Path("owner") owner: String, @Path("repo") repo: String, @Path("path") path: String,
        @Body request: UpdateFileRequest, @Header("Authorization") auth: String
    ): Response<ResponseBody>

    @POST("repos/{owner}/{repo}/releases")
    suspend fun createRelease(
        @Path("owner") owner: String, @Path("repo") repo: String,
        @Body request: CreateReleaseRequest, @Header("Authorization") auth: String
    ): Response<ReleaseResponse>

    @Multipart
    @POST
    suspend fun uploadReleaseAsset(
        @Url url: String, @Part file: MultipartBody.Part,
        @Header("Authorization") auth: String, @Header("Content-Type") contentType: String = "application/vnd.android.package-archive"
    ): Response<ResponseBody>
}

data class GitHubFileResponse(val content: String?, val sha: String, val name: String)
data class UpdateFileRequest(val message: String, val content: String, val sha: String)
data class CreateReleaseRequest(val tag_name: String, val name: String, val body: String, val draft: Boolean = false)
data class ReleaseResponse(val id: Long, val upload_url: String, val html_url: String)
