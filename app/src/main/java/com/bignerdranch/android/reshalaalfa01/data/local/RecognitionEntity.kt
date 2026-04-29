package com.bignerdranch.android.reshalaalfa01.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.RecognitionHistoryItem

@Entity(tableName = "recognition_history")
data class RecognitionEntity(
    @PrimaryKey val id: String,
    val createdAt: String,
    val originalResult: String?,
    val editedResult: String?,
    val solutionResult: String?,
    val status: String
)

fun RecognitionHistoryItem.toEntity() = RecognitionEntity(
    id = id,
    createdAt = createdAt,
    originalResult = originalResult,
    editedResult = editedResult,
    solutionResult = solutionResult,
    status = status
)
