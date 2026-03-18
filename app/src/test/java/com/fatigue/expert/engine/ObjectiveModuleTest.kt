package com.fatigue.expert.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Unit tests for objective_full.fis (Objective Component module).
 *
 * FIS internals: 6 inputs, Output1=objektiva_miegainiba
 * JS API alias: output returned as "objektiva_komponente"
 *
 * Test data: objective_test_data.txt (729 vectors — complete 3^6)
 */
class ObjectiveModuleTest {

    companion object {
        private lateinit var engine: TestFuzzyEngine
        private lateinit var system: FuzzySystem
        private lateinit var testVectors: List<TestVector>

        private const val OUTPUT_KEY = "objektiva_miegainiba"

        private val inputNames = listOf(
            "mirkskinasanas_biezums", "EEG_alfa_ritms",
            "EEG_j1", "EEG_j2", "EEG_j3", "EEG_j4"
        )

        @BeforeClass
        @JvmStatic
        fun setup() {
            engine = TestFuzzyEngine()
            system = engine.loadSystem("objective_full.fis")
            testVectors = TestDataLoader.load("test_data/objective_test_data.txt", numInputs = 6)
        }
    }

    @Test
    fun `FIS file loads correctly`() {
        assertEquals("objektiva_dala", system.name)
        assertEquals(6, system.inputs.size)
        assertEquals(1, system.outputs.size)
        assertEquals(729, system.rules.size)
        assertEquals(OUTPUT_KEY, system.outputs[0].name)
        assertEquals(AndMethod.PROD, system.andMethod)
        assertEquals(DefuzzMethod.CENTROID, system.defuzzMethod)
    }

    @Test
    fun `test data loaded`() {
        assertEquals("Expected 729 vectors (complete 3^6 matrix)", 729, testVectors.size)
    }

    @Test
    fun `all test vectors pass`() {
        val failures = mutableListOf<String>()

        testVectors.forEachIndexed { idx, tv ->
            val inputs = inputNames.zip(tv.inputs).toMap()
            val result = engine.evaluate(system, inputs)
            val actual = result[OUTPUT_KEY]?.roundToInt() ?: -1

            if (actual != tv.expected) {
                failures.add(
                    "Vector $idx: inputs=${tv.inputs.map { it.toInt() }} " +
                    "expected=${tv.expected} got=$actual (raw=${result[OUTPUT_KEY]})"
                )
            }
        }

        if (failures.isNotEmpty()) {
            fail("${failures.size}/${testVectors.size} vectors failed:\n${failures.joinToString("\n")}")
        }
    }

    @Test
    fun `all inputs low yields low`() {
        val inputs = inputNames.associateWith { 0.0 }
        val result = engine.evaluate(system, inputs)
        assertEquals(0, result[OUTPUT_KEY]?.roundToInt())
    }

    @Test
    fun `all inputs high yields high`() {
        val inputs = inputNames.associateWith { 2.0 }
        val result = engine.evaluate(system, inputs)
        assertEquals(2, result[OUTPUT_KEY]?.roundToInt())
    }

    @Test
    fun `any single high input yields high`() {
        for (name in inputNames) {
            val inputs = inputNames.associateWith { 0.0 }.toMutableMap()
            inputs[name] = 2.0
            val result = engine.evaluate(system, inputs)
            val actual = result[OUTPUT_KEY]?.roundToInt() ?: -1
            assert(actual >= 1) {
                "Single high on $name: expected >= 1, got $actual"
            }
        }
    }
}
