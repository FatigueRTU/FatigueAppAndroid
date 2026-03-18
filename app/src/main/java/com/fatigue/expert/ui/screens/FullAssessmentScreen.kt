package com.fatigue.expert.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.S
import com.fatigue.expert.ui.components.*

/**
 * Scenario 1: Pirms reisa aptauja (Pre-trip survey)
 * Thesis Table 5.3: Subjective → Fatigue → Recommendation (279 tests, 0 errors)
 * Flow: LP1 Subjektīvā → LP2 Nogurums → LP3 Rekomendācija
 *
 * Extended to full assessment matching Node-RED:
 * LP1 Subjektīvā + Objektīvā → LP2 Nogurums + Trauksme → LP3 Rekomendācija
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAssessmentScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    val subjInputDefs = listOf(
        InputDef("nakts_darbs", S.inputNightWork),
        InputDef("stress", S.inputStress),
        InputDef("paaugstinats_asinsspiediens", S.inputBloodPressure),
        InputDef("smadzenu_darbibas_traucejumi", S.inputBrainDisorders),
        InputDef("lieto_uzmundrinosus_dzerienus", S.inputStimulants),
        InputDef("lieto_nomierinosus_lidzeklus", S.inputSedatives),
        InputDef("apnoja", S.inputApnea),
        InputDef("negaidita_aizmigsana", S.inputUnexpectedSleep),
        InputDef("aptauja", S.inputSurvey),
        InputDef("reakcijas_tests", S.inputReactionTest),
        InputDef("sektoru_atmina_test", S.inputSectorMemory),
        InputDef("sektoru_secibas_atmina_tests", S.inputSequenceMemory),
        InputDef("matematiskais_laiks_tests", S.inputMathTest),
    )

    val objInputDefs = listOf(
        InputDef("mirkskinasanas_biezums", S.inputBlinkFreq, desc = S.inputBlinkFreqDesc),
        InputDef("EEG_alfa_ritms", S.inputEEGAlpha, desc = S.inputEEGAlphaDesc),
        InputDef("EEG_j1", S.inputEEGJ1, desc = S.inputEEGJ1Desc),
        InputDef("EEG_j2", S.inputEEGJ2, desc = S.inputEEGJ2Desc),
        InputDef("EEG_j3", S.inputEEGJ3, desc = S.inputEEGJ3Desc),
        InputDef("EEG_j4", S.inputEEGJ4, desc = S.inputEEGJ4Desc),
    )

    // Collapsible state
    var subjExpanded by remember { mutableStateOf(false) }
    var objExpanded by remember { mutableStateOf(false) }
    var alertExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fullAssessmentResult.value = null
        viewModel.fullCurrentStep.value = 0
        subjInputDefs.forEach { viewModel.fullSubjInputs.putIfAbsent(it.key, 0f) }
        objInputDefs.forEach { viewModel.fullObjInputs.putIfAbsent(it.key, 0f) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(S.scenario1Title.get(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            S.scenario1Flow.get(),
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
                    S.stepObjective.get(),
                    S.stepFatigue.get(),
                    S.stepAlert.get(),
                    S.stepRecommendation.get()
                ),
                currentStep = viewModel.fullCurrentStep.value
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
                    S.scenario1Info.get(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── LP1: Subjective inputs (collapsible, buttons always visible) ──
            CollapsibleSection(
                title = "① LP1 ${S.stepSubjective.get()}",
                subtitle = S.moduleSubjectiveDesc.get(),
                expanded = subjExpanded,
                onToggle = { subjExpanded = !subjExpanded },
                alwaysVisible = {
                    QuickSetButtons(
                        onSet = { v -> subjInputDefs.forEach { viewModel.fullSubjInputs[it.key] = v } },
                        labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get())
                    )
                }
            ) {
                subjInputDefs.forEach { def ->
                    FuzzyInputSlider(
                        label = def.label.get(),
                        value = viewModel.fullSubjInputs[def.key] ?: 0f,
                        onValueChange = { viewModel.fullSubjInputs[def.key] = it },
                        description = "[${def.key}]"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── LP1: Objective inputs (collapsible, buttons always visible) ──
            CollapsibleSection(
                title = "② LP1 ${S.stepObjective.get()}",
                subtitle = S.moduleObjectiveDesc.get(),
                expanded = objExpanded,
                onToggle = { objExpanded = !objExpanded },
                alwaysVisible = {
                    QuickSetButtons(
                        onSet = { v -> objInputDefs.forEach { viewModel.fullObjInputs[it.key] = v } },
                        labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get())
                    )
                }
            ) {
                objInputDefs.forEach { def ->
                    val descText = if (def.desc != null) "${def.desc.get()} [${def.key}]" else "[${def.key}]"
                    FuzzyInputSlider(
                        label = def.label.get(),
                        value = viewModel.fullObjInputs[def.key] ?: 0f,
                        onValueChange = { viewModel.fullObjInputs[def.key] = it },
                        description = descText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── LP2: Alert inputs (collapsible) ──
            CollapsibleSection(
                title = "③ LP2 ${S.stepAlert.get()}",
                subtitle = S.moduleAlertDesc.get(),
                expanded = alertExpanded,
                onToggle = { alertExpanded = !alertExpanded }
            ) {
                FuzzyInputSlider(
                    label = S.inputEEGAssessment.get(),
                    value = viewModel.fullEegAssessment.value,
                    onValueChange = { viewModel.fullEegAssessment.value = it },
                    description = "[EEG_vertejums]"
                )
                FuzzyInputSlider(
                    label = S.inputEyeAssessment.get(),
                    value = viewModel.fullEyeAssessment.value,
                    onValueChange = { viewModel.fullEyeAssessment.value = it },
                    description = "[Acu_novertejums]"
                )
                FuzzyInputSlider(
                    label = S.inputExtraordinary.get(),
                    value = viewModel.fullExtraordinarySigns.value,
                    onValueChange = { viewModel.fullExtraordinarySigns.value = it },
                    valueRange = 0f..1f,
                    description = "[Neordinaras_pazimes] (0/1)"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.runFullAssessment()
                    coroutineScope.launch {
                        delay(100)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(S.runFullAssessment.get(), fontWeight = FontWeight.Bold)
            }

            val result = viewModel.fullAssessmentResult.value
            if (result != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val totalUs = result.subjective.elapsedUs + result.objective.elapsedUs +
                    result.fatigue.elapsedUs + result.alert.elapsedUs + result.recommendation.elapsedUs
                SectionHeader("${S.resultsPipeline.get()} — ${S.pipelineTime.get()}: ${totalUs} µs")

                ResultCard("① LP1 ${S.moduleSubjective.get()}", result.subjective, Icons.Default.Psychology)
                Spacer(modifier = Modifier.height(8.dp))
                ResultCard("② LP1 ${S.moduleObjective.get()}", result.objective, Icons.Default.Sensors)
                Spacer(modifier = Modifier.height(8.dp))
                ResultCard("③ LP2 ${S.moduleFatigue.get()}", result.fatigue, Icons.Default.Hotel)
                Spacer(modifier = Modifier.height(8.dp))
                ResultCard("④ LP2 ${S.moduleAlert.get()}", result.alert, Icons.Default.Warning)
                Spacer(modifier = Modifier.height(8.dp))
                ResultCard("⑤ LP3 ${S.moduleRecommendation.get()}", result.recommendation, Icons.Default.Recommend)
            }
        }
    }
}

/**
 * Collapsible section with header button, an always-visible slot, and collapsible content.
 * The [alwaysVisible] slot (e.g. QuickSetButtons) stays visible even when collapsed.
 */
@Composable
fun CollapsibleSection(
    title: String,
    subtitle: String = "",
    expanded: Boolean,
    onToggle: () -> Unit,
    alwaysVisible: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header (always visible, clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Always-visible slot (e.g. QuickSetButtons) — shown even when collapsed
            if (alwaysVisible != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    content = alwaysVisible
                )
            }

            // Collapsible content (sliders)
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    content = content
                )
            }
        }
    }
}
