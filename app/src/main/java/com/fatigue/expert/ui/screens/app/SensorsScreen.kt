package com.fatigue.expert.ui.screens.app

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.S
import com.fatigue.expert.ui.components.*
import com.fatigue.expert.ui.screens.CollapsibleSection
import com.fatigue.expert.ui.screens.InputDef
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
private fun CheckItem(label: String, passed: Boolean?) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        when (passed) {
            true -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
            false -> Icon(Icons.Default.Cancel, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFFF44336), modifier = Modifier.size(18.dp))
            null -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

private val objInputDefs = listOf(
    InputDef("mirkskinasanas_biezums", S.inputBlinkFreq, desc = S.inputBlinkFreqDesc),
    InputDef("EEG_alfa_ritms", S.inputEEGAlpha, desc = S.inputEEGAlphaDesc),
    InputDef("EEG_j1", S.inputEEGJ1, desc = S.inputEEGJ1Desc),
    InputDef("EEG_j2", S.inputEEGJ2, desc = S.inputEEGJ2Desc),
    InputDef("EEG_j3", S.inputEEGJ3, desc = S.inputEEGJ3Desc),
    InputDef("EEG_j4", S.inputEEGJ4, desc = S.inputEEGJ4Desc),
)

private val alertKeys = listOf("EEG_vertejums", "Acu_novertejums", "Neordinaras_pazimes")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit,
    onGoHome: () -> Unit
) {
    var scanning by remember { mutableStateOf(false) }
    var scanComplete by remember { mutableStateOf(false) }
    var useSimulation by remember { mutableStateOf(false) }
    var objExpanded by remember { mutableStateOf(false) }
    var alertExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Simulation inputs
    val simInputs = remember { mutableStateMapOf<String, Float>().also { map ->
        objInputDefs.forEach { map[it.key] = 0f }
        map["EEG_vertejums"] = 0f
        map["Acu_novertejums"] = 0f
        map["Neordinaras_pazimes"] = 0f
    }}

    // Auto-simulation loop
    var simRunning by remember { mutableStateOf(false) }
    val allKeys = objInputDefs.map { it.key } + alertKeys

    LaunchedEffect(simRunning) {
        if (!simRunning) return@LaunchedEffect
        val step = 0.1f

        while (simRunning) {
            for (key in allKeys) {
                if (!simRunning) break
                val max = if (key == "Neordinaras_pazimes") 1f else 2f
                val current = simInputs[key] ?: 0f
                simInputs[key] = (Math.round((current + step).coerceAtMost(max) * 10f) / 10f)
                delay(100)
            }
            if (!simRunning) break

            // Evaluate pipeline
            simInputs.filter { it.key in objInputDefs.map { d -> d.key } }.forEach { (k, v) -> viewModel.monObjInputs[k] = v }
            viewModel.monEegAssessment.value = simInputs["EEG_vertejums"] ?: 0f
            viewModel.monEyeAssessment.value = simInputs["Acu_novertejums"] ?: 0f
            viewModel.monExtraordinarySigns.value = simInputs["Neordinaras_pazimes"] ?: 0f
            val preTripFatigue = viewModel.appFatigueResult.value
            if (preTripFatigue != null) {
                viewModel.monCurrentFatigue.value = Math.round(preTripFatigue.value).toFloat()
            }
            viewModel.runMonitoring()
            // Save last monitoring recommendation for main screen
            viewModel.monitoringResult.value?.let {
                viewModel.lastMonitoringRecommendation.value = it.recommendation
                viewModel.monitoringTimestamp.value = System.currentTimeMillis()
            }

            // Reset if all at max
            val allMax = allKeys.all { key ->
                val max = if (key == "Neordinaras_pazimes") 1f else 2f
                (simInputs[key] ?: 0f) >= max
            }
            if (allMax) {
                delay(500)
                allKeys.forEach { simInputs[it] = 0f }
                viewModel.monEegAssessment.value = 0f
                viewModel.monEyeAssessment.value = 0f
                viewModel.monExtraordinarySigns.value = 0f
            }
        }
    }

    // Scan simulation
    LaunchedEffect(scanning) {
        if (!scanning) return@LaunchedEffect
        delay(3000)
        scanning = false
        scanComplete = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (useSimulation) BiString("Monitoringa simulācija", "Monitoring Simulation").get()
                        else BiString("Sensoru savienojums", "Sensor Connection").get()
                    )
                },
                navigationIcon = { IconButton(onClick = { simRunning = false; onBack() }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!scanComplete && !useSimulation) {
                // ── Setup instructions + scan ──
                Text(
                    BiString("Sensora sagatavošana", "Sensor Setup").get(),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Checklist
                val checks = listOf(
                    Icons.Default.BatteryChargingFull to BiString(
                        "Pārliecinieties, ka sensora baterija ir uzlādēta",
                        "Make sure the sensor battery is charged"
                    ),
                    Icons.Default.Bluetooth to BiString(
                        "Ieslēdziet Bluetooth savienojumu ierīcē",
                        "Enable Bluetooth on your device"
                    ),
                    Icons.Default.Sensors to BiString(
                        "Uzlieciet sensoru uz pieres un pārliecinieties par labu elektrodu kontaktu ar ādu",
                        "Place the sensor on the forehead and ensure good electrode-skin contact"
                    ),
                    Icons.Default.PowerSettingsNew to BiString(
                        "Ieslēdziet sensoru un gaidiet, līdz indikators mirgo",
                        "Turn on the sensor and wait for the indicator to blink"
                    ),
                )

                checks.forEach { (icon, text) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text.get(), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Connection status checks (simulated — will show after scan)
                if (scanning) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(BiString("Savienojuma pārbaude...", "Connection check...").get(),
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            CheckItem(BiString("Bluetooth savienojums", "Bluetooth connection").get(), null)
                            CheckItem(BiString("Baterijas līmenis", "Battery level").get(), null)
                            CheckItem(BiString("Elektrodu kontakts", "Electrode contact").get(), null)
                            CheckItem(BiString("Signāla kvalitāte", "Signal quality").get(), null)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    // Buttons
                    Icon(
                        Icons.Default.BluetoothDisabled,
                        contentDescription = null, modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { scanning = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(BiString("Meklēt sensorus", "Search for Sensors").get())
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { useSimulation = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Tune, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(BiString("Simulēt sensoru datus", "Simulate Sensor Data").get())
                    }
                }

            } else if (scanComplete && !useSimulation) {
                // ── Sensor not found ──
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.BluetoothDisabled, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Text(BiString("Sensors nav atrasts", "Sensor not found").get(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { scanComplete = false; scanning = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Refresh, contentDescription = null); Spacer(modifier = Modifier.width(8.dp))
                    Text(BiString("Mēģināt vēlreiz", "Try Again").get())
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { useSimulation = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Tune, contentDescription = null); Spacer(modifier = Modifier.width(8.dp))
                    Text(BiString("Simulēt sensoru datus", "Simulate Sensor Data").get())
                }
                Spacer(modifier = Modifier.weight(1f))

            } else {
                // ── Simulation mode ──
                val objQS = rememberQuickSetState()
                val alertQS = rememberQuickSetState()

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(BiString(
                            "Simulācijas režīms — iestatiet parametru vērtības manuāli vai izmantojiet automātisko simulāciju.",
                            "Simulation mode — set parameter values manually or use automatic simulation."
                        ).get(), style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Pre-trip fatigue
                val preTripFatigue = viewModel.appFatigueResult.value
                if (preTripFatigue != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(BiString("Pirms reisa noguruma pakāpe", "Pre-trip fatigue level").get(),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(preTripFatigue.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Objective parameters (collapsible)
                CollapsibleSection(
                    title = BiString("Objektīvie parametri", "Objective Parameters").get(),
                    subtitle = S.moduleObjectiveDesc.get(),
                    expanded = objExpanded,
                    onToggle = { objExpanded = !objExpanded },
                    alwaysVisible = {
                        QuickSetButtons(
                            onSet = { v -> objInputDefs.forEach { simInputs[it.key] = v } },
                            labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()),
                            state = objQS
                        )
                    }
                ) {
                    objInputDefs.forEach { def ->
                        val descText = if (def.desc != null) "${def.desc.get()} [${def.key}]" else "[${def.key}]"
                        FuzzyInputSlider(label = def.label.get(), value = simInputs[def.key] ?: 0f,
                            onValueChange = { simInputs[def.key] = it; objQS.clear() }, description = descText)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Alert parameters (collapsible)
                CollapsibleSection(
                    title = BiString("Trauksmes parametri", "Alert Parameters").get(),
                    subtitle = S.moduleAlertDesc.get(),
                    expanded = alertExpanded,
                    onToggle = { alertExpanded = !alertExpanded },
                    alwaysVisible = {
                        QuickSetButtons(
                            onSet = { v ->
                                simInputs["EEG_vertejums"] = v
                                simInputs["Acu_novertejums"] = v
                                simInputs["Neordinaras_pazimes"] = v.coerceAtMost(1f)
                            },
                            labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()),
                            state = alertQS
                        )
                    }
                ) {
                    FuzzyInputSlider(label = S.inputEEGAssessment.get(), value = simInputs["EEG_vertejums"] ?: 0f,
                        onValueChange = { simInputs["EEG_vertejums"] = it; alertQS.clear() }, description = "[EEG_vertejums]")
                    FuzzyInputSlider(label = S.inputEyeAssessment.get(), value = simInputs["Acu_novertejums"] ?: 0f,
                        onValueChange = { simInputs["Acu_novertejums"] = it; alertQS.clear() }, description = "[Acu_novertejums]")
                    FuzzyInputSlider(label = S.inputExtraordinary.get(), value = simInputs["Neordinaras_pazimes"] ?: 0f,
                        onValueChange = { simInputs["Neordinaras_pazimes"] = it; alertQS.clear() }, valueRange = 0f..1f, description = "[Neordinaras_pazimes] (0/1)")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Auto-simulation button ──
                Button(
                    onClick = { simRunning = !simRunning },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (simRunning) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(if (simRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (simRunning) S.simStop.get() else S.simStart.get(), fontWeight = FontWeight.Bold)
                }
                if (simRunning) {
                    Text(S.simRunning.get(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Manual evaluate button ──
                OutlinedButton(
                    onClick = {
                        simInputs.filter { it.key in objInputDefs.map { d -> d.key } }.forEach { (k, v) -> viewModel.monObjInputs[k] = v }
                        viewModel.monEegAssessment.value = simInputs["EEG_vertejums"] ?: 0f
                        viewModel.monEyeAssessment.value = simInputs["Acu_novertejums"] ?: 0f
                        viewModel.monExtraordinarySigns.value = simInputs["Neordinaras_pazimes"] ?: 0f
                        if (preTripFatigue != null) {
                            viewModel.monCurrentFatigue.value = Math.round(preTripFatigue.value).toFloat()
                        }
                        viewModel.runMonitoring()
                        viewModel.monitoringResult.value?.let {
                            viewModel.lastMonitoringRecommendation.value = it.recommendation
                            viewModel.monitoringTimestamp.value = System.currentTimeMillis()
                        }
                        coroutineScope.launch { delay(100); scrollState.animateScrollTo(scrollState.maxValue) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(BiString("Novērtēt manuāli", "Evaluate Manually").get())
                }

                // Results
                val result = viewModel.monitoringResult.value
                if (result != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val totalUs = result.objective.elapsedUs + result.alert.elapsedUs + result.recommendation.elapsedUs
                    SectionHeader("${S.resultsPipeline.get()} — ${S.pipelineTime.get()}: ${totalUs} µs")
                    ResultCard(S.moduleObjective.get(), result.objective, Icons.Default.Sensors)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultCard(S.moduleAlert.get(), result.alert, Icons.Default.Warning)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultCard(S.moduleRecommendation.get(), result.recommendation, Icons.Default.Recommend)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { simRunning = false; onGoHome() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(BiString("Atgriezties sākumā", "Return to Main").get())
                    }
                }
            }
        }
    }
}
