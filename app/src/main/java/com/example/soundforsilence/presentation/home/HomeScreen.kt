package com.example.soundforsilence.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.example.soundforsilence.domain.model.MostRecent
import java.util.*
import java.util.concurrent.TimeUnit

// small helper for "Continue" row
@Composable
fun HomeScreen(
    currentLanguage: AppLanguage,
    onCategoryClick: (String) -> Unit,
    onProgressClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onVideosClick: () -> Unit,
    onResumeVideo: (videoId: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    childViewModel: ChildProfileViewModel = hiltViewModel()
) {
    val strings = stringsFor(currentLanguage)

    // NEW: read flows (defensive)
    val categories by viewModel.categoriesWithProgress.collectAsState()
    val profileState by profileViewModel.state.collectAsState()
    val childState by childViewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val totalVideos by viewModel.totalVideosCount.collectAsState()
    val completedVideos by viewModel.completedVideosCount.collectAsState()
    val overallPercent by viewModel.overallProgressPercent.collectAsState()
    val dayStreak by viewModel.dayStreak.collectAsState()
    val mostRecent by viewModel.mostRecent.collectAsState(initial = null) // nullable MostRecent?

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        childViewModel.loadChildProfile()
    }

    val parentName = when {
        !currentUser?.name.isNullOrBlank() -> currentUser!!.name
        profileState.name.isNotBlank() -> profileState.name
        else -> "Sunita Ji"
    }

    val childName = when {
        !currentUser?.childName.isNullOrBlank() -> currentUser!!.childName
        childState.name.isNotBlank() -> childState.name
        else -> "Ravi"
    }

    val trackingText = when (currentLanguage) {
        AppLanguage.ENGLISH -> "Tracking $childName's Progress"
        AppLanguage.HINDI -> "$childName की प्रोग्रेस ट्रैक कर रहे हैं"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Text(
            text = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Welcome back,"
                AppLanguage.HINDI -> "वापसी पर स्वागत है,"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = parentName,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = trackingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))

        // Continue watching (if available)
//        mostRecent?.let { recent ->
//            ContinueWatchingRow(
//                recent = recent,
//                onResume = { onResumeVideo(recent.videoId) }
//            )
//            Spacer(Modifier.height(16.dp))
//        }

        // Quick stats — aligned cards of equal height
        StatsRow(
            totalVideos = totalVideos,
            progressPercent = overallPercent,
            dayStreak = dayStreak
        )

        Spacer(Modifier.height(20.dp))

        SectionHeader(
            title = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Learning Stages"
                AppLanguage.HINDI -> "लर्निंग स्टेजेस"
            }
        )

        Spacer(Modifier.height(12.dp))

        if (categories.isEmpty()) {
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
                    CategoryCard(category = category, onClick = { onCategoryClick(category.id) })
                }
            }
        }
    }
}

@Composable
private fun ContinueWatchingRow(recent: MostRecent, onResume: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // circular thumbnail placeholder (image loading library can replace this)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                // you can add AsyncImage here (coil) using recent.thumbnailUrl
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recent.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Last: ${formatRel(Date(recent.lastPlayedAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = (recent.progressPercent.coerceIn(0, 100) / 100f),
                        modifier = Modifier
                            .height(6.dp)
                            .width(120.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Button(onClick = onResume, shape = RoundedCornerShape(12.dp)) {
                Text(text = "Resume")
            }
        }
    }
}

@Composable
private fun StatsRow(totalVideos: Int, progressPercent: Int, dayStreak: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(label = "VIDEOS", value = totalVideos.toString(), modifier = Modifier.weight(1f))
        StatCard(label = "PROGRESS", value = "${progressPercent}%", modifier = Modifier.weight(1f))
        StatCard(label = "DAY STREAK", value = dayStreak.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(78.dp)
            .shadow(1.dp, shape = MaterialTheme.shapes.small),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${category.completedVideos}/${category.totalVideos}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if ((category as? Any) != null) { /* keep shape — lastPlayedAt may be on Category model */
                            }
                            // If Category provides lastPlayedAt: show it (defensive cast below in HomeViewModel)
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${category.completedVideos} of ${category.totalVideos} videos completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            val progress =
                if (category.totalVideos == 0) 0f else category.completedVideos.toFloat() / category.totalVideos.toFloat()
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

/** tiny relative formatter (few units) */
private fun formatRel(d: Date): String {
    val diff = Date().time - d.time
    if (diff < 0) return "just now"
    val secs = TimeUnit.MILLISECONDS.toSeconds(diff)
    if (secs < 60) return "${secs}s ago"
    val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
    if (mins < 60) return "${mins}m ago"
    val hrs = TimeUnit.MILLISECONDS.toHours(diff)
    if (hrs < 24) return "${hrs}h ago"
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return if (days <= 7) "${days}d ago" else android.text.format.DateFormat.getMediumDateFormat(
        null
    ).format(d)
}


/*
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

    // categories & user/profile state
    val categories by viewModel.categoriesWithProgress.collectAsState()
    val profileState by profileViewModel.state.collectAsState()
    val childState by childViewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // derived stats from viewModel
    val totalVideos by viewModel.totalVideosCount.collectAsState()
    val completedVideos by viewModel.completedVideosCount.collectAsState()
    val overallPercent by viewModel.overallProgressPercent.collectAsState()
    val dayStreak by viewModel.dayStreak.collectAsState()

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

        // Quick stats row (VIDEOS / PROGRESS / DAY STREAK) — live values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                title = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "VIDEOS"
                    AppLanguage.HINDI -> "वीडियो"
                },
                value = totalVideos.toString(),
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "PROGRESS"
                    AppLanguage.HINDI -> "प्रोग्रेस"
                },
                value = "$overallPercent%",
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "DAY STREAK"
                    AppLanguage.HINDI -> "दिनों की स्ट्रीक"
                },
                value = dayStreak.toString(),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        // badges column
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            // Completion badge (X/Y)
                            BadgeSmall(text = "${category.completedVideos}/${category.totalVideos}")

                            // Last played badge (if available)
                            if ((category.lastPlayedAt ?: 0L) > 0L) {
                                val rel = formatRel(Date(category.lastPlayedAt ?: 0L))
                                BadgeSmall(text = "Last: $rel")
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "${category.completedVideos} of ${category.totalVideos} videos completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(8.dp))

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

@Composable
private fun BadgeSmall(text: String) {
    Box(
        modifier = Modifier
            .padding(bottom = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** tiny relative formatter (few units) */
private fun formatRel(d: Date): String {
    val diff = Date().time - d.time
    if (diff < 0) return "just now"
    val secs = TimeUnit.MILLISECONDS.toSeconds(diff)
    if (secs < 60) return "${secs}s ago"
    val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
    if (mins < 60) return "${mins}m ago"
    val hrs = TimeUnit.MILLISECONDS.toHours(diff)
    if (hrs < 24) return "${hrs}h ago"
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return if (days <= 7) "${days}d ago" else android.text.format.DateFormat.getMediumDateFormat(null).format(d)
}



 */





