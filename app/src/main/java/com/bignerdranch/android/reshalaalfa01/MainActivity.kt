package com.bignerdranch.android.reshalaalfa01

import kotlin.getValue
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import java.util.Locale
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class MainActivity : ComponentActivity() {
    private val json = Json { ignoreUnknownKeys = true }
    var baseURL = BuildConfig.BASE_URL
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
            .baseUrl(baseURL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    private val apiService by lazy { retrofit.create(AuthApiService::class.java) }
    private lateinit var tokenManager: TokenManager
    private lateinit var repository: AuthRepository

    private val viewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(repository) as T
            }
        }
    }

    private val recognitionViewModel: RecognitionViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecognitionViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        
        val database = AppDatabase.getDatabase(applicationContext)
        tokenManager = TokenManager(applicationContext)
        repository = AuthRepository(apiService, tokenManager, database.recognitionDao(), json)

        super.onCreate(savedInstanceState)
        
        handleIntent(intent)
        
        enableEdgeToEdge()
        setContent {
            ReshalaAlfa01Theme {
                AuthNavigation(viewModel, recognitionViewModel)
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
fun AuthNavigation(viewModel: AuthViewModel, recognitionViewModel: RecognitionViewModel) {
    val authState by viewModel.authState.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val history by viewModel.history.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val resendTimer by viewModel.resendTimer.collectAsState()
    
    val recognitionState by recognitionViewModel.state.collectAsState()
    
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
                val navBackStackEntry by authNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = authNavController, 
                        startDestination = "home",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("home") {
                            HomeScreen(
                                userData = userData,
                                history = history,
                                isRefreshing = isRefreshing,
                                onRefresh = { viewModel.refreshHistory() },
                                onLogout = { viewModel.logout() },
                                onShowMoreClick = { authNavController.navigate("history") },
                                onTaskClick = { taskId -> authNavController.navigate("task/$taskId") },
                                onFeedbackClick = { task -> recognitionViewModel.startFeedback(task) }
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                history = history,
                                isRefreshing = isRefreshing,
                                onRefresh = { viewModel.refreshHistory() },
                                onTaskClick = { taskId -> authNavController.navigate("task/$taskId") },
                                onFeedbackClick = { task -> recognitionViewModel.startFeedback(task) },
                                onBackClick = { authNavController.popBackStack() }
                            )
                        }
                        composable("task/{taskId}") { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId")
                            val task = history.find { it.id == taskId }
                            TaskDetailScreen(
                                task = task,
                                onFeedbackClick = { taskEntity -> recognitionViewModel.startFeedback(taskEntity) },
                                onDeleteClick = { id ->
                                    viewModel.deleteRecognition(id) {
                                        authNavController.popBackStack()
                                    }
                                },
                                onBackClick = { authNavController.popBackStack() }
                            )
                        }
                        composable("camera") {
                            CameraScreen(
                                onClose = { authNavController.popBackStack() },
                                onResult = { bitmap, rect, uiWidth, uiHeight ->
                                    recognitionViewModel.processBitmap(bitmap, rect, uiWidth, uiHeight)
                                    authNavController.popBackStack()
                                }
                            )
                        }
                        composable("manual") {
                            ManualEntryScreen(
                                onClose = { authNavController.popBackStack() },
                                onSolve = { equation ->
                                    recognitionViewModel.solveManual(equation)
                                    authNavController.popBackStack()
                                }
                            )
                        }
                    }

                    if (currentRoute != "camera" && currentRoute != "manual") {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .navigationBarsPadding() // Автоматический отступ от системной панели навигации
                                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                tonalElevation = 3.dp,
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    IconButton(onClick = { authNavController.navigate("home") }) {
                                        Icon(
                                            Icons.Default.Home, 
                                            contentDescription = "Home",
                                            tint = if (currentRoute == "home") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Grouped Solve Actions
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        modifier = Modifier.height(52.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(onClick = { authNavController.navigate("manual") }) {
                                                Icon(
                                                    Icons.Default.Functions, 
                                                    contentDescription = "Manual Entry",
                                                    tint = if (currentRoute == "manual") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            FloatingActionButton(
                                                onClick = { authNavController.navigate("camera") },
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = Color.White,
                                                shape = androidx.compose.foundation.shape.CircleShape,
                                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                                                modifier = Modifier.size(42.dp)
                                            ) {
                                                Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }

                                    IconButton(onClick = { authNavController.navigate("history") }) {
                                        Icon(
                                            Icons.Default.History, 
                                            contentDescription = "History",
                                            tint = if (currentRoute == "history" || currentRoute?.startsWith("task/") == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                RecognitionDialog(
                    state = recognitionState,
                    onDismiss = { recognitionViewModel.reset() },
                    onAccept = { id -> recognitionViewModel.sendFeedback(id, accepted = true) },
                    onEdit = { id, text -> recognitionViewModel.sendFeedback(id, accepted = false, editedResult = text) },
                    onSuccessFinished = { recognitionViewModel.finishSuccess() }
                )
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
                        is AuthState.AwaitingPasswordReset -> {
                            val email = state.email
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Check your email", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "We've sent a password reset link to $email. Please follow the link in the email to set a new password.",
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    if (resendTimer > 0) {
                                        Text(
                                            "Resend in ${resendTimer / 60}:${String.format(Locale.getDefault(), "%02d", resendTimer % 60)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Button(
                                            onClick = { viewModel.forgotPassword(email) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Resend Link")
                                        }
                                    }
                                    TextButton(onClick = { viewModel.resetToLogin() }) {
                                        Text("Back to Login")
                                    }
                                }
                            }
                        }
                        is AuthState.AwaitingVerification -> {
                            val email = state.email
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Verify your email", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "We've sent a verification link to $email. Please click the link to activate your account.",
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    if (resendTimer > 0) {
                                        Text(
                                            "Resend in ${resendTimer / 60}:${String.format(Locale.getDefault(), "%02d", resendTimer % 60)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Button(
                                            onClick = { viewModel.resendConfirmation() },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Resend Link")
                                        }
                                    }
                                    TextButton(onClick = { viewModel.resetToLogin() }) {
                                        Text("Back to Login")
                                    }
                                }
                            }
                        }
                        is AuthState.EmailVerified -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Email Verified!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Your email has been successfully verified. You can now log in to your account.",
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Button(
                                        onClick = { viewModel.resetToLogin() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Go to Login")
                                    }
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
