package com.fatigue.expert.ui.screens.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.Language
import com.fatigue.expert.ui.S
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.currentLanguage
import com.fatigue.expert.ui.theme.*

private val preTripTitle = BiString("Aptauja pirms reisa", "Pre-trip Survey")
private val preTripDesc = BiString(
    "Aizpildiet aptauju un veiciet kognitīvos testus, lai novērtētu noguruma pakāpi pirms darba uzsākšanas.",
    "Complete the survey and cognitive tests to assess fatigue level before starting work."
)
private val monitoringTitle = BiString("Monitoringa aktivitāte", "Monitoring Activity")
private val monitoringDesc = BiString(
    "Pieslēdziet sensorus un uzsāciet reāllaika noguruma un miegainības monitoringu darba laikā.",
    "Connect sensors and start real-time fatigue and drowsiness monitoring during work."
)
private val menuExpert = BiString("Ekspertu panelis", "Expert Panel")
private val menuSettings = BiString("Iestatījumi", "Settings")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToPreTrip: () -> Unit,
    onNavigateToMonitoring: () -> Unit,
    onNavigateToExpertPanel: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToResults: () -> Unit = {},
    onNavigateToEvalSurvey: () -> Unit = {},
    onEndSession: () -> Unit,
    viewModel: FatigueViewModel
) {
    val preTripDone = viewModel.appRecommendationResult.value != null
    val monitoringDone = viewModel.lastMonitoringRecommendation.value != null
    val hasResults = preTripDone || monitoringDone

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(S.appSubtitle.get(), style = MaterialTheme.typography.titleSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    TextButton(onClick = {
                        currentLanguage.value = when (currentLanguage.value) {
                            Language.LV -> Language.EN
                            Language.EN -> Language.LV
                        }
                    }) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            when (currentLanguage.value) { Language.LV -> "EN"; Language.EN -> "LV" },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Greeting
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "${BiString("Sveiki,", "Hello,").get()} ${viewModel.userName.value}!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        BiString(
                            "Izvēlieties vienu no zemāk norādītajiem scenārijiem, lai uzsāktu noguruma novērtēšanu.",
                            "Select one of the scenarios below to start the fatigue assessment."
                        ).get(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 1. Pre-trip survey ──
            Card(
                onClick = onNavigateToPreTrip,
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (preTripDone) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (preTripDone) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            tint = LevelLow, modifier = Modifier.size(48.dp))
                    } else {
                        Icon(Icons.Default.Assignment, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(preTripTitle.get(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (preTripDone) {
                            Text(
                                BiString("✓ Pabeigts — spiediet, lai atkārtotu", "✓ Completed — tap to redo").get(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(preTripDesc.get(), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            // Pre-trip recommendation result (below the button)
            if (preTripDone) {
                val reco = viewModel.appRecommendationResult.value
                if (reco != null) {
                    val recoColor = when (Math.round(reco.value)) {
                        0L -> LevelLow
                        1L -> LevelMedium
                        2L -> LevelHigh
                        else -> LevelCritical
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = recoColor.copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Recommend, contentDescription = null, tint = recoColor,
                                modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    BiString("Pirms reisa rekomendācija", "Pre-trip recommendation").get(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    reco.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = recoColor
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. Monitoring Activity (only after pre-trip is done) ──
            if (preTripDone) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    onClick = onNavigateToMonitoring,
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (monitoringDone) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (monitoringDone) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null,
                                tint = LevelLow, modifier = Modifier.size(48.dp))
                        } else {
                            Icon(Icons.Default.Monitor, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(48.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(monitoringTitle.get(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                                color = if (monitoringDone) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (monitoringDone) {
                                Text(
                                    BiString("✓ Pabeigts — spiediet, lai atkārtotu", "✓ Completed — tap to repeat").get(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(monitoringDesc.get(), style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null,
                            tint = if (monitoringDone) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }

                // Last monitoring recommendation (below the monitoring button)
                val lastMonReco = viewModel.lastMonitoringRecommendation.value
                if (lastMonReco != null) {
                    val recoColor = when (Math.round(lastMonReco.value)) {
                        0L -> LevelLow
                        1L -> LevelMedium
                        2L -> LevelHigh
                        else -> LevelCritical
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = recoColor.copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = recoColor,
                                modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(BiString("Monitoringa rekomendācija", "Monitoring recommendation").get(),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(lastMonReco.label, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold, color = recoColor)
                            }
                        }
                    }
                }
            }

            // ── 3. Session Results (visible when any results exist) ──
            if (hasResults) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    onClick = onNavigateToResults,
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TableChart, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                BiString("Sesijas rezultāti", "Session Results").get(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                BiString("Apskatīt rezultātu tabulu un eksportēt CSV", "View results table and export CSV").get(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Other menu items ──
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MenuCard(menuExpert.get(), Icons.Default.Science, MaterialTheme.colorScheme.error, Modifier.weight(1f), onNavigateToExpertPanel)
                MenuCard(menuSettings.get(), Icons.Default.Settings, MaterialTheme.colorScheme.outline, Modifier.weight(1f), onNavigateToSettings)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // End session
            OutlinedButton(
                onClick = onEndSession,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(BiString("Beigt sesiju", "End Session").get())
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result evaluation survey
            OutlinedButton(
                onClick = onNavigateToEvalSurvey,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(BiString("Rezultātu novērtējuma aptauja", "Result Evaluation Survey").get())
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuCard(
    label: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}
