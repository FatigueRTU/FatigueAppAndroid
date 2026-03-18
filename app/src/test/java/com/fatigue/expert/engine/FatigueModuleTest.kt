package com.fatigue.expert.engine

import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Unit tests for nogurums.fis (Mental Fatigue module).
 *
 * FIS internals: Input1=objektiva_komponente, Input2=subjektiva_komponente,
 *                Output1=objektiva_miegainiba
 * JS API alias: output returned as "nogurums"
 *
 * Test data: nogurums_test_data.txt (9 vectors, complete 3x3 matrix)
 * Format: col0=objektiva col1=subjektiva col2=expected (all 1-based)
 */
class FatigueModuleTest {

    companion object {
        private lateinit var engine: TestFuzzyEngine
        private lateinit var system: FuzzySystem
        private lateinit var testVectors: List<TestVector>

        // Actual FIS output variable name (not the JS API alias)
        private const val OUTPUT_KEY = "objektiva_miegainiba"

        @BeforeClass
        @JvmStatic
        fun setup() {
            engine = TestFuzzyEngine()
            system = engine.loadSystem("nogurums.fis")
            testVectors = TestDataLoader.load("test_data/nogurums_test_data.txt", numInputs = 2)
        }
    }

    @Test
    fun `FIS file loads correctly`() {
        assertEquals("nogurums", system.name)
        assertEquals(2, system.inputs.size)
        assertEquals(1, system.outputs.size)
        assertEquals(9, system.rules.size)
        // Input1 in FIS is objektiva, Input2 is subjektiva
        assertEquals("objektiva_komponente", system.inputs[0].name)
        assertEquals("subjektiva_komponente", system.inputs[1].name)
        assertEquals(OUTPUT_KEY, system.outputs[0].name)
    }

    @Test
    fun `test data loaded`() {
        assertEquals("Expected 9 vectors (complete 3x3 matrix)", 9, testVectors.size)
    }

    @Test
    fun `all test vectors pass`() {
        val failures = mutableListOf<String>()

        testVectors.forEachIndexed { idx, tv ->
            // Test data col order matches FIS input order: objektiva, subjektiva
            val inputs = mapOf(
                "objektiva_komponente" to tv.inputs[0],
                "subjektiva_komponente" to tv.inputs[1]
            )
            val result = engine.evaluate(system, inputs)
            val actual = result[OUTPUT_KEY]?.roundToInt() ?: -1

            if (actual != tv.expected) {
                failures.add(
                    "Vector $idx: inputs=[${tv.inputs[0]}, ${tv.inputs[1]}] " +
                    "expected=${tv.expected} got=$actual (raw=${result[OUTPUT_KEY]})"
                )
            }
        }

        if (failures.isNotEmpty()) {
            org.junit.Assert.fail(
                "${failures.size}/${testVectors.size} vectors failed:\n${failures.joinToString("\n")}"
            )
        }
    }

    @Test
    fun `both low yields low`() {
        val result = engine.evaluate(system, mapOf(
            "objektiva_komponente" to 0.0,
            "subjektiva_komponente" to 0.0
        ))
        assertEquals(0, result[OUTPUT_KEY]?.roundToInt())
    }

    @Test
    fun `both high yields high`() {
        val result = engine.evaluate(system, mapOf(
            "objektiva_komponente" to 2.0,
            "subjektiva_komponente" to 2.0
        ))
        assertEquals(2, result[OUTPUT_KEY]?.roundToInt())
    }

    @Test
    fun `high subjective low objective yields high`() {
        val result = engine.evaluate(system, mapOf(
            "objektiva_komponente" to 0.0,
            "subjektiva_komponente" to 2.0
        ))
        assertEquals(2, result[OUTPUT_KEY]?.roundToInt())
    }
}
