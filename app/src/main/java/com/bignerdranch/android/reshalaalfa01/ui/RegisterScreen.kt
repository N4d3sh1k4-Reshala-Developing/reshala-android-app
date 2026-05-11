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
import androidx.compose.ui.res.stringResource
import com.bignerdranch.android.reshalaalfa01.R
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
            stringResource(R.string.register), 
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isError = email.isNotEmpty() && !isEmailValid,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            supportingText = {
                if (email.isNotEmpty() && !isEmailValid) {
                    Text(stringResource(R.string.email_invalid_format))
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isError = password.isNotEmpty() && !isPasswordValid,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        if (passwordVisible) stringResource(R.string.hide) else stringResource(R.string.show), 
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            supportingText = {
                if (password.isNotEmpty() && !isPasswordValid) {
                    Text(stringResource(R.string.password_hint_register))
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(R.string.confirm_password)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isError = confirmPassword.isNotEmpty() && confirmPassword != password,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { confirmVisible = !confirmVisible }) {
                    Text(
                        if (confirmVisible) stringResource(R.string.hide) else stringResource(R.string.show), 
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            supportingText = {
                if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                    Text(stringResource(R.string.passwords_dont_match))
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
                Text(stringResource(R.string.register))
            }
        }
        TextButton(
            onClick = onNavigateToLogin,
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.already_have_account))
        }
    }
}
