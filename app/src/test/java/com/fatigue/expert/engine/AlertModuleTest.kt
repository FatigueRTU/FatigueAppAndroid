package com.fatigue.expert.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Unit tests for trauksme.fis (Alert Type module).
 *
 * Mirrors test_alert.js which sends EEG_vertejums, Acu_novertejums,
 * Neordinaras_pazimes to /alert and asserts
 * Math.round(response.trauksmes_veids) == expected.
 *
 * Test data: trauksme_test_data.txt (18 test vectors, complete rule coverage)
 * Format: EEG_vertejums Acu_novertejums Neordinaras_pazimes expected (all 1-based)
 *
 * Output mapping (0-based): 0=None, 1=Buzzer, 2=Bell, 3=Siren+Light
 */
class AlertModuleTest {

    companion object {
        private lateinit var engine: TestFuzzyEngine
        private lateinit var system: FuzzySystem
        private lateinit var testVectors: List<TestVector>

        @BeforeClass
        @JvmStatic
        fun setup() {
            engine = TestFuzzyEngine()
            system = engine.loadSystem("trauksme.fis")
            testVectors = TestDataLoader.load("test_data/trauksme_test_data.txt", numInputs = 3)
        }
    }

    @Test
    fun `FIS file loads correctly`() {
        assertEquals("trauksme", system.name)
        assertEquals(3, system.inputs.size)
        assertEquals(1, system.outputs.size)
        assertEquals(18, system.rules.size)
        assertEquals("EEG_vertejums", system.inputs[0].name)
        assertEquals("Acu_novertejums", system.inputs[1].name)
        assertEquals("Neordinaras_pazimes", system.inputs[2].name)
        assertEquals("trauksmes_veids", system.outputs[0].name)
    }

    @Test
    fun `test data loaded`() {
        assertEquals("Expected 18 vectors (complete rule coverage)", 18, testVectors.size)
    }

    @Test
    fun `all test vectors pass`() {
        val failures = mutableListOf<String>()

        testVectors.forEachIndexed { idx, tv ->
            val inputs = mapOf(
                "EEG_vertejums" to tv.inputs[0],
                "Acu_novertejums" to tv.inputs[1],
                "Neordinaras_pazimes" to tv.inputs[2]
            )
            val result = engine.evaluate(system, inputs)
            val actual = result["trauksmes_veids"]?.roundToInt() ?: -1

            if (actual != tv.expected) {
                failures.add(
                    "Vector $idx: EEG=${tv.inputs[0].toInt()} Eye=${tv.inputs[1].toInt()} " +
                    "Extra=${tv.inputs[2].toInt()} expected=${tv.expected} got=$actual " +
                    "(raw=${result["trauksmes_veids"]})"
                )
            }
        }

        if (failures.isNotEmpty()) {
            fail("${failures.size}/${testVectors.size} vectors failed:\n${failures.joinToString("\n")}")
        }
    }

    // ── Explicit tests from the alert decision matrix ──

    @Test
    fun `all low no extraordinary yields none`() {
        val result = engine.evaluate(system, mapOf(
            "EEG_vertejums" to 0.0,
            "Acu_novertejums" to 0.0,
            "Neordinaras_pazimes" to 0.0
        ))
        assertEquals(0, result["trauksmes_veids"]?.roundToInt())
    }

    @Test
    fun `all low with extraordinary yields buzzer`() {
        val result = engine.evaluate(system, mapOf(
            "EEG_vertejums" to 0.0,
            "Acu_novertejums" to 0.0,
            "Neordinaras_pazimes" to 1.0
        ))
        assertEquals(1, result["trauksmes_veids"]?.roundToInt())
    }

    @Test
    fun `high EEG always yields siren`() {
        for (eye in listOf(0.0, 1.0, 2.0)) {
            for (extra in listOf(0.0, 1.0)) {
                val result = engine.evaluate(system, mapOf(
                    "EEG_vertejums" to 2.0,
                    "Acu_novertejums" to eye,
                    "Neordinaras_pazimes" to extra
                ))
                assertEquals(
                    "EEG=high, Eye=$eye, Extra=$extra should be siren(3)",
                    3, result["trauksmes_veids"]?.roundToInt()
                )
            }
        }
    }

    @Test
    fun `high eye always yields siren`() {
        for (eeg in listOf(0.0, 1.0, 2.0)) {
            for (extra in listOf(0.0, 1.0)) {
                val result = engine.evaluate(system, mapOf(
                    "EEG_vertejums" to eeg,
                    "Acu_novertejums" to 2.0,
                    "Neordinaras_pazimes" to extra
                ))
                assertEquals(
                    "EEG=$eeg, Eye=high, Extra=$extra should be siren(3)",
                    3, result["trauksmes_veids"]?.roundToInt()
                )
            }
        }
    }
}
