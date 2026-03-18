package com.fatigue.expert

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.fatigue.expert.engine.*

class FatigueViewModel(application: Application) : AndroidViewModel(application) {

    private val scenarios = FatigueScenarios(application)

    fun getScenarios() = scenarios

    // ── Session ──
    val userName = mutableStateOf("")

    // ── User App State (from Unity port) ──
    val surveyAnswers = mutableStateMapOf<String, Float>()
    val testResultDetails = mutableStateMapOf<String, String>() // raw test metrics for display
    val appSubjectiveResult = mutableStateOf<ModuleResult?>(null)
    val appFatigueResult = mutableStateOf<ModuleResult?>(null)
    val appRecommendationResult = mutableStateOf<ModuleResult?>(null)
    val lastMonitoringRecommendation = mutableStateOf<ModuleResult?>(null)
    val preTripTimestamp = mutableStateOf<Long?>(null)
    val monitoringTimestamp = mutableStateOf<Long?>(null)

    fun endSession() {
        userName.value = ""
        surveyAnswers.clear()
        testResultDetails.clear()
        appSubjectiveResult.value = null
        appFatigueResult.value = null
        appRecommendationResult.value = null
        lastMonitoringRecommendation.value = null
        preTripTimestamp.value = null
        monitoringTimestamp.value = null
        resetAll()
    }

    // ── Individual Module State ──

    val moduleInputs = mutableStateMapOf<String, Float>()
    val moduleResult = mutableStateOf<ModuleResult?>(null)

    fun evaluateModule(moduleName: String) {
        val inputs = moduleInputs.mapValues { it.value.toDouble() }
        moduleResult.value = when (moduleName) {
            "subjective" -> scenarios.evaluateSubjective(inputs)
            "objective" -> scenarios.evaluateObjective(inputs)
            "fatigue" -> scenarios.evaluateFatigue(
                inputs["subjektiva_komponente"] ?: 0.0,
                inputs["objektiva_komponente"] ?: 0.0
            )
            "alert" -> scenarios.evaluateAlert(
                inputs["EEG_vertejums"] ?: 0.0,
                inputs["Acu_novertejums"] ?: 0.0,
                inputs["Neordinaras_pazimes"] ?: 0.0
            )
            "recommendation" -> scenarios.evaluateRecommendation(
                inputs["miegainibas_limenis"] ?: 0.0,
                inputs["trauksmes_veids"] ?: 0.0
            )
            "extraordinary" -> scenarios.evaluateExtraordinarySigns(inputs)
            "eeg_bypass" -> scenarios.evaluateEegBypass(inputs)
            "monitoring_eeg" -> scenarios.evaluateMonitoringEeg(inputs)
            else -> null
        }
    }

    // ── Scenario 1: Full Assessment ──

    val fullAssessmentResult = mutableStateOf<FatigueScenarios.FullAssessmentResult?>(null)
    val fullSubjInputs = mutableStateMapOf<String, Float>()
    val fullObjInputs = mutableStateMapOf<String, Float>()
    val fullEegAssessment = mutableStateOf(0f)
    val fullEyeAssessment = mutableStateOf(0f)
    val fullExtraordinarySigns = mutableStateOf(0f)
    val fullCurrentStep = mutableStateOf(0)

    fun runFullAssessment() {
        val input = FatigueScenarios.FullAssessmentInput(
            subjective = fullSubjInputs.mapValues { it.value.toDouble() },
            objective = fullObjInputs.mapValues { it.value.toDouble() },
            eegAssessment = fullEegAssessment.value.toDouble(),
            eyeAssessment = fullEyeAssessment.value.toDouble(),
            extraordinarySigns = fullExtraordinarySigns.value.toDouble()
        )
        fullAssessmentResult.value = scenarios.runFullAssessment(input)
        fullCurrentStep.value = 5
    }

    // ── Scenario 2: Monitoring ──

    val monitoringResult = mutableStateOf<FatigueScenarios.MonitoringResult?>(null)
    val monObjInputs = mutableStateMapOf<String, Float>()
    val monEegAssessment = mutableStateOf(0f)
    val monEyeAssessment = mutableStateOf(0f)
    val monExtraordinarySigns = mutableStateOf(0f)
    val monCurrentFatigue = mutableStateOf(1f)
    val monCurrentStep = mutableStateOf(0)

    fun runMonitoring() {
        val input = FatigueScenarios.MonitoringInput(
            objective = monObjInputs.mapValues { it.value.toDouble() },
            eegAssessment = monEegAssessment.value.toDouble(),
            eyeAssessment = monEyeAssessment.value.toDouble(),
            extraordinarySigns = monExtraordinarySigns.value.toDouble(),
            currentFatigueLevel = monCurrentFatigue.value.toDouble()
        )
        monitoringResult.value = scenarios.runMonitoring(input)
        monCurrentStep.value = 3
    }

    // ── Scenario 3: Quick Check ──

    val quickCheckResult = mutableStateOf<FatigueScenarios.QuickCheckResult?>(null)
    val quickSubjComponent = mutableStateOf(0f)
    val quickObjComponent = mutableStateOf(0f)
    val quickEegAssessment = mutableStateOf(0f)
    val quickEyeAssessment = mutableStateOf(0f)
    val quickExtraordinarySigns = mutableStateOf(0f)

    fun runQuickCheck() {
        val input = FatigueScenarios.QuickCheckInput(
            subjectiveComponent = quickSubjComponent.value.toDouble(),
            objectiveComponent = quickObjComponent.value.toDouble(),
            eegAssessment = quickEegAssessment.value.toDouble(),
            eyeAssessment = quickEyeAssessment.value.toDouble(),
            extraordinarySigns = quickExtraordinarySigns.value.toDouble()
        )
        quickCheckResult.value = scenarios.runQuickCheck(input)
    }

    // ── Reset ──

    fun resetAll() {
        moduleInputs.clear()
        moduleResult.value = null
        fullAssessmentResult.value = null
        fullSubjInputs.clear()
        fullObjInputs.clear()
        fullEegAssessment.value = 0f
        fullEyeAssessment.value = 0f
        fullExtraordinarySigns.value = 0f
        fullCurrentStep.value = 0
        monitoringResult.value = null
        monObjInputs.clear()
        monEegAssessment.value = 0f
        monEyeAssessment.value = 0f
        monExtraordinarySigns.value = 0f
        monCurrentFatigue.value = 1f
        monCurrentStep.value = 0
        quickCheckResult.value = null
        quickSubjComponent.value = 0f
        quickObjComponent.value = 0f
        quickEegAssessment.value = 0f
        quickEyeAssessment.value = 0f
        quickExtraordinarySigns.value = 0f
    }
}
