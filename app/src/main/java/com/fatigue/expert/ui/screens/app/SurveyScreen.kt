package com.fatigue.expert.ui.screens.app

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
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.S

/**
 * Pre-shift survey — maps to the Unity Aptauja scenes (18+5 questions).
 * Answers feed directly into the subjective component's 13 fuzzy inputs.
 * Questions are grouped into anamnesis (medical history) and pre-shift self-assessment.
 */

private data class SurveyQuestion(
    val question: BiString,
    val inputKey: String, // maps to FIS input variable
    val answers: List<BiString>, // maps to 0, 1, 2 (Z/V/A)
)

private val surveyQuestions = listOf(
    // Anamnesis group (inputs 1-8)
    SurveyQuestion(
        BiString("Vai strādājat nakts maiņās?", "Do you work night shifts?"),
        "nakts_darbs",
        listOf(
            BiString("Nē — tikai dienas maiņas", "No — day shifts only"),
            BiString("Dažreiz — 1–3 nakts maiņas mēnesī", "Sometimes — 1–3 night shifts per month"),
            BiString("Regulāri — 4+ nakts maiņas mēnesī", "Regularly — 4+ night shifts per month")
        )
    ),
    SurveyQuestion(
        BiString("Kāds ir jūsu stresa līmenis?", "What is your stress level?"),
        "stress",
        listOf(
            BiString("Zems — jūtos mierīgs, bez spiediena", "Low — feeling calm, no pressure"),
            BiString("Vidējs — periodiski saspringts", "Medium — occasionally tense"),
            BiString("Augsts — pastāvīgs spiediens, trauksme", "High — constant pressure, anxiety")
        )
    ),
    SurveyQuestion(
        BiString("Vai jums ir paaugstināts asinsspiediens?", "Do you have high blood pressure?"),
        "paaugstinats_asinsspiediens",
        listOf(
            BiString("Nē — normāls (<130/85)", "No — normal (<130/85)"),
            BiString("Dažreiz — robežvērtības (130–139/85–89)", "Sometimes — borderline (130–139/85–89)"),
            BiString("Jā — diagnosticēts (≥140/90)", "Yes — diagnosed (≥140/90)")
        )
    ),
    SurveyQuestion(
        BiString("Vai jums ir smadzeņu darbības traucējumi?", "Do you have brain function disorders?"),
        "smadzenu_darbibas_traucejumi",
        listOf(
            BiString("Nē — nav zināmu traucējumu", "No — no known disorders"),
            BiString("Viegli — nelielas koncentrēšanās grūtības", "Mild — minor concentration difficulties"),
            BiString("Izteikti — diagnosticēti traucējumi", "Significant — diagnosed disorders")
        )
    ),
    SurveyQuestion(
        BiString(
            "Vai lietojat uzmundrinošus dzērienus (kafija, tēja, enerģijas dzērieni)?",
            "Do you use stimulant drinks (coffee, tea, energy drinks)?"
        ),
        "lieto_uzmundrinosus_dzerienus",
        listOf(
            BiString("Nē — nelietoju vai ļoti reti", "No — none or very rarely"),
            BiString("Mēreni — 1–2 tasītes dienā", "Moderately — 1–2 cups per day"),
            BiString("Bieži — 3+ tasītes vai enerģijas dzērieni", "Frequently — 3+ cups or energy drinks")
        )
    ),
    SurveyQuestion(
        BiString("Vai lietojat nomierinošus līdzekļus?", "Do you use sedatives?"),
        "lieto_nomierinosus_lidzeklus",
        listOf(
            BiString("Nē — nelietoju", "No — none"),
            BiString("Dažreiz — pēc vajadzības, ne katru dienu", "Sometimes — as needed, not daily"),
            BiString("Regulāri — katru dienu vai gandrīz katru", "Regularly — daily or almost daily")
        )
    ),
    SurveyQuestion(
        BiString("Vai jums ir miega apnoja?", "Do you have sleep apnea?"),
        "apnoja",
        listOf(
            BiString("Nē — nav simptomu", "No — no symptoms"),
            BiString("Iespējams — krācu, dažreiz pamostos", "Possibly — snoring, occasional waking"),
            BiString("Jā — diagnosticēta apnoja", "Yes — diagnosed sleep apnea")
        )
    ),
    SurveyQuestion(
        BiString("Vai esat piedzīvojis negaidītu aizmigšanu?", "Have you experienced unexpected falling asleep?"),
        "negaidita_aizmigsana",
        listOf(
            BiString("Nekad — ne reizi", "Never — not once"),
            BiString("Reti — 1–2 reizes pēdējā gadā", "Rarely — 1–2 times in the past year"),
            BiString("Vairākkārt — 3+ reizes vai regulāri", "Multiple times — 3+ times or regularly")
        )
    ),
    // Pre-shift self-assessment (input 9 — Karolinska Sleepiness Scale)
    SurveyQuestion(
        BiString(
            "Kā jūtaties šobrīd? (Karolinska miegainības skala)",
            "How do you feel right now? (Karolinska Sleepiness Scale)"
        ),
        "aptauja",
        listOf(
            BiString("Modrs, možs — varu koncentrēties bez piepūles", "Alert, awake — can concentrate without effort"),
            BiString("Nedaudz miegains — grūtības saglabāt uzmanību", "Slightly sleepy — difficulty staying focused"),
            BiString("Ļoti miegains — cīnos ar miegu", "Very sleepy — fighting to stay awake")
        )
    ),
)

// Note: inputs 10-13 (reaction test, memory tests, math test) are filled by the Tests screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onNavigateToTests: () -> Unit
) {
    var currentQuestion by remember { mutableStateOf(0) }
    val answers = remember { mutableStateMapOf<String, Int>() }

    // Clear all previous results when starting a new survey
    LaunchedEffect(Unit) {
        viewModel.surveyAnswers.clear()
        viewModel.testResultDetails.clear()
        viewModel.appSubjectiveResult.value = null
        viewModel.appFatigueResult.value = null
        viewModel.appRecommendationResult.value = null
        viewModel.lastMonitoringRecommendation.value = null
        viewModel.preTripTimestamp.value = null
        viewModel.monitoringTimestamp.value = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            BiString("Pirms maiņas aptauja", "Pre-shift Survey").get(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${currentQuestion + 1} / ${surveyQuestions.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = (currentQuestion + 1).toFloat() / surveyQuestions.size,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (currentQuestion < surveyQuestions.size) {
                val q = surveyQuestions[currentQuestion]
                val selected = answers[q.inputKey]

                // Question text
                Text(
                    q.question.get(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Answer buttons
                q.answers.forEachIndexed { index, answer ->
                    val isSelected = selected == index
                    if (isSelected) {
                        Button(
                            onClick = { answers[q.inputKey] = index },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(answer.get(), style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { answers[q.inputKey] = index },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(answer.get(), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentQuestion > 0) {
                        OutlinedButton(onClick = { currentQuestion-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(BiString("Iepriekšējais", "Previous").get())
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (currentQuestion < surveyQuestions.size - 1) {
                                currentQuestion++
                            } else {
                                // Save answers to ViewModel
                                answers.forEach { (key, value) ->
                                    viewModel.surveyAnswers[key] = value.toFloat()
                                }
                                // Evaluate subjective component with survey answers only
                                val subjInputs = listOf(
                                    "nakts_darbs", "stress", "paaugstinats_asinsspiediens",
                                    "smadzenu_darbibas_traucejumi", "lieto_uzmundrinosus_dzerienus",
                                    "lieto_nomierinosus_lidzeklus", "apnoja", "negaidita_aizmigsana",
                                    "aptauja", "reakcijas_tests", "sektoru_atmina_test",
                                    "sektoru_secibas_atmina_tests", "matematiskais_laiks_tests"
                                ).associateWith { (viewModel.surveyAnswers[it] ?: 0f).toDouble() }
                                viewModel.appSubjectiveResult.value = viewModel.getScenarios().evaluateSubjective(subjInputs)
                                // Navigate to tests
                                onNavigateToTests()
                            }
                        },
                        enabled = selected != null
                    ) {
                        Text(
                            if (currentQuestion < surveyQuestions.size - 1)
                                BiString("Nākamais", "Next").get()
                            else
                                BiString("Turpināt ar testiem", "Continue to Tests").get()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            if (currentQuestion < surveyQuestions.size - 1) Icons.Default.ArrowForward
                            else Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
