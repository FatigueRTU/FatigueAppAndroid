package com.fatigue.expert.ui.screens.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.BiString
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Sector memory test — a simple "Simon Says" style pattern recall.
 * A grid of 4 sectors lights up in a sequence; user must repeat the sequence.
 * Tests short-term memory. Result feeds into "sektoru_atmina_test".
 */

// Bright, vivid colors when highlighted
private val sectorColorsLit = listOf(
    Color(0xFF4CAF50), // Green — bright
    Color(0xFF2196F3), // Blue — bright
    Color(0xFFFFEB3B), // Yellow — bright
    Color(0xFFF44336), // Red — bright
)

// Very muted/dark when idle — high contrast with lit state
private val sectorColorsDim = listOf(
    Color(0xFF1B5E20), // Green — very dark
    Color(0xFF0D47A1), // Blue — very dark
    Color(0xFF827717), // Yellow — dark olive
    Color(0xFF7F0000), // Red — very dark maroon
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorMemoryTestScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    var round by remember { mutableStateOf(0) }
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var userInput by remember { mutableStateOf(mutableListOf<Int>()) }
    var showingSequence by remember { mutableStateOf(false) }
    var highlightedSector by remember { mutableStateOf(-1) }
    var correctRounds by remember { mutableStateOf(0) }
    var started by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(-1) } // -1 = not counting, 3/2/1 = counting
    var tappedSector by remember { mutableStateOf(-1) } // brief flash on user tap
    val totalRounds = 5
    val startLength = 3

    // ── Countdown before each round ──
    LaunchedEffect(countdown) {
        if (countdown <= 0) return@LaunchedEffect
        delay(1000)
        countdown--
        if (countdown == 0) {
            // Countdown finished — start showing the sequence
            countdown = -1
            showingSequence = true
        }
    }

    // ── Show sequence animation ──
    LaunchedEffect(showingSequence, round) {
        if (!showingSequence || sequence.isEmpty()) return@LaunchedEffect
        delay(800) // pause before first highlight
        for (s in sequence) {
            highlightedSector = s
            delay(700) // show each sector
            highlightedSector = -1
            delay(400) // gap between sectors
        }
        showingSequence = false
    }

    // ── Brief flash when user taps a sector ──
    LaunchedEffect(tappedSector) {
        if (tappedSector < 0) return@LaunchedEffect
        delay(200)
        tappedSector = -1
    }

    fun startRound() {
        val len = startLength + round
        sequence = List(len) { Random.nextInt(4) }
        userInput = mutableListOf()
        countdown = 3 // 3-2-1 countdown before showing sequence
    }

    fun onSectorTap(index: Int) {
        if (showingSequence || done || countdown > 0) return
        tappedSector = index
        userInput.add(index)
        if (userInput.size == sequence.size) {
            if (userInput == sequence) correctRounds++
            round++
            if (round >= totalRounds) {
                done = true
                val score = when {
                    correctRounds >= 4 -> 0f
                    correctRounds >= 2 -> 1f
                    else -> 2f
                }
                viewModel.surveyAnswers["sektoru_atmina_test"] = score
                viewModel.testResultDetails["sektoru_atmina_test"] =
                    "$correctRounds / $totalRounds ${BiString("pareizi", "correct").get()}"
            } else {
                startRound()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BiString("Sektoru atmiņas tests", "Sector Memory Test").get()) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!started) {
                // ── Instructions screen ──
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.GridOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    BiString(
                        "Iegaumē sektoru secību un atkārto to!\n\n" +
                        "Sektori iedegsies viens pēc otra.\n" +
                        "Kad secība beigsies, pieskaries sektoriem tādā pašā secībā.\n\n" +
                        "Secība kļūst garāka ar katru raundu.\n" +
                        "Kopā $totalRounds raundi.",
                        "Memorize the sector sequence and repeat it!\n\n" +
                        "Sectors will light up one by one.\n" +
                        "When the sequence ends, tap the sectors in the same order.\n\n" +
                        "The sequence gets longer each round.\n" +
                        "$totalRounds rounds total."
                    ).get(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { started = true; startRound() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(BiString("Sākt testu", "Start Test").get(), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.weight(1f))
            } else if (done) {
                // ── Results screen ──
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(BiString("Tests pabeigts!", "Test Complete!").get(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${BiString("Pareizi", "Correct").get()}: $correctRounds / $totalRounds", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(BiString("Atgriezties", "Return").get())
                }
                Spacer(modifier = Modifier.weight(1f))
            } else {
                // ── Game screen ──
                Text("${BiString("Raunds", "Round").get()} ${round + 1} / $totalRounds",
                    style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = round.toFloat() / totalRounds,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                // Status text
                when {
                    countdown > 0 -> {
                        Text(
                            BiString("Gatavs? Secība sāksies...", "Ready? Sequence starting...").get(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    showingSequence -> {
                        Text(
                            BiString("👀 Iegaumē secību...", "👀 Memorize the sequence...").get(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {
                        Text(
                            "👆 ${BiString("Atkārto", "Repeat").get()}: ${userInput.size} / ${sequence.size}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sequence length hint
                Text(
                    "${BiString("Secības garums", "Sequence length").get()}: ${startLength + round}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                // ── Countdown overlay or sector grid ──
                // Constrain to max 280dp so it doesn't overflow the screen
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // 2x2 sector grid (always visible)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        for (row in 0..1) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                for (col in 0..1) {
                                    val idx = row * 2 + col
                                    val isHighlighted = highlightedSector == idx
                                    val isTapped = tappedSector == idx
                                    val isLit = isHighlighted || isTapped

                                    val bgColor = if (isLit) sectorColorsLit[idx] else sectorColorsDim[idx]
                                    val borderColor = if (isLit) Color.White else Color.Transparent
                                    val sectorScale = if (isHighlighted) 1.05f else 1.0f

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .scale(sectorScale)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(bgColor)
                                            .then(
                                                if (isLit) Modifier.border(
                                                    3.dp, borderColor, RoundedCornerShape(16.dp)
                                                ) else Modifier
                                            )
                                            .clickable(enabled = !showingSequence && !done && countdown <= 0) {
                                                onSectorTap(idx)
                                            }
                                    )
                                }
                            }
                        }
                    }

                    // Countdown number overlay
                    if (countdown > 0) {
                        Text(
                            "$countdown",
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
