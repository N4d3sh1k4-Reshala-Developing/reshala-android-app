package com.bignerdranch.android.reshalaalfa01.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SolutionStep(
    val title: String,
    val explanation: String,
    val latex: String
)
