package com.bignerdranch.android.reshalaalfa01

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bignerdranch.android.reshalaalfa01.data.AuthRepository
import com.bignerdranch.android.reshalaalfa01.data.local.AppDatabase
import com.bignerdranch.android.reshalaalfa01.data.local.TokenManager
import com.bignerdranch.android.reshalaalfa01.data.remote.AuthApiService
import com.bignerdranch.android.reshalaalfa01.data.remote.PersistentCookieJar
import com.bignerdranch.android.reshalaalfa01.ui.*
import com.bignerdranch.android.reshalaalfa01.ui.theme.ReshalaAlfa01Theme
import com.bignerdranch.android.reshalaalfa01.ui.util.Validator
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class MainActivity : ComponentActivity() {
    private val json = Json { ignoreUnknownKeys = true }
    
    private val cookieJar by lazy { PersistentCookieJar(applicationContext) }

    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .cookieJar(cookieJar)
            .authenticator(object : Authenticator {
                override fun authenticate(route: Route?, response: Response): Request? {
                    val path = response.request.url.encodedPath
                    if (path.contains("refresh") || path.contains("login")) return null
                    if (response.priorResponse != null) return null

                    return runBlocking {
                        val refreshResult = repository.refresh()
                        if (refreshResult.isSuccess) {
                            val newToken = tokenManager.accessToken.firstOrNull()
                            response.request.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                        } else {
                            null
                        }
                    }
                }
            })
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8180/api/v0/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    private val apiService by lazy { retrofit.create(AuthApiService::class.java) }
    private lateinit var tokenManager: TokenManager
    private lateinit var repository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(applicationContext)
        tokenManager = TokenManager(applicationContext)
        repository = AuthRepository(apiService, tokenManager, database.recognitionDao(), json)
        viewModel = AuthViewModel(repository)
        
        handleIntent(intent)
        
        enableEdgeToEdge()
        setContent {
            ReshalaAlfa01Theme {
                AuthNavigation(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            viewModel.handleDeepLink(uri)
        }
    }
}

@Composable
fun AuthNavigation(viewModel: AuthViewModel) {
    val authState by viewModel.authState.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val history by viewModel.history.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val resendTimer by viewModel.resendTimer.collectAsState()
    
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val authOptions = remember {
        YandexAuthOptions(context, loggingEnabled = true)
    }
    val yandexAuthSdk = remember { YandexAuthSdk.create(authOptions) }

    val yandexLauncher = rememberLauncherForActivityResult(yandexAuthSdk.contract) { result ->
        when (result) {
            is YandexAuthResult.Success -> {
                viewModel.loginWithYandex(result.token.value)
            }
            is YandexAuthResult.Failure -> {}
            is YandexAuthResult.Cancelled -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = authState) {
            is AuthState.Loading -> {
                LoadingScreen()
            }
            is AuthState.Authenticated -> {
                val authNavController = rememberNavController()
                NavHost(navController = authNavController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            userData = userData,
                            history = history,
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refreshHistory() },
                            onLogout = { viewModel.logout() },
                            onShowMoreClick = { authNavController.navigate("history") },
                            onTaskClick = { taskId -> authNavController.navigate("task/$taskId") }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            history = history,
                            onTaskClick = { taskId -> authNavController.navigate("task/$taskId") },
                            onBackClick = { authNavController.popBackStack() }
                        )
                    }
                    composable("task/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")
                        val task = history.find { it.id == taskId }
                        TaskDetailScreen(
                            task = task,
                            onBackClick = { authNavController.popBackStack() }
                        )
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.padding(padding)) {
                    when (state) {
                        is AuthState.ForgotPassword -> {
                            ForgotPasswordScreen(
                                isLoading = false,
                                onSendClick = { viewModel.forgotPassword(it) },
                                onBackClick = { viewModel.resetToLogin() }
                            )
                        }
                        is AuthState.ResetPassword -> {
                            val token = state.token
                            ResetPasswordScreen(
                                isLoading = false,
                                onResetClick = { pass, confirm -> viewModel.resetPassword(token, pass, confirm) },
                                onBackClick = { viewModel.resetToLogin() }
                            )
                        }
                        is AuthState.AwaitingVerification -> {
                            val email = state.email
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text("Check your email", style = MaterialTheme.typography.headlineSmall)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "We've sent a link to $email",
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    if (resendTimer > 0) {
                                        Text("Resend in ${resendTimer / 60}:${String.format("%02d", resendTimer % 60)}")
                                    } else {
                                        Button(onClick = { viewModel.resendConfirmation() }) { Text("Resend") }
                                    }
                                    TextButton(onClick = { viewModel.resetToLogin() }) { Text("Back to Login") }
                                }
                            }
                        }
                        is AuthState.Error -> {
                            LaunchedEffect(state) {
                                snackbarHostState.showSnackbar(state.message)
                                viewModel.resetToLogin()
                            }
                        }
                        is AuthState.Unauthenticated -> {
                            NavHost(navController = navController, startDestination = "login") {
                                composable("login") {
                                    LoginScreen(
                                        isLoading = false,
                                        onNavigateToRegister = { navController.navigate("register") },
                                        onLoginClick = { email, pass, rememberMe -> 
                                            viewModel.login(email, pass, rememberMe)
                                        },
                                        onForgotPasswordClick = { viewModel.navigateToForgotPassword() },
                                        onYandexLoginClick = {
                                            yandexLauncher.launch(YandexAuthLoginOptions())
                                        }
                                    )
                                }
                                composable("register") {
                                    RegisterScreen(
                                        isLoading = false,
                                        onNavigateToLogin = { navController.navigate("login") },
                                        onRegisterClick = { email, pass, confirm -> viewModel.register(email, pass, confirm) }
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(isLoading: Boolean, onSendClick: (String) -> Unit, onBackClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    val isEmailValid = Validator.isValidEmail(email)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), 
        verticalArrangement = Arrangement.Center, 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Forgot Password", 
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email, 
            onValueChange = { email = it }, 
            label = { Text("Email") }, 
            modifier = Modifier.fillMaxWidth(), 
            enabled = !isLoading,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            isError = email.isNotEmpty() && !isEmailValid,
            supportingText = {
                if (email.isNotEmpty() && !isEmailValid) {
                    Text("Invalid email format (max 50 chars)")
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onSendClick(email) }, 
            modifier = Modifier.fillMaxWidth(), 
            enabled = !isLoading && isEmailValid,
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Send Reset Link")
        }
        TextButton(onClick = onBackClick, enabled = !isLoading) { Text("Back to Login") }
    }
}

@Composable
fun ResetPasswordScreen(isLoading: Boolean, onResetClick: (String, String) -> Unit, onBackClick: () -> Unit) {
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    
    val isPasswordValid = Validator.isValidRegisterPassword(pass)
    val isConfirmValid = confirm == pass && confirm.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), 
        verticalArrangement = Arrangement.Center, 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Reset Password", 
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = pass, 
            onValueChange = { pass = it }, 
            label = { Text("New Password") }, 
            modifier = Modifier.fillMaxWidth(), 
            enabled = !isLoading,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passVisible = !passVisible }) {
                    Text(if (passVisible) "HIDE" else "SHOW", style = MaterialTheme.typography.labelSmall)
                }
            },
            isError = pass.isNotEmpty() && !isPasswordValid,
            supportingText = {
                if (pass.isNotEmpty() && !isPasswordValid) {
                    Text("8-50 chars, must include lower, upper, digit and special char")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirm, 
            onValueChange = { confirm = it }, 
            label = { Text("Confirm New Password") }, 
            modifier = Modifier.fillMaxWidth(), 
            enabled = !isLoading,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { confirmVisible = !confirmVisible }) {
                    Text(if (confirmVisible) "HIDE" else "SHOW", style = MaterialTheme.typography.labelSmall)
                }
            },
            isError = confirm.isNotEmpty() && confirm != pass,
            supportingText = {
                if (confirm.isNotEmpty() && confirm != pass) {
                    Text("Passwords do not match")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onResetClick(pass, confirm) }, 
            modifier = Modifier.fillMaxWidth(), 
            enabled = !isLoading && isPasswordValid && isConfirmValid
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Reset Password")
        }
        TextButton(onClick = onBackClick, enabled = !isLoading) { Text("Back to Login") }
    }
}
