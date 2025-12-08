package com.example.soundforsilence.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        childViewModel.loadChildProfile()
    }

    // Parent name: Auth → profile → fallback
    val parentName = when {
        !currentUser?.name.isNullOrBlank() -> currentUser!!.name
        profileState.name.isNotBlank() -> profileState.name
        else -> "Sunita Ji"
    }

    // Child name: Auth → child profile → fallback
    val childName = when {
        !currentUser?.childName.isNullOrBlank() -> currentUser!!.childName
        childState.name.isNotBlank() -> childState.name
        else -> "Ravi"
    }

    val trackingText = when (currentLanguage) {
        AppLanguage.ENGLISH -> "Tracking $childName's Progress"
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
        // “Welcome back,” line
        Text(
            text = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Welcome back,"
                AppLanguage.HINDI -> "वापसी पर स्वागत है,"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Parent name
        Text(
            text = parentName,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(4.dp))

        // Tracking child text + small icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = trackingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        // Quick stats row (VIDEOS / PROGRESS / DAY STREAK)
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

        if (categories.isEmpty()) {
            // If Firestore has no categories yet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.ENGLISH -> "No learning stages available yet."
                        AppLanguage.HINDI -> "अभी कोई लर्निंग स्टेज उपलब्ध नहीं है।"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left icon block (like the green rounded square in the design)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq, // you can switch icon per category.icon
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${category.completedVideos} of ${category.totalVideos} videos completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar
            val progress =
                if (category.totalVideos == 0) 0f
                else category.completedVideos.toFloat() / category.totalVideos.toFloat()

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.small)
            )
        }
    }
}






