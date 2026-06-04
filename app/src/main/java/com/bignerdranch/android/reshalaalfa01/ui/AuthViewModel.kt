package com.bignerdranch.android.reshalaalfa01.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.reshalaalfa01.data.AuthRepository
import com.bignerdranch.android.reshalaalfa01.data.local.RecognitionEntity
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.LoginRequest
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.RegisterRequest
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.ResetPasswordRequest
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.UserData
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.UserStatisticData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class AwaitingVerification(val email: String) : AuthState()
    object ForgotPassword : AuthState()
    data class AwaitingPasswordReset(val email: String) : AuthState()
    data class ResetPassword(val token: String) : AuthState()
    data class SocialLinkRequired(
        val email: String,
        val provider: String,
        val providerUserId: String,
        val error: String? = null
    ) : AuthState()
    object EmailVerified : AuthState()
    object PasswordResetSuccess : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    private val _statistic = MutableStateFlow<UserStatisticData?>(null)
    val statistic: StateFlow<UserStatisticData?> = _statistic.asStateFlow()

    private val _history = MutableStateFlow<List<RecognitionEntity>>(emptyList())
    val history: StateFlow<List<RecognitionEntity>> = _history.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()
    
    private var timerJob: Job? = null

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            repository.accessToken.collect { token ->
                if (token != null) {
                    _authState.value = AuthState.Authenticated
                    fetchUserData()
                    fetchUserStatistic()
                    observeHistory()
                    refreshHistory()
                } else if (_authState.value !is AuthState.AwaitingVerification && 
                           _authState.value !is AuthState.ForgotPassword &&
                           _authState.value !is AuthState.ResetPassword &&
                           _authState.value !is AuthState.EmailVerified &&
                           _authState.value !is AuthState.AwaitingPasswordReset) {
                    _authState.value = AuthState.Unauthenticated
                    _userData.value = null
                }
            }
        }
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            repository.getUserData().onSuccess {
                _userData.value = it
            }.onFailure {
                logout()
            }
        }
    }

    private fun fetchUserStatistic() {
        viewModelScope.launch {
            repository.getUserStatistic().onSuccess {
                _statistic.value = it
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            repository.getHistoryFromDb().collect {
                _history.value = it
            }
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchAndSaveHistory()
            repository.getUserStatistic().onSuccess { _statistic.value = it }
            // Даем KaTeX немного времени на отрисовку в WebView
            delay(1000)
            _isRefreshing.value = false
        }
    }

    fun login(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(LoginRequest(email, password, rememberMe.toString()))
            handleAuthResult(result, email)
        }
    }

    fun loginWithYandex(token: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginWithYandex(token)
            handleAuthResult(result, "Yandex User")
        }
    }

    private fun handleAuthResult(result: Result<com.bignerdranch.android.reshalaalfa01.data.remote.dto.LoginResponse>, email: String) {
        result.onSuccess { loginResponse ->
            val data = loginResponse.data
            val errorCode = loginResponse.error?.code ?: data?.error
            
            if (errorCode == "EMAIL_NOT_VERIFIED") {
                resendConfirmation(email)
                _authState.value = AuthState.AwaitingVerification(email)
            } else if (errorCode == "email_exists_link_required") {
                _authState.value = AuthState.SocialLinkRequired(
                    email = data?.email ?: email,
                    provider = data?.provider ?: "",
                    providerUserId = data?.providerUserId ?: ""
                )
            } else if (loginResponse.success && data?.accessToken != null) {
                _authState.value = AuthState.Authenticated
                fetchUserData()
                refreshHistory()
            } else {
                val currentState = _authState.value
                val message = loginResponse.error?.message ?: data?.error ?: "Login failed"
                if (currentState is AuthState.SocialLinkRequired) {
                    _authState.value = currentState.copy(error = message)
                } else {
                    _authState.value = AuthState.Error(message)
                }
            }
        }.onFailure {
            val currentState = _authState.value
            if (currentState is AuthState.SocialLinkRequired) {
                _authState.value = currentState.copy(error = it.message ?: "Auth failed")
            } else {
                _authState.value = AuthState.Error(it.message ?: "Auth failed")
            }
        }
    }

    fun linkSocial(password: String) {
        val currentState = _authState.value as? AuthState.SocialLinkRequired ?: return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.linkSocial(
                com.bignerdranch.android.reshalaalfa01.data.remote.dto.LinkSocialRequest(
                    email = currentState.email,
                    password = password,
                    provider = currentState.provider,
                    providerUserId = currentState.providerUserId
                )
            )
            handleAuthResult(result, currentState.email)
        }
    }

    fun register(email: String, password: String, confirm: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(RegisterRequest(email, password, confirm))
            if (result.isSuccess) {
                _authState.value = AuthState.AwaitingVerification(email)
                startResendTimer()
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.forgotPassword(email)
            if (result.isSuccess) {
                _authState.value = AuthState.AwaitingPasswordReset(email)
                startResendTimer()
            } else {
                _authState.value = AuthState.Error("Forgot password failed")
            }
        }
    }

    fun resetPassword(token: String, pass: String, confirm: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.resetPassword(ResetPasswordRequest(token, pass, confirm))
            if (result.isSuccess) {
                _authState.value = AuthState.PasswordResetSuccess
            } else {
                _authState.value = AuthState.Error("Reset password failed")
            }
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        _resendTimer.value = 300
        timerJob = viewModelScope.launch {
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value -= 1
            }
        }
    }

    fun resendConfirmation(email: String? = null) {
        val targetEmail = email ?: (authState.value as? AuthState.AwaitingVerification)?.email
        if (targetEmail != null) {
            viewModelScope.launch {
                repository.resendConfirmation(targetEmail)
                startResendTimer()
            }
        }
    }

    fun handleDeepLink(uri: android.net.Uri) {
        if (uri.path?.contains("confirm-email") == true) {
            val token = uri.getQueryParameter("token")
            if (token != null) confirmEmail(token)
        } else if (uri.path?.contains("reset-password") == true) {
            val token = uri.getQueryParameter("token")
            if (token != null) {
                _authState.value = AuthState.ResetPassword(token)
            }
        }
    }

    private fun confirmEmail(token: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.confirmEmail(token)
            if (result.isSuccess) {
                _authState.value = AuthState.Unauthenticated
                _authState.value = AuthState.EmailVerified
            } else {
                _authState.value = AuthState.Error("Verification failed")
            }
        }
    }

    fun navigateToForgotPassword() {
        _authState.value = AuthState.ForgotPassword
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.logout()
            _authState.value = AuthState.Unauthenticated
            _userData.value = null
        }
    }

    fun deleteRecognition(taskId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecognition(taskId).onSuccess {
                onSuccess()
            }
        }
    }
    
    fun resetToLogin() {
        _authState.value = AuthState.Unauthenticated
    }
}
