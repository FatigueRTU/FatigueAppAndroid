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
 * Scenario 3: Monitorings nestandarta situācijām ar apsteidzošo trauksmi
 * Thesis Table 5.3: Objective + Extraordinary + Fatigue + Alert (328 tests, 0 errors)
 * Flow: LP1 Objektīvā → LP2 Trauksme + Nogurums → LP3 Rekomendācija
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    val objInputDefs = listOf(
        InputDef("mirkskinasanas_biezums", S.inputBlinkFreq, desc = S.inputBlinkFreqDesc),
        InputDef("EEG_alfa_ritms", S.inputEEGAlpha, desc = S.inputEEGAlphaDesc),
        InputDef("EEG_j1", S.inputEEGJ1, desc = S.inputEEGJ1Desc),
        InputDef("EEG_j2", S.inputEEGJ2, desc = S.inputEEGJ2Desc),
        InputDef("EEG_j3", S.inputEEGJ3, desc = S.inputEEGJ3Desc),
        InputDef("EEG_j4", S.inputEEGJ4, desc = S.inputEEGJ4Desc),
    )

    var objExpanded by remember { mutableStateOf(false) }
    var alertExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.monitoringResult.value = null
        viewModel.monCurrentStep.value = 0
        objInputDefs.forEach { viewModel.monObjInputs.putIfAbsent(it.key, 0f) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(S.scenario2Title.get(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            S.scenario2Flow.get(),
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
            PipelineIndicator(
                steps = listOf(
                    S.stepSubjective.get(),
                    S.stepFatigue.get(),
                    S.stepRecommendation.get()
                ),
                currentStep = viewModel.monCurrentStep.value
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Scenario description ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    S.scenario2Info.get(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Current fatigue level ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        S.currentFatigueLevel.get(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        S.currentFatigueLevelDesc.get(),
                        style = MaterialTheme.typography.bodySmall
                    )
                    FuzzyInputSlider(
                        label = S.inputDrowsinessLevel.get(),
                        value = viewModel.monCurrentFatigue.value,
                        onValueChange = { viewModel.monCurrentFatigue.value = it },
                        description = "[miegainibas_limenis]"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── LP1: Objective inputs (collapsible, buttons always visible) ──
            CollapsibleSection(
                title = "① LP1 ${S.stepObjective.get()}",
                subtitle = S.moduleObjectiveDesc.get(),
                expanded = objExpanded,
                onToggle = { objExpanded = !objExpanded },
                alwaysVisible = {
                    QuickSetButtons(
                        onSet = { v -> objInputDefs.forEach { viewModel.monObjInputs[it.key] = v } },
                        labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get())
                    )
                }
            ) {
                objInputDefs.forEach { def ->
                    val descText = if (def.desc != null) "${def.desc.get()} [${def.key}]" else "[${def.key}]"
                    FuzzyInputSlider(
                        label = def.label.get(),
                        value = viewModel.monObjInputs[def.key] ?: 0f,
                        onValueChange = { viewModel.monObjInputs[def.key] = it },
                        description = descText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── LP2: Alert inputs (collapsible) ──
            CollapsibleSection(
                title = "② LP2 ${S.stepAlert.get()}",
                subtitle = S.moduleAlertDesc.get(),
                expanded = alertExpanded,
                onToggle = { alertExpanded = !alertExpanded }
            ) {
                FuzzyInputSlider(
                    label = S.inputEEGAssessment.get(),
                    value = viewModel.monEegAssessment.value,
                    onValueChange = { viewModel.monEegAssessment.value = it },
                    description = "[EEG_vertejums]"
                )
                FuzzyInputSlider(
                    label = S.inputEyeAssessment.get(),
                    value = viewModel.monEyeAssessment.value,
                    onValueChange = { viewModel.monEyeAssessment.value = it },
                    description = "[Acu_novertejums]"
                )
                FuzzyInputSlider(
                    label = S.inputExtraordinary.get(),
                    value = viewModel.monExtraordinarySigns.value,
                    onValueChange = { viewModel.monExtraordinarySigns.value = it },
                    valueRange = 0f..1f,
                    description = "[Neordinaras_pazimes] (0/1)"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.runMonitoring()
                    coroutineScope.launch {
                        delay(100)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Monitor, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(S.runMonitoring.get(), fontWeight = FontWeight.Bold)
            }

            val result = viewModel.monitoringResult.value
            if (result != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val totalUs = result.objective.elapsedUs + result.alert.elapsedUs + result.recommendation.elapsedUs
                SectionHeader("${S.resultsPipeline.get()} — ${S.pipelineTime.get()}: ${totalUs} µs")
                ResultCard("① LP1 ${S.moduleObjective.get()}", result.objective, Icons.Default.Sensors)
                Spacer(modifier = Modifier.height(8.dp))
                ResultCard("② LP2 ${S.moduleAlert.get()}", result.alert, Icons.Default.Warning)
                Spacer(modifier = Modifier.height(8.dp))
                ResultCard("③ LP3 ${S.moduleRecommendation.get()}", result.recommendation, Icons.Default.Recommend)
            }
        }
    }
}
