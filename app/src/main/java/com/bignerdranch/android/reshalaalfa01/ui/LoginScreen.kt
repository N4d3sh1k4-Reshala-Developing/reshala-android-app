package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.reshalaalfa01.ui.theme.ReshalaAlfa01Theme
import com.bignerdranch.android.reshalaalfa01.ui.util.Validator

@Composable
fun LoginScreen(
    isLoading: Boolean,
    onNavigateToRegister: () -> Unit,
    onLoginClick: (String, String, Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onYandexLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val isEmailValid = Validator.isValidEmail(email)
    val isPasswordValid = Validator.isValidLoginPassword(password)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Login", 
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
            isError = email.isNotEmpty() && !isEmailValid,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            supportingText = {
                if (email.isNotEmpty() && !isEmailValid) {
                    Text("Invalid email format (max 50 chars)")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isError = password.isNotEmpty() && !isPasswordValid,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "HIDE" else "SHOW", style = MaterialTheme.typography.labelSmall)
                }
            },
            supportingText = {
                if (password.isNotEmpty() && !isPasswordValid) {
                    Text("Password required (max 50 chars)")
                }
            }
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    enabled = !isLoading
                )
                Text("Remember Me", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(
                onClick = onForgotPasswordClick,
                enabled = !isLoading
            ) {
                Text("Forgot Password?")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onLoginClick(email, password, rememberMe) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && isEmailValid && isPasswordValid
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onYandexLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFCC00),
                contentColor = Color.Black
            )
        ) {
            Text("Sign in with Yandex")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onNavigateToRegister,
            enabled = !isLoading
        ) {
            Text("Don't have an account? Register")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    ReshalaAlfa01Theme {
        LoginScreen(
            isLoading = false,
            onNavigateToRegister = {},
            onLoginClick = { _, _, _ -> },
            onForgotPasswordClick = {},
            onYandexLoginClick = {}
        )
    }
}
