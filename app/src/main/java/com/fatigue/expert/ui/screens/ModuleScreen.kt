package com.fatigue.expert.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.S
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.components.*
import com.fatigue.expert.ui.components.DecisionTreeButton
import com.fatigue.expert.ui.theme.LevelHigh
import com.fatigue.expert.ui.theme.LevelMedium
import kotlinx.coroutines.delay

data class InputDef(
    val key: String,
    val label: BiString,
    val desc: BiString? = null,
    val min: Float = 0f,
    val max: Float = 2f,
    val steps: Int = 0
)

// Subjective inputs split into two groups (Thesis §2.1):
// Group 1: Anamnesis + pre-shift survey answers (inputs 1-9)
// Group 2: Cognitive test activity results (inputs 10-13)
private val subjSurveyInputs = listOf(
    InputDef("nakts_darbs", S.inputNightWork),
    InputDef("stress", S.inputStress),
    InputDef("paaugstinats_asinsspiediens", S.inputBloodPressure),
    InputDef("smadzenu_darbibas_traucejumi", S.inputBrainDisorders),
    InputDef("lieto_uzmundrinosus_dzerienus", S.inputStimulants),
    InputDef("lieto_nomierinosus_lidzeklus", S.inputSedatives),
    InputDef("apnoja", S.inputApnea),
    InputDef("negaidita_aizmigsana", S.inputUnexpectedSleep),
    InputDef("aptauja", S.inputSurvey),
)

private val subjTestInputs = listOf(
    InputDef("reakcijas_tests", S.inputReactionTest),
    InputDef("sektoru_atmina_test", S.inputSectorMemory),
    InputDef("sektoru_secibas_atmina_tests", S.inputSequenceMemory),
    InputDef("matematiskais_laiks_tests", S.inputMathTest),
)

private val subjectiveInputs = subjSurveyInputs + subjTestInputs

private val objectiveInputs = listOf(
    InputDef("mirkskinasanas_biezums", S.inputBlinkFreq, desc = S.inputBlinkFreqDesc),
    InputDef("EEG_alfa_ritms", S.inputEEGAlpha, desc = S.inputEEGAlphaDesc),
    InputDef("EEG_j1", S.inputEEGJ1, desc = S.inputEEGJ1Desc),
    InputDef("EEG_j2", S.inputEEGJ2, desc = S.inputEEGJ2Desc),
    InputDef("EEG_j3", S.inputEEGJ3, desc = S.inputEEGJ3Desc),
    InputDef("EEG_j4", S.inputEEGJ4, desc = S.inputEEGJ4Desc),
)

private val fatigueInputs = listOf(
    InputDef("subjektiva_komponente", S.inputSubjComponent),
    InputDef("objektiva_komponente", S.inputObjComponent),
)

private val alertInputs = listOf(
    InputDef("EEG_vertejums", S.inputEEGAssessment),
    InputDef("Acu_novertejums", S.inputEyeAssessment),
    InputDef("Neordinaras_pazimes", S.inputExtraordinary, max = 1f),
)

private val recommendationInputs = listOf(
    InputDef("miegainibas_limenis", S.inputDrowsinessLevel),
    InputDef("trauksmes_veids", S.inputAlertType, max = 3f),
)

// LP1 – Non-standard situation detection
private val extraordinaryInputs = listOf(
    InputDef("mikromiegs", S.inputMicrosleep, max = 1f),
    InputDef("narkolepsija", S.inputNarcolepsy, max = 1f),
    InputDef("miegainiba_bez_noguruma", S.inputSleepWithoutFatigue, max = 1f),
    InputDef("bezmiegs", S.inputInsomnia, max = 1f),
)

private val eegBypassInputs = listOf(
    InputDef("EEG_alfa_A_posms", S.inputAlphaAPhase, max = 1f),
    InputDef("EEG_alfa_B_posms", S.inputAlphaBPhase, max = 1f),
)

private val monitoringEegInputs = listOf(
    InputDef("EEG_alfa_ilgums", S.inputAlphaDuration, max = 5f),
    InputDef("EEG_beta_klatbutne", S.inputBetaPresence, max = 1f),
)

private fun getInputDefs(moduleName: String): List<InputDef> = when (moduleName) {
    "subjective" -> subjectiveInputs
    "objective" -> objectiveInputs
    "fatigue" -> fatigueInputs
    "alert" -> alertInputs
    "recommendation" -> recommendationInputs
    "extraordinary" -> extraordinaryInputs
    "eeg_bypass" -> eegBypassInputs
    "monitoring_eeg" -> monitoringEegInputs
    "nonstandard" -> emptyList() // handled by sub-modules
    else -> emptyList()
}

private fun getModuleTitle(moduleName: String): BiString = when (moduleName) {
    "subjective" -> S.moduleSubjective
    "objective" -> S.moduleObjective
    "fatigue" -> S.moduleFatigue
    "alert" -> S.moduleAlert
    "recommendation" -> S.moduleRecommendation
    "nonstandard" -> S.moduleNonstandard
    "extraordinary" -> S.moduleExtraordinary
    "eeg_bypass" -> S.moduleEegBypass
    "monitoring_eeg" -> S.moduleMonitoringEeg
    else -> BiString(moduleName, moduleName)
}

private fun getModuleDesc(moduleName: String): BiString = when (moduleName) {
    "subjective" -> S.moduleSubjectiveDesc
    "objective" -> S.moduleObjectiveDesc
    "fatigue" -> S.moduleFatigueDesc
    "alert" -> S.moduleAlertDesc
    "recommendation" -> S.moduleRecommendationDesc
    "nonstandard" -> S.moduleNonstandardDesc
    "extraordinary" -> S.moduleExtraordinaryDesc
    "eeg_bypass" -> S.moduleEegBypassDesc
    "monitoring_eeg" -> S.moduleMonitoringEegDesc
    else -> BiString("", "")
}

private fun getModuleInfo(moduleName: String): BiString? = when (moduleName) {
    "subjective" -> S.moduleSubjectiveInfo
    "objective" -> S.moduleObjectiveInfo
    "fatigue" -> S.moduleFatigueInfo
    "alert" -> S.moduleAlertInfo
    "recommendation" -> S.moduleRecommendationInfo
    "nonstandard" -> S.moduleNonstandardInfo
    "extraordinary" -> S.moduleExtraordinaryInfo
    "eeg_bypass" -> S.moduleEegBypassInfo
    "monitoring_eeg" -> S.moduleMonitoringEegInfo
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleScreen(
    moduleName: String,
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    val inputDefs = getInputDefs(moduleName)
    val isObjective = moduleName == "objective"

    // ── Simulation (objective module only) ──
    var simRunning by remember { mutableStateOf(false) }

    // Stop simulation when leaving the screen
    DisposableEffect(moduleName) {
        onDispose { simRunning = false }
    }

    LaunchedEffect(simRunning) {
        if (!simRunning || !isObjective) return@LaunchedEffect
        val keys = inputDefs.map { it.key }
        val max = 2.0f
        val step = 0.1f

        while (simRunning) {
            // One full sequence: increment each slider one at a time
            for (key in keys) {
                if (!simRunning) break
                val current = viewModel.moduleInputs[key] ?: 0f
                val next = (current + step).coerceAtMost(max)
                viewModel.moduleInputs[key] = Math.round(next * 10f) / 10f
                delay(100)
            }
            if (!simRunning) break

            // Evaluate after each full sequence
            viewModel.evaluateModule(moduleName)

            // All at max → reset
            val allMax = keys.all { (viewModel.moduleInputs[it] ?: 0f) >= max }
            if (allMax) {
                delay(500)
                keys.forEach { viewModel.moduleInputs[it] = 0f }
                viewModel.evaluateModule(moduleName)
            }
        }
    }

    // For auto-scrolling to results
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(moduleName) {
        viewModel.moduleInputs.clear()
        viewModel.moduleResult.value = null
        inputDefs.forEach { def ->
            viewModel.moduleInputs[def.key] = 0f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(getModuleTitle(moduleName).get(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            getModuleDesc(moduleName).get(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, S.back.get())
                    }
                }
            )
        }
    ) { padding ->
        val context = LocalContext.current
        val isAlert = moduleName == "alert"

        // ── Alert audio-visual flash state ──
        var alertFlash by remember { mutableStateOf(false) }
        var alertFlashColor by remember { mutableStateOf(Color.Transparent) }

        // Trigger alert effects when result changes (alert module only)
        val result = viewModel.moduleResult.value
        LaunchedEffect(result) {
            if (!isAlert || result == null) return@LaunchedEffect
            val rounded = Math.round(result.value)
            when {
                // Buzzer → vibrate 2x short
                rounded == 1L -> {
                    try {
                        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            (context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 100, 80), -1))
                        }
                    } catch (_: Exception) {}
                }
                // Bell → short chime tone
                rounded == 2L -> {
                    try {
                        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 40)
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                        delay(200)
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                        delay(200)
                        tg.release()
                    } catch (_: Exception) {}
                }
                // Siren + light → tone + 2x screen flash
                rounded >= 3L -> {
                    try {
                        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)
                        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)

                        alertFlashColor = LevelHigh.copy(alpha = 0.3f)
                        alertFlash = true
                        delay(200)
                        alertFlash = false
                        delay(150)
                        alertFlash = true
                        delay(200)
                        alertFlash = false

                        delay(100)
                        tg.release()
                    } catch (_: Exception) {}
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    if (alertFlash) Modifier.drawBehind {
                        drawRect(alertFlashColor)
                    } else Modifier
                )
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // ── Module info card + decision tree button ──
            val info = getModuleInfo(moduleName)
            if (info != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(info.get(), style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        DecisionTreeButton(moduleName = moduleName)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Special: Nonstandard combined module with 3 sub-modules ──
            val isNonstandard = moduleName == "nonstandard"

            if (isNonstandard) {
                // Sub-module 1: Extraordinary Signs
                NonstandardSubModule(
                    title = "① ${S.moduleExtraordinary.get()}",
                    desc = S.moduleExtraordinaryDesc.get(),
                    info = S.moduleExtraordinaryInfo.get(),
                    inputDefs = extraordinaryInputs,
                    viewModel = viewModel,
                    subModuleName = "extraordinary",
                    icon = Icons.Default.ReportProblem,
                    scrollState = scrollState,
                    coroutineScope = coroutineScope
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sub-module 2: EEG Bypass Alert
                NonstandardSubModule(
                    title = "② ${S.moduleEegBypass.get()}",
                    desc = S.moduleEegBypassDesc.get(),
                    info = S.moduleEegBypassInfo.get(),
                    inputDefs = eegBypassInputs,
                    viewModel = viewModel,
                    subModuleName = "eeg_bypass",
                    icon = Icons.Default.FlashOn,
                    scrollState = scrollState,
                    coroutineScope = coroutineScope
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sub-module 3: Monitoring EEG
                NonstandardSubModule(
                    title = "③ ${S.moduleMonitoringEeg.get()}",
                    desc = S.moduleMonitoringEegDesc.get(),
                    info = S.moduleMonitoringEegInfo.get(),
                    inputDefs = monitoringEegInputs,
                    viewModel = viewModel,
                    subModuleName = "monitoring_eeg",
                    icon = Icons.Default.Monitor,
                    scrollState = scrollState,
                    coroutineScope = coroutineScope
                )
            }

            if (!isNonstandard) {
            // ── Inputs: grouped collapsible for subjective, single collapsible for objective, flat for small modules ──
            val isSubjective = moduleName == "subjective"
            val useCollapsible = isSubjective || moduleName == "objective"

            // Shared quick-set states — slider changes clear them
            val allQS = rememberQuickSetState()

            if (isSubjective) {
                var surveyExpanded by remember { mutableStateOf(false) }
                var testsExpanded by remember { mutableStateOf(false) }
                val surveyQS = rememberQuickSetState()
                val testsQS = rememberQuickSetState()

                fun clearAllQS() { allQS.clear(); surveyQS.clear(); testsQS.clear() }

                SectionHeader(S.quickSetAll.get())
                QuickSetButtons(
                    onSet = { value ->
                        inputDefs.forEach { def -> viewModel.moduleInputs[def.key] = value.coerceAtMost(def.max) }
                        surveyQS.selected = allQS.selected; testsQS.selected = allQS.selected
                    },
                    labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()),
                    state = allQS
                )
                Spacer(modifier = Modifier.height(8.dp))

                CollapsibleSection(
                    title = S.groupSurvey.get(),
                    subtitle = "${subjSurveyInputs.size} ${S.inputVariables.get().lowercase()}",
                    expanded = surveyExpanded,
                    onToggle = { surveyExpanded = !surveyExpanded },
                    alwaysVisible = {
                        QuickSetButtons(
                            onSet = { value -> subjSurveyInputs.forEach { def -> viewModel.moduleInputs[def.key] = value.coerceAtMost(def.max) } },
                            labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()),
                            state = surveyQS
                        )
                    }
                ) {
                    subjSurveyInputs.forEach { def ->
                        FuzzyInputSlider(
                            label = def.label.get(),
                            value = viewModel.moduleInputs[def.key] ?: 0f,
                            onValueChange = { viewModel.moduleInputs[def.key] = it; clearAllQS() },
                            valueRange = def.min..def.max,
                            description = "[${def.key}]"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                CollapsibleSection(
                    title = S.groupTests.get(),
                    subtitle = "${subjTestInputs.size} ${S.inputVariables.get().lowercase()}",
                    expanded = testsExpanded,
                    onToggle = { testsExpanded = !testsExpanded },
                    alwaysVisible = {
                        QuickSetButtons(
                            onSet = { value -> subjTestInputs.forEach { def -> viewModel.moduleInputs[def.key] = value.coerceAtMost(def.max) } },
                            labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()),
                            state = testsQS
                        )
                    }
                ) {
                    subjTestInputs.forEach { def ->
                        FuzzyInputSlider(
                            label = def.label.get(),
                            value = viewModel.moduleInputs[def.key] ?: 0f,
                            onValueChange = { viewModel.moduleInputs[def.key] = it; clearAllQS() },
                            valueRange = def.min..def.max,
                            description = "[${def.key}]"
                        )
                    }
                }
            } else if (useCollapsible) {
                var slidersExpanded by remember { mutableStateOf(false) }
                CollapsibleSection(
                    title = S.inputVariables.get(),
                    subtitle = getModuleDesc(moduleName).get(),
                    expanded = slidersExpanded,
                    onToggle = { slidersExpanded = !slidersExpanded },
                    alwaysVisible = {
                        QuickSetButtons(
                            onSet = { value -> inputDefs.forEach { def -> viewModel.moduleInputs[def.key] = value.coerceAtMost(def.max) } },
                            labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()),
                            state = allQS
                        )
                    }
                ) {
                    inputDefs.forEach { def ->
                        val descText = if (def.desc != null) "${def.desc.get()} [${def.key}]" else "[${def.key}]"
                        FuzzyInputSlider(
                            label = def.label.get(),
                            value = viewModel.moduleInputs[def.key] ?: 0f,
                            onValueChange = { viewModel.moduleInputs[def.key] = it; allQS.clear() },
                            valueRange = def.min..def.max,
                            steps = def.steps,
                            description = descText
                        )
                    }
                }
            } else {
                // Small modules (fatigue, alert, recommendation) — flat layout
                SectionHeader(S.quickSetAll.get())
                if (isAlert) {
                    QuickSetButtons(
                        onSet = { value ->
                            when (value) {
                                0f -> inputDefs.forEach { viewModel.moduleInputs[it.key] = 0f }
                                1f -> {
                                    viewModel.moduleInputs["EEG_vertejums"] = 0.5f
                                    viewModel.moduleInputs["Acu_novertejums"] = 0.5f
                                    viewModel.moduleInputs["Neordinaras_pazimes"] = 0f
                                }
                                2f -> inputDefs.forEach { def -> viewModel.moduleInputs[def.key] = def.max }
                            }
                        },
                        labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()),
                        values = listOf(0f, 1f, 2f),
                        state = allQS
                    )
                } else {
                    QuickSetButtons(
                        onSet = { value -> inputDefs.forEach { def -> viewModel.moduleInputs[def.key] = value.coerceAtMost(def.max) } },
                        labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()),
                        state = allQS
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                inputDefs.forEach { def ->
                    val descText = if (def.desc != null) "${def.desc.get()} [${def.key}]" else "[${def.key}]"
                    FuzzyInputSlider(
                        label = def.label.get(),
                        value = viewModel.moduleInputs[def.key] ?: 0f,
                        onValueChange = { viewModel.moduleInputs[def.key] = it; allQS.clear() },
                        valueRange = def.min..def.max,
                        steps = def.steps,
                        description = descText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Simulation button + tip (objective module only) ──
            if (isObjective) {
                Text(
                    S.simTip.get(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { simRunning = !simRunning },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (simRunning)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    else
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        if (simRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (simRunning) S.simStop.get() else S.simStart.get(),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (simRunning) {
                    Text(
                        S.simRunning.get(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Evaluate button ──
            Button(
                onClick = {
                    viewModel.evaluateModule(moduleName)
                    coroutineScope.launch {
                        delay(100)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(S.evaluate.get(), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Result ──
            if (result != null) {
                SectionHeader(S.result.get())
                ResultCard(
                    title = getModuleTitle(moduleName).get(),
                    result = result,
                    icon = when (moduleName) {
                        "subjective" -> Icons.Default.Psychology
                        "objective" -> Icons.Default.Sensors
                        "fatigue" -> Icons.Default.Hotel
                        "alert" -> Icons.Default.Warning
                        "recommendation" -> Icons.Default.Recommend
                        "extraordinary" -> Icons.Default.ReportProblem
                        "eeg_bypass" -> Icons.Default.FlashOn
                        "monitoring_eeg" -> Icons.Default.Monitor
                        else -> Icons.Default.Assessment
                    }
                )
            }
            } // end if (!isNonstandard)
        }
    }
}

/**
 * A self-contained sub-module section for the combined Nonstandard Situations module.
 * Each sub-module has its own inputs, evaluate button, and result card.
 */
@Composable
private fun NonstandardSubModule(
    title: String,
    desc: String,
    info: String,
    inputDefs: List<InputDef>,
    viewModel: FatigueViewModel,
    subModuleName: String,
    icon: ImageVector,
    scrollState: ScrollState,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    var subResult by remember { mutableStateOf<com.fatigue.expert.engine.ModuleResult?>(null) }

    // Info card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(info, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            DecisionTreeButton(moduleName = subModuleName)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Inputs
    inputDefs.forEach { def ->
        FuzzyInputSlider(
            label = def.label.get(),
            value = viewModel.moduleInputs[def.key] ?: 0f,
            onValueChange = { viewModel.moduleInputs[def.key] = it },
            valueRange = def.min..def.max,
            description = "[${def.key}]"
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Evaluate button
    OutlinedButton(
        onClick = {
            val inputs = viewModel.moduleInputs.mapValues { it.value.toDouble() }
            subResult = when (subModuleName) {
                "extraordinary" -> viewModel.getScenarios().evaluateExtraordinarySigns(inputs)
                "eeg_bypass" -> viewModel.getScenarios().evaluateEegBypass(inputs)
                "monitoring_eeg" -> viewModel.getScenarios().evaluateMonitoringEeg(inputs)
                else -> null
            }
            coroutineScope.launch {
                delay(100)
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("${S.evaluate.get()} — ${getModuleTitle(subModuleName).get()}")
    }

    // Result
    subResult?.let { result ->
        Spacer(modifier = Modifier.height(8.dp))
        ResultCard(
            title = getModuleTitle(subModuleName).get(),
            result = result,
            icon = icon
        )
    }
}
