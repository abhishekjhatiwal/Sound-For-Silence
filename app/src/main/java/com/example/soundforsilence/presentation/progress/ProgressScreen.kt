package com.example.soundforsilence.presentation.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.soundforsilence.core.AppLanguage
import com.example.soundforsilence.core.stringsFor
import com.example.soundforsilence.domain.model.Assessment
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(
    currentLanguage: AppLanguage,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val strings = stringsFor(currentLanguage)

    val assessments by viewModel.assessments.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = strings.progressTitle, // "Progress" / "प्रोग्रेस"
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(assessments) { assessment ->
                AssessmentCard(
                    assessment = assessment,
                    dateText = dateFormat.format(Date(assessment.assessmentDate)),
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

@Composable
private fun AssessmentCard(
    assessment: Assessment,
    dateText: String,
    currentLanguage: AppLanguage
) {
    val capLabel = when (currentLanguage) {
        AppLanguage.ENGLISH -> "CAP Score"
        AppLanguage.HINDI -> "CAP स्कोर"
    }
    val sirLabel = when (currentLanguage) {
        AppLanguage.ENGLISH -> "SIR Score"
        AppLanguage.HINDI -> "SIR स्कोर"
    }
    val capOutOf = when (currentLanguage) {
        AppLanguage.ENGLISH -> "out of 7"
        AppLanguage.HINDI -> "7 में से"
    }
    val sirOutOf = when (currentLanguage) {
        AppLanguage.ENGLISH -> "out of 5"
        AppLanguage.HINDI -> "5 में से"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Title row: "6 Month Assessment"   "15 Oct 2025"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = assessment.period,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreCard(
                    label = capLabel,
                    value = assessment.capScore.toString(),
                    outOf = capOutOf,
                    modifier = Modifier.weight(1f)
                )
                ScoreCard(
                    label = sirLabel,
                    value = assessment.sirScore.toString(),
                    outOf = sirOutOf,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScoreCard(
    label: String,
    value: String,
    outOf: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = outOf,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

