package com.fatigue.expert.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.fatigue.expert.ui.BiString
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Expert evaluation survey — matches the EXPERT_SURVEY.md structure.
 * Sections A–E with Likert 1–5 ratings, section F with open text,
 * plus expert info fields. Results exportable as CSV.
 */

private data class LikertQuestion(
    val id: String,
    val statement: BiString,
    val commentPrompt: BiString? = null
)

private data class SurveySection(
    val title: BiString,
    val instruction: BiString? = null,
    val questions: List<LikertQuestion>
)

private val sections = listOf(
    SurveySection(
        title = BiString(
            "A. Aptaujas un testu novērtējums",
            "A. Survey and Tests Evaluation"
        ),
        instruction = BiString(
            "Lūdzu, veiciet pirms reisa aptauju un kognitīvos testus lietotnē.",
            "Please complete the pre-trip survey and cognitive tests in the app."
        ),
        questions = listOf(
            LikertQuestion("A1", BiString(
                "Aptaujas jautājumi ir saprotami un atbilžu varianti ir skaidri formulēti.",
                "The survey questions are understandable and the answer options are clearly formulated."
            )),
            LikertQuestion("A2", BiString(
                "Aptaujā iekļautie parametri (nakts darbs, stress, asinsspiediens, medikamenti, miega kvalitāte u.c.) ir klīniski nozīmīgi noguruma novērtēšanā.",
                "The parameters included in the survey (night work, stress, blood pressure, medications, sleep quality, etc.) are clinically relevant for fatigue assessment."
            ), BiString("Komentāri:", "Comments:")),
            LikertQuestion("A3", BiString(
                "Kognitīvie testi (reakcijas ātrums, aritmētika, atmiņa, modrība) ir piemēroti noguruma izpausmju novērtēšanai.",
                "The cognitive tests (reaction speed, arithmetic, memory, vigilance) are suitable for evaluating fatigue manifestations."
            )),
            LikertQuestion("A4", BiString(
                "Sistēmas sniegtie rezultāti (noguruma pakāpe, rekomendācija) ir saprotami un interpretējami bez papildu skaidrojumiem.",
                "The results provided by the system (fatigue level, recommendation) are understandable and interpretable without additional explanation."
            ))
        )
    ),
    SurveySection(
        title = BiString(
            "B. Lēmumu pareizība",
            "B. Decision Correctness"
        ),
        instruction = BiString(
            "Lūdzu, lietotnē izmēģiniet dažādas ieejas kombinācijas un novērtējiet, vai sistēmas lēmumi ir loģiski.",
            "Please try different input combinations in the app and evaluate whether the system decisions are logical."
        ),
        questions = listOf(
            LikertQuestion("B1", BiString(
                "Sistēma pareizi nosaka noguruma pakāpi (zems / vidējs / augsts) — augstāks nogurums tiek konstatēts, ja ir vairāk riska faktoru.",
                "The system correctly determines the fatigue level (low / medium / high) — higher fatigue is detected when more risk factors are present."
            )),
            LikertQuestion("B2", BiString(
                "Sistēmas rekomendācijas (turpināt darbu / pauze / atpūta / beigt maiņu) atbilst konstatētajam noguruma līmenim.",
                "The system recommendations (continue work / pause / rest / end shift) correspond to the detected fatigue level."
            )),
            LikertQuestion("B3", BiString(
                "Monitoringa laikā trauksmes līmenis (nav / zummers / zvans / sirēna) pieaug atbilstoši situācijas nopietnībai.",
                "During monitoring, the alert level (none / buzzer / bell / siren) increases appropriately with situation severity."
            )),
            LikertQuestion("B4", BiString(
                "Sistēma drīzāk pārnovērtē nogurumu nekā to nenovērtē — tas ir pieņemami drošības kritiskās jomās.",
                "The system tends to overestimate fatigue rather than underestimate it — this is acceptable in safety-critical domains."
            ), BiString("Komentāri:", "Comments:"))
        )
    ),
    SurveySection(
        title = BiString(
            "C. Sistēmas uzbūve un pieeja",
            "C. System Design and Approach"
        ),
        questions = listOf(
            LikertQuestion("C1", BiString(
                "Sistēma apvieno subjektīvos datus (aptauja, testi) un objektīvos datus (EEG sensori) kopējā novērtējumā — šī pieeja ir pamatota.",
                "The system combines subjective data (survey, tests) and objective data (EEG sensors) into an overall assessment — this approach is justified."
            )),
            LikertQuestion("C2", BiString(
                "Trīs soļu process (1. datu ievākšana → 2. noguruma un trauksmes novērtēšana → 3. rekomendācija) ir loģisks un saprotams.",
                "The three-step process (1. data collection → 2. fatigue and alert assessment → 3. recommendation) is logical and understandable."
            )),
            LikertQuestion("C3", BiString(
                "Trīs pakāpju skala nogurumam (zems / vidējs / augsts) un četru pakāpju skala rekomendācijām ir pietiekama un praktiski lietojama.",
                "The three-level scale for fatigue (low / medium / high) and four-level scale for recommendations is sufficient and practically applicable."
            )),
            LikertQuestion("C4", BiString(
                "Apsteidzošās trauksmes ideja (brīdināt pirms aizmigšanas, nevis pēc) ir praktiski noderīga un klīniski pamatota.",
                "The preemptive alert concept (warning before falling asleep, not after) is practically useful and clinically justified."
            ), BiString("Komentāri:", "Comments:"))
        )
    ),
    SurveySection(
        title = BiString(
            "D. Lietotnes lietojamība un veiktspēja",
            "D. App Usability and Performance"
        ),
        instruction = BiString(
            "Sistēma darbojas pilnībā lokāli ierīcē — visi aprēķini tiek veikti tieši planšetdatorā/telefonā bez interneta savienojuma vai mākoņpakalpojumiem.",
            "The system runs entirely locally on the device — all calculations are performed directly on the tablet/phone without internet connection or cloud services."
        ),
        questions = listOf(
            LikertQuestion("D1", BiString(
                "Lietotnes saskarni ir viegli saprast un izmantot bez papildu apmācības.",
                "The app interface is easy to understand and use without additional training."
            )),
            LikertQuestion("D2", BiString(
                "Lietotnes navigācija (aptauja → testi → rezultāts → monitorings) ir loģiska un intuitīva.",
                "The app navigation (survey → tests → result → monitoring) is logical and intuitive."
            )),
            LikertQuestion("D3", BiString(
                "Sistēma reaģē ātri — rezultāti parādās praktiski uzreiz pēc datu ievades (bez ielādes laika vai gaidīšanas).",
                "The system responds quickly — results appear practically instantly after data input (no loading time or waiting)."
            )),
            LikertQuestion("D4", BiString(
                "Sistēmas spēja darboties pilnībā lokāli (bez interneta) ir priekšrocība salīdzinājumā ar mākoņa risinājumiem, jo nodrošina datu privātumu un darbību bez tīkla.",
                "The system's ability to operate entirely locally (without internet) is an advantage compared to cloud solutions, as it ensures data privacy and offline operation."
            )),
            LikertQuestion("D5", BiString(
                "Rezultātu attēlojums (krāsu kodējums, rekomendāciju teksti, moduļu vērtības) ir informatīvs un pārskatāms.",
                "The result presentation (color coding, recommendation texts, module values) is informative and clear."
            ), BiString("Komentāri:", "Comments:"))
        )
    ),
    SurveySection(
        title = BiString(
            "E. Praktiskā pielietojamība",
            "E. Practical Applicability"
        ),
        questions = listOf(
            LikertQuestion("E1", BiString(
                "Sistēma ir pielāgojama dažādām nozarēm, mainot aptaujas jautājumus un rekomendāciju saturu.",
                "The system is adaptable to different industries by modifying survey questions and recommendation content."
            )),
            LikertQuestion("E2", BiString(
                "Kopumā sistēma aptver galvenās funkcijas, kas nepieciešamas noguruma novērtēšanai un brīdināšanai.",
                "Overall, the system covers the main functions needed for fatigue assessment and alerting."
            )),
            LikertQuestion("E3", BiString(
                "Android lietotne ir noderīgs rīks sistēmas demonstrēšanai un novērtēšanai.",
                "The Android app is a useful tool for demonstrating and evaluating the system."
            ))
        )
    )
)

private val openQuestions = listOf(
    "F1" to BiString("Kādas ir sistēmas galvenās stiprās puses?", "What are the main strengths of the system?"),
    "F2" to BiString("Kādas ir galvenās vājās puses vai ierobežojumi?", "What are the main weaknesses or limitations?"),
    "F3" to BiString("Kādus uzlabojumus jūs ieteiktu?", "What improvements would you suggest?"),
    "F4" to BiString("Vai sistēmai trūkst kādas būtiskas funkcijas?", "Is the system missing any essential functionality?")
)

private val expertFields = listOf(
    "name" to BiString("Vārds, uzvārds", "Name"),
    "institution" to BiString("Iestāde", "Institution"),
    "position" to BiString("Amats", "Position"),
    "specialty" to BiString("Specialitāte", "Specialty"),
    "experience" to BiString("Pieredze jomā (gadi)", "Experience (years)")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertSurveyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val ratings = remember { mutableStateMapOf<String, Int>() }
    val comments = remember { mutableStateMapOf<String, String>() }
    val openAnswers = remember { mutableStateMapOf<String, String>() }
    val expertInfo = remember { mutableStateMapOf<String, String>() }
    val scrollState = rememberScrollState()

    val totalQuestions = sections.sumOf { it.questions.size }
    val answeredCount = ratings.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            BiString("Ekspertu novērtējuma aptauja", "Expert Evaluation Survey").get(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "$answeredCount / $totalQuestions ${BiString("atbildēti", "answered").get()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            // ── Scale legend ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        BiString("Vērtējuma skala", "Rating scale").get(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        "1" to BiString("Pilnīgi nepiekrītu", "Strongly disagree"),
                        "2" to BiString("Nepiekrītu", "Disagree"),
                        "3" to BiString("Neitrāli", "Neutral"),
                        "4" to BiString("Piekrītu", "Agree"),
                        "5" to BiString("Pilnīgi piekrītu", "Strongly agree")
                    ).forEach { (num, label) ->
                        Text("$num = ${label.get()}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // ── Sections A–E ──
            sections.forEach { section ->
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    section.title.get(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                section.instruction?.let {
                    Text(it.get(), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                section.questions.forEach { q ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${q.id}. ${q.statement.get()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Likert 1–5 buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..5).forEach { value ->
                            val selected = ratings[q.id] == value
                            if (selected) {
                                Button(
                                    onClick = { ratings[q.id] = value },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("$value", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { ratings[q.id] = value },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("$value")
                                }
                            }
                        }
                    }

                    // Optional comment field
                    if (q.commentPrompt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = comments[q.id] ?: "",
                            onValueChange = { comments[q.id] = it },
                            label = { Text(q.commentPrompt.get(), style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // ── Section F: Open questions ──
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                BiString("F. Brīvie jautājumi", "F. Open Questions").get(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            openQuestions.forEach { (id, question) ->
                Spacer(modifier = Modifier.height(8.dp))
                Text("$id. ${question.get()}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = openAnswers[id] ?: "",
                    onValueChange = { openAnswers[id] = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            // ── Expert info ──
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                BiString("Eksperta informācija", "Expert Information").get(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            expertFields.forEach { (key, label) ->
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = expertInfo[key] ?: "",
                    onValueChange = { expertInfo[key] = it },
                    label = { Text(label.get()) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            // ── Export button ──
            Spacer(modifier = Modifier.height(24.dp))

            val allAnswered = answeredCount == totalQuestions
            Button(
                onClick = {
                    exportSurveyCsv(context, ratings, comments, openAnswers, expertInfo)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = allAnswered
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    BiString("Eksportēt un kopīgot (.csv)", "Export and share (.csv)").get(),
                    fontWeight = FontWeight.Bold
                )
            }

            if (!allAnswered) {
                Text(
                    BiString(
                        "Lūdzu, atbildiet uz visiem $totalQuestions jautājumiem pirms eksportēšanas.",
                        "Please answer all $totalQuestions questions before exporting."
                    ).get(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun exportSurveyCsv(
    context: android.content.Context,
    ratings: Map<String, Int>,
    comments: Map<String, String>,
    openAnswers: Map<String, String>,
    expertInfo: Map<String, String>
) {
    val sb = StringBuilder()
    sb.appendLine("Expert Evaluation Survey Results")
    sb.appendLine("Date,${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
    expertFields.forEach { (key, label) ->
        sb.appendLine("${label.en},${expertInfo[key] ?: ""}")
    }
    sb.appendLine()

    sb.appendLine("Likert Ratings (1-5)")
    sb.appendLine("ID,Rating,Comment")
    sections.forEach { section ->
        section.questions.forEach { q ->
            val rating = ratings[q.id] ?: ""
            val comment = comments[q.id]?.replace("\n", " ") ?: ""
            sb.appendLine("${q.id},$rating,\"$comment\"")
        }
    }
    sb.appendLine()

    sb.appendLine("Open Questions")
    sb.appendLine("ID,Answer")
    openQuestions.forEach { (id, _) ->
        val answer = openAnswers[id]?.replace("\n", " ") ?: ""
        sb.appendLine("$id,\"$answer\"")
    }

    // Statistics
    sb.appendLine()
    sb.appendLine("Statistics")
    val allRatings = ratings.values.toList()
    if (allRatings.isNotEmpty()) {
        val avg = allRatings.average()
        val stddev = kotlin.math.sqrt(allRatings.map { (it - avg) * (it - avg) }.average())
        sb.appendLine("Total questions,${allRatings.size}")
        sb.appendLine("Average,${String.format("%.2f", avg)}")
        sb.appendLine("Std deviation,${String.format("%.2f", stddev)}")

        // Per-section averages
        sections.forEach { section ->
            val sectionRatings = section.questions.mapNotNull { ratings[it.id] }
            if (sectionRatings.isNotEmpty()) {
                val sAvg = sectionRatings.average()
                sb.appendLine("${section.title.en} avg,${String.format("%.2f", sAvg)}")
            }
        }
    }

    try {
        val fileName = "expert_survey_${
            (expertInfo["name"] ?: "anonymous").replace(" ", "_")
        }_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(sb.toString())

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Expert Survey — ${expertInfo["name"] ?: "Anonymous"}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Survey"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
