package com.fatigue.expert.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fatigue.expert.ui.BiString

/**
 * Real-time EEG waveform plot with blink event markers.
 *
 * @param samples Raw EEG samples (512 = 1 second at 512 Hz)
 * @param blinkEvents List of (sampleIndex, strength) for blink markers
 * @param signalQuality 0=good, 200=no contact
 * @param attention eSense attention value 0-100
 * @param meditation eSense meditation value 0-100
 */
@Composable
fun EegWaveformPlot(
    samples: ShortArray,
    sampleCount: Int,
    blinkDetected: Boolean = false,
    blinkStrength: Int = 0,
    signalQuality: Int = 200,
    attention: Int = 0,
    meditation: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                BiString("EEG viļņu forma (512 Hz)", "EEG Waveform (512 Hz)").get(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            val sigColor = when {
                signalQuality == 0 -> Color(0xFF4CAF50)
                signalQuality < 50 -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            }
            Text(
                "SQ: $signalQuality",
                style = MaterialTheme.typography.bodySmall,
                color = sigColor,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Waveform canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A2E))
        ) {
            if (sampleCount > 1) {
                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f

                    // Find min/max for auto-scaling
                    var minVal = Short.MAX_VALUE
                    var maxVal = Short.MIN_VALUE
                    for (i in 0 until sampleCount) {
                        if (samples[i] < minVal) minVal = samples[i]
                        if (samples[i] > maxVal) maxVal = samples[i]
                    }
                    val range = (maxVal - minVal).toFloat().coerceAtLeast(1f)

                    // Grid lines
                    val gridColor = Color(0xFF2A2A4A)
                    // Horizontal center line
                    drawLine(gridColor, Offset(0f, midY), Offset(w, midY), strokeWidth = 1f)
                    // Quarter lines
                    drawLine(gridColor, Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 0.5f)
                    drawLine(gridColor, Offset(0f, h * 0.75f), Offset(w, h * 0.75f), strokeWidth = 0.5f)

                    // EEG waveform path
                    val path = Path()
                    val stepX = w / (sampleCount - 1).toFloat()

                    for (i in 0 until sampleCount) {
                        val x = i * stepX
                        val normalized = (samples[i] - minVal).toFloat() / range
                        val y = h - (normalized * h * 0.9f + h * 0.05f)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    // Draw waveform
                    drawPath(
                        path = path,
                        color = Color(0xFF00E676), // bright green
                        style = Stroke(width = 1.5f)
                    )

                    // Blink marker (red vertical line + label)
                    if (blinkDetected && blinkStrength > 0) {
                        // Draw at the right edge (most recent)
                        val blinkX = w - 20f
                        drawLine(
                            Color(0xFFFF5252),
                            Offset(blinkX, 0f),
                            Offset(blinkX, h),
                            strokeWidth = 3f
                        )
                        // Blink strength circle
                        val radius = (blinkStrength / 255f * 20f).coerceAtLeast(6f)
                        drawCircle(
                            Color(0xFFFF5252).copy(alpha = 0.6f),
                            radius = radius,
                            center = Offset(blinkX, 20f)
                        )
                    }
                }
            } else {
                // No data yet
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        BiString("Gaida datus...", "Waiting for data...").get(),
                        color = Color(0xFF666688),
                        fontSize = 12.sp
                    )
                }
            }

            // Blink strength overlay text
            if (blinkDetected && blinkStrength > 0) {
                Text(
                    "⚡ BLINK ($blinkStrength)",
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                )
            }
        }

        // Bottom info row
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${sampleCount} ${BiString("paraugi", "samples").get()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("ATT: $attention  MED: $meditation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
