package com.fatigue.expert.ui.screens

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
import com.fatigue.expert.ui.Language
import com.fatigue.expert.ui.S
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.currentLanguage
import com.fatigue.expert.ui.components.DecisionTreeDropdown
import com.fatigue.expert.ui.components.scenario1Diagram
import com.fatigue.expert.ui.components.scenario2Diagram
import com.fatigue.expert.ui.components.scenario3Diagram

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToModule: (String) -> Unit,
    onNavigateToScenario: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(S.appTitle.get()) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, S.back.get())
                        }
                    }
                },
                actions = {
                    // Language toggle
                    TextButton(
                        onClick = {
                            currentLanguage.value = when (currentLanguage.value) {
                                Language.LV -> Language.EN
                                Language.EN -> Language.LV
                            }
                        }
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            when (currentLanguage.value) {
                                Language.LV -> "EN"
                                Language.EN -> "LV"
                            },
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
            // Expert panel description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        S.mainPageTip.get(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Chained Scenarios (Thesis Table 5.3) ──
            Text(
                S.scenarios.get(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                S.scenariosSubtitle.get(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Scenario 1: Pirms reisa aptauja
            ScenarioCard(
                title = S.scenario1Title.get(),
                description = S.scenario1Desc.get(),
                icon = Icons.Default.Assignment,
                flow = S.scenario1Flow.get(),
                onClick = { onNavigateToScenario("full") }
            )
            DecisionTreeDropdown(
                title = BiString("Lēmumu plūsmas diagramma", "Decision flow diagram").get(),
                diagram = scenario1Diagram
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Scenario 2: Monitorings ar rekomendācijām
            ScenarioCard(
                title = S.scenario2Title.get(),
                description = S.scenario2Desc.get(),
                icon = Icons.Default.Monitor,
                flow = S.scenario2Flow.get(),
                onClick = { onNavigateToScenario("monitoring") }
            )
            DecisionTreeDropdown(
                title = BiString("Lēmumu plūsmas diagramma", "Decision flow diagram").get(),
                diagram = scenario2Diagram
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Scenario 3: Apsteidzošā trauksme
            ScenarioCard(
                title = S.scenario3Title.get(),
                description = S.scenario3Desc.get(),
                icon = Icons.Default.FlashOn,
                flow = S.scenario3Flow.get(),
                onClick = { onNavigateToScenario("quick") }
            )
            DecisionTreeDropdown(
                title = BiString("Lēmumu plūsmas diagramma", "Decision flow diagram").get(),
                diagram = scenario3Diagram
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Individual Modules ──
            Text(
                S.individualModules.get(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                S.individualModulesSubtitle.get(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModuleChip(S.moduleSubjective.get(), "subjective", Icons.Default.Psychology, Modifier.weight(1f), onNavigateToModule)
                ModuleChip(S.moduleObjective.get(), "objective", Icons.Default.Sensors, Modifier.weight(1f), onNavigateToModule)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModuleChip(S.moduleFatigue.get(), "fatigue", Icons.Default.Hotel, Modifier.weight(1f), onNavigateToModule)
                ModuleChip(S.moduleAlert.get(), "alert", Icons.Default.Warning, Modifier.weight(1f), onNavigateToModule)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModuleChip(S.moduleRecommendation.get(), "recommendation", Icons.Default.Recommend, Modifier.weight(1f), onNavigateToModule)
                ModuleChip(S.moduleNonstandard.get(), "nonstandard", Icons.Default.ReportProblem, Modifier.weight(1f), onNavigateToModule)
            }

            
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScenarioCard(
    title: String,
    description: String,
    icon: ImageVector,
    flow: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    flow,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleChip(
    label: String,
    route: String,
    icon: ImageVector,
    modifier: Modifier,
    onNavigate: (String) -> Unit
) {
    OutlinedCard(
        onClick = { onNavigate(route) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}
