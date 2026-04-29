package com.bignerdranch.android.reshalaalfa01.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecognitionHistoryResponse(
    val success: Boolean,
    val data: List<RecognitionHistoryItem>
)

@Serializable
data class RecognitionHistoryItem(
    val id: String,
    val createdAt: String,
    val originalResult: String,
    val editedResult: String? = null,
    val solutionResult: String? = null,
    val status: String
)
