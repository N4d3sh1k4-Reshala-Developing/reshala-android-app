package com.bignerdranch.android.reshalaalfa01.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseResponse(
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val name: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("body") val body: String? = null
)
