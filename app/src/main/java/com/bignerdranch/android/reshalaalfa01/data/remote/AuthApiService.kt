package com.bignerdranch.android.reshalaalfa01.data.remote

import com.bignerdranch.android.reshalaalfa01.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    //---AUTH---
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
    @POST("auth/refresh")
    suspend fun refresh(): Response<LoginResponse>

    //--UserAccountData
    @GET("user")
    suspend fun getUser(@Header("Authorization") token: String): Response<UserResponse>


    @GET("equation/user/history")
    suspend fun getRecognitionHistory(@Header("Authorization") token: String): Response<RecognitionHistoryResponse>
    @GET("equation/user/statistic")
    suspend fun getUserStatistic(@Header("Authorization") token: String): Response<UserStatisticResponse>
    @Multipart
    @POST("equation/recognition/process")
    suspend fun processImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<RecognitionResponse>
    @GET("equation/recognition/{task_id}")
    suspend fun getRecognitionStatus(
        @Header("Authorization") token: String,
        @Path("task_id") taskId: String
    ): Response<RecognitionResponse>
    @POST("equation/recognition/{task_id}/feedback")
    suspend fun sendFeedback(
        @Header("Authorization") token: String,
        @Path("task_id") taskId: String,
        @Body request: FeedbackRequest
    ): Response<Unit>
    @DELETE("equation/user/{task_id}")
    suspend fun deleteRecognition(
        @Header("Authorization") token: String,
        @Path("task_id") taskId: String
    ): Response<Unit>
    @POST("equation/recognition/process/solve")
    suspend fun solveManual(
        @Header("Authorization") token: String,
        @Body request: ManualSolveRequest
    ): Response<RecognitionResponse>
}
