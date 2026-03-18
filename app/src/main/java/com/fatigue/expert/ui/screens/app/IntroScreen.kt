package com.fatigue.expert.ui.screens.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fatigue.expert.ui.Language
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.currentLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroScreen(onContinue: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var languageChosen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).fillMaxWidth()
        ) {
            Text(
                "Noguruma novērtēšana",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "Fatigue Assessment",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Language selection
            if (!languageChosen) {
                Text(
                    "Izvēlieties valodu / Choose language",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { currentLanguage.value = Language.LV; languageChosen = true },
                        modifier = Modifier.width(140.dp).height(56.dp)
                    ) {
                        Text("🇱🇻 Latviešu", style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedButton(
                        onClick = { currentLanguage.value = Language.EN; languageChosen = true },
                        modifier = Modifier.width(140.dp).height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("🇬🇧 English", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                // Name input
                Text(
                    BiString("Ievadiet savu vārdu", "Enter your name").get(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(BiString("Vārds", "Name").get()) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onContinue(name.trim()) },
                    enabled = name.trim().isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        BiString("Turpināt", "Continue").get(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
