package com.fatigue.expert.engine

import android.content.Context

/**
 * Defines the 3 chained scenarios matching the Node-RED flow.
 * Each scenario chains multiple FIS models together, passing outputs as inputs to the next stage.
 *
 * Module LP1 – Non-standard situation detection (Thesis Table 5.2, module 6):
 *   - neordinaras_pazimes.fis: 4 boolean inputs (microsleep, narcolepsy, etc.) → OR → 0/1
 *   - eeg_apsteigsana.fis: 2 boolean inputs (alpha A/B phase) → 0/1/2 bypass alert
 *   - monitoringa_eeg.fis: alpha duration + beta presence → 0/1/2 monitoring EEG assessment
 */
class FatigueScenarios(context: Context) {

    private val engine = FuzzyEngine(context)

    // Pre-load all systems
    private val subjSystem = engine.loadSystem("subjektiva_dala.fis")
    private val objSystem = engine.loadSystem("objective_full.fis")
    private val fatigueSystem = engine.loadSystem("nogurums.fis")
    private val alertSystem = engine.loadSystem("trauksme.fis")
    private val recoSystem = engine.loadSystem("rekomendacijas.fis")

    // LP1 – Non-standard situation detection module (6th module)
    private val extraordinarySystem = engine.loadSystem("neordinaras_pazimes.fis")
    private val eegBypassSystem = engine.loadSystem("eeg_apsteigsana.fis")
    private val monitoringEegSystem = engine.loadSystem("monitoringa_eeg.fis")

    // ── Individual Module Evaluations ──

    fun evaluateSubjective(inputs: Map<String, Double>): ModuleResult {
        val t0 = System.nanoTime()
        val result = engine.evaluate(subjSystem, inputs)
        val elapsed = (System.nanoTime() - t0) / 1_000
        val value = result["subjektiva_miegainiba"] ?: 0.0
        return ModuleResult("subjektiva_komponente", value, classifyLevel(value), elapsed)
    }

    fun evaluateObjective(inputs: Map<String, Double>): ModuleResult {
        val t0 = System.nanoTime()
        val result = engine.evaluate(objSystem, inputs)
        val elapsed = (System.nanoTime() - t0) / 1_000
        val value = result["objektiva_miegainiba"] ?: 0.0
        return ModuleResult("objektiva_komponente", value, classifyLevel(value), elapsed)
    }

    fun evaluateFatigue(subjective: Double, objective: Double): ModuleResult {
        val t0 = System.nanoTime()
        val result = engine.evaluate(fatigueSystem, mapOf(
            "objektiva_komponente" to objective,
            "subjektiva_komponente" to subjective
        ))
        val elapsed = (System.nanoTime() - t0) / 1_000
        val value = result["objektiva_miegainiba"] ?: 0.0
        return ModuleResult("nogurums", value, classifyLevel(value), elapsed)
    }

    fun evaluateAlert(eegAssessment: Double, eyeAssessment: Double, extraordinarySigns: Double): ModuleResult {
        val t0 = System.nanoTime()
        val result = engine.evaluate(alertSystem, mapOf(
            "EEG_vertejums" to eegAssessment,
            "Acu_novertejums" to eyeAssessment,
            "Neordinaras_pazimes" to extraordinarySigns
        ))
        val elapsed = (System.nanoTime() - t0) / 1_000
        val value = result["trauksmes_veids"] ?: 0.0
        return ModuleResult("trauksmes_veids", value, classifyAlertType(value), elapsed)
    }

    fun evaluateRecommendation(drowsinessLevel: Double, alertType: Double): ModuleResult {
        val t0 = System.nanoTime()
        val result = engine.evaluate(recoSystem, mapOf(
            "miegainibas_limenis" to drowsinessLevel,
            "trauksmes_veids" to alertType
        ))
        val elapsed = (System.nanoTime() - t0) / 1_000
        val value = result["rekomendacija"] ?: 0.0
        return ModuleResult("rekomendacija", value, classifyRecommendation(value), elapsed)
    }

    // ── LP1 Non-standard situation detection ──

    /**
     * Extraordinary signs aggregation (OR logic over 4 checkboxes).
     * Node-RED: if ANY checkbox true → 1, else → 0
     */
    fun evaluateExtraordinarySigns(inputs: Map<String, Double>): ModuleResult {
        val t0 = System.nanoTime()
        val result = engine.evaluate(extraordinarySystem, inputs)
        val elapsed = (System.nanoTime() - t0) / 1_000
        val value = result["neordinaras_pazimes_rezultats"] ?: 0.0
        return ModuleResult("neordinaras_pazimes", value, classifyExtraordinary(value), elapsed)
    }

    /**
     * EEG bypass alert assessment (pre-trip: alpha A + alpha B phases).
     * Node-RED: both=2(Augsts), one=1(Vidējs), neither=0(Zems)
     */
    fun evaluateEegBypass(inputs: Map<String, Double>): ModuleResult {
        val t0 = System.nanoTime()
        val result = engine.evaluate(eegBypassSystem, inputs)
        val elapsed = (System.nanoTime() - t0) / 1_000
        val value = result["EEG_vertejums"] ?: 0.0
        return ModuleResult("EEG_vertejums", value, classifyLevel(value), elapsed)
    }

    /**
     * Monitoring EEG assessment (alpha duration + beta presence).
     * Node-RED: alpha>3s → 2(Augsts), alpha≤2 & no beta → 1(Vidējs), else → 0(Zems)
     */
    fun evaluateMonitoringEeg(inputs: Map<String, Double>): ModuleResult {
        val t0 = System.nanoTime()
        val result = engine.evaluate(monitoringEegSystem, inputs)
        val elapsed = (System.nanoTime() - t0) / 1_000
        val value = result["EEG_vertejums"] ?: 0.0
        return ModuleResult("EEG_vertejums", value, classifyLevel(value), elapsed)
    }

    // ── Scenario 1: Full Assessment ──
    // Node-RED flow: Subjective → Fatigue ← Objective → Alert ← Extraordinary → Recommendation
    data class FullAssessmentInput(
        val subjective: Map<String, Double>,   // 13 inputs
        val objective: Map<String, Double>,     // 6 inputs
        val eegAssessment: Double,             // From monitoring
        val eyeAssessment: Double,             // From blink frequency
        val extraordinarySigns: Double          // 0 or 1
    )

    data class FullAssessmentResult(
        val subjective: ModuleResult,
        val objective: ModuleResult,
        val fatigue: ModuleResult,
        val alert: ModuleResult,
        val recommendation: ModuleResult
    )

    fun runFullAssessment(input: FullAssessmentInput): FullAssessmentResult {
        // Stage 1: Evaluate leaf modules
        val subjResult = evaluateSubjective(input.subjective)
        val objResult = evaluateObjective(input.objective)

        // Stage 2: Combine into fatigue + alert
        val fatigueResult = evaluateFatigue(
            Math.round(subjResult.value).toDouble(),
            Math.round(objResult.value).toDouble()
        )
        val alertResult = evaluateAlert(
            input.eegAssessment,
            input.eyeAssessment,
            input.extraordinarySigns
        )

        // Stage 3: Final recommendation
        val recoResult = evaluateRecommendation(
            Math.round(fatigueResult.value).toDouble(),
            Math.round(alertResult.value).toDouble()
        )

        return FullAssessmentResult(subjResult, objResult, fatigueResult, alertResult, recoResult)
    }

    // ── Scenario 2: Monitoring ──
    // Node-RED flow: Objective → EEG/Eye → Alert → Recommendation (uses existing fatigue level)
    data class MonitoringInput(
        val objective: Map<String, Double>,    // 6 EEG/eye inputs
        val eegAssessment: Double,
        val eyeAssessment: Double,
        val extraordinarySigns: Double,
        val currentFatigueLevel: Double        // From previous full assessment or manual
    )

    data class MonitoringResult(
        val objective: ModuleResult,
        val alert: ModuleResult,
        val recommendation: ModuleResult
    )

    fun runMonitoring(input: MonitoringInput): MonitoringResult {
        // Stage 1: Evaluate objective
        val objResult = evaluateObjective(input.objective)

        // Stage 2: Alert from EEG/eye assessment
        val alertResult = evaluateAlert(
            input.eegAssessment,
            input.eyeAssessment,
            input.extraordinarySigns
        )

        // Stage 3: Recommendation using current fatigue + alert
        val recoResult = evaluateRecommendation(
            input.currentFatigueLevel,
            Math.round(alertResult.value).toDouble()
        )

        return MonitoringResult(objResult, alertResult, recoResult)
    }

    // ── Scenario 3: Quick Check ──
    // Direct: Fatigue(2 inputs) → Alert(3 inputs) → Recommendation
    data class QuickCheckInput(
        val subjectiveComponent: Double,   // 0-2
        val objectiveComponent: Double,    // 0-2
        val eegAssessment: Double,         // 0-2
        val eyeAssessment: Double,         // 0-2
        val extraordinarySigns: Double     // 0-1
    )

    data class QuickCheckResult(
        val fatigue: ModuleResult,
        val alert: ModuleResult,
        val recommendation: ModuleResult
    )

    fun runQuickCheck(input: QuickCheckInput): QuickCheckResult {
        val fatigueResult = evaluateFatigue(input.subjectiveComponent, input.objectiveComponent)
        val alertResult = evaluateAlert(input.eegAssessment, input.eyeAssessment, input.extraordinarySigns)
        val recoResult = evaluateRecommendation(
            Math.round(fatigueResult.value).toDouble(),
            Math.round(alertResult.value).toDouble()
        )
        return QuickCheckResult(fatigueResult, alertResult, recoResult)
    }

    // ── Classification helpers ──
    // Thesis Appendix 4: R=1..4 recommendation classes
    // Thesis §2.2: T=0..3 alert types

    companion object {
        /**
         * Fatigue level: Z/V/A three-grade linguistic scale (Thesis §3.1)
         *
         * Classification uses Math.round() to match the Node-RED flow and JS test behavior.
         * The degenerate trimf [0,1,1] MFs produce centroid values like 0.67 for "medium"
         * inputs — the raw float is below 0.7 but rounds to 1 (medium).
         */
        fun classifyLevel(value: Double): String {
            val rounded = Math.round(value)
            return when {
                rounded <= 0L -> "Zems | Low"
                rounded == 1L -> "Vidējs | Medium"
                else -> "Augsts | High"
            }
        }

        /** Alert type: 4 grades (Thesis §2.2) */
        fun classifyAlertType(value: Double): String {
            val rounded = Math.round(value).toDouble()
            return when {
                rounded < 0.6 -> "Nav trauksmes | No alert"
                rounded < 1.5 -> "Zummers | Buzzer"
                rounded <= 2.5 -> "Zvans | Bell"
                else -> "Sirēna un gaisma | Siren and light"
            }
        }

        /** Extraordinary signs: binary (Thesis Table 5.2, module 6) */
        fun classifyExtraordinary(value: Double): String {
            val rounded = Math.round(value)
            return if (rounded <= 0L) "Nav konstatētas | Not detected"
            else "Ir konstatētas | Detected"
        }

        /**
         * Recommendation: 4 classes (Thesis Appendix 4, page 25-26)
         * R=1: Var turpināt darbu
         * R=2: Neliels ierobežojums jeb pastaigu pauze
         * R=3: Būtisks ierobežojums jeb atpūta, snauda vai pusdienu pārtraukums
         * R=4: Jābeidz maiņa, jāizsauc dublieris
         */
        fun classifyRecommendation(value: Double): String {
            val rounded = Math.round(value).toDouble()
            return when {
                rounded < 0.6 -> "Var turpināt darbu | Can continue work"
                rounded < 1.5 -> "Pastaigu pauze | Walking pause"
                rounded <= 2.5 -> "Atpūta / pusdienu pārtraukums | Rest / lunch break"
                else -> "Jābeidz maiņa | Must end shift"
            }
        }
    }
}

/** Result from a single fuzzy module evaluation */
data class ModuleResult(
    val outputName: String,
    val value: Double,
    val label: String,
    val elapsedUs: Long = 0
)
