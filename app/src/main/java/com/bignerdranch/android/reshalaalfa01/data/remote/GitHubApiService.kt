package com.bignerdranch.android.reshalaalfa01.data.remote

import com.bignerdranch.android.reshalaalfa01.data.remote.dto.GitHubReleaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface GitHubApiService {
    @GET
    suspend fun getLatestRelease(@Url url: String): Response<GitHubReleaseResponse>
}
