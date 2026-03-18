package com.fatigue.expert.ui.screens.app

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.BiString
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Reaction test — ported from Unity GameControl.cs / Test_TimerSwitch.cs.
 * User taps a target area when it turns GREEN. Measures reaction time in ms.
 * Result feeds into fuzzy input "reakcijas_tests".
 */

private enum class TestState { WAITING, READY, TOO_EARLY, SHOW_GREEN, RESULT, DONE }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ReactionTestScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    var state by remember { mutableStateOf(TestState.WAITING) }
    var greenShownAtNano by remember { mutableStateOf(0L) }
    var reactionTime by remember { mutableStateOf(0L) }
    var attempts by remember { mutableStateOf(mutableListOf<Long>()) }
    var round by remember { mutableStateOf(0) }
    val totalRounds = 5

    // Auto-advance from READY to SHOW_GREEN after random delay
    LaunchedEffect(state) {
        if (state == TestState.READY) {
            val waitMs = Random.nextLong(2000, 5000)
            delay(waitMs)
            if (state == TestState.READY) {
                greenShownAtNano = System.nanoTime()
                state = TestState.SHOW_GREEN
            }
        }
        if (state == TestState.TOO_EARLY) {
            delay(1500)
            state = TestState.READY
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BiString("Reakcijas tests", "Reaction Test").get()) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress
            if (state != TestState.WAITING && state != TestState.DONE) {
                Text(
                    "${BiString("Raunds", "Round").get()} ${round + 1} / $totalRounds",
                    style = MaterialTheme.typography.titleMedium
                )
                LinearProgressIndicator(
                    progress = (round).toFloat() / totalRounds,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (state) {
                TestState.WAITING -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        BiString(
                            "Nospied zaļo laukumu pēc iespējas ātrāk!\nKamēr laukums ir sarkans — gaidi.",
                            "Tap the green area as fast as you can!\nWhile the area is red — wait."
                        ).get(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { state = TestState.READY; round = 0; attempts.clear() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(BiString("Sākt testu", "Start Test").get(), style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                TestState.READY -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFE53935))
                            .clickable { state = TestState.TOO_EARLY },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            BiString("Gaidi...", "Wait...").get(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                TestState.TOO_EARLY -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFFFA000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            BiString("Par agru! Mēģini vēlreiz.", "Too early! Try again.").get(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                TestState.SHOW_GREEN -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF43A047))
                            .pointerInteropFilter { motionEvent ->
                                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                                    reactionTime = (System.nanoTime() - greenShownAtNano) / 1_000_000
                                    attempts.add(reactionTime)
                                    state = TestState.RESULT
                                    true
                                } else false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            BiString("SPIED!", "TAP!").get(),
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                TestState.RESULT -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "$reactionTime ms",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        round++
                        if (round >= totalRounds) {
                            // Save average to ViewModel
                            val avg = attempts.average()
                            // Map: <300ms → 0 (good), 300-500ms → 1 (medium), >500ms → 2 (bad)
                            val score = when {
                                avg < 300 -> 0f
                                avg < 500 -> 1f
                                else -> 2f
                            }
                            viewModel.surveyAnswers["reakcijas_tests"] = score
                            viewModel.testResultDetails["reakcijas_tests"] =
                                "${BiString("Vid.", "Avg").get()} ${avg.toLong()} ms (${totalRounds} ${BiString("mēģinājumi", "attempts").get()})"
                            state = TestState.DONE
                        } else {
                            state = TestState.READY
                        }
                    }) {
                        Text(BiString("Turpināt", "Continue").get())
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                TestState.DONE -> {
                    Spacer(modifier = Modifier.weight(1f))
                    val avg = attempts.average().toLong()
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        BiString("Tests pabeigts!", "Test Complete!").get(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        BiString("Vidējais reakcijas laiks", "Average reaction time").get() + ": $avg ms",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    attempts.forEachIndexed { i, t ->
                        Text("${BiString("Raunds", "Round").get()} ${i + 1}: $t ms", style = MaterialTheme.typography.bodyMedium)
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
}
