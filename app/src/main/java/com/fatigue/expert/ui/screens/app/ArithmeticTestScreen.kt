package com.fatigue.expert.ui.screens.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.BiString
import kotlin.random.Random

/**
 * Arithmetic test — ported from Unity/Flask arithmetic.html.
 * Displays basic math equations, user provides the answer.
 * Timed: 60 seconds. Result feeds into "matematiskais_laiks_tests".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArithmeticTestScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    var started by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    var correct by remember { mutableStateOf(0) }
    var wrong by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(60) }
    var equation by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    val totalTime = 60

    fun generateEquation() {
        val op = Random.nextInt(3)
        val a: Int; val b: Int; val res: Int
        when (op) {
            0 -> { a = Random.nextInt(1, 100); b = Random.nextInt(1, 100); res = a + b; equation = "$a + $b = ?" }
            1 -> { a = Random.nextInt(10, 100); b = Random.nextInt(1, a); res = a - b; equation = "$a − $b = ?" }
            else -> { a = Random.nextInt(2, 13); b = Random.nextInt(2, 13); res = a * b; equation = "$a × $b = ?" }
        }
        answer = res
        userInput = ""
    }

    // Timer countdown
    LaunchedEffect(started) {
        if (!started) return@LaunchedEffect
        generateEquation()
        timeLeft = totalTime
        while (timeLeft > 0 && !done) {
            kotlinx.coroutines.delay(1000)
            timeLeft--
        }
        if (!done) {
            done = true
            val total = correct + wrong
            val accuracy = if (total > 0) correct.toFloat() / total else 0f
            val score = when {
                accuracy > 0.8f && correct >= 10 -> 0f
                accuracy > 0.5f && correct >= 5 -> 1f
                else -> 2f
            }
            viewModel.surveyAnswers["matematiskais_laiks_tests"] = score
            val pct = (accuracy * 100).toInt()
            viewModel.testResultDetails["matematiskais_laiks_tests"] =
                "$correct ${BiString("pareizi", "correct").get()} / ${correct + wrong} ($pct%)"
        }
    }

    fun checkAnswer() {
        val parsed = userInput.toIntOrNull()
        if (parsed == answer) correct++ else wrong++
        generateEquation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BiString("Aritmētikas tests", "Arithmetic Test").get()) },
                navigationIcon = { IconButton(onClick = { done = true; onBack() }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!started) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    BiString(
                        "Atrisini pēc iespējas vairāk aritmētikas uzdevumu 60 sekundēs!\nSaskaitīšana, atņemšana un reizināšana.",
                        "Solve as many arithmetic problems as you can in 60 seconds!\nAddition, subtraction and multiplication."
                    ).get(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { started = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(BiString("Sākt testu", "Start Test").get(), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.weight(1f))
            } else if (done) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(BiString("Tests pabeigts!", "Test Complete!").get(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${BiString("Pareizi", "Correct").get()}: $correct", style = MaterialTheme.typography.titleMedium)
                        Text("${BiString("Nepareizi", "Wrong").get()}: $wrong", style = MaterialTheme.typography.titleMedium)
                        val total = correct + wrong
                        val acc = if (total > 0) (correct.toFloat() / total * 100) else 0f
                        Text("${BiString("Precizitāte", "Accuracy").get()}: ${String.format("%.0f", acc)}%",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(BiString("Atgriezties", "Return").get())
                }
                Spacer(modifier = Modifier.weight(1f))
            } else {
                // Timer + score
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("⏱ $timeLeft s", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = if (timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    Text("✓ $correct  ✗ $wrong", style = MaterialTheme.typography.titleMedium)
                }
                LinearProgressIndicator(
                    progress = timeLeft.toFloat() / totalTime,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Equation
                Text(
                    equation,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text(BiString("Atbilde", "Answer").get()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { checkAnswer() }),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { checkAnswer() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = userInput.isNotEmpty()
                ) {
                    Text(BiString("Pārbaudīt", "Check").get(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
