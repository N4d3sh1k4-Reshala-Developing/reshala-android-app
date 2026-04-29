package com.bignerdranch.android.reshalaalfa01.data.remote

import com.bignerdranch.android.reshalaalfa01.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/confirm-email")
    suspend fun confirmEmail(@Query("token") token: String): Response<Unit>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>

    @POST("auth/resend-confirmation")
    suspend fun resendConfirmation(@Query("email") email: String): Response<Unit>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    @POST("auth/yandex-mobile")
    suspend fun loginWithYandex(@Body request: YandexLoginRequest): Response<LoginResponse>

    @GET("user")
    suspend fun getUser(@Header("Authorization") token: String): Response<UserResponse>

    @POST("auth/refresh")
    suspend fun refresh(): Response<LoginResponse>

    @GET("recognition/data/history")
    suspend fun getRecognitionHistory(@Header("Authorization") token: String): Response<RecognitionHistoryResponse>
}
