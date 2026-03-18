package com.fatigue.expert.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.S
import com.fatigue.expert.ui.components.*

/**
 * Scenario 1: Pirms reisa aptauja (Pre-trip survey) — simplified quick check
 * Thesis Table 5.3: Subjective → Fatigue → Recommendation
 * Direct component values: Fatigue(subj+obj) + Alert(3) → Recommendation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCheckScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.quickCheckResult.value = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(S.scenario3Title.get(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            S.scenario3Flow.get(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, S.back.get()) }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Scenario description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        S.scenario3Info.get(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        S.quickModeDesc.get(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── LP2: Fatigue inputs ──
            SectionHeader("LP2 ${S.groupFatigue.get()}")
            FuzzyInputSlider(
                label = S.inputSubjComponent.get(),
                value = viewModel.quickSubjComponent.value,
                onValueChange = { viewModel.quickSubjComponent.value = it },
                description = "[subjektiva_komponente] (${S.low.get()}=0, ${S.medium.get()}=1, ${S.high.get()}=2)"
            )
            FuzzyInputSlider(
                label = S.inputObjComponent.get(),
                value = viewModel.quickObjComponent.value,
                onValueChange = { viewModel.quickObjComponent.value = it },
                description = "[objektiva_komponente] (${S.low.get()}=0, ${S.medium.get()}=1, ${S.high.get()}=2)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── LP2: Alert inputs ──
            SectionHeader("LP2 ${S.groupAlert.get()}")
            FuzzyInputSlider(
                label = S.inputEEGAssessment.get(),
                value = viewModel.quickEegAssessment.value,
                onValueChange = { viewModel.quickEegAssessment.value = it },
                description = "[EEG_vertejums]"
            )
            FuzzyInputSlider(
                label = S.inputEyeAssessment.get(),
                value = viewModel.quickEyeAssessment.value,
                onValueChange = { viewModel.quickEyeAssessment.value = it },
                description = "[Acu_novertejums]"
            )
            FuzzyInputSlider(
                label = S.inputExtraordinary.get(),
                value = viewModel.quickExtraordinarySigns.value,
                onValueChange = { viewModel.quickExtraordinarySigns.value = it },
                valueRange = 0f..1f,
                description = "[Neordinaras_pazimes] (0/1)"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.runQuickCheck()
                    coroutineScope.launch {
                        delay(100)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(S.runQuickCheck.get(), fontWeight = FontWeight.Bold)
            }

            val result = viewModel.quickCheckResult.value
            if (result != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val totalUs = result.fatigue.elapsedUs + result.alert.elapsedUs + result.recommendation.elapsedUs
                SectionHeader("${S.resultsPipeline.get()} — ${S.pipelineTime.get()}: ${totalUs} µs")
                ResultCard("LP2 ${S.moduleFatigue.get()}", result.fatigue, Icons.Default.Hotel)
                Spacer(modifier = Modifier.height(8.dp))
                ResultCard("LP2 ${S.moduleAlert.get()}", result.alert, Icons.Default.Warning)
                Spacer(modifier = Modifier.height(8.dp))
                ResultCard("LP3 ${S.moduleRecommendation.get()}", result.recommendation, Icons.Default.Recommend)
            }
        }
    }
}
