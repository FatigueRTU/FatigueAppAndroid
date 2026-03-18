package com.fatigue.expert.ui.screens.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fatigue.expert.ui.BiString

/**
 * Pre-trip survey introduction screen.
 * Explains the steps and flow before starting the survey.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreTripIntroScreen(
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BiString("Aptauja pirms reisa", "Pre-trip Survey").get()) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            Text(
                BiString(
                    "Noguruma novērtēšanas procedūra",
                    "Fatigue Assessment Procedure"
                ).get(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                BiString(
                    "Pirms darba uzsākšanas tiek veikta noguruma pakāpes novērtēšana, kas sastāv no diviem posmiem.",
                    "Before starting work, a fatigue level assessment is performed, consisting of two stages."
                ).get(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Step 1
            StepRow(
                number = "1",
                title = BiString("Aptauja", "Survey").get(),
                description = BiString(
                    "Aizpildiet 9 jautājumu aptauju par veselības stāvokli, miega kvalitāti un pašsajūtu. " +
                    "Aptauja ietver anamnēzes datus un Karolinska miegainības pašvērtējumu.",
                    "Complete a 9-question survey about health status, sleep quality and well-being. " +
                    "The survey includes anamnesis data and Karolinska Sleepiness Scale self-assessment."
                ).get(),
                icon = Icons.Default.Assignment
            )

            StepDivider()

            // Step 2
            StepRow(
                number = "2",
                title = BiString("Kognitīvie testi", "Cognitive Tests").get(),
                description = BiString(
                    "Veiciet 4 kognitīvos testus, kas novērtē reakcijas ātrumu, aritmētiskās spējas, " +
                    "atmiņu un modrību. Katrs tests aizņem 1–2 minūtes.",
                    "Complete 4 cognitive tests that evaluate reaction speed, arithmetic ability, " +
                    "memory and vigilance. Each test takes 1–2 minutes."
                ).get(),
                icon = Icons.Default.TouchApp
            )

            StepDivider()

            // Step 3
            StepRow(
                number = "3",
                title = BiString("Rezultāts un rekomendācija", "Result and Recommendation").get(),
                description = BiString(
                    "Pēc visu testu pabeigšanas sistēma automātiski aprēķina noguruma pakāpi un sniedz rekomendāciju: " +
                    "turpināt darbu, paņemt pauzi, atpūsties vai beigt maiņu.",
                    "After completing all tests, the system automatically calculates the fatigue level and provides a recommendation: " +
                    "continue work, take a pause, rest or end shift."
                ).get(),
                icon = Icons.Default.Recommend
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Start button
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    BiString("Sākt aptauju", "Start Survey").get(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StepRow(
    number: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Circle number badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                number,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepDivider() {
    Row(modifier = Modifier.padding(start = 15.dp)) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(16.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}
