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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.sensor.EegSignalProcessor
import com.fatigue.expert.sensor.MindWaveConnection
import com.fatigue.expert.sensor.MindWaveViewModel
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
            true -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
            false -> Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(18.dp))
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
    val mindWaveVm: MindWaveViewModel = viewModel()
    val connState by mindWaveVm.connectionState.collectAsState()
    val rawData by mindWaveVm.rawData.collectAsState()
    val objInputs by mindWaveVm.objectiveInputs.collectAsState()

    var useSimulation by remember { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }
    var pairedDevices by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var objExpanded by remember { mutableStateOf(false) }
    var alertExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Simulation inputs
    val simInputs = remember { mutableStateMapOf<String, Float>().also { map ->
        objInputDefs.forEach { map[it.key] = 0f }
        map["EEG_vertejums"] = 0f; map["Acu_novertejums"] = 0f; map["Neordinaras_pazimes"] = 0f
    }}
    var simRunning by remember { mutableStateOf(false) }
    val allKeys = objInputDefs.map { it.key } + alertKeys

    // Cleanup on leave
    DisposableEffect(Unit) { onDispose { mindWaveVm.disconnect(); simRunning = false } }

    // Auto-simulation loop (unchanged)
    LaunchedEffect(simRunning) {
        if (!simRunning) return@LaunchedEffect
        val step = 0.1f
        while (simRunning) {
            for (key in allKeys) {
                if (!simRunning) break
                val max = if (key == "Neordinaras_pazimes") 1f else 2f
                simInputs[key] = (Math.round(((simInputs[key] ?: 0f) + step).coerceAtMost(max) * 10f) / 10f)
                delay(100)
            }
            if (!simRunning) break
            evaluateMonitoring(viewModel, simInputs, objInputDefs)
            val allMax = allKeys.all { k -> (simInputs[k] ?: 0f) >= (if (k == "Neordinaras_pazimes") 1f else 2f) }
            if (allMax) { delay(500); allKeys.forEach { simInputs[it] = 0f }; viewModel.monEegAssessment.value = 0f; viewModel.monEyeAssessment.value = 0f; viewModel.monExtraordinarySigns.value = 0f }
        }
    }

    // Live monitoring: auto-evaluate when connected and signal is good
    LaunchedEffect(connState, objInputs) {
        if (connState != MindWaveConnection.ConnectionState.CONNECTED) return@LaunchedEffect
        if (!objInputs.signalGood) return@LaunchedEffect
        // Feed live data into viewModel
        val objMap = mindWaveVm.getObjectiveInputMap()
        objMap.forEach { (k, v) -> viewModel.monObjInputs[k] = v }
        val alertMap = mindWaveVm.getAlertInputMap()
        viewModel.monEegAssessment.value = alertMap["EEG_vertejums"] ?: 0f
        viewModel.monEyeAssessment.value = alertMap["Acu_novertejums"] ?: 0f
        viewModel.monExtraordinarySigns.value = 0f
        val preTripFatigue = viewModel.appFatigueResult.value
        if (preTripFatigue != null) viewModel.monCurrentFatigue.value = Math.round(preTripFatigue.value).toFloat()
        viewModel.runMonitoring()
        viewModel.monitoringResult.value?.let {
            viewModel.lastMonitoringRecommendation.value = it.recommendation
            viewModel.monitoringTimestamp.value = System.currentTimeMillis()
        }
    }

    val isLiveConnected = connState == MindWaveConnection.ConnectionState.CONNECTED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when {
                        isLiveConnected -> BiString("Tiešsaistes monitorings", "Live Monitoring").get()
                        useSimulation -> BiString("Monitoringa simulācija", "Monitoring Simulation").get()
                        else -> BiString("Sensoru savienojums", "Sensor Connection").get()
                    })
                },
                navigationIcon = { IconButton(onClick = { simRunning = false; mindWaveVm.disconnect(); onBack() }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isLiveConnected && !useSimulation) {
                // ══════════════════════════════════════
                // SCAN / CONNECT PHASE
                // ══════════════════════════════════════
                Text(BiString("Sensora sagatavošana", "Sensor Setup").get(),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                val checks = listOf(
                    Icons.Default.BatteryChargingFull to BiString("Pārliecinieties, ka sensora baterija ir uzlādēta", "Make sure the sensor battery is charged"),
                    Icons.Default.Bluetooth to BiString("Ieslēdziet Bluetooth savienojumu ierīcē", "Enable Bluetooth on your device"),
                    Icons.Default.Sensors to BiString("Uzlieciet sensoru uz pieres un pārliecinieties par labu elektrodu kontaktu", "Place sensor on forehead, ensure good electrode contact"),
                    Icons.Default.PowerSettingsNew to BiString("Ieslēdziet sensoru un gaidiet, līdz indikators mirgo", "Turn on sensor and wait for indicator to blink"),
                )
                checks.forEach { (icon, text) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text.get(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Connection state feedback
                when (connState) {
                    MindWaveConnection.ConnectionState.CONNECTING -> {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(BiString("Savienojuma izveide...", "Connecting...").get(),
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                CheckItem(BiString("Bluetooth savienojums", "Bluetooth connection").get(), null)
                                CheckItem(BiString("Signāla kvalitāte", "Signal quality").get(), null)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                    MindWaveConnection.ConnectionState.ERROR -> {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(BiString("Savienojuma kļūda", "Connection error").get(),
                            style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { scanning = false; pairedDevices = emptyList() }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(BiString("Mēģināt vēlreiz", "Try Again").get())
                        }
                    }
                    else -> {
                        // Show scan results or scan button
                        if (scanning) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(BiString("Meklē pārotos MindWave sensorus...", "Searching for paired MindWave devices...").get(),
                                style = MaterialTheme.typography.bodySmall)
                            LaunchedEffect(scanning) {
                                delay(1000) // brief delay for UX
                                pairedDevices = mindWaveVm.findPairedDevices()
                                scanning = false
                            }
                        } else if (pairedDevices.isNotEmpty()) {
                            // Found devices — show list
                            Text(BiString("Atrasti MindWave sensori:", "Found MindWave devices:").get(),
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            pairedDevices.forEach { (name, address) ->
                                Card(
                                    onClick = { mindWaveVm.connect(address) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(name, fontWeight = FontWeight.Bold)
                                            Text(address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(onClick = { scanning = true; pairedDevices = emptyList() }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(BiString("Meklēt vēlreiz", "Search again").get())
                            }
                        } else if (!scanning) {
                            // No devices found yet or initial state
                            if (pairedDevices.isEmpty() && connState == MindWaveConnection.ConnectionState.DISCONNECTED) {
                                Icon(Icons.Default.BluetoothSearching, contentDescription = null,
                                    modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            Button(onClick = { scanning = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(BiString("Meklēt MindWave sensorus", "Search for MindWave Sensors").get())
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { useSimulation = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(BiString("Simulēt sensoru datus", "Simulate Sensor Data").get())
                }

            } else if (isLiveConnected) {
                // ══════════════════════════════════════
                // LIVE MONITORING MODE
                // ══════════════════════════════════════
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.15f))) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(BiString("MindWave savienots", "MindWave Connected").get(),
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            val sigText = if (rawData.signalQuality < 50) BiString("Signāls: labs", "Signal: good").get()
                                else BiString("Signāls: vājš (${rawData.signalQuality})", "Signal: poor (${rawData.signalQuality})").get()
                            Text(sigText, style = MaterialTheme.typography.bodySmall)
                        }
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
                                    style = MaterialTheme.typography.labelSmall)
                                Text(preTripFatigue.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live EEG data display
                Text(BiString("Tiešsaistes EEG dati", "Live EEG Data").get(),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Band power bars
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        BandPowerRow("Delta (δ)", rawData.delta)
                        BandPowerRow("Theta (θ)", rawData.theta)
                        BandPowerRow("Alpha (α)", rawData.alpha)
                        BandPowerRow("Beta (β)", rawData.beta)
                        BandPowerRow("Gamma (γ)", rawData.gamma)
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Attention: ${rawData.attention}", style = MaterialTheme.typography.bodySmall)
                            Text("Meditation: ${rawData.meditation}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Processed FIS inputs
                Text(BiString("Apstrādātie FIS parametri", "Processed FIS Parameters").get(),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        FisInputRow(S.inputBlinkFreq.get(), objInputs.blinkFrequency)
                        FisInputRow(S.inputEEGAlpha.get(), objInputs.eegAlpha)
                        FisInputRow("J1 — ${BiString("Iesaiste", "Engagement").get()}", objInputs.j1)
                        FisInputRow("J2 — ${BiString("Uzmanība", "Attention").get()}", objInputs.j2)
                        FisInputRow("J3 — ${BiString("Stress", "Stress").get()}", objInputs.j3)
                        FisInputRow("J4 — ${BiString("Modrība", "Alertness").get()}", objInputs.j4)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        FisInputRow(BiString("EEG novērtējums", "EEG assessment").get(), objInputs.eegAssessment)
                        FisInputRow(BiString("Acu novērtējums", "Eye assessment").get(), objInputs.eyeAssessment)
                    }
                }

                // Results (auto-evaluated)
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
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { mindWaveVm.disconnect() }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Default.BluetoothDisabled, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(BiString("Atvienot", "Disconnect").get())
                    }
                    Button(onClick = { mindWaveVm.disconnect(); onGoHome() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(BiString("Sākums", "Home").get())
                    }
                }

            } else {
                // ══════════════════════════════════════
                // SIMULATION MODE (unchanged)
                // ══════════════════════════════════════
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

                CollapsibleSection(
                    title = BiString("Objektīvie parametri", "Objective Parameters").get(),
                    subtitle = S.moduleObjectiveDesc.get(), expanded = objExpanded,
                    onToggle = { objExpanded = !objExpanded },
                    alwaysVisible = {
                        QuickSetButtons(onSet = { v -> objInputDefs.forEach { simInputs[it.key] = v } },
                            labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()), state = objQS)
                    }
                ) {
                    objInputDefs.forEach { def ->
                        val descText = if (def.desc != null) "${def.desc.get()} [${def.key}]" else "[${def.key}]"
                        FuzzyInputSlider(label = def.label.get(), value = simInputs[def.key] ?: 0f,
                            onValueChange = { simInputs[def.key] = it; objQS.clear() }, description = descText)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                CollapsibleSection(
                    title = BiString("Trauksmes parametri", "Alert Parameters").get(),
                    subtitle = S.moduleAlertDesc.get(), expanded = alertExpanded,
                    onToggle = { alertExpanded = !alertExpanded },
                    alwaysVisible = {
                        QuickSetButtons(onSet = { v -> simInputs["EEG_vertejums"] = v; simInputs["Acu_novertejums"] = v; simInputs["Neordinaras_pazimes"] = v.coerceAtMost(1f) },
                            labels = listOf(S.levelShortLow.get(), S.levelShortMed.get(), S.levelShortHigh.get()), state = alertQS)
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

                Button(onClick = { simRunning = !simRunning }, modifier = Modifier.fillMaxWidth(),
                    colors = if (simRunning) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(if (simRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (simRunning) S.simStop.get() else S.simStart.get(), fontWeight = FontWeight.Bold)
                }
                if (simRunning) { Text(S.simRunning.get(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp)) }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(onClick = {
                    evaluateMonitoring(viewModel, simInputs, objInputDefs)
                    coroutineScope.launch { delay(100); scrollState.animateScrollTo(scrollState.maxValue) }
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(BiString("Novērtēt manuāli", "Evaluate Manually").get())
                }

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

/** Helper: evaluate monitoring pipeline from simulation inputs */
private fun evaluateMonitoring(viewModel: FatigueViewModel, simInputs: Map<String, Float>, objDefs: List<InputDef>) {
    simInputs.filter { it.key in objDefs.map { d -> d.key } }.forEach { (k, v) -> viewModel.monObjInputs[k] = v }
    viewModel.monEegAssessment.value = simInputs["EEG_vertejums"] ?: 0f
    viewModel.monEyeAssessment.value = simInputs["Acu_novertejums"] ?: 0f
    viewModel.monExtraordinarySigns.value = simInputs["Neordinaras_pazimes"] ?: 0f
    val preTripFatigue = viewModel.appFatigueResult.value
    if (preTripFatigue != null) viewModel.monCurrentFatigue.value = Math.round(preTripFatigue.value).toFloat()
    viewModel.runMonitoring()
    viewModel.monitoringResult.value?.let {
        viewModel.lastMonitoringRecommendation.value = it.recommendation
        viewModel.monitoringTimestamp.value = System.currentTimeMillis()
    }
}

/** Band power visualization row */
@Composable
private fun BandPowerRow(label: String, value: Float) {
    val maxVal = 1f // band powers are relative
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(80.dp))
        LinearProgressIndicator(
            progress = value.coerceIn(0f, maxVal) / maxVal,
            modifier = Modifier.weight(1f).height(8.dp),
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
        Text(String.format("%.2f", value), style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
    }
}

/** FIS input display row with level color */
@Composable
private fun FisInputRow(label: String, value: Double) {
    val color = when (Math.round(value)) {
        0L -> Color(0xFF4CAF50)
        1L -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    val levelText = when (Math.round(value)) {
        0L -> "Z"; 1L -> "V"; else -> "A"
    }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text("$levelText (${String.format("%.1f", value)})", style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium, color = color)
    }
}
