package com.example.soundforsilence.presentation.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.soundforsilence.core.AppLanguage
import com.example.soundforsilence.core.stringsFor
import com.example.soundforsilence.presentation.profile.child.ChildProfileViewModel
import com.example.soundforsilence.presentation.profile.parent.ProfileViewModel

@Composable
fun SettingsScreen(
    onLogoutSuccess: () -> Unit,
    onProfileClick: () -> Unit,
    onChildClick: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,

    // 🌐 language props from parent
    currentLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,

    viewModel: SettingsViewModel = hiltViewModel()
) {
    // Localized strings
    val strings = stringsFor(currentLanguage)

    // 🔹 Observe current logged in user from AuthRepository
    val currentUser by viewModel.currentUser.collectAsState(initial = null)

    // Profile + child VMs (still used for image + other details)
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileState by profileViewModel.state.collectAsState()

    val childViewModel: ChildProfileViewModel = hiltViewModel()
    val childState by childViewModel.state.collectAsState()

    // Load once when screen appears
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        childViewModel.loadChildProfile()
    }

    var notificationsEnabled by remember { mutableStateOf(true) }
    val isLoggingOut = viewModel.isLoggingOut
    val logoutError = viewModel.logoutError

    // 🌐 language dialog state
    var showLanguageDialog by remember { mutableStateOf(false) }
    var tempSelectedLanguage by remember { mutableStateOf(currentLanguage) }

    // 🔹 Decide what to show for parent & child name:
    val displayParentName = when {
        !currentUser?.name.isNullOrBlank() -> currentUser!!.name
        profileState.name.isNotBlank() -> profileState.name
        else -> "Rakesh Kumar"
    }

    val displayChildName = when {
        !currentUser?.childName.isNullOrBlank() -> currentUser!!.childName
        childState.name.isNotBlank() -> childState.name
        else -> "Abhishek Verma"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // Top title
        Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(16.dp))

        // PROFILE SECTION
        Text(
            text = "PROFILE",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        // My Profile card
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.profileTitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = displayParentName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!profileState.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profileState.imageUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Child details card
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChildClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.childDetailsTitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = displayChildName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // APP SETTINGS SECTION
        Text(
            text = "APP SETTINGS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        // 🌐 Language card – make the ROW clickable, not the Card
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        tempSelectedLanguage = currentLanguage
                        showLanguageDialog = true
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.language,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "English"
                            AppLanguage.HINDI -> "हिन्दी"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Notifications
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = if (notificationsEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    thumbContent = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null
                        )
                    }
                )
            }
        }
/*
        // Dark Mode
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.darkMode,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = if (isDarkTheme) "On" else "Off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeChanged(it) }
                )
            }
        }
*/
        Spacer(Modifier.height(24.dp))

        // LOGOUT
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoggingOut) {
                        viewModel.onLogoutClick(onLogoutSuccess)
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.logout,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )

                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        if (logoutError != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = logoutError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // 🌐 Language selection dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Choose App Language") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempSelectedLanguage = AppLanguage.ENGLISH }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempSelectedLanguage == AppLanguage.ENGLISH,
                            onClick = { tempSelectedLanguage = AppLanguage.ENGLISH }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("English")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempSelectedLanguage = AppLanguage.HINDI }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempSelectedLanguage == AppLanguage.HINDI,
                            onClick = { tempSelectedLanguage = AppLanguage.HINDI }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("हिन्दी")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onLanguageChanged(tempSelectedLanguage)
                    showLanguageDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(content = content)
    }
}



