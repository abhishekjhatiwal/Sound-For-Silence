package com.example.soundforsilence.presentation.profile.child

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ChildProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🔁 Local UI flag: view mode vs edit mode
    var isEditing by remember { mutableStateOf(false) }

    // Load data once when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadChildProfile()
    }

    // ✅ On success: show snackbar, go back to view mode
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
            isEditing = false
        }
    }

    // ✅ On error: show snackbar
    LaunchedEffect(state.error) {
        state.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Show Edit / Cancel depending on mode
                    if (!isEditing) {
                        IconButton(
                            onClick = { isEditing = true },
                            enabled = !state.loading
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit child details")
                        }
                    } else {
                        TextButton(
                            onClick = { isEditing = false },
                            enabled = !state.loading
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Optional: small loading indicator at top
            if (state.loading && !isEditing && state.name.isBlank() && state.age.isBlank() && state.notes.isBlank()) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
            }

            if (!isEditing) {
                // ============================
                // 📖 VIEW MODE (read-only)
                // ============================
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (state.name.isNotBlank()) state.name else "Not set",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Age",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (state.age.isNotBlank()) state.age else "Not set",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Notes / Special Needs",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (state.notes.isNotBlank()) state.notes else "No notes added",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                // ============================
                // ✏️ EDIT MODE (form)
                // ============================
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Child Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.age,
                    onValueChange = viewModel::onAgeChanged,
                    label = { Text("Age") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChanged,
                    label = { Text("Notes / Special Needs (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.saveChildProfile() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Child Details")
                    }
                }
            }
        }
    }
}

