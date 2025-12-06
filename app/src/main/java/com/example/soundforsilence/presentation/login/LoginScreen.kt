package com.example.soundforsilence.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.soundforsilence.presentation.components.PrimaryButton

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val phone = viewModel.phoneNumber
    val password = viewModel.password
    val loading = viewModel.isLoading
    val error = viewModel.errorMessage

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Sound for Silence",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Parent App",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Mobile Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

//            Button(
//                onClick = { viewModel.onLoginClick(onLoginSuccess) },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !loading
//            ) {
//                if (loading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(18.dp),
//                        strokeWidth = 2.dp
//                    )
//                } else {
//                    Text("Sign In")
//                }
//            }

            PrimaryButton(
                text = "Sign In",
                isLoading = loading,
                onClick = { viewModel.onLoginClick(onLoginSuccess) }
            )

        }
    }
}
