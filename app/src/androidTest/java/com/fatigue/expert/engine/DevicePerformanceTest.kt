package com.fatigue.expert.engine

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.sqrt

/**
 * On-device performance benchmark for each FIS module.
 * Run with: ./gradlew connectedAndroidTest
 * Results appear in logcat with tag "PERF".
 *
 * Each module is evaluated 1000 times, rotating through all input vectors.
 * These values are intended for Thesis Tables 5.2 and 5.3.
 */
@RunWith(AndroidJUnit4::class)
class DevicePerformanceTest {

    companion object {
        private const val TAG = "PERF"
        private const val TOTAL_RUNS = 1000
        private const val WARMUP_RUNS = 50

        private lateinit var engine: FuzzyEngine

        private lateinit var subjSystem: FuzzySystem
        private lateinit var objSystem: FuzzySystem
        private lateinit var fatigueSystem: FuzzySystem
        private lateinit var alertSystem: FuzzySystem
        private lateinit var recoSystem: FuzzySystem
        private lateinit var extraordinarySystem: FuzzySystem
        private lateinit var eegBypassSystem: FuzzySystem
        private lateinit var monitoringEegSystem: FuzzySystem

        private val subjNames = listOf(
            "nakts_darbs", "stress", "paaugstinats_asinsspiediens",
            "smadzenu_darbibas_traucejumi", "lieto_uzmundrinosus_dzerienus",
            "lieto_nomierinosus_lidzeklus", "apnoja", "negaidita_aizmigsana",
            "aptauja", "reakcijas_tests", "sektoru_atmina_test",
            "sektoru_secibas_atmina_tests", "matematiskais_laiks_tests"
        )
        private val objNames = listOf(
            "mirkskinasanas_biezums", "EEG_alfa_ritms",
            "EEG_j1", "EEG_j2", "EEG_j3", "EEG_j4"
        )
        private val extraNames = listOf(
            "mikromiegs", "narkolepsija", "miegainiba_bez_noguruma", "bezmiegs"
        )
        private val bypassNames = listOf("EEG_alfa_A_posms", "EEG_alfa_B_posms")

        /** Generate all combinations of [levels] for [n] inputs */
        fun combos(n: Int, levels: List<Double> = listOf(0.0, 1.0, 2.0)): List<List<Double>> {
            if (n == 0) return listOf(emptyList())
            val sub = combos(n - 1, levels)
            return levels.flatMap { v -> sub.map { listOf(v) + it } }
        }

        @BeforeClass
        @JvmStatic
        fun setup() {
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext
            engine = FuzzyEngine(ctx)
            subjSystem = engine.loadSystem("subjektiva_dala.fis")
            objSystem = engine.loadSystem("objective_full.fis")
            fatigueSystem = engine.loadSystem("nogurums.fis")
            alertSystem = engine.loadSystem("trauksme.fis")
            recoSystem = engine.loadSystem("rekomendacijas.fis")
            extraordinarySystem = engine.loadSystem("neordinaras_pazimes.fis")
            eegBypassSystem = engine.loadSystem("eeg_apsteigsana.fis")
            monitoringEegSystem = engine.loadSystem("monitoringa_eeg.fis")
        }
    }

    private fun benchModule(
        system: FuzzySystem,
        inputNames: List<String>,
        allCombos: List<List<Double>>
    ): List<Long> {
        // Warmup with rotating vectors
        repeat(WARMUP_RUNS) { i ->
            val inputs = inputNames.zip(allCombos[i % allCombos.size]).toMap()
            engine.evaluate(system, inputs)
        }

        // Benchmark: rotate through vectors for exactly TOTAL_RUNS evaluations
        val times = mutableListOf<Long>()
        repeat(TOTAL_RUNS) { i ->
            val values = allCombos[i % allCombos.size]
            val inputs = inputNames.zip(values).toMap()
            val t0 = System.nanoTime()
            engine.evaluate(system, inputs)
            times.add((System.nanoTime() - t0) / 1000) // microseconds
        }
        return times
    }

    @Test
    fun benchmarkAllModules() {
        Log.i(TAG, "")
        Log.i(TAG, "==========================================================================================")
        Log.i(TAG, "  ON-DEVICE FUZZY ENGINE PERFORMANCE — ${android.os.Build.MODEL} (N=$TOTAL_RUNS per module)")
        Log.i(TAG, "==========================================================================================")
        Log.i(TAG, String.format("  %-24s  %7s  %6s  %8s  %8s  %8s  %8s  %8s",
            "Module", "Config", "N", "Avg µs", "σ µs", "Min µs", "p50 µs", "p95 µs"))
        Log.i(TAG, "-".repeat(90))

        // ── Subjective: 200 representative combinations (shuffled from 3^13) ──
        val subjCombos = combos(13).shuffled().take(200)
        val subjTimes = benchModule(subjSystem, subjNames, subjCombos)
        logStats("Subjective Component", "13in/200r", subjTimes)

        // ── Objective: all 729 combinations ──
        val objCombos = combos(6)
        val objTimes = benchModule(objSystem, objNames, objCombos)
        logStats("Objective Component", "6in/729r", objTimes)

        // ── Fatigue: all 9 (3×3) ──
        val fatCombos = combos(2)
        val fatTimes = benchModule(fatigueSystem, listOf("objektiva_komponente", "subjektiva_komponente"), fatCombos)
        logStats("Mental Fatigue", "2in/9r", fatTimes)

        // ── Alert: all 18 (3×3×2) ──
        val alertCombos = listOf(0.0, 1.0, 2.0).flatMap { eeg ->
            listOf(0.0, 1.0, 2.0).flatMap { eye ->
                listOf(0.0, 1.0).map { ext -> listOf(eeg, eye, ext) }
            }
        }
        val alertTimes = benchModule(alertSystem, listOf("EEG_vertejums", "Acu_novertejums", "Neordinaras_pazimes"), alertCombos)
        logStats("Alert Type", "3in/18r", alertTimes)

        // ── Recommendation: all 12 (3×4) ──
        val recoCombos = listOf(0.0, 1.0, 2.0).flatMap { fat ->
            listOf(0.0, 1.0, 2.0, 3.0).map { alert -> listOf(fat, alert) }
        }
        val recoTimes = benchModule(recoSystem, listOf("miegainibas_limenis", "trauksmes_veids"), recoCombos)
        logStats("Recommendation", "2in/12r", recoTimes)

        // ── Extraordinary Signs: all 16 (2⁴) ──
        val extraCombos = combos(4, listOf(0.0, 1.0))
        val extraTimes = benchModule(extraordinarySystem, extraNames, extraCombos)
        logStats("Extraordinary Signs", "4in/16r", extraTimes)

        // ── EEG Bypass: all 4 (2²) ──
        val bypassCombos = combos(2, listOf(0.0, 1.0))
        val bypassTimes = benchModule(eegBypassSystem, bypassNames, bypassCombos)
        logStats("EEG Bypass Alert", "2in/4r", bypassTimes)

        // ── Monitoring EEG: 3 alpha levels × 2 beta = 6 ──
        val monCombos = listOf(0.0, 2.5, 5.0).flatMap { a ->
            listOf(0.0, 1.0).map { b -> listOf(a, b) }
        }
        val monTimes = benchModule(monitoringEegSystem, listOf("EEG_alfa_ilgums", "EEG_beta_klatbutne"), monCombos)
        logStats("Monitoring EEG", "2in/6r", monTimes)

        // ══════════════════════════════════════════════════════════════════
        // SCENARIO BENCHMARKS (using FatigueScenarios API)
        // ══════════════════════════════════════════════════════════════════
        Log.i(TAG, "")
        Log.i(TAG, "==========================================================================================")
        Log.i(TAG, "  SCENARIO PIPELINE BENCHMARKS (N=$TOTAL_RUNS per scenario)")
        Log.i(TAG, "==========================================================================================")
        Log.i(TAG, String.format("  %-24s  %7s  %6s  %8s  %8s  %8s  %8s  %8s",
            "Scenario", "Modules", "N", "Avg µs", "σ µs", "Min µs", "p50 µs", "p95 µs"))
        Log.i(TAG, "-".repeat(90))

        val scenarios = FatigueScenarios(
            InstrumentationRegistry.getInstrumentation().targetContext
        )

        // ── Scenario 1: Full Assessment (5 modules: Subj → Obj → Fatigue → Alert → Reco) ──
        val subjCombosForScenario = combos(13).shuffled().take(50)
        val objCombosForScenario = combos(6).shuffled().take(50)
        // Warmup
        repeat(WARMUP_RUNS) { i ->
            val sInputs = subjNames.zip(subjCombosForScenario[i % subjCombosForScenario.size]).toMap()
            val oInputs = objNames.zip(objCombosForScenario[i % objCombosForScenario.size]).toMap()
            scenarios.runFullAssessment(FatigueScenarios.FullAssessmentInput(
                subjective = sInputs, objective = oInputs,
                eegAssessment = 1.0, eyeAssessment = 1.0, extraordinarySigns = 0.0
            ))
        }
        val s1Times = (0 until TOTAL_RUNS).map { i ->
            val sInputs = subjNames.zip(subjCombosForScenario[i % subjCombosForScenario.size]).toMap()
            val oInputs = objNames.zip(objCombosForScenario[i % objCombosForScenario.size]).toMap()
            val eeg = listOf(0.0, 1.0, 2.0)[i % 3]
            val eye = listOf(0.0, 1.0, 2.0)[(i / 3) % 3]
            val ext = listOf(0.0, 1.0)[i % 2]
            val t0 = System.nanoTime()
            scenarios.runFullAssessment(FatigueScenarios.FullAssessmentInput(
                subjective = sInputs, objective = oInputs,
                eegAssessment = eeg, eyeAssessment = eye, extraordinarySigns = ext
            ))
            (System.nanoTime() - t0) / 1000
        }
        logStats("S1: Full Assessment", "5 mod", s1Times)

        // ── Scenario 2: Monitoring (3 modules: Obj → Alert → Reco, uses existing fatigue) ──
        repeat(WARMUP_RUNS) { i ->
            val oInputs = objNames.zip(objCombosForScenario[i % objCombosForScenario.size]).toMap()
            scenarios.runMonitoring(FatigueScenarios.MonitoringInput(
                objective = oInputs, eegAssessment = 1.0, eyeAssessment = 1.0,
                extraordinarySigns = 0.0, currentFatigueLevel = 1.0
            ))
        }
        val s2Times = (0 until TOTAL_RUNS).map { i ->
            val oInputs = objNames.zip(objCombosForScenario[i % objCombosForScenario.size]).toMap()
            val eeg = listOf(0.0, 1.0, 2.0)[i % 3]
            val eye = listOf(0.0, 1.0, 2.0)[(i / 3) % 3]
            val ext = listOf(0.0, 1.0)[i % 2]
            val fat = listOf(0.0, 1.0, 2.0)[(i / 6) % 3]
            val t0 = System.nanoTime()
            scenarios.runMonitoring(FatigueScenarios.MonitoringInput(
                objective = oInputs, eegAssessment = eeg, eyeAssessment = eye,
                extraordinarySigns = ext, currentFatigueLevel = fat
            ))
            (System.nanoTime() - t0) / 1000
        }
        logStats("S2: Monitoring", "3 mod", s2Times)

        // ── Scenario 3: Quick Check (3 modules: Fatigue → Alert → Reco, direct values) ──
        val quickCombos = listOf(0.0, 1.0, 2.0).flatMap { s ->
            listOf(0.0, 1.0, 2.0).flatMap { o ->
                listOf(0.0, 1.0, 2.0).flatMap { eeg ->
                    listOf(0.0, 1.0, 2.0).flatMap { eye ->
                        listOf(0.0, 1.0).map { ext -> listOf(s, o, eeg, eye, ext) }
                    }
                }
            }
        }
        repeat(WARMUP_RUNS) { i ->
            val c = quickCombos[i % quickCombos.size]
            scenarios.runQuickCheck(FatigueScenarios.QuickCheckInput(
                subjectiveComponent = c[0], objectiveComponent = c[1],
                eegAssessment = c[2], eyeAssessment = c[3], extraordinarySigns = c[4]
            ))
        }
        val s3Times = (0 until TOTAL_RUNS).map { i ->
            val c = quickCombos[i % quickCombos.size]
            val t0 = System.nanoTime()
            scenarios.runQuickCheck(FatigueScenarios.QuickCheckInput(
                subjectiveComponent = c[0], objectiveComponent = c[1],
                eegAssessment = c[2], eyeAssessment = c[3], extraordinarySigns = c[4]
            ))
            (System.nanoTime() - t0) / 1000
        }
        logStats("S3: Quick Check", "3 mod", s3Times)

        // ── Combined: S1 + S2 + S3 in sequence ──
        repeat(WARMUP_RUNS) { runCombined(scenarios, subjCombosForScenario, objCombosForScenario, quickCombos, it) }
        val combinedTimes = (0 until TOTAL_RUNS).map { i ->
            runCombined(scenarios, subjCombosForScenario, objCombosForScenario, quickCombos, i)
        }
        logStats("S1+S2+S3 Combined", "11 mod", combinedTimes)

        Log.i(TAG, "")
        Log.i(TAG, "==========================================================================================")
        Log.i(TAG, "  Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        Log.i(TAG, "  Android: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
        Log.i(TAG, "  S1 = Subj → Obj → Fatigue → Alert → Reco (5 modules)")
        Log.i(TAG, "  S2 = Obj → Alert → Reco + existing fatigue (3 modules)")
        Log.i(TAG, "  S3 = Fatigue → Alert → Reco from direct values (3 modules)")
        Log.i(TAG, "  N = $TOTAL_RUNS per scenario. σ = standard deviation.")
        Log.i(TAG, "  All times in microseconds (µs). 1 ms = 1000 µs.")
        Log.i(TAG, "==========================================================================================")
        Log.i(TAG, "")
    }

    private fun runCombined(
        scenarios: FatigueScenarios,
        subjCombos: List<List<Double>>,
        objCombos: List<List<Double>>,
        quickCombos: List<List<Double>>,
        i: Int
    ): Long {
        val sInputs = subjNames.zip(subjCombos[i % subjCombos.size]).toMap()
        val oInputs = objNames.zip(objCombos[i % objCombos.size]).toMap()
        val eeg = listOf(0.0, 1.0, 2.0)[i % 3]
        val eye = listOf(0.0, 1.0, 2.0)[(i / 3) % 3]
        val ext = listOf(0.0, 1.0)[i % 2]
        val c = quickCombos[i % quickCombos.size]

        val t0 = System.nanoTime()

        // S1: Full Assessment
        val s1 = scenarios.runFullAssessment(FatigueScenarios.FullAssessmentInput(
            subjective = sInputs, objective = oInputs,
            eegAssessment = eeg, eyeAssessment = eye, extraordinarySigns = ext
        ))

        // S2: Monitoring (uses fatigue from S1)
        scenarios.runMonitoring(FatigueScenarios.MonitoringInput(
            objective = oInputs, eegAssessment = eeg, eyeAssessment = eye,
            extraordinarySigns = ext,
            currentFatigueLevel = Math.round(s1.fatigue.value).toDouble()
        ))

        // S3: Quick Check
        scenarios.runQuickCheck(FatigueScenarios.QuickCheckInput(
            subjectiveComponent = c[0], objectiveComponent = c[1],
            eegAssessment = c[2], eyeAssessment = c[3], extraordinarySigns = c[4]
        ))

        return (System.nanoTime() - t0) / 1000
    }

    private fun logStats(name: String, config: String, times: List<Long>) {
        val n = times.size
        val avg = times.average()
        val variance = times.map { (it - avg) * (it - avg) }.average()
        val stddev = sqrt(variance)
        val sorted = times.sorted()
        val min = sorted.first()
        val median = sorted[n / 2]
        val p95 = sorted[(n * 0.95).toInt().coerceAtMost(n - 1)]

        Log.i(TAG, String.format("  %-24s  %7s  %6d  %8.1f  %8.1f  %8d  %8d  %8d",
            name, config, n, avg, stddev, min, median, p95))
    }
}
