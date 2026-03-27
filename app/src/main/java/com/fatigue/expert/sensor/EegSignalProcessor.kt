package com.fatigue.expert.sensor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Processes raw MindWave data into the 6 FIS objective inputs (0–2 scale).
 *
 * Inputs computed:
 *   mirkskinasanas_biezums  — blink frequency (blinks/min → 0/1/2)
 *   EEG_alfa_ritms          — alpha rhythm prominence duration (seconds → 0/1/2)
 *   EEG_j1                  — Task engagement: β / (α + θ)
 *   EEG_j2                  — Attention: θ / α
 *   EEG_j3                  — Stress: θ / β
 *   EEG_j4                  — Alertness: α / β
 *
 * Also computes alert-level inputs:
 *   EEG_vertejums           — overall EEG assessment (0/1/2)
 *   Acu_novertejums         — eye assessment from blink pattern (0/1/2)
 */
class EegSignalProcessor {

    /** Processed inputs ready for FIS evaluation */
    data class ObjectiveInputs(
        val blinkFrequency: Double = 0.0,   // mirkskinasanas_biezums (0–2)
        val eegAlpha: Double = 0.0,         // EEG_alfa_ritms (0–2)
        val j1: Double = 0.0,              // Task engagement (0–2)
        val j2: Double = 0.0,              // Attention (0–2)
        val j3: Double = 0.0,              // Stress (0–2)
        val j4: Double = 0.0,              // Alertness (0–2)
        val eegAssessment: Double = 0.0,   // EEG_vertejums (0–2)
        val eyeAssessment: Double = 0.0,   // Acu_novertejums (0–2)
        val signalGood: Boolean = false,
        val timestamp: Long = 0
    )

    private val _inputs = MutableStateFlow(ObjectiveInputs())
    val inputs: StateFlow<ObjectiveInputs> = _inputs.asStateFlow()

    // ── Blink tracking ──
    private val blinkTimestamps = mutableListOf<Long>()
    private val BLINK_WINDOW_MS = 60_000L // 1 minute window

    // ── Alpha prominence tracking ──
    private var alphaHighStart: Long? = null
    private var lastAlphaDurationSec = 0.0
    private val ALPHA_THRESHOLD = 0.35f // alpha is "prominent" when > 35% of total power

    // ── Band power history for smoothing ──
    private val bandPowerHistory = mutableListOf<MindWaveConnection.MindWaveData>()
    private val SMOOTHING_WINDOW = 5 // average over last 5 samples (~5 seconds)

    /**
     * Process a new data packet from MindWaveConnection.
     * Call this every time MindWaveConnection.data emits a new value.
     */
    fun process(data: MindWaveConnection.MindWaveData) {
        val now = data.timestamp
        val signalGood = data.signalQuality < 50 // 0 = perfect, 200 = no contact

        // ── Track blinks ──
        if (data.blinkDetected) {
            blinkTimestamps.add(now)
        }
        // Remove blinks older than 1 minute
        blinkTimestamps.removeAll { now - it > BLINK_WINDOW_MS }
        val blinksPerMinute = blinkTimestamps.size

        // ── Track alpha prominence duration ──
        val totalPower = data.delta + data.theta + data.alpha + data.beta + data.gamma
        val alphaRatio = if (totalPower > 0) data.alpha / totalPower else 0f

        if (alphaRatio > ALPHA_THRESHOLD && signalGood) {
            if (alphaHighStart == null) alphaHighStart = now
            lastAlphaDurationSec = (now - (alphaHighStart ?: now)) / 1000.0
        } else {
            alphaHighStart = null
            // Decay slowly
            lastAlphaDurationSec = (lastAlphaDurationSec * 0.9).coerceAtLeast(0.0)
        }

        // ── Smooth band power ──
        if (signalGood && totalPower > 0) {
            bandPowerHistory.add(data)
            if (bandPowerHistory.size > SMOOTHING_WINDOW) {
                bandPowerHistory.removeAt(0)
            }
        }

        // Compute averaged band powers
        val avgAlpha = bandPowerHistory.map { it.alpha }.average().toFloat()
        val avgBeta = bandPowerHistory.map { it.beta }.average().toFloat()
        val avgTheta = bandPowerHistory.map { it.theta }.average().toFloat()

        // ── Compute J1–J4 indices (Thesis Table 5.1) ──
        val j1Raw = if (avgAlpha + avgTheta > 0) avgBeta / (avgAlpha + avgTheta) else 0f
        val j2Raw = if (avgAlpha > 0) avgTheta / avgAlpha else 0f
        val j3Raw = if (avgBeta > 0) avgTheta / avgBeta else 0f
        val j4Raw = if (avgBeta > 0) avgAlpha / avgBeta else 0f

        // ── Classify all inputs to 0/1/2 ──
        val blinkLevel = classifyBlinks(blinksPerMinute)
        val alphaLevel = classifyAlphaDuration(lastAlphaDurationSec)
        val j1Level = classifyIndex(j1Raw, lowThresh = 0.5f, highThresh = 1.5f, invert = true)
        val j2Level = classifyIndex(j2Raw, lowThresh = 0.8f, highThresh = 1.5f, invert = false)
        val j3Level = classifyIndex(j3Raw, lowThresh = 0.8f, highThresh = 1.5f, invert = false)
        val j4Level = classifyIndex(j4Raw, lowThresh = 0.8f, highThresh = 1.5f, invert = false)

        // ── Overall EEG and eye assessment ──
        val eegAssessment = maxOf(alphaLevel, j1Level, j4Level) // worst-case from EEG indicators
        val eyeAssessment = blinkLevel

        _inputs.value = ObjectiveInputs(
            blinkFrequency = blinkLevel,
            eegAlpha = alphaLevel,
            j1 = j1Level,
            j2 = j2Level,
            j3 = j3Level,
            j4 = j4Level,
            eegAssessment = eegAssessment,
            eyeAssessment = eyeAssessment,
            signalGood = signalGood,
            timestamp = now
        )
    }

    /**
     * Classify blinks per minute:
     *   <10 blinks/min → 0 (Low — normal)
     *   10–20 blinks/min → 1 (Medium — elevated)
     *   >20 blinks/min → 2 (High — drowsy)
     *
     * Normal blink rate is ~15–20/min. Drowsiness increases blink rate and duration.
     */
    private fun classifyBlinks(blinksPerMin: Int): Double = when {
        blinksPerMin < 10 -> 0.0
        blinksPerMin <= 20 -> 1.0
        else -> 2.0
    }

    /**
     * Classify alpha rhythm prominence duration:
     *   <2s → 0 (Low — alert)
     *   2–4s → 1 (Medium — drowsy onset)
     *   >4s → 2 (High — drowsy)
     */
    private fun classifyAlphaDuration(seconds: Double): Double = when {
        seconds < 2.0 -> 0.0
        seconds <= 4.0 -> 1.0
        else -> 2.0
    }

    /**
     * Classify a spectral ratio index to 0/1/2.
     * @param invert if true, LOW ratio = HIGH fatigue (e.g., J1: low engagement = high fatigue)
     */
    private fun classifyIndex(value: Float, lowThresh: Float, highThresh: Float, invert: Boolean): Double {
        val level = when {
            value < lowThresh -> 0.0
            value <= highThresh -> 1.0
            else -> 2.0
        }
        return if (invert) 2.0 - level else level
    }

    /** Reset all tracking state */
    fun reset() {
        blinkTimestamps.clear()
        bandPowerHistory.clear()
        alphaHighStart = null
        lastAlphaDurationSec = 0.0
        _inputs.value = ObjectiveInputs()
    }
}
