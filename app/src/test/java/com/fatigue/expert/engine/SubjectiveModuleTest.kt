package com.fatigue.expert.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Unit tests for subjektiva_dala.fis (Subjective Component module).
 *
 * FIS internals: 13 inputs, Output1=subjektiva_miegainiba
 * JS API alias: output returned as "subjektiva_komponente"
 *
 * Test data: subjective_test_data.txt (200 test vectors)
 */
class SubjectiveModuleTest {

    companion object {
        private lateinit var engine: TestFuzzyEngine
        private lateinit var system: FuzzySystem
        private lateinit var testVectors: List<TestVector>

        private const val OUTPUT_KEY = "subjektiva_miegainiba"

        private val inputNames = listOf(
            "nakts_darbs", "stress", "paaugstinats_asinsspiediens",
            "smadzenu_darbibas_traucejumi", "lieto_uzmundrinosus_dzerienus",
            "lieto_nomierinosus_lidzeklus", "apnoja", "negaidita_aizmigsana",
            "aptauja", "reakcijas_tests", "sektoru_atmina_test",
            "sektoru_secibas_atmina_tests", "matematiskais_laiks_tests"
        )

        @BeforeClass
        @JvmStatic
        fun setup() {
            engine = TestFuzzyEngine()
            system = engine.loadSystem("subjektiva_dala.fis")
            testVectors = TestDataLoader.load("test_data/subjective_test_data.txt", numInputs = 13)
        }
    }

    @Test
    fun `FIS file loads correctly`() {
        assertEquals("subjektiva_dala", system.name)
        assertEquals(13, system.inputs.size)
        assertEquals(1, system.outputs.size)
        assertEquals(200, system.rules.size)
        assertEquals(OUTPUT_KEY, system.outputs[0].name)
        assertEquals(AndMethod.PROD, system.andMethod)
        assertEquals(AggMethod.PROBOR, system.aggMethod)
        assertEquals(DefuzzMethod.MOM, system.defuzzMethod)
    }

    @Test
    fun `test data loaded`() {
        assertEquals("Expected 200 vectors matching subjektiva_dala.fis rules", 200, testVectors.size)
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
    fun `high survey overrides to high`() {
        val inputs = inputNames.associateWith { 0.0 }.toMutableMap()
        inputs["aptauja"] = 2.0
        val result = engine.evaluate(system, inputs)
        assertEquals(2, result[OUTPUT_KEY]?.roundToInt())
    }
}
