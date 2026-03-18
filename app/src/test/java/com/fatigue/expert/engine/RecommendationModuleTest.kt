package com.fatigue.expert.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Unit tests for rekomendacijas.fis (Recommendation module).
 *
 * Mirrors test_recommendations.js which sends miegainibas_limenis + trauksmes_veids
 * to /recommendation and asserts Math.round(response.rekomendacija) == expected.
 *
 * Test data: rekomendacijas_test_data.txt (12 test vectors, complete 3x4 matrix)
 * Format: miegainibas_limenis trauksmes_veids expected (all 1-based)
 *
 * Output mapping (0-based):
 *   0 = Turpināt darbu (Continue work)
 *   1 = Paņemt pauzi (Take a pause)
 *   2 = Pusdienu pārtraukums (Lunch break)
 *   3 = Pārtraukt aktivitāti (Stop activity)
 */
class RecommendationModuleTest {

    companion object {
        private lateinit var engine: TestFuzzyEngine
        private lateinit var system: FuzzySystem
        private lateinit var testVectors: List<TestVector>

        @BeforeClass
        @JvmStatic
        fun setup() {
            engine = TestFuzzyEngine()
            system = engine.loadSystem("rekomendacijas.fis")
            testVectors = TestDataLoader.load("test_data/rekomendacijas_test_data.txt", numInputs = 2)
        }
    }

    @Test
    fun `FIS file loads correctly`() {
        assertEquals("rekomendacijas", system.name)
        assertEquals(2, system.inputs.size)
        assertEquals(1, system.outputs.size)
        assertEquals(12, system.rules.size)
        assertEquals("miegainibas_limenis", system.inputs[0].name)
        assertEquals("trauksmes_veids", system.inputs[1].name)
        assertEquals("rekomendacija", system.outputs[0].name)
    }

    @Test
    fun `test data loaded`() {
        assertEquals("Expected 12 vectors (complete 3x4 matrix)", 12, testVectors.size)
    }

    @Test
    fun `all test vectors pass`() {
        val failures = mutableListOf<String>()

        testVectors.forEachIndexed { idx, tv ->
            val inputs = mapOf(
                "miegainibas_limenis" to tv.inputs[0],
                "trauksmes_veids" to tv.inputs[1]
            )
            val result = engine.evaluate(system, inputs)
            val actual = result["rekomendacija"]?.roundToInt() ?: -1

            if (actual != tv.expected) {
                failures.add(
                    "Vector $idx: drowsiness=${tv.inputs[0].toInt()} alert=${tv.inputs[1].toInt()} " +
                    "expected=${tv.expected} got=$actual (raw=${result["rekomendacija"]})"
                )
            }
        }

        if (failures.isNotEmpty()) {
            fail("${failures.size}/${testVectors.size} vectors failed:\n${failures.joinToString("\n")}")
        }
    }

    // ── Explicit tests from the recommendation decision matrix ──

    @Test
    fun `low drowsiness no alert yields continue work`() {
        val result = engine.evaluate(system, mapOf(
            "miegainibas_limenis" to 0.0,
            "trauksmes_veids" to 0.0
        ))
        assertEquals(0, result["rekomendacija"]?.roundToInt())
    }

    @Test
    fun `low drowsiness buzzer yields pause`() {
        val result = engine.evaluate(system, mapOf(
            "miegainibas_limenis" to 0.0,
            "trauksmes_veids" to 1.0
        ))
        assertEquals(1, result["rekomendacija"]?.roundToInt())
    }

    @Test
    fun `any bell or siren yields stop`() {
        // Bell (2) or Siren (3) with any drowsiness level → Stop (3)
        for (drowsiness in listOf(0.0, 1.0, 2.0)) {
            for (alert in listOf(2.0, 3.0)) {
                val result = engine.evaluate(system, mapOf(
                    "miegainibas_limenis" to drowsiness,
                    "trauksmes_veids" to alert
                ))
                assertEquals(
                    "drowsiness=$drowsiness, alert=$alert should be stop(3)",
                    3, result["rekomendacija"]?.roundToInt()
                )
            }
        }
    }

    @Test
    fun `medium drowsiness no alert yields lunch break`() {
        val result = engine.evaluate(system, mapOf(
            "miegainibas_limenis" to 1.0,
            "trauksmes_veids" to 0.0
        ))
        assertEquals(2, result["rekomendacija"]?.roundToInt())
    }

    @Test
    fun `high drowsiness no alert yields lunch break`() {
        val result = engine.evaluate(system, mapOf(
            "miegainibas_limenis" to 2.0,
            "trauksmes_veids" to 0.0
        ))
        assertEquals(2, result["rekomendacija"]?.roundToInt())
    }

    @Test
    fun `high drowsiness buzzer yields stop`() {
        val result = engine.evaluate(system, mapOf(
            "miegainibas_limenis" to 2.0,
            "trauksmes_veids" to 1.0
        ))
        assertEquals(3, result["rekomendacija"]?.roundToInt())
    }
}
