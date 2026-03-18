package com.fatigue.expert.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fatigue.expert.engine.ModuleResult
import com.fatigue.expert.ui.theme.*

/**
 * Card displaying a single module result with color-coded severity.
 */
@Composable
fun ResultCard(
    title: String,
    result: ModuleResult?,
    icon: ImageVector = Icons.Default.Assessment,
    modifier: Modifier = Modifier
) {
    val color = result?.let { getSeverityColor(it.value, it.outputName) } ?: MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (result != null) {
                    Text(
                        text = result.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${result.outputName} = ${String.format("%.2f", result.value)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${result.elapsedUs} µs",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                } else {
                    Text(
                        text = "Not evaluated",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Pipeline visualization showing the flow of data through chained FIS modules.
 */
@Composable
fun PipelineIndicator(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isComplete = index < currentStep
            val isCurrent = index == currentStep
            val color = when {
                isComplete -> LevelLow
                isCurrent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    if (isComplete) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            "${index + 1}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    step,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (index < steps.lastIndex) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Section header with divider.
 */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Divider(modifier = Modifier.padding(top = 4.dp))
    }
}

private fun getSeverityColor(value: Double, outputName: String): Color {
    val rounded = Math.round(value)
    return when (outputName) {
        "trauksmes_veids" -> when (rounded) {
            0L -> LevelLow
            1L -> LevelMedium
            2L -> LevelHigh
            else -> LevelCritical
        }
        "rekomendacija" -> when (rounded) {
            0L -> LevelLow
            1L -> LevelMedium
            2L -> LevelHigh
            else -> LevelCritical
        }
        else -> when (rounded) {
            0L -> LevelLow
            1L -> LevelMedium
            else -> LevelHigh
        }
    }
}
