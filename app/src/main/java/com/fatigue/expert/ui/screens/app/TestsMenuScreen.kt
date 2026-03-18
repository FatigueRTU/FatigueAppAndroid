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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.S
import com.fatigue.expert.ui.components.ResultCard
import com.fatigue.expert.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tests menu with completion checklist.
 * Each test shows ✓ with its result once completed.
 * When all 4 tests are done, auto-evaluates the full pre-trip pipeline and shows the result.
 */

private data class TestItem(
    val key: String,
    val title: BiString,
    val description: BiString,
    val icon: ImageVector,
    val route: String
)

private val testItems = listOf(
    TestItem(
        "reakcijas_tests",
        BiString("Reakcijas tests", "Reaction Test"),
        BiString("Nospied zaļo laukumu pēc iespējas ātrāk", "Tap the green area as fast as you can"),
        Icons.Default.TouchApp,
        "reaction_test"
    ),
    TestItem(
        "matematiskais_laiks_tests",
        BiString("Aritmētikas tests", "Arithmetic Test"),
        BiString("Atrisini aritmētikas uzdevumus 60 sekundēs", "Solve arithmetic problems in 60 seconds"),
        Icons.Default.Calculate,
        "arithmetic_test"
    ),
    TestItem(
        "sektoru_atmina_test",
        BiString("Sektoru atmiņas tests", "Sector Memory Test"),
        BiString("Iegaumē un atkārto sektoru secību", "Memorize and repeat sector sequence"),
        Icons.Default.GridView,
        "memory_test"
    ),
    TestItem(
        "sektoru_secibas_atmina_tests",
        BiString("Laika tests", "Timer Test"),
        BiString("Spied tikai zaļajā fāzē, pārbauda modrību", "Tap only during green phase, tests vigilance"),
        Icons.Default.Timer,
        "timer_test"
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsMenuScreen(
    viewModel: FatigueViewModel,
    onNavigateToTest: (String) -> Unit,
    onBack: () -> Unit,
    onGoHome: () -> Unit = onBack
) {
    val answers = viewModel.surveyAnswers
    val completedCount = testItems.count { answers.containsKey(it.key) }
    val allComplete = completedCount == testItems.size
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Re-evaluate every time completedCount changes and all are done
    var lastEvaluatedCount by remember { mutableStateOf(-1) }
    LaunchedEffect(completedCount) {
        if (allComplete && lastEvaluatedCount != completedCount) {
            lastEvaluatedCount = completedCount
            // Build full subjective inputs from all survey + test answers
            val subjInputs = listOf(
                "nakts_darbs", "stress", "paaugstinats_asinsspiediens",
                "smadzenu_darbibas_traucejumi", "lieto_uzmundrinosus_dzerienus",
                "lieto_nomierinosus_lidzeklus", "apnoja", "negaidita_aizmigsana",
                "aptauja", "reakcijas_tests", "sektoru_atmina_test",
                "sektoru_secibas_atmina_tests", "matematiskais_laiks_tests"
            ).associateWith { (answers[it] ?: 0f).toDouble() }

            val subjResult = viewModel.getScenarios().evaluateSubjective(subjInputs)
            val fatigueResult = viewModel.getScenarios().evaluateFatigue(
                Math.round(subjResult.value).toDouble(), 0.0
            )
            val recoResult = viewModel.getScenarios().evaluateRecommendation(
                Math.round(fatigueResult.value).toDouble(), 0.0
            )

            viewModel.appSubjectiveResult.value = subjResult
            viewModel.appFatigueResult.value = fatigueResult
            viewModel.appRecommendationResult.value = recoResult
            viewModel.preTripTimestamp.value = System.currentTimeMillis()

            delay(100)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(BiString("Kognitīvie testi", "Cognitive Tests").get(), style = MaterialTheme.typography.titleMedium)
                        val completed = testItems.count { answers.containsKey(it.key) }
                        Text("$completed / ${testItems.size} ${BiString("pabeigti", "completed").get()}",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                BiString(
                    "Veiciet visus 4 testus, lai pabeigtu pirms reisa novērtējumu. Testu rezultāti tiek izmantoti noguruma pakāpes noteikšanā.",
                    "Complete all 4 tests to finish the pre-trip assessment. Test results are used in fatigue level determination."
                ).get(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Test cards with completion status
            testItems.forEach { test ->
                val isComplete = answers.containsKey(test.key)
                val score = answers[test.key]
                val levelLabel = when (score?.toInt()) {
                    0 -> BiString("Zems", "Low")
                    1 -> BiString("Vidējs", "Medium")
                    2 -> BiString("Augsts", "High")
                    else -> null
                }
                val levelColor = when (score?.toInt()) { 0 -> LevelLow; 1 -> LevelMedium; else -> LevelHigh }
                val rawDetail = viewModel.testResultDetails[test.key]

                Card(
                    onClick = { if (!isComplete) onNavigateToTest(test.route) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isComplete) 0.dp else 2.dp),
                    colors = if (isComplete) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                             else CardDefaults.cardColors()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status icon
                        if (isComplete) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = levelColor, modifier = Modifier.size(40.dp))
                        } else {
                            Icon(test.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(test.title.get(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (isComplete) {
                                // Raw test result (e.g., "Avg 320 ms", "4/5 correct", "85%")
                                if (rawDetail != null) {
                                    Text(
                                        rawDetail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // Decision rating
                                if (levelLabel != null) {
                                    Text(
                                        "${BiString("Novērtējums", "Rating").get()}: ${levelLabel.get()} (${score?.toInt()})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = levelColor
                                    )
                                }
                            } else {
                                Text(test.description.get(), style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (!isComplete) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Skip / quick-set remaining tests ──
            val incompleteTests = testItems.filter { !answers.containsKey(it.key) }
            if (incompleteTests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    BiString("Izlaist atlikušos testus ar rezultātu:", "Skip remaining tests with result:").get(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0f to BiString("Z (Zems)", "L (Low)"),
                        1f to BiString("V (Vidējs)", "M (Med)"),
                        2f to BiString("A (Augsts)", "H (High)")
                    ).forEach { (value, label) ->
                        OutlinedButton(
                            onClick = {
                                incompleteTests.forEach { test ->
                                    viewModel.surveyAnswers[test.key] = value
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(label.get(), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // ── Final result when all tests complete ──
            if (allComplete && viewModel.appFatigueResult.value != null) {
                Spacer(modifier = Modifier.height(24.dp))

                val fatigue = viewModel.appFatigueResult.value
                val reco = viewModel.appRecommendationResult.value
                val subj = viewModel.appSubjectiveResult.value

                if (fatigue != null && reco != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                BiString("Pirms reisa novērtējuma rezultāts", "Pre-trip Assessment Result").get(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    subj?.let {
                        ResultCard(S.moduleSubjective.get(), it, Icons.Default.Psychology)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    ResultCard(S.moduleFatigue.get(), fatigue, Icons.Default.Hotel)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultCard(S.moduleRecommendation.get(), reco, Icons.Default.Recommend)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onGoHome,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            BiString("Atgriezties sākumā", "Return to Main").get(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
