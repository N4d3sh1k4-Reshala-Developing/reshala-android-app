package com.bignerdranch.android.reshalaalfa01.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecognitionResponse(
    val success: Boolean,
    val data: RecognitionTaskData
)

@Serializable
data class RecognitionTaskData(
    val id: String,
    val createdAt: String,
    val status: String,
    val originalResult: String? = null,
    val editedResult: String? = null,
    val feedbackDeadline: String? = null,
    val solutionResult: String? = null
)

@Serializable
data class FeedbackRequest(
    val editStatus: String, // "true" or "false" based on your description
    val editedResult: String? = null
)

@Serializable
data class ManualSolveRequest(
    val equation: String
)
