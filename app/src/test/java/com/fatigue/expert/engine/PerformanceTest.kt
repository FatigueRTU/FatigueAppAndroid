package com.fatigue.expert.engine

import org.junit.BeforeClass
import org.junit.Test
import kotlin.math.sqrt

/**
 * Performance benchmarks for each FIS module.
 * Runs all input combinations multiple times and reports avg, stddev, p50, p95.
 */
class PerformanceTest {

    companion object {
        private lateinit var engine: TestFuzzyEngine

        private lateinit var subjSystem: FuzzySystem
        private lateinit var objSystem: FuzzySystem
        private lateinit var fatigueSystem: FuzzySystem
        private lateinit var alertSystem: FuzzySystem
        private lateinit var recoSystem: FuzzySystem
        private lateinit var extraordinarySystem: FuzzySystem
        private lateinit var eegBypassSystem: FuzzySystem
        private lateinit var monitoringEegSystem: FuzzySystem

        private val subjInputNames = listOf(
            "nakts_darbs", "stress", "paaugstinats_asinsspiediens",
            "smadzenu_darbibas_traucejumi", "lieto_uzmundrinosus_dzerienus",
            "lieto_nomierinosus_lidzeklus", "apnoja", "negaidita_aizmigsana",
            "aptauja", "reakcijas_tests", "sektoru_atmina_test",
            "sektoru_secibas_atmina_tests", "matematiskais_laiks_tests"
        )
        private val objInputNames = listOf(
            "mirkskinasanas_biezums", "EEG_alfa_ritms",
            "EEG_j1", "EEG_j2", "EEG_j3", "EEG_j4"
        )
        private val extraInputNames = listOf(
            "mikromiegs", "narkolepsija", "miegainiba_bez_noguruma", "bezmiegs"
        )
        private val bypassInputNames = listOf("EEG_alfa_A_posms", "EEG_alfa_B_posms")

        @BeforeClass
        @JvmStatic
        fun setup() {
            engine = TestFuzzyEngine()
            subjSystem = engine.loadSystem("subjektiva_dala.fis")
            objSystem = engine.loadSystem("objective_full.fis")
            fatigueSystem = engine.loadSystem("nogurums.fis")
            alertSystem = engine.loadSystem("trauksme.fis")
            recoSystem = engine.loadSystem("rekomendacijas.fis")
            extraordinarySystem = engine.loadSystem("neordinaras_pazimes.fis")
            eegBypassSystem = engine.loadSystem("eeg_apsteigsana.fis")
            monitoringEegSystem = engine.loadSystem("monitoringa_eeg.fis")
        }

        /** Generate all combinations of [levels] for [n] inputs */
        fun combos(n: Int, levels: List<Double> = listOf(0.0, 1.0, 2.0)): List<List<Double>> {
            if (n == 0) return listOf(emptyList())
            val sub = combos(n - 1, levels)
            return levels.flatMap { v -> sub.map { listOf(v) + it } }
        }
    }

    private fun benchModule(
        system: FuzzySystem,
        inputNames: List<String>,
        allCombos: List<List<Double>>,
        warmupRuns: Int = 50,
        totalRuns: Int = 1000
    ): List<Long> {
        // Warmup with rotating vectors
        repeat(warmupRuns) { i ->
            val inputs = inputNames.zip(allCombos[i % allCombos.size]).toMap()
            engine.evaluate(system, inputs)
        }

        // Benchmark: rotate through vectors for exactly totalRuns evaluations
        val times = mutableListOf<Long>()
        repeat(totalRuns) { i ->
            val values = allCombos[i % allCombos.size]
            val inputs = inputNames.zip(values).toMap()
            val t0 = System.nanoTime()
            engine.evaluate(system, inputs)
            times.add((System.nanoTime() - t0) / 1000) // microseconds
        }
        return times
    }

    @Test
    fun `benchmark all modules`() {
        println("\n${"=".repeat(90)}")
        println("  FUZZY INFERENCE ENGINE — PERFORMANCE REPORT (with σ)")
        println("${"=".repeat(90)}")
        println(String.format("  %-24s  %7s  %6s  %8s  %8s  %8s  %8s  %8s",
            "Module", "Config", "N", "Avg µs", "σ µs", "Min µs", "p50 µs", "p95 µs"))
        println("-".repeat(90))

        // ── Subjective: 200 test vectors from JS data ──
        val subjVectors = TestDataLoader.load("test_data/subjective_test_data.txt", numInputs = 13)
        val subjCombos = subjVectors.map { it.inputs.toList() }
        val subjTimes = benchModule(subjSystem, subjInputNames, subjCombos)
        printStats("Subjective Component", "13in/200r", subjTimes)

        // ── Objective: all 729 combinations ──
        val objVectors = TestDataLoader.load("test_data/objective_test_data.txt", numInputs = 6)
        val objCombos = objVectors.map { it.inputs.toList() }
        val objTimes = benchModule(objSystem, objInputNames, objCombos)
        printStats("Objective Component", "6in/729r", objTimes)

        // ── Fatigue: all 9 (3×3) ──
        val fatCombos = combos(2)
        val fatTimes = benchModule(fatigueSystem, listOf("objektiva_komponente", "subjektiva_komponente"), fatCombos)
        printStats("Mental Fatigue", "2in/9r", fatTimes)

        // ── Alert: all 18 (3×3×2) ──
        val alertCombos = listOf(0.0, 1.0, 2.0).flatMap { eeg ->
            listOf(0.0, 1.0, 2.0).flatMap { eye ->
                listOf(0.0, 1.0).map { ext -> listOf(eeg, eye, ext) }
            }
        }
        val alertTimes = benchModule(alertSystem, listOf("EEG_vertejums", "Acu_novertejums", "Neordinaras_pazimes"), alertCombos)
        printStats("Alert Type", "3in/18r", alertTimes)

        // ── Recommendation: all 12 (3×4) ──
        val recoCombos = listOf(0.0, 1.0, 2.0).flatMap { fat ->
            listOf(0.0, 1.0, 2.0, 3.0).map { alert -> listOf(fat, alert) }
        }
        val recoTimes = benchModule(recoSystem, listOf("miegainibas_limenis", "trauksmes_veids"), recoCombos)
        printStats("Recommendation", "2in/12r", recoTimes)

        // ── Extraordinary Signs: all 16 (2⁴) ──
        val extraCombos = combos(4, listOf(0.0, 1.0))
        val extraTimes = benchModule(extraordinarySystem, extraInputNames, extraCombos)
        printStats("Extraordinary Signs", "4in/16r", extraTimes)

        // ── EEG Bypass: all 4 (2²) ──
        val bypassCombos = combos(2, listOf(0.0, 1.0))
        val bypassTimes = benchModule(eegBypassSystem, bypassInputNames, bypassCombos)
        printStats("EEG Bypass Alert", "2in/4r", bypassTimes)

        // ── Monitoring EEG: 3 alpha levels × 2 beta = 6 ──
        val monCombos = listOf(0.0, 2.5, 5.0).flatMap { a ->
            listOf(0.0, 1.0).map { b -> listOf(a, b) }
        }
        val monTimes = benchModule(monitoringEegSystem, listOf("EEG_alfa_ilgums", "EEG_beta_klatbutne"), monCombos)
        printStats("Monitoring EEG", "2in/6r", monTimes)

        // ── Full pipeline (8 modules chained) ──
        println("-".repeat(90))
        // Warmup
        repeat(50) { runPipeline() }
        val pTimes = (1..1000).map { runPipeline() }
        printStats("FULL PIPELINE (8 mod)", "8×FIS", pTimes)

        println("\n${"=".repeat(90)}")
        println("  Pipeline = Extra → EEG_Bypass → Subj → Obj → Fatigue → Alert → Reco")
        println("  N = number of timed evaluations. σ = standard deviation.")
        println("  All times in microseconds (µs). 1 ms = 1000 µs.")
        println("${"=".repeat(90)}\n")
    }

    private fun runPipeline(): Long {
        val t0 = System.nanoTime()
        // LP1: Extraordinary signs
        val extra = engine.evaluate(extraordinarySystem, extraInputNames.associateWith { 0.0 })
        val extraVal = Math.round(extra.values.first()).toDouble()
        // LP1: EEG bypass
        val bypass = engine.evaluate(eegBypassSystem, mapOf("EEG_alfa_A_posms" to 1.0, "EEG_alfa_B_posms" to 0.0))
        val bypassVal = Math.round(bypass.values.first()).toDouble()
        // LP1: Subjective + Objective
        val subj = engine.evaluate(subjSystem, subjInputNames.associateWith { 1.0 })
        val obj = engine.evaluate(objSystem, objInputNames.associateWith { 1.0 })
        val subjVal = Math.round(subj["subjektiva_miegainiba"] ?: 0.0).toDouble()
        val objVal = Math.round(obj["objektiva_miegainiba"] ?: 0.0).toDouble()
        // LP2: Fatigue + Alert
        val fat = engine.evaluate(fatigueSystem, mapOf("objektiva_komponente" to objVal, "subjektiva_komponente" to subjVal))
        val fatVal = Math.round(fat["objektiva_miegainiba"] ?: 0.0).toDouble()
        val alert = engine.evaluate(alertSystem, mapOf("EEG_vertejums" to bypassVal, "Acu_novertejums" to 1.0, "Neordinaras_pazimes" to extraVal))
        val alertVal = Math.round(alert["trauksmes_veids"] ?: 0.0).toDouble()
        // LP3: Recommendation
        engine.evaluate(recoSystem, mapOf("miegainibas_limenis" to fatVal, "trauksmes_veids" to alertVal))
        return (System.nanoTime() - t0) / 1000
    }

    private fun printStats(name: String, config: String, times: List<Long>) {
        val n = times.size
        val avg = times.average()
        val variance = times.map { (it - avg) * (it - avg) }.average()
        val stddev = sqrt(variance)
        val sorted = times.sorted()
        val min = sorted.first()
        val median = sorted[n / 2]
        val p95 = sorted[(n * 0.95).toInt().coerceAtMost(n - 1)]

        println(String.format("  %-24s  %7s  %6d  %8.1f  %8.1f  %8d  %8d  %8d",
            name, config, n, avg, stddev, min, median, p95))
    }
}
