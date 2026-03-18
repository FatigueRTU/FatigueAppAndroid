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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringIntroScreen(
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BiString("Monitoringa aktivitāte", "Monitoring Activity").get()) },
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
            Text(
                BiString("Reāllaika noguruma monitorings", "Real-time Fatigue Monitoring").get(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                BiString(
                    "Monitoringa scenārijs tiek realizēts, kad cilvēkam pieslēgti sensori un persona veic darbu. " +
                    "Sistēma nepārtraukti novērtē objektīvos parametrus un sniedz trauksmi un rekomendācijas.",
                    "The monitoring scenario is executed when sensors are connected and the person is performing work. " +
                    "The system continuously evaluates objective parameters and provides alerts and recommendations."
                ).get(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Step 1
            StepRow(
                number = "1",
                title = BiString("Sensoru savienojums", "Sensor Connection").get(),
                description = BiString(
                    "Pieslēdziet EEG sensoru un pārliecinieties par savienojuma kvalitāti. Ja sensors nav pieejams, iespējams simulēt datus.",
                    "Connect the EEG sensor and verify connection quality. If no sensor is available, data simulation is possible."
                ).get(),
                icon = Icons.Default.Sensors
            )

            StepDivider()

            // Step 2
            StepRow(
                number = "2",
                title = BiString("Monitorings", "Monitoring").get(),
                description = BiString(
                    "Sistēma analizē acu mirkšķināšanas biežumu, EEG alfa ritmu un spektrālo joslu indeksus (J1–J4) reāllaikā.",
                    "The system analyzes blink frequency, EEG alpha rhythm and spectral band indices (J1–J4) in real-time."
                ).get(),
                icon = Icons.Default.Monitor
            )

            StepDivider()

            // Step 3
            StepRow(
                number = "3",
                title = BiString("Trauksme un rekomendācija", "Alert and Recommendation").get(),
                description = BiString(
                    "Balstoties uz objektīvajiem datiem, sistēma nosaka trauksmes veidu un sniedz rekomendāciju noguruma mazināšanai.",
                    "Based on objective data, the system determines the alert type and provides a fatigue mitigation recommendation."
                ).get(),
                icon = Icons.Default.Recommend
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    BiString("Turpināt uz savienojumu", "Continue to Connection").get(),
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
        ) {
            Text(
                number,
                color = MaterialTheme.colorScheme.onTertiary,
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
                    tint = MaterialTheme.colorScheme.tertiary,
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
