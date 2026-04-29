package com.bignerdranch.android.reshalaalfa01.data

import com.bignerdranch.android.reshalaalfa01.data.local.RecognitionDao
import com.bignerdranch.android.reshalaalfa01.data.local.RecognitionEntity
import com.bignerdranch.android.reshalaalfa01.data.local.TokenManager
import com.bignerdranch.android.reshalaalfa01.data.local.toEntity
import com.bignerdranch.android.reshalaalfa01.data.remote.AuthApiService
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.Response

class AuthRepository(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val recognitionDao: RecognitionDao,
    private val json: Json
) {
    val accessToken: Flow<String?> = tokenManager.accessToken

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = apiService.login(request)
            handleLoginResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithYandex(accessToken: String): Result<LoginResponse> {
        return try {
            val response = apiService.loginWithYandex(YandexLoginRequest(accessToken))
            handleLoginResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleLoginResponse(response: Response<LoginResponse>): Result<LoginResponse> {
        return if (response.isSuccessful && response.body() != null) {
            tokenManager.saveAccessToken(response.body()!!.data!!.accessToken)
            Result.success(response.body()!!)
        } else if (response.code() == 403) {
            val errorBody = response.errorBody()?.string()
            val errorResponse = json.decodeFromString<LoginResponse>(errorBody ?: "")
            Result.success(errorResponse)
        } else {
            Result.failure(Exception("Auth failed: ${response.code()}"))
        }
    }

    suspend fun register(request: RegisterRequest): Result<Unit> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmEmail(token: String): Result<Unit> {
        return try {
            val response = apiService.confirmEmail(token)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Email confirmation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendConfirmation(email: String): Result<Unit> {
        return try {
            val response = apiService.resendConfirmation(email)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Resend failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Forgot password request failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Result<Unit> {
        return try {
            val response = apiService.resetPassword(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Reset password failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(): Result<UserData> {
        return try {
            val token = accessToken.firstOrNull()
            if (token != null) {
                val response = apiService.getUser("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception("Failed to get user data"))
                }
            } else {
                Result.failure(Exception("No token found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getHistoryFromDb(): Flow<List<RecognitionEntity>> {
        return recognitionDao.getAllHistory()
    }

//    suspend fun fetchAndSaveHistory(): Result<Unit> {
//        return try {
//            val token = accessToken.firstOrNull()
//            if (token != null) {
//                val response = apiService.getRecognitionHistory("Bearer $token")
//                if (response.isSuccessful && response.body() != null) {
//                    val entities = response.body()!!.data.map { it.toEntity() }
//                    recognitionDao.insertAll(entities)
//                    Result.success(Unit)
//                } else {
//                    Result.failure(Exception("Failed to fetch history"))
//                }
//            } else {
//                Result.failure(Exception("No token found"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    suspend fun fetchAndSaveHistory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = accessToken.firstOrNull() ?: return@withContext Result.failure(Exception("No token found"))

            val response = apiService.getRecognitionHistory("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val entities = body.data.map { it.toEntity() }

                    recognitionDao.updateHistory(entities)

                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Server returned success: false"))
                }
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refresh(): Result<Unit> {
        return try {
            val response = apiService.refresh()
            if (response.isSuccessful && response.body() != null) {
                tokenManager.saveAccessToken(response.body()!!.data!!.accessToken)
                Result.success(Unit)
            } else {
                tokenManager.clearAccessToken()
                Result.failure(Exception("Refresh failed"))
            }
        } catch (e: Exception) {
            tokenManager.clearAccessToken()
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val currentToken = accessToken.firstOrNull()
            if (currentToken != null) {
                apiService.logout("Bearer $currentToken")
            }
            tokenManager.clearAccessToken()
            recognitionDao.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearAccessToken()
            recognitionDao.clearAll()
            Result.failure(e)
        }
    }
}
