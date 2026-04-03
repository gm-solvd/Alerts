package com.privacyalert.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.ui.tooling.preview.Preview
import com.privacyalert.android.ui.component.ShieldLogo
import com.privacyalert.android.ui.theme.ComponentSize
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.android.ui.theme.Spacing
import com.privacyalert.presentation.viewmodel.AuthUiState
import com.privacyalert.presentation.viewmodel.AuthViewModel

class RegisterScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<AuthViewModel>()
        val authState by viewModel.authState.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow

        RegisterContent(
            authState = authState,
            onRegister = { email, password -> viewModel.register(email, password) },
            onNavigateToLogin = { navigator.pop() },
            onClearError = { viewModel.clearError() },
        )
    }
}

@Composable
private fun RegisterContent(
    authState: AuthUiState,
    onRegister: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onClearError: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val isLoading = authState is AuthUiState.Loading
    val passwordsMatch = password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShieldLogo(size = 80.dp)

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(Spacing.xxl))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (authState is AuthUiState.Error) onClearError()
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (authState is AuthUiState.Error) onClearError()
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                if (authState is AuthUiState.Error) onClearError()
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !isLoading,
            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                { Text("Passwords do not match") }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (authState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = authState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        Button(
            onClick = { onRegister(email, password) },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && passwordsMatch,
            modifier = Modifier
                .fillMaxWidth()
                .height(ComponentSize.buttonHeight),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Spacing.lg),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = Spacing.xs,
                )
            } else {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterContentIdlePreview() {
    PrivacyAlertTheme {
        RegisterContent(
            authState = AuthUiState.Idle,
            onRegister = { _, _ -> },
            onNavigateToLogin = {},
            onClearError = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterContentErrorPreview() {
    PrivacyAlertTheme {
        RegisterContent(
            authState = AuthUiState.Error("Email already registered"),
            onRegister = { _, _ -> },
            onNavigateToLogin = {},
            onClearError = {},
        )
    }
}
