package com.bignerdranch.android.reshalaalfa01.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val confirmPassword: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val data: AuthData? = null,
    val error: ApiError? = null
)

@Serializable
data class AuthData(
    val accessToken: String,
    val type: String
)

@Serializable
data class ApiError(
    val code: String,
    val message: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val password: String,
    val confirmPassword: String
)

@Serializable
data class YandexLoginRequest(
    val accessToken: String
)

@Serializable
data class UserResponse(
    val success: Boolean,
    val data: UserData
)

@Serializable
data class UserData(
    val username: String,
    val email: String
)
