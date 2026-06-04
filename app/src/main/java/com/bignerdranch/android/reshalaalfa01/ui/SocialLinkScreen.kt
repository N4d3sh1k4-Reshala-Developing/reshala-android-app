package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.reshalaalfa01.R

@Composable
fun SocialLinkScreen(
    email: String,
    error: String?,
    isLoading: Boolean,
    onLinkClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.link_account_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.link_account_msg, email),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        if (passwordVisible) stringResource(R.string.hide) else stringResource(R.string.show),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLinkClick(password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && password.isNotEmpty(),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(R.string.link_account_button))
            }
        }

        TextButton(onClick = onBackClick, enabled = !isLoading) {
            Text(stringResource(R.string.back_to_login))
        }
    }
}
