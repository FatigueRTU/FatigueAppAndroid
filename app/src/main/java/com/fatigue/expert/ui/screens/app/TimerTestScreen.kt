package com.fatigue.expert.ui.screens.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.fatigue.expert.ui.BiString
import kotlinx.coroutines.delay

/**
 * Timer/color test — ported from Unity Test_TimerSwitch.cs.
 * A panel cycles green (safe) → red (danger). User must tap ONLY during green.
 * Tests vigilance and impulse control. Result feeds into "matematiskais_laiks_tests".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerTestScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    var running by remember { mutableStateOf(false) }
    var isGreen by remember { mutableStateOf(true) }
    var correctTaps by remember { mutableStateOf(0) }
    var wrongTaps by remember { mutableStateOf(0) }
    var totalCycles by remember { mutableStateOf(0) }
    var done by remember { mutableStateOf(false) }
    val maxCycles = 8

    var tappedThisCycle by remember { mutableStateOf(false) }

    // Color cycle timer
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        totalCycles = 0; correctTaps = 0; wrongTaps = 0; done = false
        while (running && totalCycles < maxCycles) {
            isGreen = true
            tappedThisCycle = false
            delay(3000) // green for 3s
            if (!running) break
            isGreen = false
            tappedThisCycle = false
            delay(2000) // red for 2s
            totalCycles++
        }
        if (running) {
            running = false
            done = true
            val total = correctTaps + wrongTaps
            val accuracy = if (total > 0) correctTaps.toFloat() / total else 0f
            val score = when {
                accuracy > 0.8f -> 0f
                accuracy > 0.5f -> 1f
                else -> 2f
            }
            viewModel.surveyAnswers["sektoru_secibas_atmina_tests"] = score
            val pct = (accuracy * 100).toInt()
            viewModel.testResultDetails["sektoru_secibas_atmina_tests"] =
                "$correctTaps ${BiString("pareizi", "correct").get()} / $total ($pct%)"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BiString("Laika tests", "Timer Test").get()) },
                navigationIcon = {
                    IconButton(onClick = { running = false; onBack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!running && !done) {
                // Instructions
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    BiString(
                        "Uzklikšķini uz laukuma, kad ieraugi ZAĻU krāsu!\nKamēr laukums ir SARKANS — neklikšķini!",
                        "Tap the area when you see GREEN!\nWhile the area is RED — don't tap!"
                    ).get(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { running = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(BiString("Sākt testu", "Start Test").get(), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.weight(1f))
            } else if (running) {
                // Progress
                Text(
                    "${BiString("Cikls", "Cycle").get()} ${totalCycles + 1} / $maxCycles",
                    style = MaterialTheme.typography.titleMedium
                )
                LinearProgressIndicator(
                    progress = totalCycles.toFloat() / maxCycles,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                Text(
                    "✓ $correctTaps   ✗ $wrongTaps",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                // Tap area
                val bgColor = if (isGreen) Color(0xFF43A047) else Color(0xFFE53935)
                val label = if (isGreen) BiString("SPIED!", "TAP!") else BiString("GAIDI!", "WAIT!")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(bgColor)
                        .clickable {
                            if (!tappedThisCycle) {
                                tappedThisCycle = true
                                if (isGreen) correctTaps++ else wrongTaps++
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label.get(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            } else if (done) {
                // Results
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    BiString("Tests pabeigts!", "Test Complete!").get(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                val total = correctTaps + wrongTaps
                val accuracy = if (total > 0) (correctTaps.toFloat() / total * 100) else 0f

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${BiString("Pareizi", "Correct").get()}: $correctTaps", style = MaterialTheme.typography.titleMedium)
                        Text("${BiString("Nepareizi", "Wrong").get()}: $wrongTaps", style = MaterialTheme.typography.titleMedium)
                        Text("${BiString("Precizitāte", "Accuracy").get()}: ${String.format("%.0f", accuracy)}%",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(BiString("Atgriezties", "Return").get())
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
