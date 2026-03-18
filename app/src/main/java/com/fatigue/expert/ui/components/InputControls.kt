package com.fatigue.expert.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Labeled slider for fuzzy input values with discrete steps.
 */
@Composable
fun FuzzyInputSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..2f,
    steps: Int = 0,
    description: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = String.format("%.1f", value),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

/**
 * Holds the selection state for a QuickSetButtons group.
 * Pass the same instance to QuickSetButtons and to slider onValueChange
 * so that manual slider changes clear the toggle.
 */
class QuickSetState {
    var selected by mutableStateOf<Int?>(null)
    fun clear() { selected = null }
}

@Composable
fun rememberQuickSetState() = remember { QuickSetState() }

/**
 * Quick-set buttons for Low/Medium/High (0/1/2) values.
 * Selected button stays toggled. Clears when [state].clear() is called
 * (e.g. from a slider onValueChange).
 */
@Composable
fun QuickSetButtons(
    onSet: (Float) -> Unit,
    labels: List<String> = listOf("Z (Low)", "V (Med)", "A (High)"),
    values: List<Float> = listOf(0f, 1f, 2f),
    state: QuickSetState? = null,
    modifier: Modifier = Modifier
) {
    val internalState = rememberQuickSetState()
    val s = state ?: internalState

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = s.selected == index
            if (isSelected) {
                Button(
                    onClick = { onSet(values[index]) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        s.selected = index
                        onSet(values[index])
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Checkbox-style toggle for binary inputs (0/1).
 */
@Composable
fun FuzzyCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            if (description.isNotEmpty()) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
