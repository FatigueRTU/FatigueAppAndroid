package com.fatigue.expert.ui.screens.app

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.engine.FatigueScenarios
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.S
import com.fatigue.expert.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Session results screen — shows a complete summary of pre-trip and monitoring results.
 * Includes survey answers, test results, module outputs, and recommendations.
 * Provides CSV export via share intent.
 */

private val surveyInputLabels = mapOf(
    "nakts_darbs" to BiString("Nakts darbs", "Night work"),
    "stress" to BiString("Stress", "Stress"),
    "paaugstinats_asinsspiediens" to BiString("Asinsspiediens", "Blood pressure"),
    "smadzenu_darbibas_traucejumi" to BiString("Smadzeņu traucējumi", "Brain disorders"),
    "lieto_uzmundrinosus_dzerienus" to BiString("Uzmundrinoši dzērieni", "Stimulant drinks"),
    "lieto_nomierinosus_lidzeklus" to BiString("Nomierinošie līdzekļi", "Sedatives"),
    "apnoja" to BiString("Miega apnoja", "Sleep apnea"),
    "negaidita_aizmigsana" to BiString("Negaidīta aizmigšana", "Unexpected sleep"),
    "aptauja" to BiString("KSS pašvērtējums", "KSS self-assessment"),
)

private val testInputLabels = mapOf(
    "reakcijas_tests" to BiString("Reakcijas tests", "Reaction test"),
    "matematiskais_laiks_tests" to BiString("Aritmētikas tests", "Arithmetic test"),
    "sektoru_atmina_test" to BiString("Sektoru atmiņas tests", "Sector memory test"),
    "sektoru_secibas_atmina_tests" to BiString("Laika tests", "Timer test"),
)

private fun levelLabel(value: Float): String = when (value.toInt()) {
    0 -> "Zems / Low"
    1 -> "Vidējs / Medium"
    2 -> "Augsts / High"
    else -> "$value"
}

private fun formatTimestamp(millis: Long?): String {
    if (millis == null) return "—"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionResultsScreen(
    viewModel: FatigueViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val answers = viewModel.surveyAnswers
    val details = viewModel.testResultDetails
    val subj = viewModel.appSubjectiveResult.value
    val fatigue = viewModel.appFatigueResult.value
    val reco = viewModel.appRecommendationResult.value
    val monResult = viewModel.monitoringResult.value
    val monReco = viewModel.lastMonitoringRecommendation.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(BiString("Sesijas rezultāti", "Session Results").get(),
                            style = MaterialTheme.typography.titleMedium)
                        Text(viewModel.userName.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Header card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${BiString("Persona", "Person").get()}: ${viewModel.userName.value}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    viewModel.preTripTimestamp.value?.let {
                        Text(
                            "${BiString("Aptauja pirms reisa", "Pre-trip survey").get()}: ${formatTimestamp(it)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    viewModel.monitoringTimestamp.value?.let {
                        Text(
                            "${BiString("Monitorings", "Monitoring").get()}: ${formatTimestamp(it)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // ── Pre-trip Survey Answers ──
            if (answers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle(BiString("Aptaujas atbildes", "Survey Answers").get())

                surveyInputLabels.forEach { (key, label) ->
                    val value = answers[key]
                    if (value != null) {
                        ResultRow(label.get(), levelLabel(value), value.toInt())
                    }
                }
            }

            // ── Cognitive Test Results ──
            val hasTests = testInputLabels.keys.any { answers.containsKey(it) }
            if (hasTests) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle(BiString("Kognitīvo testu rezultāti", "Cognitive Test Results").get())

                testInputLabels.forEach { (key, label) ->
                    val value = answers[key]
                    val detail = details[key]
                    if (value != null) {
                        ResultRowWithDetail(label.get(), detail, levelLabel(value), value.toInt())
                    }
                }
            }

            // ── Pre-trip Module Results ──
            if (subj != null || fatigue != null || reco != null) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle(BiString("Pirms reisa moduļu rezultāti", "Pre-trip Module Results").get())

                subj?.let {
                    ModuleResultRow(S.moduleSubjective.get(), it.value, it.label)
                }
                fatigue?.let {
                    ModuleResultRow(S.moduleFatigue.get(), it.value, it.label)
                }
                reco?.let {
                    val recoColor = when (Math.round(it.value)) {
                        0L -> LevelLow; 1L -> LevelMedium; 2L -> LevelHigh; else -> LevelCritical
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = recoColor.copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Recommend, contentDescription = null, tint = recoColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(S.moduleRecommendation.get(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(it.label, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold, color = recoColor)
                                Text("${it.outputName} = ${String.format("%.2f", it.value)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // ── Monitoring Results ──
            if (monResult != null) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle(BiString("Monitoringa rezultāti", "Monitoring Results").get())

                ModuleResultRow(S.moduleObjective.get(), monResult.objective.value, monResult.objective.label)
                ModuleResultRow(S.moduleAlert.get(), monResult.alert.value, monResult.alert.label)

                monReco?.let {
                    val recoColor = when (Math.round(it.value)) {
                        0L -> LevelLow; 1L -> LevelMedium; 2L -> LevelHigh; else -> LevelCritical
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = recoColor.copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = recoColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(BiString("Monitoringa rekomendācija", "Monitoring recommendation").get(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(it.label, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold, color = recoColor)
                                Text("${it.outputName} = ${String.format("%.2f", it.value)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // ── Export CSV Button ──
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { shareCsv(context, viewModel) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    BiString("Kopīgot rezultātus (.csv)", "Share results (.csv)").get(),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Divider(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
}

@Composable
private fun ResultRow(label: String, valueText: String, level: Int) {
    val color = when (level) { 0 -> LevelLow; 1 -> LevelMedium; else -> LevelHigh }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(valueText, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun ResultRowWithDetail(label: String, detail: String?, valueText: String, level: Int) {
    val color = when (level) { 0 -> LevelLow; 1 -> LevelMedium; else -> LevelHigh }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            if (detail != null) {
                Text(detail, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp)
            }
        }
        Text(valueText, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun ModuleResultRow(moduleName: String, value: Double, label: String) {
    val rounded = Math.round(value)
    val color = when (rounded) { 0L -> LevelLow; 1L -> LevelMedium; else -> LevelHigh }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(moduleName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(label, style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium, color = color)
            Text(String.format("%.2f", value), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

private fun shareCsv(context: android.content.Context, viewModel: FatigueViewModel) {
    val answers = viewModel.surveyAnswers
    val details = viewModel.testResultDetails
    val subj = viewModel.appSubjectiveResult.value
    val fatigue = viewModel.appFatigueResult.value
    val reco = viewModel.appRecommendationResult.value
    val monResult = viewModel.monitoringResult.value
    val monReco = viewModel.lastMonitoringRecommendation.value

    val sb = StringBuilder()
    sb.appendLine("Fatigue Assessment Results")
    sb.appendLine("Person,${viewModel.userName.value}")
    sb.appendLine("Pre-trip timestamp,${formatTimestamp(viewModel.preTripTimestamp.value)}")
    sb.appendLine("Monitoring timestamp,${formatTimestamp(viewModel.monitoringTimestamp.value)}")
    sb.appendLine()

    // Survey answers
    sb.appendLine("Survey Answers")
    sb.appendLine("Input,Label,Value,Level")
    surveyInputLabels.forEach { (key, label) ->
        val value = answers[key]
        if (value != null) {
            sb.appendLine("$key,${label.en},${value.toInt()},${levelLabel(value)}")
        }
    }
    sb.appendLine()

    // Test results
    sb.appendLine("Cognitive Test Results")
    sb.appendLine("Input,Label,Detail,Value,Level")
    testInputLabels.forEach { (key, label) ->
        val value = answers[key]
        val detail = details[key] ?: ""
        if (value != null) {
            sb.appendLine("$key,${label.en},\"$detail\",${value.toInt()},${levelLabel(value)}")
        }
    }
    sb.appendLine()

    // Module results
    sb.appendLine("Module Results")
    sb.appendLine("Module,Output Name,Raw Value,Label")
    subj?.let { sb.appendLine("Subjective Component,${it.outputName},${String.format("%.4f", it.value)},${it.label}") }
    fatigue?.let { sb.appendLine("Mental Fatigue,${it.outputName},${String.format("%.4f", it.value)},${it.label}") }
    reco?.let { sb.appendLine("Recommendation,${it.outputName},${String.format("%.4f", it.value)},${it.label}") }
    sb.appendLine()

    // Monitoring results
    if (monResult != null) {
        sb.appendLine("Monitoring Results")
        sb.appendLine("Module,Output Name,Raw Value,Label")
        sb.appendLine("Objective Component,${monResult.objective.outputName},${String.format("%.4f", monResult.objective.value)},${monResult.objective.label}")
        sb.appendLine("Alert Type,${monResult.alert.outputName},${String.format("%.4f", monResult.alert.value)},${monResult.alert.label}")
        monReco?.let { sb.appendLine("Monitoring Recommendation,${it.outputName},${String.format("%.4f", it.value)},${it.label}") }
    }

    // Write to file and share
    try {
        val fileName = "fatigue_results_${viewModel.userName.value.replace(" ", "_")}_${
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        }.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(sb.toString())

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Fatigue Assessment — ${viewModel.userName.value}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Results"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
