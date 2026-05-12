package com.bignerdranch.android.reshalaalfa01.data

import com.bignerdranch.android.reshalaalfa01.data.remote.GitHubApiService
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.GitHubReleaseResponse

class UpdateRepository(
    private val gitHubApiService: GitHubApiService
) {
    suspend fun getLatestRelease(owner: String, repo: String): Result<GitHubReleaseResponse> {
        return try {
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val response = gitHubApiService.getLatestRelease(url)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch latest release: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
