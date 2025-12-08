package com.example.soundforsilence.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.soundforsilence.core.AppLanguage
import com.example.soundforsilence.core.stringsFor
import com.example.soundforsilence.domain.model.Category
import com.example.soundforsilence.presentation.components.InfoCard
import com.example.soundforsilence.presentation.components.SectionHeader
import com.example.soundforsilence.presentation.profile.child.ChildProfileViewModel
import com.example.soundforsilence.presentation.profile.parent.ProfileViewModel

@Composable
fun HomeScreen(
    currentLanguage: AppLanguage,
    onCategoryClick: (String) -> Unit,
    onProgressClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onVideosClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    childViewModel: ChildProfileViewModel = hiltViewModel()
) {
    val strings = stringsFor(currentLanguage)

    val categories by viewModel.categories.collectAsState()
    val profileState by profileViewModel.state.collectAsState()
    val childState by childViewModel.state.collectAsState()

    // 👇 NEW: observe current logged-in user from AuthRepository
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        childViewModel.loadChildProfile()
    }

    // 🔹 Parent name: prefer Auth user, then profileState, then fallback
    val parentName = when {
        !currentUser?.name.isNullOrBlank() -> currentUser!!.name
        profileState.name.isNotBlank() -> profileState.name
        else -> "Rakesh Kumar Ji"
    }

    // 🔹 Child name: prefer Auth user, then childState, then fallback
    val childName = when {
        !currentUser?.childName.isNullOrBlank() -> currentUser!!.childName
        childState.name.isNotBlank() -> childState.name
        else -> "Abhishek"
    }

    val trackingText = when (currentLanguage) {
        AppLanguage.ENGLISH -> "Tracking $childName's progress"
        AppLanguage.HINDI -> "$childName की प्रोग्रेस ट्रैक कर रहे हैं"
    }

    val stagesTitle = when (currentLanguage) {
        AppLanguage.ENGLISH -> "Learning Stages"
        AppLanguage.HINDI -> "लर्निंग स्टेजेस"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Small label (like section caption)
        Text(
            text = strings.homeTitle, // "Home" / "होम"
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ✅ Parent name from logged-in user
        Text(
            text = parentName,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(4.dp))

        // ✅ Child tracking text with dynamic childName
        Text(
            text = trackingText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        // Quick stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                title = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "VIDEOS"
                    AppLanguage.HINDI -> "वीडियो"
                },
                value = "12",
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "PROGRESS"
                    AppLanguage.HINDI -> "प्रोग्रेस"
                },
                value = "65%",
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "DAY STREAK"
                    AppLanguage.HINDI -> "दिनों की स्ट्रीक"
                },
                value = "4",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        SectionHeader(title = stagesTitle)

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                category.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${category.completedVideos} of ${category.totalVideos} videos completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = category.progressPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.small)
            )
        }
    }
}






