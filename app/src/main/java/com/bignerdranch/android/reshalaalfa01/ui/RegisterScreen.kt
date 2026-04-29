package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.reshalaalfa01.ui.util.Validator

@Composable
fun RegisterScreen(
    isLoading: Boolean,
    onNavigateToLogin: () -> Unit,
    onRegisterClick: (String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val isEmailValid = Validator.isValidEmail(email)
    val isPasswordValid = Validator.isValidRegisterPassword(password)
    val isConfirmPasswordValid = confirmPassword == password && confirmPassword.isNotEmpty()
    
    val canRegister = isEmailValid && isPasswordValid && isConfirmPasswordValid && !isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Register", 
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
                    Text("8-50 chars, must include lower, upper, digit and special char")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isError = confirmPassword.isNotEmpty() && confirmPassword != password,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { confirmVisible = !confirmVisible }) {
                    Text(if (confirmVisible) "HIDE" else "SHOW", style = MaterialTheme.typography.labelSmall)
                }
            },
            supportingText = {
                if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                    Text("Passwords do not match")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onRegisterClick(email, password, confirmPassword) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canRegister
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register")
            }
        }
        TextButton(
            onClick = onNavigateToLogin,
            enabled = !isLoading
        ) {
            Text("Already have an account? Login")
        }
    }
}
