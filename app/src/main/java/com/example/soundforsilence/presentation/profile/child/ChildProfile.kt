package com.example.soundforsilence.presentation.profile.child

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProfileScreen(
    onBackClick: () -> Unit
) {
    var childName by remember { mutableStateOf("") }
    var childAge by remember { mutableStateOf("") }
    var childGender by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {

            OutlinedTextField(
                value = childName,
                onValueChange = { childName = it },
                label = { Text("Child Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = childAge,
                onValueChange = { childAge = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = childGender,
                onValueChange = { childGender = it },
                label = { Text("Gender") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    // TODO: Save to Firestore
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Child Details")
            }
        }
    }
}
