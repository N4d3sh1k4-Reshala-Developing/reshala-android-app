package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
import com.bignerdranch.android.reshalaalfa01.R
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
            stringResource(R.string.login), 
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
                    Text(stringResource(R.string.password_required))
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
                Text(stringResource(R.string.remember_me), style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(
                onClick = onForgotPasswordClick,
                enabled = !isLoading
            ) {
                Text(stringResource(R.string.forgot_password_q))
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
                Text(stringResource(R.string.login))
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
            Text(stringResource(R.string.sign_in_yandex))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onNavigateToRegister,
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.dont_have_account))
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
