package com.bignerdranch.android.reshalaalfa01.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserStatisticResponse(
    val success: Boolean,
    val data: UserStatisticData
)

@Serializable
data class UserStatisticData(
    val userId: String,
    val totalTasks: Int,
    val successTasks: Int,
    val errorTasks: Int,
    val editedTasks: Int,
    val directSolutionTasks: Int
)
