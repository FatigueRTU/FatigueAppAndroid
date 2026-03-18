package com.fatigue.expert.ui.screens.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.engine.FatigueScenarios
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.S
import com.fatigue.expert.ui.components.ResultCard
import com.fatigue.expert.ui.theme.*

/**
 * Results screen — shows the current fatigue assessment based on survey + test data.
 * Runs the pre-trip scenario (Subjective → Fatigue → Recommendation) using collected answers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    val answers = viewModel.surveyAnswers
    val hasData = answers.isNotEmpty()

    // Auto-evaluate if we have survey data
    var evaluated by remember { mutableStateOf(false) }
    LaunchedEffect(hasData) {
        if (hasData && !evaluated) {
            // Build subjective inputs from survey answers (default 0 for missing)
            val subjInputs = listOf(
                "nakts_darbs", "stress", "paaugstinats_asinsspiediens",
                "smadzenu_darbibas_traucejumi", "lieto_uzmundrinosus_dzerienus",
                "lieto_nomierinosus_lidzeklus", "apnoja", "negaidita_aizmigsana",
                "aptauja", "reakcijas_tests", "sektoru_atmina_test",
                "sektoru_secibas_atmina_tests", "matematiskais_laiks_tests"
            ).associateWith { (answers[it] ?: 0f).toDouble() }

            // Run subjective module
            val subjResult = viewModel.getScenarios().evaluateSubjective(subjInputs)
            val fatigueResult = viewModel.getScenarios().evaluateFatigue(
                Math.round(subjResult.value).toDouble(), 0.0 // no objective data yet
            )
            val recoResult = viewModel.getScenarios().evaluateRecommendation(
                Math.round(fatigueResult.value).toDouble(), 0.0 // no alert yet
            )

            viewModel.appSubjectiveResult.value = subjResult
            viewModel.appFatigueResult.value = fatigueResult
            viewModel.appRecommendationResult.value = recoResult
            evaluated = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BiString("Rezultāti", "Results").get()) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasData) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    BiString(
                        "Nav datu. Lūdzu, vispirms aizpildiet aptauju un veiciet testus.",
                        "No data. Please complete the survey and tests first."
                    ).get(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                // Fatigue level gauge
                val fatigueResult = viewModel.appFatigueResult.value
                val recoResult = viewModel.appRecommendationResult.value

                if (fatigueResult != null) {
                    val level = Math.round(fatigueResult.value)
                    val color = when (level) { 0L -> LevelLow; 1L -> LevelMedium; else -> LevelHigh }
                    val levelText = when (level) {
                        0L -> BiString("Zems", "Low")
                        1L -> BiString("Vidējs", "Medium")
                        else -> BiString("Augsts", "High")
                    }

                    Text(
                        BiString("Miegainības līmenis", "Drowsiness Level").get(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(80.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                levelText.get(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                String.format("%.2f", fatigueResult.value),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recommendation
                if (recoResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                BiString("Rekomendācija", "Recommendation").get(),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                recoResult.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detailed results
                Text(
                    BiString("Detalizēti rezultāti", "Detailed Results").get(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                viewModel.appSubjectiveResult.value?.let {
                    ResultCard(BiString("Subjektīvā komponente", "Subjective Component").get(), it, Icons.Default.Psychology)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                fatigueResult?.let {
                    ResultCard(BiString("Noguruma pakāpe", "Fatigue Level").get(), it, Icons.Default.Hotel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                recoResult?.let {
                    ResultCard(BiString("Rekomendācija", "Recommendation").get(), it, Icons.Default.Recommend)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Survey answers summary
                if (answers.isNotEmpty()) {
                    Text(
                        BiString("Aptaujas atbildes", "Survey Answers").get(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            answers.forEach { (key, value) ->
                                val label = when (Math.round(value).toInt()) { 0 -> "Z"; 1 -> "V"; else -> "A" }
                                Text("$key: $label (${value.toInt()})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
